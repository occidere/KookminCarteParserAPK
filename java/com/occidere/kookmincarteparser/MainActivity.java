package com.occidere.kookmincarteparser;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    static String print;
    private static final String address = "http://kmucoop.kookmin.ac.kr/restaurant/restaurant.php?w=";
    private static String breakfast="", lunch="", dinner="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Made by occidere", Toast.LENGTH_SHORT).show();
        //커스텀 아이콘 설정시 필수
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //인터넷 연결시 onCreate 하단에 필수 선언
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        setContentView(R.layout.activity_main);
        print="";
        tv = (TextView)findViewById(R.id.mainText);
        try{
            String today = findToday();
            print+="## "+today+" 식단표 ##\n\n";
            parseBubsik();
            parseHaksik();
            parseFaculty();
            parseChunghyang();
            printAll();
            tv.setText(print);
            tv.setTextColor(Color.BLACK);
        }
        catch(Exception e){
            tv.setText(e.getLocalizedMessage()); //메뉴 대신 에러메세지 출력
        }
    }

    //오늘의 날짜 찾기
    private static String findToday(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH)+1, date = cal.get(Calendar.DATE);
        return year+"년 "+month+"월 "+date+"일";
    }
    //각 식당마다 오늘 메뉴 부분만 뽑아서 Elements 타입으로 리턴
    private static Elements jsoupConnect(String address) throws Exception {
        String tag = "td[bgcolor=#eaffd9]";
        Document doc = Jsoup.connect(address).timeout(5000).get(); //최대 5초까지 기다림
        return doc.select(tag);
    }

    //법식
    private static void parseBubsik() throws Exception {
        String bubsik = "[법식]", tmp;
        Elements menu = jsoupConnect(address+1);
        for (Element res : menu) {
            tmp = removeBracket(res.text());
            if(tmp.contains("중식")){
                if(tmp.contains("석식")){
                    lunch+=bubsik+tmp.substring(tmp.indexOf("식*")+2, tmp.indexOf("*석식*"))+"\n";
                    dinner+=bubsik+tmp.substring(tmp.indexOf("*석식*")+4)+"\n";
                }
                else lunch+=bubsik+tmp.substring(tmp.indexOf("식*")+2)+"\n";
            }
            else if(tmp.contains("중석식")){
                lunch+=bubsik+tmp.substring(tmp.indexOf("식*")+2)+"\n";
                dinner+=bubsik+tmp.substring(tmp.indexOf("식*")+2)+"\n";
            }
            else breakfast+=bubsik+" "+tmp+"\n";
        }
        breakfast+="\n"; lunch+="\n"; dinner+="\n";
    }
    //학식
    private static void parseHaksik() throws Exception {
        String haksik = "[학식]", tmp;
        Elements menu = jsoupConnect(address+2);
        int i=0;
        for (Element res : menu) {
            tmp = removeBracket(res.text());
            if(i==0) breakfast+=(haksik+" "+tmp+"\n"); //조식
            else if(i<6) lunch+=(haksik+" "+tmp+"\n"); //중식
            else dinner+=(haksik+" "+tmp+"\n"); //석식
            i++;
            if(i>8) break; //학식 중국집 메뉴는 출력 안한다.
        }
        breakfast+="\n"; lunch+="\n"; dinner+="\n";
    }

    //교직원식당
    private static void parseFaculty() throws Exception{
        String faculty = "[교직원]", tmp;
        Elements menu = jsoupConnect(address+3);
        int i=0;
        for(Element res : menu){
            tmp = removeBracket(res.text());tmp = tmp.substring(tmp.indexOf(']')+1).trim();
            if(i>2) dinner+=(faculty+" "+tmp+"\n");//석식
            else lunch+=(faculty+" "+tmp+"\n");//중식
            i++;
        }
        lunch+="\n"; dinner+="\n";
    }

    //청향
    private static void parseChunghyang() throws Exception{
        String chunghyang = "[청향]", tmp;
        Elements menu = jsoupConnect(address+4);
        //청향은 중식만 운영
        for(Element res : menu){
            tmp = removeBracket(res.text());
            lunch+=(chunghyang+" "+tmp+"\n");
        }
        lunch+="\n";
    }

    //파싱한 메뉴 출력
    private static void printAll(){
        print+="---------- <조식> ----------\n";
        print+=breakfast;
        print+="---------- <중식> ----------\n";
        print+=lunch;
        print+="---------- <석식> ----------\n";
        print+=dinner;
    }

    //메뉴 앞에 붙은 보기싫은 수식어구 제거
    private static String removeBracket(String menu){
        char tmp[] = menu.toCharArray();
        int i, j=0, size = menu.length();
        StringBuilder res = new StringBuilder();
        char stack[] = new char[size];
        for(i=0;i<size;i++){
            //() 또는 []내부의 쓸모없는 원산지나 미사여구 제거
            if(tmp[i]=='('){
                while(true) {
                    if(tmp[i]==')') break;
                    else i++;
                }
            }
            else if(tmp[i]=='[') {
                while(true) {
                    if(tmp[i]==']') break;
                    else i++;
                }
            }
            //괄호 밖의 평문인 경우 전부 스택에 담는다.
            else stack[j++] = tmp[i];
        }
        for(i=0;i<j;i++) res.append(stack[i]);
        return res.toString().trim();
    }
    //공유하기 기능
    public void onClick(View view){
        Intent msg = new Intent(Intent.ACTION_SEND);
        msg.addCategory(Intent.CATEGORY_DEFAULT);
        msg.putExtra(Intent.EXTRA_TEXT, print);
        msg.setType("text/plain");
        startActivity(Intent.createChooser(msg, "공유"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        breakfast=""; lunch=""; dinner="";
        print="";
    }
}

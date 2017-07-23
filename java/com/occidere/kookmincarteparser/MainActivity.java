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
    private static String breakfast, lunch, dinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        print = breakfast = lunch = dinner = "";
        Toast.makeText(this, "Made by occidere (ver 0.2.5)", Toast.LENGTH_SHORT).show();
        //커스텀 아이콘 설정시 필수
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //인터넷 연결시 onCreate 하단에 필수 선언
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        setContentView(R.layout.activity_main);
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
        String bubsik = "[법]", tmp;
        Elements menu = jsoupConnect(address+1);
        for (Element res : menu) {
            tmp = removeAllBracket(res.text());
            if(tmp.contains("*중식*")){ //중식으로 시작하는 경우
                if(tmp.contains("*석식*")){ //중식과 석식이 한줄에 같이 있는 경우
                    lunch+=bubsik+tmp.substring(tmp.indexOf("*중식*")+4, tmp.indexOf("*석식*"))+"\n";
                    dinner+=bubsik+tmp.substring(tmp.indexOf("*석식*")+4)+"\n";
                }
                else lunch+=bubsik+tmp.substring(tmp.indexOf("*중식*")+4)+"\n"; //중식만 있는 경우
            }
            //석식만 있는 경우
            else if(tmp.contains("*석식*")) dinner+=bubsik+tmp.substring(tmp.indexOf("*석식*")+4)+"\n";
                //중석식 형태로 있는 경우
            else if(tmp.contains("*중석식*")){
                lunch+=bubsik+tmp.substring(tmp.indexOf("*중석식*")+5)+"\n";
                dinner+=bubsik+tmp.substring(tmp.indexOf("*중석식*")+5)+"\n";
            }
            else breakfast+=bubsik+" "+tmp+"\n"; //조식
        }
        breakfast+="\n"; lunch+="\n"; dinner+="\n";
    }

    //학식
    private static void parseHaksik() throws Exception {
        String haksik = "[학]", tmp;
        Elements menu = jsoupConnect(address+2);
        int i=0;
        for (Element res : menu) {
            tmp = removeAllBracket(res.text());
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
        String faculty = "[교]", tmp;
        Elements menu = jsoupConnect(address+3);
        int i=0;
        for(Element res : menu){
            tmp = removeAllBracket(res.text());
            tmp = tmp.substring(tmp.indexOf(']')+1).trim();
            if(i>2) dinner+=(faculty+" "+tmp+"\n");//석식
            else lunch+=(faculty+" "+tmp+"\n");//중식
            i++;
        }
        lunch+="\n"; dinner+="\n";
    }

    //청향
    private static void parseChunghyang() throws Exception{
        String chunghyang = "[청]";
        Elements menu = jsoupConnect(address+4);
        //청향은 중식만 운영
        for(Element res : menu){
            lunch+=(chunghyang+" "+removeAllBracket(res.text())+"\n");
        }
        lunch+="\n";
    }

    //파싱한 메뉴 출력
    private static void printAll(){
        print+="----------- <조식> -----------\n";
        print+=breakfast;
        print+="----------- <중식> -----------\n";
        print+=lunch;
        print+="----------- <석식> -----------\n";
        print+=dinner;
    }

    //괄호 안 ( ) [ ] 의 원산지나 부연설명 제거
    private static String removeAllBracket(String menu){
        StringBuilder res = new StringBuilder();
        int i, len = menu.length();
        char ch;
        for(i=0;i<len;i++){
            ch = menu.charAt(i);
            if(ch=='[') {
                while(ch!=']' && i<len){
                    if(ch=='￦'){
                        i-=2;
                        break;
                    }
                    ch = menu.charAt(i++);
                }
            }
            else res.append(ch);
        }
        menu = res.toString().trim();
        res = new StringBuilder();
        for(len = menu.length(), i=0;i<len;i++){
            ch = menu.charAt(i);
            if(ch=='(') {
                while(ch!=')' && i<len){
                    if(ch=='￦'){
                        i-=2;
                        break;
                    }
                    ch = menu.charAt(i++);
                }
            }
            else res.append(ch);
        }
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
        print = breakfast = lunch = dinner = "";
    }
}

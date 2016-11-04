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
    private static int i;

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
            bubsikPrint(today);
            print+="\n";
            haksikPrint(today);
            tv.setText(print);
            tv.setTextColor(Color.BLACK);
        }
        catch(Exception e){
            tv.setText(e.getLocalizedMessage()); //메뉴 대신 에러메세지 출력
        }
    }

    private static String findToday(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH)+1, date = cal.get(Calendar.DATE);
        return year+"년 "+month+"월 "+date+"일";
    }
    private static void bubsikPrint(String today) throws Exception {
        print+="## 법학관 한울식당 메뉴 ("+today+") ##\n";
        String tmp; i = 0;
        String bubsikMenu[] = { "바로바로1", "바로바로2", "면이랑", "밥이랑 하나", "밥이랑 두울", "石火랑", "石火랑(조식)"};

        Document doc = Jsoup.connect(address+1).get();
        Elements menu = doc.select("td[bgcolor=#eaffd9]");
        for (Element res : menu) {
            tmp = res.text();
            if (tmp.contains("*중식")) tmp = tmp.replace("*석식*", "\n*석식*");
            bubsikMenu[i] = "------- <"+bubsikMenu[i]+"> -------\n" + tmp+"\n";
            if(1<i && i<6) print+=bubsikMenu[i]+"\n";
            i++;
        }
    }
    private static void haksikPrint(String today) throws Exception {
        print+="## 복지관 학생식당 메뉴 ("+today+") ##\n";
        String haksikMenu[] = { "착한아침", "가마", "누들송(면)", "누들송(카페테리아)", "인터쉐프", "데일리밥"};

        for(i=0;i<6;i++) haksikMenu[i] = "------- <"+haksikMenu[i]+"> -------\n"; i=0;

        Document doc = Jsoup.connect(address+2).get();
        Elements menu = doc.select("td[bgcolor=#eaffd9]");

        for (Element res : menu) {
            String tmp = res.text();

            if(i==0) tmp = "*조식* "+tmp+"\n";
            else if(i<6) tmp = "*중식* "+tmp+"\n";
            else tmp = "*석식* "+tmp+"\n";

            if(i==6) haksikMenu[1]+=tmp;
            else if(i==7) haksikMenu[4]+=tmp;
            else if(i==8){ haksikMenu[5]+=tmp; break; }
            else haksikMenu[i]+=tmp;
            i++;
        }
        for(i=0;i<6;i++) print+=haksikMenu[i]+"\n";
    }

    //공유하기 기능
    public void onClick(View view){
        Intent msg = new Intent(Intent.ACTION_SEND);
        msg.addCategory(Intent.CATEGORY_DEFAULT);
        msg.putExtra(Intent.EXTRA_TEXT, print);
        msg.setType("text/plain");
        startActivity(Intent.createChooser(msg, "공유"));
    }
}

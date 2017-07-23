package com.occidere.kookmincarteparser;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kmuc.food.Carte;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private String carteByTime = "";
    private Carte carte = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "Made by occidere (ver 0.3.0)", Toast.LENGTH_SHORT).show();

        //커스텀 아이콘 설정시 필수
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //인터넷 연결시 onCreate 하단에 필수 선언
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.mainText);

        try{
            carte = new Carte();
            carteByTime = carte.getCarteByTime();

            tv.setText(carteByTime);
            tv.setTextColor(Color.BLACK);
        }
        catch(Exception e){
            tv.setText(e.getLocalizedMessage()); //메뉴 대신 에러메세지 출력
        }
    }

    //공유하기 기능
    public void onClick(View view){
        Intent msg = new Intent(Intent.ACTION_SEND);
        msg.addCategory(Intent.CATEGORY_DEFAULT);
        msg.putExtra(Intent.EXTRA_TEXT, carteByTime);
        msg.setType("text/plain");
        startActivity(Intent.createChooser(msg, "공유"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        carte = null;
        carteByTime = "";
    }
}

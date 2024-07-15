package com.kss.first;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class TitleActivity2 extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_theme1);	//splash_theme1으로 화면을 지정한다.

        moveMain(1);	//1초 후 main activity 로 넘어감
    }

    private void moveMain(int sec) {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //현재 화면에서 메인 화면으로 인텐트를 이동한다
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                startActivity(intent);

                finish();	//현재 액티비티 종료
            }
        }, 1000 * sec); // sec초 정도 딜레이를 준 후 시작
    }
}


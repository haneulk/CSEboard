package com.hnkim.cseboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by hnkim on 2017-01-18.
 */

public class MainActivity extends AppCompatActivity {

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        startActivity(new Intent(this, Splash.class));
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.NoticeBtn :
                Intent noticeIn = new Intent(MainActivity.this, MainNotice.class);
                startActivity(noticeIn);
                break;

            case R.id.FreeBtn :
                Intent freeIn = new Intent(MainActivity.this, MainFreeboard.class);
                startActivity(freeIn);
                break;

            case R.id.SettingBtn :
                Intent setIn = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(setIn);
                break;

        }
    }

    //뒤로가기버튼 2번 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();

//            android.os.Process.killProcess(android.os.Process.myPid()); //앱 구동 완전히 중지
            MainActivity.this.finish();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(MainActivity.this, "'뒤로'버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}

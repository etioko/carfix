package com.final_project.carfix;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LogoPage extends AppCompatActivity {
    private static int SPLASH_LOGO_TIME = 3000; //3 minutes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_page);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LogoPage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_LOGO_TIME);

    }
}

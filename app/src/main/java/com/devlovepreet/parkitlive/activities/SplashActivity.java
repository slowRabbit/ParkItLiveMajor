package com.devlovepreet.parkitlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.devlovepreet.parkitlive.R;
import com.devlovepreet.parkitlive.utils.SessionManager;

import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        final SessionManager session = new SessionManager(getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (session.isLoggedIn()) {

                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                    if (getIntent().getExtras() != null && !getIntent().getExtras().containsKey(getResources().getString(R.string.profile))) {
//                        myIntent.putExtras(getIntent().getExtras());
//                    }
                    startActivity(myIntent);

                } else {
                    Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(myIntent);
                }
                finish();
            }
        }, 2000);

    }
}
package com.akfa.apsproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

public class SplashNew extends AppCompatActivity {
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_new);
        getSupportActionBar().hide();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPrefs.getString("Логин пользователя", null) != null)
        {
            String rememberedLogin = sharedPrefs.getString("Логин пользователя", null);
            String rememberedPosition = sharedPrefs.getString("Должность", null);
            switch (rememberedPosition)
            {
                case "operator":
                    String rememberedPultNo = sharedPrefs.getString("Номер пульта", null);
                    Intent openPult = new Intent(getApplicationContext(), MainActivity.class);
                    openPult.putExtra("Номер пульта", rememberedPultNo);
                    openPult.putExtra("Логин пользователя", rememberedLogin);
                    openPult.putExtra("Должность", rememberedPosition);
                    keepSplash(openPult);
                    break;
                case "master":
                    Intent openFactoryCondition = new Intent(getApplicationContext(), QuestMainActivity.class); //actually there should be the FactoryCondition.class, but it is incomplete yet
                    openFactoryCondition.putExtra("Логин пользователя", rememberedLogin);
                    openFactoryCondition.putExtra("Должность", rememberedPosition);
                    keepSplash(openFactoryCondition);
                    break;
                case "repair":
                    Intent openProblemsList = new Intent(getApplicationContext(), RepairersProblemsList.class);
                    openProblemsList.putExtra("Логин пользователя", rememberedLogin);
                    openProblemsList.putExtra("Должность", rememberedPosition);
                    keepSplash(openProblemsList);
                    break;
            }
            stopService(new Intent(getBaseContext(), BackgroundService.class)); //если до этого уже сервис для другого аккаунта был включен и произошел повторный логин
            Intent startBackgroundService = new Intent(getApplicationContext(), BackgroundService.class);
            startBackgroundService.putExtra("Должность", rememberedPosition);
 //эта функция запускает фоновый сервис
            ContextCompat.startForegroundService(getApplicationContext(), startBackgroundService);
        }
        else
        {
            handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    Intent intent=new Intent(SplashNew.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            },2000);
        }

    }

    private void keepSplash(final Intent intent)
    {
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                startActivity(intent);
                finish();
            }
        },2000);
    }
}

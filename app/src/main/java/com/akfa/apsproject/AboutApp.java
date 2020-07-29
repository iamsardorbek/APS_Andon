package com.akfa.apsproject;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutApp extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;
    private String employeeLogin, employeePosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        setTitle("О приложении");
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");
        toggle = InitNavigationBar.setUpNavBar(AboutApp.this, getApplicationContext(),  getSupportActionBar(), R.id.about, R.id.activity_about_app);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}

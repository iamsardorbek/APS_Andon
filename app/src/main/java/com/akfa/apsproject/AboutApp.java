package com.akfa.apsproject;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AboutApp extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        setTitle("О приложении");
        toggle = InitNavigationBar.setUpNavBar(AboutApp.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.about, R.id.activity_about_app);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}

package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

//------ВЕБ БРАУЗЕР СОСТОЯНИЯ ПРОИЗВОДСТВА, ВЫВОДЯЩИЙ САЙТ В WebView------//
//------Мастер и ремонтик имеет доступ к данному активити-------//
public class FactoryCondition extends AppCompatActivity {
    WebView webview;
    ActionBarDrawerToggle toggle;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_condition);
        initInstances();
        try {
            toggle = InitNavigationBar.setUpNavBar(FactoryCondition.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.web_monitoring, R.id.factory_condition);
        } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
        setTitle("Веб-мониторинг");
        //иниц WebView
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl(getString(R.string.website_address_factory_condition));
    }

    // find all variables
    protected void initInstances(){
        webview=findViewById(R.id.webview);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        }
        else {
            super.onBackPressed();
        }
    }
}
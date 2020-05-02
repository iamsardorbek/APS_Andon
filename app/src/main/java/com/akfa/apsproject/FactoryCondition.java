package com.akfa.apsproject;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class FactoryCondition extends AppCompatActivity{
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_condition);
        getSupportActionBar().hide();
        find_objects();
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl("https://aps-project-akfa.web.app/");
    }

    // find all variables
    protected void find_objects(){
        webview=findViewById(R.id.webview);

    }

}
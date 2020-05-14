package com.akfa.apsproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class FactoryCondition extends AppCompatActivity{
    WebView webview;
    ActionBarDrawerToggle toggle;
    String login, position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_condition);
        find_objects();
        toggle = setUpNavBar();
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl("https://aps-project-akfa.web.app/");
    }

    // find all variables
    protected void find_objects(){
        webview=findViewById(R.id.webview);
        login = getIntent().getExtras().getString("Логин пользователя");
        position = getIntent().getExtras().getString("Должность");
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Веб-мониторинг");
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.factory_condition);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        navigationView.getMenu().clear();
        switch(position){
            case "repair":
                navigationView.inflateMenu(R.menu.repair_menu);
                break;
            case "master":
                navigationView.inflateMenu(R.menu.master_menu);
                break;
            //other positions shouldn't be able to access checking page at all
            //if some changes, u can add a case
        }
        //ниже действия, выполняемые при нажатиях на элементы нав бара
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.urgent_problems:
                        Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                        openUrgentProblemsList.putExtra("Логин пользователя", login);
                        openUrgentProblemsList.putExtra("Должность", position);
                        startActivity(openUrgentProblemsList);
                        break;
                    case R.id.problems_list:
                        Intent openProblemsList = new Intent(getApplicationContext(), RepairersProblemsList.class);
                        openProblemsList.putExtra("Логин пользователя", login);
                        openProblemsList.putExtra("Должность", position);
                        startActivity(openProblemsList); //когда нажали на сам пульт, нав бар просто закрывается
                        break;
                    case R.id.web_monitoring: //переход в модуль проверки
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.check_equipment: //переход в модуль проверки
                        Intent openQuest = new Intent(getApplicationContext(), QuestMainActivity.class);
                        openQuest.putExtra("Логин пользователя", login);
                        openQuest.putExtra("Должность", position);
                        startActivity(openQuest);
                        break;
                    case R.id.about: //инфа про приложение и компанию и иинструкции может
//                        Intent openAbout = new Intent(getApplicationContext(), About.class);
//                        startActivity(openAbout);
                        Toast.makeText(getApplicationContext(), "Приложение создано Akfa R&D в 2020 году в Ташкенте.",Toast.LENGTH_SHORT).show();break;
                    case R.id.log_out: //возвращение в логин page
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.clear();
                        editor.commit();
                        Intent logOut = new Intent(getApplicationContext(), Login.class);
                        logOut.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(logOut);
                        finish();
                    default:
                        return true;
                }
                return true;
            }
        });
        return toggle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}
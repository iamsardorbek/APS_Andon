package com.akfa.apsproject;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//----------------------АКТИВИТИ, ВЫВОДЯЩЕЕ ИТОГИ ТО ПРОВЕРКИ----------------------//
public class QuestEndOfChecking extends AppCompatActivity {
    Button newChecking; //кнопка, переводящая юзера в QuestMainActivity для запуска новой ТО проверки
    TextView shop, equipmentLine, numberOfProblems, checkingDuration; //данные и  статистика, накопившаяся за текущую ТО проверку
    String employeeLogin, employeePosition; //данные о пользователе, чтобы передавать их в дальнейшие активити
    ActionBarDrawerToggle toggle; //для navigation bar
    //Данные, которые нужно вывести:
//    из таблицы equipment возьми название цеха основываясь на номере цеха - Quest Main Activity.groupPositionG
//    из таблицы equipment возьми название оборудования/линии основываясь на номере оборудования/линии - Quest Main Activity.childPositionG
    // время проверки - QuestPointDynamic.checkDuration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_end_of_checking);
        setTitle(R.string.endOfChecking); //задать текст app bar как "Проверка линии закончена"

        initInstances(); //инициализировать переменные и объекты views
        //currentMenuID is set to -1 because when you click on Проверка линий u should be taken to QuestMainActivity
        toggle = InitNavigationBar.setUpNavBar(QuestEndOfChecking.this, getApplicationContext(),  getSupportActionBar(), employeeLogin, employeePosition, -1, R.id.quest_activity_end_of_checking); //настроить нав бар
        setTitle("Проверка линий");

        DatabaseReference equipmentRef = FirebaseDatabase.getInstance().getReference("Shops/" + QuestMainActivity.shopNoGlobal);
        equipmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                shop.setText(shopSnap.child("shop_name").getValue().toString()); //название цеха записать в текствью shop
                String equipmentLineName = shopSnap.child("Equipment_lines/" + QuestMainActivity.equipmentNoGlobal + "/equipment_name").getValue().toString(); //название линии записать в текствью equipmentLine
                equipmentLine.setText(equipmentLineName);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        newChecking = findViewById(R.id.newChecking);
        newChecking.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Возвратиться на главное окно
                Intent intent = new Intent(getApplicationContext(), QuestMainActivity.class);
                intent.putExtra("Логин пользователя", employeeLogin);
                intent.putExtra("Должность", employeePosition);

                startActivity(intent);
            }
        });
    }

    private void initInstances()
    {
        //инициализируем все элементы дизайна
        shop = findViewById(R.id.shop);
        equipmentLine = findViewById(R.id.equipmentLine);
        numberOfProblems = findViewById(R.id.numberOfProblems);
        checkingDuration = findViewById(R.id.checkingDuration);
        //инициализируем все переменные, переданные в arguments
        Bundle arguments = getIntent().getExtras();
        int problemsCount = arguments.getInt("Количество обнаруженных проблем");
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");

        //задать текста в textviews с данными
        shop.setText(Integer.toString(QuestMainActivity.shopNoGlobal)); //для подстраховки задаем номер цеха
        equipmentLine.setText(Integer.toString(QuestMainActivity.equipmentNoGlobal)); //и номер линии
        numberOfProblems.setText(Integer.toString(problemsCount)); //задаем количество обнаруженных
        checkingDuration.setText(QuestPointDynamic.checkDuration); //задаем в textview checkingDuration продолжительность проверки в мм:сс
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if(isTaskRoot()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPrefs.getString("Логин пользователя", null) == null) //Еcли в sharedPrefs есть данные юзера, открой соот активти
            {
                stopService(new Intent(getApplicationContext(), BackgroundService.class)); //если до этого уже сервис был включен, выключи сервис
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                stopService(new Intent(getApplicationContext(), BackgroundService.class));
                final Handler handler = new Handler();
                Runnable runnableCode = new Runnable() {
                    @Override
                    public void run() {
                        //do something you want
                        //stop service
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if (sharedPrefs.getString("Логин пользователя", null) == null) //Еcли в sharedPrefs есть данные юзера, открой соот активти
                        {
                            stopService(new Intent(getApplicationContext(), BackgroundService.class)); //если до этого уже сервис был включен, выключи сервис
                        }
                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();

                    }
                };
                handler.postDelayed(runnableCode, 12000);
            }
            super.onBackPressed();
        }
    }
}


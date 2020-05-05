package com.akfa.apsproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

public class QuestEndOfChecking extends AppCompatActivity {
    Button newChecking;
    TextView shop, equipmentLine, numberOfProblems, checkingDuration;
    String employeeLogin, employeePosition;
    ActionBarDrawerToggle toggle;
    //Данные, которые нужно вывести:
//    из таблицы equipment возьми название цеха основываясь на номере цеха - MainActivity.groupPositionG
//    из таблицы equipment возьми название оборудования/линии основываясь на номере оборудования/линии - MainActivity.childPositionG

    // время проверки - PointDynamic.checkDuration
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_end_of_checking);
        getSupportActionBar().hide();
        shop = findViewById(R.id.shop);
        equipmentLine = findViewById(R.id.equipmentLine);
        numberOfProblems = findViewById(R.id.numberOfProblems);
        checkingDuration = findViewById(R.id.checkingDuration);
        Bundle arguments = getIntent().getExtras();
        int problemsCount = arguments.getInt("Количество обнаруженных проблем");
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");
        toggle = setUpNavBar();

        shop.setText(Integer.toString(QuestMainActivity.groupPositionG));
        equipmentLine.setText(Integer.toString(QuestMainActivity.childPositionG));
        numberOfProblems.setText(Integer.toString(problemsCount));
        checkingDuration.setText(QuestPointDynamic.checkDuration);

        DatabaseReference equipmentRef = FirebaseDatabase.getInstance().getReference("Shops/" + QuestMainActivity.groupPositionG);
        equipmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                shop.setText(shopSnap.child("shop_name").getValue().toString()); //название цеха записать в текствью
                String equipmentLineName = shopSnap.child("Equipment_lines/" + QuestMainActivity.childPositionG + "/equipment_name").getValue().toString(); //название линии
                equipmentLine.setText(equipmentLineName);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        newChecking = findViewById(R.id.newChecking);
        newChecking.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Возвратиться на главное окно
                Intent intent = new Intent(getApplicationContext(), QuestMainActivity.class);
                startActivity(intent);
            }
        });
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Проверка линий");
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.quest_activity_end_of_checking);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
//        здесь адаптируем меню в нав баре в зависимости от уровня доступа пользователя: мастер/оператор, у ремонтника нет прав проверки
        navigationView.getMenu().clear();
        switch(employeePosition){
            case "operator":
                navigationView.inflateMenu(R.menu.operator_menu);
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
                    case R.id.pult:
                        Intent openMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        openMainActivity.putExtra("Логин пользователя", employeeLogin);
                        openMainActivity.putExtra("Должность", employeePosition);
                        startActivity(openMainActivity);
                        break;
                    case R.id.check_equipment: //переход в модуль проверки
                        drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на саму проверку, нав бар просто закрывается
                        break;
                    case R.id.web_monitoring:
                        Intent openFactoryCondition = new Intent(getApplicationContext(), FactoryCondition.class);
                        openFactoryCondition.putExtra("Логин пользователя", employeeLogin);
                        openFactoryCondition.putExtra("Должность", employeePosition);
                        startActivity(openFactoryCondition);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}


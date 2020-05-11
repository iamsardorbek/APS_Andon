package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UrgentProblemsList extends AppCompatActivity implements View.OnTouchListener {
    private final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;
    private List<String> problemIDs;
    private Button qrScan;
    ActionBarDrawerToggle toggle;
    LinearLayout linearLayout;
    String employeeLogin, employeePosition;
    View.OnClickListener textviewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent_problems_list);
        qrScan = findViewById(R.id.qr_scan);
        linearLayout = findViewById(R.id.linearLayout);
        qrScan.setOnTouchListener(this);
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");
        toggle = setUpNavBar();
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int nomerProblemy = v.getId() - ID_TEXTVIEWS;
//                Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
//                String IDOfSelectedProblem = problemIDs.get(nomerProblemy);
//                intent.putExtra("ID проблемы в таблице Problems", IDOfSelectedProblem);
//                intent.putExtra("Логин пользователя", employeeLogin);
//                startActivity(intent);
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                openQR.putExtra("Открой PointDynamic", "срочная проблема");
                openQR.putExtra("Должность", employeePosition);
                openQR.putExtra("Логин пользователя", employeeLogin); //передавать логин пользователя взятый из Firebase
                startActivity(openQR);
            }
        };
        addProblemsFromDatabase();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                openQR.putExtra("Открой PointDynamic", "срочная проблема");
                openQR.putExtra("Должность", employeePosition);
                openQR.putExtra("Логин пользователя", employeeLogin); //передавать логин пользователя взятый из Firebase
                startActivity(openQR);
                break;
        }
        return false;
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.activity_urgent_problems_list);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
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
                    case R.id.urgent_problems:
                        drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на сам пульт, нав бар просто закрывается
                    case R.id.problems_list:
                        Intent openProblemsList = new Intent(getApplicationContext(), RepairersProblemsList.class);
                        openProblemsList.putExtra("Логин пользователя", employeeLogin);
                        openProblemsList.putExtra("Должность", employeePosition);
                        startActivity(openProblemsList); //когда нажали на сам пульт, нав бар просто закрывается
                        break;
                    case R.id.check_equipment: //переход в модуль проверки
                        Intent openQuest = new Intent(getApplicationContext(), QuestMainActivity.class);
                        openQuest.putExtra("Логин пользователя", employeeLogin);
                        openQuest.putExtra("Должность", employeePosition);
                        startActivity(openQuest);
                        break;
                    case R.id.web_monitoring: //переход в модуль проверки
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
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void addProblemsFromDatabase() {
        //на самом деле нужно взять количество строк в таблице problems
        DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference().child("Urgent_problems");
        urgentProblemsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override public void onDataChange(@NonNull DataSnapshot urgentProblemsSnap) {
                linearLayout.removeAllViews(); //для обновления данных удали все результаты предыдущего поиска
                if(urgentProblemsSnap.getValue() == null)
                {
                    setTitle("Все проблемы решены");
                }
                else
                {
                    setTitle("Срочные проблемы на линиях");
                    for(DataSnapshot urgentProblemSnap : urgentProblemsSnap.getChildren())
                    {
                        UrgentProblem urgentProblem = urgentProblemSnap.getValue(UrgentProblem.class);
//                        problemIDs.add(urgentProblemSnap.getKey());
                        String problemInfoFromDB = "Цех: " + urgentProblem.getShop_name() + "\nОборудование: " + urgentProblem.getEquipment_name() + "\nУчасток №" + urgentProblem.getStation_no()
                                                        + "\nДата и время обнаружения: " + urgentProblem.getDate_detected() + " " + urgentProblem.getTime_detected();
                        TextView problemsInfo;
                        problemsInfo = new TextView(getApplicationContext());
                        problemsInfo.setText(problemInfoFromDB);
                        problemsInfo.setPadding(25, 25, 25, 25);
                        problemsInfo.setId(ID_TEXTVIEWS + problemCount);
                        problemsInfo.setTextColor(Color.parseColor(getString(R.color.text)));
                        problemsInfo.setTextSize(13);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(20, 25, 20, 25);
                        problemsInfo.setLayoutParams(params);
                        problemsInfo.setClickable(true);
                        problemsInfo.setBackgroundResource(R.drawable.list_group_layout);
                        problemsInfo.setOnClickListener(textviewClickListener);
                        linearLayout.addView(problemsInfo);
                        problemCount++;
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {            }
        });
    }
}

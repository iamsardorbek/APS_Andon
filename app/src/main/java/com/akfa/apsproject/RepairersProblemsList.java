package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class RepairersProblemsList extends AppCompatActivity {
    private final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;
    private List<String> problemIDs;
    private String login, position;
    LinearLayout linearLayout;
    View.OnClickListener textviewClickListener;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairers_activity_problems_list);
        toggle = setUpNavBar();
        login = getIntent().getExtras().getString("Логин пользователя");
        position = getIntent().getExtras().getString("Должность");
        linearLayout = findViewById(R.id.linearLayout);
        problemIDs = new ArrayList<>();
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nomerProblemy = v.getId() - ID_TEXTVIEWS;
                Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                String IDOfSelectedProblem = problemIDs.get(nomerProblemy);
                intent.putExtra("ID проблемы в таблице Problems", IDOfSelectedProblem);
                intent.putExtra("Логин пользователя", login);
                startActivity(intent);
            }
        };
        addProblemsFromDatabase();
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Загрузка данных...");
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.repairers_activity);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        //ниже действия, выполняемые при нажатиях на элементы нав бара
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.problems_list:
                        drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на сам пульт, нав бар просто закрывается
                        break;
                    case R.id.web_monitoring: //переход в модуль проверки
                        Intent openFactoryCondition = new Intent(getApplicationContext(), FactoryCondition.class);
                        openFactoryCondition.putExtra("Логин пользователя", login);
                        openFactoryCondition.putExtra("Должность", position);
                        startActivity(openFactoryCondition);
                        break;
                    case R.id.about: //инфа про приложение и компанию и иинструкции может
//                        Intent openAbout = new Intent(getApplicationContext(), About.class);
//                        startActivity(openAbout);
                        Toast.makeText(getApplicationContext(), "Приложение создано Akfa R&D в 2020 году в Ташкенте.",Toast.LENGTH_SHORT).show();break;
                    case R.id.log_out: //возвращение в логин page
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
        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference().child("Problems");
        problemsRef.orderByChild("solved").equalTo(false).addChildEventListener(new ChildEventListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onChildAdded(@NonNull DataSnapshot problemDataSnapshot, @Nullable String prevChildKey) {
                Problem problem = problemDataSnapshot.getValue(Problem.class);
                problemIDs.add(problemDataSnapshot.getKey());
                String problemInfoFromDB = "Цех: " + problem.getShop_name() + "\nОборудование: " + problem.getEquipment_line_name() + "\nПункт №" + problem.getPoint() + "\nПодпункт №" + problem.getSubpoint();
                TextView problemsInfo;
                problemsInfo = new TextView(getApplicationContext());
                problemsInfo.setText(problemInfoFromDB);
                problemsInfo.setId(ID_TEXTVIEWS + problemCount);
                problemsInfo.setTextColor(Color.parseColor(getString(R.color.text)));
                problemsInfo.setTextSize(15);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(25*2, 25*2, 25*2, 25*2);
                problemsInfo.setLayoutParams(params);
                problemsInfo.setClickable(true);
                problemsInfo.setBackgroundResource(R.drawable.list_group_layout);
                problemsInfo.setOnClickListener(textviewClickListener);
                linearLayout.addView(problemsInfo);
                problemCount++;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

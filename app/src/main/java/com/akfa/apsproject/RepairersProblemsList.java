package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;

//----------ПОКАЗЫВАЕТ СПИСОК ТО ПРОБЛЕМ НА ЗАВОДЕ-------//
//----------ПРИ НАЖАТИИ НА TextView С ПРОБЛЕМОЙ, ОТКРЫВАЕТ RepairersSeparateProblem---------//
//---------layout xml пустой почти, потому что элементы динамически добавляются, заголовок задается программно---------//
public class RepairersProblemsList extends AppCompatActivity {
    private final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;
    private String login, position;
    private List<String> problemIDs;
    LinearLayout linearLayout;
    ActionBarDrawerToggle toggle;
    View.OnClickListener textviewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairers_activity_problems_list);
        setTitle("Загрузка данных..."); //если нет проблем, надо сделать: нету проблем
        initInstances();
        toggle = setUpNavBar();
        addProblemsFromDatabase();
    }
    private void initInstances()
    {//иниц кросс-активити перем-х
        login = getIntent().getExtras().getString("Логин пользователя");
        position = getIntent().getExtras().getString("Должность");
        linearLayout = findViewById(R.id.linearLayout);
        problemIDs = new ArrayList<>();
        //к каждому textview проблемы будет прикреплен этот listener
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nomerProblemy = v.getId() - ID_TEXTVIEWS;
                Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                String IDOfSelectedProblem = problemIDs.get(nomerProblemy);
                intent.putExtra("ID проблемы в таблице Maintenance_problems", IDOfSelectedProblem);
                intent.putExtra("Логин пользователя", login);
                intent.putExtra("Должность", position);
                startActivity(intent);
            }
        };
    }

    private void addProblemsFromDatabase() {
        //----САМОЕ ГЛАВНОЕ ЭТОГО АКТИВИТИ----//
        //на самом деле нужно взять количество строк в таблице problems
        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference().child("Maintenance_problems"); //ссылка на проблемы ТО
        problemsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override public void onDataChange(@NonNull DataSnapshot problemsSnap) {
                linearLayout.removeAllViews(); //для обновления данных удали все результаты предыдущего поиска
                if(problemsSnap.getValue() == null)
                {
                    setTitle("Все проблемы решены");
                }
                else
                {
                    setTitle("Проблемы на линиях");
                    for(DataSnapshot problemDataSnapshot : problemsSnap.getChildren())
                    { //пройдись по всем проблемах в ветке
                        MaintenanceProblem problem = problemDataSnapshot.getValue(MaintenanceProblem.class); //считай в объект
                        problemIDs.add(problemDataSnapshot.getKey()); //добавь айди данной проблемы в лист

                        //инициализация TEXTVIEW
                        String problemInfoFromDB = "Цех: " + problem.getShop_name() + "\nОборудование: " + problem.getEquipment_line_name() + "\nУчасток №" + problem.getStation_no() + "\nПункт №" + problem.getPoint_no();
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
                        //добавить textview в layout
                        linearLayout.addView(problemsInfo);
                        problemCount++; //итерируй для уникализации айдишек textview и обращения к лист элементам
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
        drawerLayout = findViewById(R.id.repairers_activity);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        View headerView = navigationView.getHeaderView(0);
        TextView userInfo = headerView.findViewById(R.id.user_info);
        userInfo.setText(login);
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

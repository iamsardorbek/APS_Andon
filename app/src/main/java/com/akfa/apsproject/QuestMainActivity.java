package com.akfa.apsproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class QuestMainActivity extends AppCompatActivity  {

    static int childPositionG, groupPositionG;

    ExpandableListView expandableListView; //для выпадающего списка
    Button startWithQR;
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    private MainAdapter adapter;
    private String login, position;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //inter-activity values
        login = getIntent().getExtras().getString("Логин пользователя");
        position = getIntent().getExtras().getString("Должность");
        initExpandableListView(); //иницилизация выпадающего списка
        startWithQR = findViewById(R.id.start_with_qr);
        startWithQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
//                openQR.putExtra("")
                //nedds edits, extras
                openQR.putExtra("Открой PointDynamic", "другое");
                openQR.putExtra("Должность", position);
                openQR.putExtra("Логин пользователя", login); //передавать логин пользователя взятый из Firebase
                startActivity(openQR);
            }
        });
        toggle = setUpNavBar();
    }

    private void initExpandableListView() {
        //инициализация выпадающего списка
        expandableListView = findViewById(R.id.equipment_list);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new MainAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(adapter);
        initListData();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //пользователь выбрал линию для проверки передаем данные в QRScanner
                //логин в QRScanner не используется explicitly, он передается в PointDynamic. Таким образом QrScanner выступает посредником передачи логина
                if(position.equals("master")) {
                    groupPositionG = groupPosition;
                    childPositionG = childPosition;
                }
                Intent intent = new Intent(getApplicationContext(), MachineLayoutActivity.class);
                intent.putExtra("Номер цеха", QuestMainActivity.groupPositionG);
                intent.putExtra("Номер линии", QuestMainActivity.childPositionG);
                intent.putExtra("Логин пользователя", login); //передавать логин пользователя взятый из Firebase
                intent.putExtra("Должность", position);
                startActivity(intent);
                return false;
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
        drawerLayout = findViewById(R.id.quest_activity_main);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
//        здесь адаптируем меню в нав баре в зависимости от уровня доступа пользователя: мастер/оператор, у ремонтника нет прав проверки
        navigationView.getMenu().clear();
        switch(position){
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
                        Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                        openUrgentProblemsList.putExtra("Логин пользователя", login);
                        openUrgentProblemsList.putExtra("Должность", position);
                        startActivity(openUrgentProblemsList);
                        break;
                    case R.id.pult:
                        Intent openMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        openMainActivity.putExtra("Логин пользователя", login);
                        openMainActivity.putExtra("Должность", position);
                        startActivity(openMainActivity);
                        break;
                    case R.id.check_equipment: //переход в модуль проверки
                        drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на саму проверку, нав бар просто закрывается
                        break;
                    case R.id.web_monitoring:
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    TreeMap<Integer,  Shop> shopsMap = new TreeMap<>();
    //добавить данные в раскрывающийся список
    private void initListData() {
        switch (position)
        {
            case "operator":
                //у оператора есть возможность провести проверку только на своей линии, поэтому в ExpandableListView покажем его линию
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + login);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        final String userEquipmentName = user.child("equipment_name").getValue().toString();
                        final String userShopName = user.child("shop_name").getValue().toString();
                        DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference("Shops");
                        shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot shops) {
                                int shopNo = 99, equipmentNo = 99; //IDE ругается что might not be initialized, поэтому задал эти числа
                                for (DataSnapshot shop : shops.getChildren()) { //считать данные объекта цеха в объект Shop (aka распарсить данные с файрбейз снэпшот в объект)
                                    shopNo = Integer.parseInt(shop.getKey());
                                    String shopName = (String) shop.child("shop_name").getValue();
                                    if (shopName.equals(userShopName)) {
                                        Shop currentShop = new Shop(shopName);
                                        DataSnapshot equipmentLines = shop.child("Equipment_lines");
                                        for (DataSnapshot equipmentLine : equipmentLines.getChildren()) { //распарсить линии оборудования
                                            equipmentNo = Integer.parseInt(equipmentLine.getKey());
                                            String equipmentName = (String) equipmentLine.child("equipment_name").getValue();
                                            if (equipmentName.equals(userEquipmentName)) {
                                                currentShop.equipmentLines.put(equipmentNo, equipmentName);
                                                //поставь это в дерево мап, которое будет вставлять с сортировкой
                                                shopsMap.put(shopNo, currentShop);
                                                //числа ниже используются для обращения к Firebase в Pointdynamic+QRScanner
                                                groupPositionG = shopNo;
                                                childPositionG = equipmentNo;
                                                //количество цехов для ограничения цикла
                                                //добавить это в лист и мап, нужные для добавления элементов в expandableListView
//                                                Shop currentShop = shopsMap.get(shopNo);
                                                listGroup.add(currentShop.name); //добавь название цеха в заголовок expandableTextView
                                                List<String> listOfEquipmentLines = new ArrayList<>();
                                                //перевести данные о линиях (их названия) из дерево-мапа в лист
                                                listOfEquipmentLines.add(currentShop.equipmentLines.get(equipmentNo));
                                                //добавить названия линий в expandableListView
                                                listItem.put(currentShop.name, listOfEquipmentLines);
                                                //обнови адаптер, чтобы изменения стали видны
                                                adapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
                break;
            case "master":
                // у мастера доступ ко всем линиям и цехам, поэтому ему мы покажем все
                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference("Shops");
                shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shops) {
                        for(DataSnapshot shop : shops.getChildren())
                        { //считать данные объекта цеха в объект Shop (aka распарсить данные с файрбейз снэпшот в объект)
                            int shopNo = Integer.parseInt(shop.getKey());
                            String shopName = (String) shop.child("shop_name").getValue();
                            Shop currentShop = new Shop(shopName);
                            DataSnapshot equipmentLines = shop.child("Equipment_lines");
                            for(DataSnapshot equipmentLine : equipmentLines.getChildren())
                            { //распарсить линии оборудования
                                int equipmentNo = Integer.parseInt(equipmentLine.getKey());
                                String equipmentName = (String) equipmentLine.child("equipment_name").getValue();
                                currentShop.equipmentLines.put(equipmentNo, equipmentName);
                            }
                            //поставь это в дерево мап, которое будет вставлять с сортировкой
                            shopsMap.put(shopNo, currentShop);
                        }
                        //количество цехов для ограничения цикла
                        int numOfShops = shopsMap.size();
                        for(int i = 0; i < numOfShops; i++)
                        { //добавить это в лист и мап, нужные для добавления элементов в expandableListView
                            Shop currentShop = shopsMap.get(i);
                            listGroup.add(currentShop.name); //добавь название цеха в заголовок expandableTextView
                            int numOfEquipmentLines = currentShop.equipmentLines.size(); //для ограничения итераций в цикле
                            List<String> listOfEquipmentLines = new ArrayList<>();
                            for(int j = 0; j < numOfEquipmentLines; j++)
                            { //перевести данные о линиях (их названия) из дерево-мапа в лист
                                listOfEquipmentLines.add(currentShop.equipmentLines.get(j));
                            }
                            //добавить названия линий в expandableListView
                            listItem.put(currentShop.name, listOfEquipmentLines);
                        }
                        //обнови адаптер, чтобы изменения стали видны
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
        }
    }
}


package com.akfa.apsproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    private MainAdapter adapter;
    private String login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_main);
        getSupportActionBar().hide();
        login = getIntent().getExtras().getString("Логин пользователя");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
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
                groupPositionG = groupPosition;
                childPositionG = childPosition;
                Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                intent.putExtra("Номер цеха", QuestMainActivity.groupPositionG);
                intent.putExtra("Номер линии", QuestMainActivity.childPositionG);
                int INITIAL_POINT_NUMBER_FOR_QR = 1;
                intent.putExtra("Номер пункта", INITIAL_POINT_NUMBER_FOR_QR); //1
                intent.putExtra("Открой PointDynamic", "да");
                intent.putExtra("Логин пользователя", login); //передавать логин пользователя взятый из Firebase в будущем
                startActivity(intent);
                return false;
            }
        });
    }

    TreeMap<Integer,  Shop> shopsMap = new TreeMap<>();

    //добавить данные в раскрывающийся список
    private void initListData() {
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
    }
}


package com.akfa.apsproject.checking_equipment_maintenance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.ExpandableListViewAdapter;
import com.akfa.apsproject.classes_serving_other_classes.InitNavigationBar;
import com.akfa.apsproject.QRScanner;
import com.akfa.apsproject.R;
import com.akfa.apsproject.general_data_classes.Shop;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

//------------ВЫВОДИТ EXPANDABLE LIST VIEW C ЦЕХАМИ И ЛИНИЯМИ------------//
//------------ТАКЖЕ ЕСТЬ КНОПКА QR СКАНЕРА, ЧТОБЫ СРАЗУ ОТСКАНИРОВАТЬ И НАЧАТЬ ПРОВЕРКУ------------//
//------------ПРИ НАЖАТИИ НА НАЗВАНИЯ ЛИНИЙ, ПЕРЕВОДИТ В MACHINE LAYOUT ACTIVITY------------//
public class QuestListOfEquipment extends AppCompatActivity implements View.OnTouchListener {

    static int equipmentNoGlobal, shopNoGlobal; //глоб переменные, используемые также в EndOfChecking

    Button startWithQR; //КНОПКА QR СКАНЕРА, ЧТОБЫ СРАЗУ ОТСКАНИРОВАТЬ И НАЧАТЬ ПРОВЕРКУ

    //связано с ExpandableListView
    ExpandableListView expandableListView; //для выпадающего списка
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    private ExpandableListViewAdapter adapter;

    private ActionBarDrawerToggle toggle; //для нав бара

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_list_of_equipment);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); //какой-то месседж компилятору, чтобы drawable resources задаваемые объектам могли быть векторными

        initInstances(); //инициализация переменных и кнопки
        initExpandableListView(); //иницилизация выпадающего списка
        toggle = InitNavigationBar.setUpNavBar(QuestListOfEquipment.this, getApplicationContext(),  getSupportActionBar(), R.id.check_equipment, R.id.quest_activity_main);
        setTitle(getString(R.string.maintenance_check));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances()
    {
        //inter-activity values
        startWithQR = findViewById(R.id.start_with_qr);
        startWithQR.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //дейсвтие при нажатиях на кнопку (отсканировать QR код)
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startWithQR.setBackgroundResource(R.drawable.edit_red_accent_pressed); //эффект нажатия
                break;
            case MotionEvent.ACTION_UP: //когда уже отпустил, октрой qr
                //запуск QR сканера отсканировав  qr код 1-го пункта любой линии

                startWithQR.setBackgroundResource(R.drawable.edit_red_accent);
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                openQR.putExtra("Действие", "Любой код");
                startActivity(openQR);
                break;
        }
        return false;
    }

    private void initExpandableListView() {
        //инициализация выпадающего списка
        expandableListView = findViewById(R.id.equipment_list);
        listGroup = new ArrayList<>(); //для названий цехов
        listItem = new HashMap<>(); //для названий линий
        adapter = new ExpandableListViewAdapter(this, listGroup, listItem); //адаптер задает элементы expListView сам
        expandableListView.setAdapter(adapter);
        try{
        initListData(); //получить данные из БД и записать их в спец
        }
        catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe);
            TextView exceptionText = findViewById(R.id.exception_text);
            exceptionText.setVisibility(View.VISIBLE);
        }
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //пользователь выбрал линию для проверки передаем данные в QRScanner
                //логин в QRScanner не используется explicitly, он передается в PointDynamic. Таким образом QrScanner выступает посредником передачи логина
                if(UserData.position.equals("master") || UserData.position.equals("repair")) {
                    shopNoGlobal = groupPosition;
                    equipmentNoGlobal = childPosition;
                }
                Intent intent = new Intent(getApplicationContext(), QuestMachineLayoutActivity.class);
                intent.putExtra("Номер цеха", QuestListOfEquipment.shopNoGlobal);
                intent.putExtra("Номер линии", QuestListOfEquipment.equipmentNoGlobal);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    TreeMap<Integer, Shop> shopsMap = new TreeMap<>();
    private void initListData() {//добавить данные в раскрывающийся список
        switch (UserData.position)
        { //динамически добавляет данные о доступных для этого юзера линий для проверки (мастер может проверить все линии во всех цехах; оператор может проверить только одну линию)
            case "operator":
                //у оператора есть возможность провести проверку только на своей линии, поэтому в ExpandableListView покажем только его линию
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login); //ссылка на ветку этого юзера (чтобы получить назв-я его цеха и линии)
                userRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override public void onDataChange(@NonNull DataSnapshot user)
                    {
                        final String userEquipmentName = user.child("equipment_name").getValue().toString(); //полное название его линии
                        final String userShopName = user.child("shop_name").getValue().toString(); //полное название его цеха
                        DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref)); //сделаем query и найдем ветку Firebase именно его линии, этот же концепт используется в PultActivity
                        shopsRef.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override public void onDataChange(@NonNull DataSnapshot shops)
                            {
                                int shopNo, equipmentNo; //конечное и релевантные индексов цеха и линии будут заданы в equipmentNoGlobal, shopNoGlobal
                                for (DataSnapshot shop : shops.getChildren()) //пройдем по каждой подветке shop пока не найдем нужный нам shop с shop_name соответствующий названию цеха в ветке юзера
                                { //считать данные объекта цеха в объект Shop (aka распарсить данные с файрбейз снэпшот в объект)
                                    shopNo = Integer.parseInt(Objects.requireNonNull(shop.getKey()));
                                    String shopNameDefaultLocale = (String) shop.child("shop_name").getValue();
                                    if (userShopName.equals(shopNameDefaultLocale)) //оппа, наша ветка юзера нашлась
                                    {
                                        String shopName = (String) shop.child(getString(R.string.shop_name)).getValue();
                                        Shop currentShop = new Shop(shopName); //сохранить данные о цехе в объекте Shop
                                        DataSnapshot equipmentLines = shop.child("Equipment_lines");

                                        for (DataSnapshot equipmentLine : equipmentLines.getChildren()) //пройтись по линиям данного цеха, найти нужную линию и внести это в expandableListView
                                        {
                                            equipmentNo = Integer.parseInt(Objects.requireNonNull(equipmentLine.getKey()));
                                            String equipmentNameDefaultLocale = (String) equipmentLine.child("equipment_name").getValue();
                                            if (userEquipmentName.equals(equipmentNameDefaultLocale)) { //релевантная линия относящаяся к данному оператору
                                                String equipmentName = (String) equipmentLine.child(getString(R.string.equipment_name)).getValue();
                                                currentShop.equipmentLines.put(equipmentNo, equipmentName);
                                                //поставь это в дерево мап, которое будет вставлять с сортировкой
                                                shopsMap.put(shopNo, currentShop);
                                                //числа ниже используются для обращения к Firebase в Pointdynamic+QRScanner
                                                shopNoGlobal = shopNo;
                                                equipmentNoGlobal = equipmentNo;
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
                                                return; //нашли нужную линию, записали данные, заканчивай работу функции (закрой ее)
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
            case "repair": //у ремонтника и
            case "master": // у мастера доступ ко всем линиям и цехам
                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref));
                shopsRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override public void onDataChange(@NonNull DataSnapshot shops)
                    {
                        for(DataSnapshot shop : shops.getChildren())
                        { //считать данные объекта цеха в объект Shop (aka распарсить данные с файрбейз снэпшот в объект)
                            int shopNo = Integer.parseInt(Objects.requireNonNull(shop.getKey()));
                            String shopName = (String) shop.child(getString(R.string.shop_name)).getValue();
                            Shop currentShop = new Shop(shopName);
                            DataSnapshot equipmentLines = shop.child("Equipment_lines");
                            for(DataSnapshot equipmentLine : equipmentLines.getChildren())
                            { //распарсить линии оборудования
                                int equipmentNo = Integer.parseInt(Objects.requireNonNull(equipmentLine.getKey()));
                                String equipmentName = (String) equipmentLine.child(getString(R.string.equipment_name)).getValue();
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
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                break;
        }
    }

}


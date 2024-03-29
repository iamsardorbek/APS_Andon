package com.akfa.apsproject.checking_equipment_maintenance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.InitNavigationBar;
import com.akfa.apsproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

//----------------------АКТИВИТИ, ВЫВОДЯЩЕЕ ИТОГИ ТО ПРОВЕРКИ----------------------//
public class QuestEndOfChecking extends AppCompatActivity implements View.OnTouchListener {
    Button newChecking; //кнопка, переводящая юзера в QuestMainActivity для запуска новой ТО проверки
    TextView shop, equipmentLine, numberOfProblems, checkingDuration; //данные и  статистика, накопившаяся за текущую ТО проверку
    ActionBarDrawerToggle toggle; //для navigation bar
    //Данные, которые нужно вывести:
//    из таблицы equipment возьми название цеха основываясь на номере цеха - Quest Main Activity.groupPositionG
//    из таблицы equipment возьми название оборудования/линии основываясь на номере оборудования/линии - Quest Main Activity.childPositionG
    // время проверки - QuestPointDynamic.checkDuration

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_end_of_checking);
        setTitle(R.string.checkCompleted); //задать текст app bar как "Проверка линии закончена"
        try {
            initInstances(); //инициализировать переменные и объекты views

            //currentMenuID is set to -1 because when you click on Проверка линий u should be taken to QuestMainActivity
            toggle = InitNavigationBar.setUpNavBar(QuestEndOfChecking.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), -1, R.id.quest_activity_end_of_checking); //настроить нав бар
            setTitle(getString(R.string.maintenance_check));

            DatabaseReference equipmentRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref) + "/" + QuestListOfEquipment.shopNoGlobal);
            equipmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                    shop.setText(Objects.requireNonNull(shopSnap.child(getString(R.string.shop_name)).getValue()).toString()); //название цеха записать в текствью shop
                    String equipmentLineName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + QuestListOfEquipment.equipmentNoGlobal + "/" + getString(R.string.equipment_name)).getValue()).toString(); //название линии записать в текствью equipmentLine
                    equipmentLine.setText(equipmentLineName);
                }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        catch (AssertionError | NullPointerException e) {
            ExceptionProcessing.processException(e);}
        newChecking = findViewById(R.id.newChecking);
        newChecking.setOnTouchListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void initInstances()
    {
        //инициализируем все элементы дизайна
        shop = findViewById(R.id.shop);
        equipmentLine = findViewById(R.id.equipmentLine);
        numberOfProblems = findViewById(R.id.numberOfProblems);
        checkingDuration = findViewById(R.id.checkingDuration);
        //инициализируем все переменные, переданные в arguments
        Bundle arguments = getIntent().getExtras();
        assert arguments != null;
        int problemsCount = arguments.getInt("Количество обнаруженных проблем");

        //задать текста в textviews с данными
        shop.setText(Integer.toString(QuestListOfEquipment.shopNoGlobal)); //для подстраховки задаем номер цеха
        equipmentLine.setText(Integer.toString(QuestListOfEquipment.equipmentNoGlobal)); //и номер линии
        numberOfProblems.setText(Integer.toString(problemsCount)); //задаем количество обнаруженных
        checkingDuration.setText(QuestPointDynamic.checkDuration); //задаем в textview checkingDuration продолжительность проверки в мм:сс
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //дейсвтие при нажатиях на кнопку (отсканировать QR код)
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                newChecking.setBackgroundResource(R.drawable.edit_red_accent_pressed); //эффект нажатия
                break;
            case MotionEvent.ACTION_UP: //когда уже отпустил, октрой qr
                //запуск QR сканера отсканировав  qr код 1-го пункта любой линии

                newChecking.setBackgroundResource(R.drawable.edit_red_accent);
                Intent intent = new Intent(getApplicationContext(), QuestListOfEquipment.class);
                startActivity(intent);
                break;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}


package com.akfa.apsproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuestEndOfChecking extends AppCompatActivity {
    Button newChecking;
    TextView shop, equipmentLine, numberOfProblems, checkingDuration;
    String employeeLogin, employeePosition;
    //Данные, которые нужно вывести:
//    из таблицы equipment возьми название цеха основываясь на номере цеха - MainActivity.groupPositionG
//    из таблицы equipment возьми название оборудования/линии основываясь на номере оборудования/линии - MainActivity.childPositionG
//    число проблем, обнаруженных у этой линии - PointDynamic.problemsCount
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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

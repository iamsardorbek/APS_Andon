package com.akfa.apsproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class QuestEndOfChecking extends AppCompatActivity {
    Button newChecking, closeApp;
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

        shop.setText(getString(R.string.shop) + QuestMainActivity.groupPositionG);
        equipmentLine.setText(getString(R.string.equipmentLine) + QuestMainActivity.childPositionG);
        numberOfProblems.setText(getString(R.string.numberOfProblems) + problemsCount);
        checkingDuration.setText(getString(R.string.checkingDuration) + QuestPointDynamic.checkDuration);

        newChecking = findViewById(R.id.newChecking);
        newChecking.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Возвратиться на главное окно
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        closeApp = findViewById(R.id.closeApp);
        closeApp.setOnClickListener(new Button.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                // завершить работу приложения и закрыть его
                finishAffinity();
                System.exit(0);
            }
        });
    }
}

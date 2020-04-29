package com.akfa.apsproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuestVerification extends AppCompatActivity {

    Button openDynamic;
    EditText login, password;
    private final int INITIAL_POINT_NUMBER_FOR_QR = 1;
    private String codeToDetect = "punkt 1, liniya 0, tsex 0, APS";
    Users[] users = new Users[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_verification);
        setCodeToDetect();
        users[0] = new Users("admin", "777", "Абдували", "Равшанов");
        users[1] = new Users("director", "777", "Сардор", "Рахмаджанов");
        users[2] = new Users("master", "111", "Шерали", "Жураев");
        users[3] = new Users("repairer", "111", "Дмитрий", "Билан");
        users[4] = new Users("operator", "111", "Шарофбой", "Зарифов");
        login = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        openDynamic = findViewById(R.id.button5);
        openDynamic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check if Name text control is not empty
                switch(checkUser(login.getText().toString(), password.getText().toString()))
                {
                    case 2:
                        Toast.makeText(getApplicationContext(), "Проверщик",
                                Toast.LENGTH_SHORT).show();
                        Log.i("Доступ разрешен", "Вывел Тост. Создаю интент");
                        //попросит подойти к пункту 1 и откроет окно QRScanner
                        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                        intent.putExtra("Номер цеха", QuestMainActivity.groupPositionG);
                        intent.putExtra("Номер линии", QuestMainActivity.childPositionG);
                        intent.putExtra("Номер пункта", INITIAL_POINT_NUMBER_FOR_QR); //1
                        intent.putExtra("Открой PointDynamic", "да");
                        intent.putExtra("Код пункта", codeToDetect);
                        intent.putExtra("Логин пользователя", login.getText().toString()); //передавать логин пользователя взятый из Firebase в будущем
                        startActivity(intent);
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "Ремонтник",
                                Toast.LENGTH_SHORT).show();
//                        intent = new Intent(getApplicationContext(), ProblemsListForRepairers.class);
//                        startActivity(intent);
                        break;
                    case 0:
                        Toast.makeText(getApplicationContext(), "Введите логин и пароль снова.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int checkUser(String login, String password) {
        for (int i = 0; i < 5; i++) {
            if (login.equals(users[i].login) & password.equals(users[i].password)) {
                if(login.equals("repairer"))
                    return 1;
                else
                    return 2;
            }
        }
        return 0;
    }

    private void setCodeToDetect()
    {
        DatabaseReference codeToDetectRef = FirebaseDatabase.getInstance().getReference()
                .child("Цеха/" + QuestMainActivity.groupPositionG + "/" + QuestMainActivity.childPositionG +
                        "/QR_коды/qr_1");
        codeToDetectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                codeToDetect = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

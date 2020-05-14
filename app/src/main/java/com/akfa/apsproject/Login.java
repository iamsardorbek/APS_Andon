package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private TextView loading;
    private EditText passwordView, loginView;
    private Button enter;
    private CheckBox rememberMe;
    private String login, password;
//context.startService(new Intent(context, BackgroundService.class)); //эта функция запускает фоновый сервис, когда время delayMillis прошло.
//        //он ждет Broadcast от системы, что его времяx закончилось и пора запускать сервис
// startService(new Intent(getBaseContext(), BackgroundService.class)); //эта функция запускает фоновый сервис
// stopService(new Intent(getBaseContext(), BackgroundService.class));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //возьми разрешение использования камеры
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
        }

        initInstances();
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login = loginView.getText().toString();
                password = passwordView.getText().toString();
                if(login.isEmpty() || password.isEmpty() || password.charAt(0) == ' ' || login.charAt(0) == ' ')
                {
                    Toast.makeText(getApplicationContext(), "Заполните оба поля корректно", Toast.LENGTH_SHORT).show();
                }
                else {
                    loading.setVisibility(View.VISIBLE);
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users/" + login);
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot user) {
                            loading.setVisibility(View.INVISIBLE);
                            if (user.exists()) {//3 ifs with boolean checker functions in condition: isOperator, isMaster, isRepairer
                                if (user.child("password").getValue().toString().equals(password)) {
                                    if (user.child("position").getValue().toString().equals("operator")) {
                                        Intent openPult = new Intent(getApplicationContext(), PultActivity.class);
//                                    openPult.putExtra("Номер пульта", user.child("pult_no").getValue().toString());
                                        openPult.putExtra("Логин пользователя", login);
                                        openPult.putExtra("Должность", user.child("position").getValue().toString());
                                        startActivity(openPult);
                                    } else if (user.child("position").getValue().toString().equals("master")) {
                                        Intent openFactoryCondition = new Intent(getApplicationContext(), QuestMainActivity.class); //actually there should be the FactoryCondition.class, but it is incomplete yet
                                        openFactoryCondition.putExtra("Логин пользователя", login);
                                        openFactoryCondition.putExtra("Должность", user.child("position").getValue().toString());
                                        startActivity(openFactoryCondition);
                                    } else if (user.child("position").getValue().toString().equals("repair")) {
                                        Intent openProblemsList = new Intent(getApplicationContext(), RepairersProblemsList.class);
                                        openProblemsList.putExtra("Логин пользователя", login);
                                        openProblemsList.putExtra("Должность", user.child("position").getValue().toString());
                                        startActivity(openProblemsList);
                                    } else if (user.child("position").getValue().toString().equals("raw") || user.child("position").getValue().toString().equals("quality")) {
                                        Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                        openUrgentProblemsList.putExtra("Логин пользователя", login);
                                        openUrgentProblemsList.putExtra("Должность", user.child("position").getValue().toString());
                                        startActivity(openUrgentProblemsList);
                                    }

                                    if (rememberMe.isChecked()) {
                                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        SharedPreferences.Editor editor = sharedPrefs.edit();
                                        editor.putString("Логин пользователя", login);
                                        editor.putString("Должность", user.child("position").getValue().toString());
                                        if (user.child("pult_no").getValue() != null) {
                                            editor.putString("Номер пульта", user.child("pult_no").getValue().toString());
                                        }
                                        editor.commit();
                                    }

                                    if (!user.child("position").getValue().toString().equals("operator")) {
                                        stopService(new Intent(getBaseContext(), BackgroundService.class)); //если до этого уже сервис для другого аккаунта был включен и произошел повторный логин
                                        Intent startBackgroundService = new Intent(getApplicationContext(), BackgroundService.class);
                                        startBackgroundService.putExtra("Должность", user.child("position").getValue().toString());
//                                    startService(startBackgroundService); //эта функция запускает фоновый сервис
                                        ContextCompat.startForegroundService(getApplicationContext(), startBackgroundService);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Такого пользователя не существует\nВнимательно заполните поля", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    private void initInstances()
    {
        loginView = findViewById(R.id.access_login);
        passwordView = findViewById(R.id.password);
        enter = findViewById(R.id.enter);
        loading = findViewById(R.id.loading);
        getSupportActionBar().hide();
        loading.setVisibility(View.INVISIBLE);
        rememberMe = findViewById(R.id.remember_me);
    }
}

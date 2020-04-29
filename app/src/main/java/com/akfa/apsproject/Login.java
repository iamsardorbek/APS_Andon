package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private TextView loading, tip;
    private ImageView titlePic;
    private EditText passwordView, loginView;
    private Button enter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initInstances();
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = loginView.getText().toString();
                final String password = passwordView.getText().toString();
                loading.setVisibility(View.VISIBLE);
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users/" + login);
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        loading.setVisibility(View.INVISIBLE);
                        if(user.exists())
                        {//3 ifs with boolean checker functions in condition: isOperator, isMaster, isRepairer
                            if(user.child("password").getValue().toString().equals(password)) {
                                if (user.child("position").getValue().toString().equals("operator")) {
                                    Intent openPult = new Intent(getApplicationContext(), MainActivity.class);
                                    openPult.putExtra("Номер пульта", user.child("pultNo").getValue().toString());
                                    startActivity(openPult);
                                } else if (user.child("position").getValue().toString().equals("master")) {
                                    Intent openPult = new Intent(getApplicationContext(), QuestMainActivity.class);
                                    openPult.putExtra("Номер пульта", user.child("pultNo").getValue().toString());
                                    startActivity(openPult);
                                } else if (user.child("position").getValue().toString().equals("repairer")) {
                                    Intent openPult = new Intent(getApplicationContext(), RepairersProblemsList.class);
                                    startActivity(openPult);
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Такого пользователя не существует\nВнимательно заполните поля", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void initInstances()
    {
        loginView = findViewById(R.id.access_login);
        passwordView = findViewById(R.id.password);
        enter = findViewById(R.id.enter);
        loading = findViewById(R.id.loading);
        titlePic = findViewById(R.id.title_pic);
        tip = findViewById(R.id.tip);
    }
}

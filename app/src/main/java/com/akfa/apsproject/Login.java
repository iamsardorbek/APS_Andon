package com.akfa.apsproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

//---------ДАЕТ ПОЛЬЗОВАТЕЛЮ АВТОРИЗОВАТЬСЯ-------//
//---------ЕСЛИ ПРИ ПРЕДЫДУЩЕМ ЛОГИНЕ ЧЕКНУЛ "ЗАПОМНИТЬ МЕНЯ", SPLASHACTIVITY ПРОПУСТИТ ЛОГИН АКТИВИТИ-------//

public class Login extends AppCompatActivity {
    //layout views
    private TextView loading;
    private EditText passwordView, loginView;
    private Button enter;
    //данные, введенные в полях edit text
    private String login, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //возьми разрешение использования камеры
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionStatus == PackageManager.PERMISSION_DENIED) { ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0); }

        initInstances();
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //кнопка ВОЙТИ
                //----ЗДЕСЬ САМОЕ ОСНОВНОЕ ЭТОГО АКТИВИТИ, ПРОВЕРЯЕТ ПРАВИЛЬНОСТЬ ВВОДА, РЕШАЕТ, КАКОЙ АКТИВИТЕ ЗАПУСТИТЬ ДАЛЬШЕ И ТД----//
                login = loginView.getText().toString(); //введенные в edittext логин и пароль
                password = passwordView.getText().toString();
                if(login.isEmpty() || password.isEmpty() || password.charAt(0) == ' ' || login.charAt(0) == ' ') //если пустые поля или начинаются с пробела - ошибка
                {
                    Toast.makeText(getApplicationContext(), getString(R.string.fill_both_fields_correctly), Toast.LENGTH_SHORT).show();
                }
                else {
                    loading.setVisibility(View.VISIBLE); //кликнул, пока БД не считали, стоит надпись ЗАГРУЗКА ДАННЫХ
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + login); //ссылка на node именно этого пользователя
                    userRef.addListenerForSingleValueEvent(userValueEventListener); //главные действия проходят в листенере, описанном ниже
                }
            }
        });
    }

    private void initInstances()
    {//иниц views
        Objects.requireNonNull(getSupportActionBar()).hide();
        loginView = findViewById(R.id.access_login);
        passwordView = findViewById(R.id.password);
        enter = findViewById(R.id.enter);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);
    }

    private ValueEventListener userValueEventListener = new ValueEventListener() {
        @Override public void onDataChange(@NonNull DataSnapshot user) {
            loading.setVisibility(View.INVISIBLE);
            if (user.exists()) // если подветка такая существует
            {//3 ifs with boolean checker functions in condition: isOperator, isMaster, isRepairer
                try {
                    if (password.equals(Objects.requireNonNull(user.child("password").getValue()).toString())) { //если и пароль введен правильно (проверка с подветкой user->password
                        if(!user.child("active_session_android_id").exists()) {
                            @SuppressLint("HardwareIds") String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                            DatabaseReference userRef = user.getRef();
                            userRef.child("active_session_android_id").setValue(androidID);

                            String position = Objects.requireNonNull(user.child("position").getValue()).toString();
                            saveUserData(position);
                            switch (Objects.requireNonNull(user.child("position").getValue()).toString())
                            {
                                case "operator":
                                    Intent openPult = new Intent(getApplicationContext(), PultActivity.class); //открой пульт
                                    startActivity(openPult);
                                    break;
                                case "master":
                                case "repair":
                                case "raw":
                                case "quality":
                                    Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                    startActivity(openUrgentProblemsList);
                                    break;
                                case "head":
                                    Intent openTodayChecks = new Intent(getApplicationContext(), TodayChecks.class);
                                    startActivity(openTodayChecks);
                                    break;
                            }



                            if (!Objects.requireNonNull(user.child("position").getValue()).toString().equals("head")) {
                                //если это любой специалист, кроме ГЛАВНЫХ, включи фоновый сервис слежения за появлением проблем (срочный и ТО), вызовов
                                //при появлении проблемы, будет отправлять уведомление
                                stopService(new Intent(getBaseContext(), BackgroundService.class)); //если до этого уже сервис для другого аккаунта был включен и произошел повторный логин
                                Intent startBackgroundService = new Intent(getApplicationContext(), BackgroundService.class);
                                ContextCompat.startForegroundService(getApplicationContext(), startBackgroundService);//эта функция запускает фоновый сервис
                                startService(new Intent(getApplicationContext(), AppLifecycleTrackerService.class));
                                finish();
                            }
                        }
                        else
                        { Toast.makeText(getApplicationContext(), R.string.session_active_on_another_phone, Toast.LENGTH_LONG).show(); }
                    }
                    else { Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_LONG).show(); }
                }
                catch (NullPointerException npe)
                {
                    ExceptionProcessing.processException(npe, getResources().getString(R.string.database_npe_toast), getApplicationContext(), Login.this);
                }
            }
            else { Toast.makeText(getApplicationContext(), R.string.user_doesnt_exist, Toast.LENGTH_LONG).show(); }
        }

        @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    @SuppressWarnings("deprecation")
    @SuppressLint("ApplySharedPref")
    private void saveUserData(String position) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("Логин пользователя", login);
        editor.putString("Должность", position);
        editor.commit(); //запиши данные в sharedPref
        UserData.login = login;
        UserData.position = position;
    }
}

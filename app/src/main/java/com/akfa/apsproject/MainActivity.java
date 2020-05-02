package com.akfa.apsproject;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    // all variables
    ActionBar actionBar;
    private Button[] andons = new Button[4];
    private int nomerPulta = 0;
    public Integer[] btn_condition = new Integer[4];
    private String login;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pultRef;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Bundle arguments = getIntent().getExtras();
      find_objects(); //инициализация всех layout элементов
      andons[0].setVisibility(View.INVISIBLE);
      andons[1].setVisibility(View.INVISIBLE);
      andons[2].setVisibility(View.INVISIBLE);
      andons[3].setVisibility(View.INVISIBLE);
      if(arguments != null) //был ли сделан правилно логин и возвратил ли он оттуда номер пульта
          {
            nomerPulta = Integer.parseInt(arguments.getString("Номер пульта"));
            pultRef = database.getReference("Pults/" + nomerPulta);
            //инициализируем listener базы данных, чтобы считать данные оттуда
              //пока данные не пришли с базы, в pultInfo будет показываться "Загрузка данных"
            pultRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot messages) {
                    setTitle("Пульт " + nomerPulta);
                    for (DataSnapshot oneMessage : messages.getChildren()){
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String key ="";
                        key = oneMessage.getKey();
                        long val;
                        val = (long) oneMessage.getValue();
                        int indexOfChild = 0;
                        switch(key) //switch from keys to indices
                        {
                            case "repair":
                                indexOfChild = 0;
                                break;
                            case "quality":
                                indexOfChild = 1;
                                break;
                            case "raw":
                                indexOfChild = 2;
                                break;
                            case "master":
                                indexOfChild = 3;
                                break;
                        }
                        switch(indexOfChild) //set up the button background behaviour
                        {// behaviour depends on the button index, its condition value number
                            //that's why there are two switch statements
                            case 0:
                                switch((int) val)
                                {
                                    case 0:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.remont_button);
                                        break;
                                    case 1:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.remont_button_animation);
                                        AnimationDrawable problemAlert = (AnimationDrawable) andons[indexOfChild].getBackground();
                                        problemAlert.start();
                                        break;
                                    case 2:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.remont_button_alert);
                                        break;
                                }
                                break;
                            case 1:
                                switch((int) val)
                                {
                                    case 0:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.otk_button);
                                        break;
                                    case 1:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.otk_button_animation);
                                        AnimationDrawable problemAlert = (AnimationDrawable) andons[indexOfChild].getBackground();
                                        problemAlert.start();
                                        break;
                                    case 2:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.otk_button_alert);
                                        break;
                                }
                                break;
                            case 2:
                                switch((int) val)
                                {
                                    case 0:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.materials_button);
                                        break;
                                    case 1:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.materials_button_animation);
                                        AnimationDrawable problemAlert = (AnimationDrawable) andons[indexOfChild].getBackground();
                                        problemAlert.start();
                                        break;
                                    case 2:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.materials_button_alert);
                                        break;
                                }
                                break;
                            case 3:
                                switch((int) val)
                                {
                                    case 0:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.master_button);
                                        break;
                                    case 1:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.master_button_animation);
                                        AnimationDrawable problemAlert = (AnimationDrawable) andons[indexOfChild].getBackground();
                                        problemAlert.start();
                                        break;
                                    case 2:
                                        andons[indexOfChild].setBackgroundResource(R.drawable.master_button_alert);
                                        break;
                                }
                                break;
                        }
                        btn_condition[indexOfChild] = (int) val;
                        Log.d("FB Listener",  key + " " + val);
                    }
                    //сделать элементы видимыми, когда уже считали все данные с БД
                    andons[0].setVisibility(View.VISIBLE);
                    andons[1].setVisibility(View.VISIBLE);
                    andons[2].setVisibility(View.VISIBLE);
                    andons[3].setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.e("БАЗА ДАННЫХ", "Ошибка работы с базой данных", error.toException());
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Ошибка, постарайтесь зайти снова", Toast.LENGTH_LONG).show();
        }
    }





    // find all variables
    protected void find_objects(){ //инициализация всех объектов layout
//        punktInfo = findViewById(R.id.punkt_info); initialize statusBar instead
        login = getIntent().getExtras().getString("Логин пользователя");
        andons[0] = findViewById(R.id.repair_btn);
        andons[1] = findViewById(R.id.quality_btn);
        andons[2] = findViewById(R.id.raw_btn);
        andons[3] = findViewById(R.id.master_btn);

        // set on click listener for variables
        for(Button andon : andons)
        {
            andon.setOnTouchListener(this);
        }

        actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Загрузка данных...");
        //код связанный с nav bar
        drawerLayout = findViewById(R.id.activity_main);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.pult:
                        drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на сам пульт, нав бар просто закрывается
                        break;
                    case R.id.check_equipment: //переход в модуль проверки
                        Intent openQuest = new Intent(getApplicationContext(), QuestMainActivity.class);
                        openQuest.putExtra("login", login);
                        startActivity(openQuest);
                        break;
                    case R.id.about: //инфа про приложение и компанию и иинструкции может
//                        Intent openAbout = new Intent(getApplicationContext(), About.class);
//                        startActivity(openAbout);
                        Toast.makeText(MainActivity.this, "Приложение создано Akfa R&D в 2020 году в Ташкенте.",Toast.LENGTH_SHORT).show();break;
                    case R.id.log_out: //возвращение в логин page
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
    }

    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                switch(button.getId())
                {
                    case R.id.repair_btn:
                    case R.id.raw_btn:
                        button.setBackgroundResource(R.drawable.pressed_even_button);
                        break;

                    case R.id.quality_btn:
                    case R.id.master_btn:
                        button.setBackgroundResource(R.drawable.pressed_odd_button);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch(button.getId()) {
                    //4 случая ниже - для обработки больших кнопок: ремонт, отк, сырье, мастер
                    case R.id.repair_btn:
                        btn_condition[0]++;
                        updateButton(0);
                        break;

                    case R.id.quality_btn:
                        btn_condition[1]++;
                        updateButton(1);
                        break;

                    case R.id.raw_btn:
                        btn_condition[2]++;
                        updateButton(2);
                        break;

                    case R.id.master_btn:
                        btn_condition[3]++;
                        updateButton(3);
                        break;
                }
                break;
        }
        return true;
    }

    //запиши в базу данныъ новое состояние одной конкретной кнопки
    private void updateButton(int signalTypeIndex)
    {
        // check status and drop for 0 if more than 2
        if ((btn_condition[signalTypeIndex] >= 3)){ //всего 3 состояния, если больше 2, переведи в 0
                btn_condition[signalTypeIndex]=0;
        }
        // Write a message to the database
        switch (signalTypeIndex){
            case 0:
                pultRef.child("repair").setValue(btn_condition[0]);
                break;
            case 1:
                pultRef.child("quality").setValue(btn_condition[1]);
                break;
            case 2:
                pultRef.child("raw").setValue(btn_condition[2]);
                break;
            case 3:
                pultRef.child("master").setValue(btn_condition[3]);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}

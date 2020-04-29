package com.akfa.apsproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final int GET_NOMER_PUNKTA = 5;
    // all variables
    private Button repair_btn, quality_btn, raw_btn, master_btn, openFactoryConditionActivity, settings;
    private TextView punktInfo;
    private Button[] andons = new Button[4];
    private String nomerPunkta;
    public Integer[] btn_condition = new Integer[4];
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pultRef;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Bundle arguments = getIntent().getExtras();
      find_objects(); //инициализация всех layout элементов
      if(arguments != null) //был ли сделан правилно логин и возвратил ли он оттуда номер пульта
          {
            nomerPunkta = arguments.getString("Номер пункта");
            pultRef = database.getReference(nomerPunkta);
            punktInfo.setText("Загрузка\nданных...");
            //инициализируем listener базы данных, чтобы считать данные оттуда
              //пока данные не пришли с базы, в pultInfo будет показываться "Загрузка данных"
            pultRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot messages) {
                    for (DataSnapshot oneMessage : messages.getChildren()){
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String key = oneMessage.getKey();
                        long val = (long) oneMessage.getValue();
                        int indexOfChild = 0;
                        switch(key) //switch from keys to indices
                        {
                            case "Ремонт":
                                indexOfChild = 0;
                                break;
                            case "ОТК":
                                indexOfChild = 1;
                                break;
                            case "Сырье":
                                indexOfChild = 2;
                                break;
                            case "Мастер":
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
                    punktInfo.setText(nomerPunkta);
                    master_btn.setVisibility(View.VISIBLE);
                    raw_btn.setVisibility(View.VISIBLE);
                    quality_btn.setVisibility(View.VISIBLE);
                    repair_btn.setVisibility(View.VISIBLE);
                    andons[0].setVisibility(View.VISIBLE);
                    andons[1].setVisibility(View.VISIBLE);
                    andons[2].setVisibility(View.VISIBLE);
                    andons[3].setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("FB read", "Failed to read value.", error.toException());
                }
            });
        }
        else
        {
            //если логин не успешен, спрятаем кнопки и выведем в текст вью что пульт не выбран
            master_btn.setVisibility(View.INVISIBLE);
            raw_btn.setVisibility(View.INVISIBLE);
            quality_btn.setVisibility(View.INVISIBLE);
            repair_btn.setVisibility(View.INVISIBLE);
            andons[0].setVisibility(View.INVISIBLE);
            andons[1].setVisibility(View.INVISIBLE);
            andons[2].setVisibility(View.INVISIBLE);
            andons[3].setVisibility(View.INVISIBLE);
            punktInfo.setText("Не выбран\nпульт");
        }
    }





    // find all variables
    protected void find_objects(){ //инициализация всех объектов layout
        repair_btn=findViewById(R.id.repair_btn);
        quality_btn=findViewById(R.id.quality_btn);
        raw_btn=findViewById(R.id.raw_btn);
        master_btn=findViewById(R.id.master_btn);
        openFactoryConditionActivity = findViewById(R.id.open_factory_condition);
        punktInfo = findViewById(R.id.punkt_info);
        settings = findViewById(R.id.settings);
        andons[0] = findViewById(R.id.repair_btn);
        andons[1] = findViewById(R.id.quality_btn);
        andons[2] = findViewById(R.id.raw_btn);
        andons[3] = findViewById(R.id.master_btn);

        // set on click listener for variables
        repair_btn.setOnTouchListener(this);
        quality_btn.setOnTouchListener(this);
        raw_btn.setOnTouchListener(this);
        master_btn.setOnTouchListener(this);
        settings.setOnTouchListener(this);
        openFactoryConditionActivity.setOnTouchListener(this);
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
                    case R.id.open_factory_condition: //переход в веб интерфейс
                        button.setBackgroundResource(R.drawable.monitoring_pressed_button);
                        break;
                    case R.id.settings:
                        button.setBackgroundResource(R.drawable.login_pressed_button);
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
                    case R.id.open_factory_condition: //переход в веб интерфейс
                        startActivity(new Intent(getApplicationContext(), FactoryCondition.class));
                        break;
                    case R.id.settings: //перевох в Login activity для выбора пульта
                        startActivityForResult(new Intent(getApplicationContext(), Login.class), GET_NOMER_PUNKTA);
                        master_btn.setVisibility(View.INVISIBLE);
                        raw_btn.setVisibility(View.INVISIBLE);
                        quality_btn.setVisibility(View.INVISIBLE);
                        repair_btn.setVisibility(View.INVISIBLE);
                        andons[0].setVisibility(View.INVISIBLE);
                        andons[1].setVisibility(View.INVISIBLE);
                        andons[2].setVisibility(View.INVISIBLE);
                        andons[3].setVisibility(View.INVISIBLE);
                        break;
                }
                break;
        }
        return true;
    }

/*    @Override
    public void onClick(View view) {
 //обработка кликов
        switch(view.getId()){
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
            case R.id.open_factory_condition: //переход в веб интерфейс
                startActivity(new Intent(getApplicationContext(), FactoryCondition.class));
                break;
            case R.id.settings: //перевох в Login activity для выбора пульта
                startActivityForResult(new Intent(getApplicationContext(), Login.class), GET_NOMER_PUNKTA);
                master_btn.setVisibility(View.INVISIBLE);
                raw_btn.setVisibility(View.INVISIBLE);
                quality_btn.setVisibility(View.INVISIBLE);
                repair_btn.setVisibility(View.INVISIBLE);
                andons[0].setVisibility(View.INVISIBLE);
                andons[1].setVisibility(View.INVISIBLE);
                andons[2].setVisibility(View.INVISIBLE);
                andons[3].setVisibility(View.INVISIBLE);
                break;
        }
    }*/

    @Override //обработка результата Login activity -> какой пункт выбрали
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK)
        {
            Bundle args;
            if (intent != null) {
                args = intent.getExtras();

                nomerPunkta = args.getString("Номер пункта");
                //DatabaseReference
            }
        }
    }

    //запиши в базу данныъ новое состояние 1-й конкретной кнопки
    private void updateButton(int signalTypeIndex)
    {
        // check status and drop for 0 if more than 2
        if ((btn_condition[signalTypeIndex] >= 3)){ //всего 3 состояния, если больше 2, переведи в 0
                btn_condition[signalTypeIndex]=0;
        }
        // Write a message to the database
        switch (signalTypeIndex){
            case 0:
                pultRef.child("Ремонт").setValue(btn_condition[0]);
                break;
            case 1:
                pultRef.child("ОТК").setValue(btn_condition[1]);
                break;
            case 2:
                pultRef.child("Сырье").setValue(btn_condition[2]);
                break;
            case 3:
                pultRef.child("Мастер").setValue(btn_condition[3]);
                break;
        }

    }
}

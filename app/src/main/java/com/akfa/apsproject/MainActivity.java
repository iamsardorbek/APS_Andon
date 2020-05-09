package com.akfa.apsproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.fragment.app.DialogFragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//----------------PULT----------------//
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, ChooseProblematicStationDialog.ChooseProblematicStationDialogListener, QRCodeDialog.QRCodeDialogListener { //здесь пульты
    // all variables
    int numOfButtons = 4;
    private Button[] andons = new Button[numOfButtons];
    public Integer[] btn_condition = new Integer[numOfButtons];
    private boolean[] btnBlocked = new boolean[numOfButtons];
    private String[] positionTypes = {"repair", "quality", "raw", "master"};
    private int nomerPulta = 0;
    private String login, position; //inter-activity strings
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pultRef;
    private ActionBarDrawerToggle toggle;
    ValueEventListener pultRefListener;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Bundle arguments = getIntent().getExtras();
      find_objects(); //инициализация всех layout элементов
      toggle = setUpNavBar(); //setUpNavBar выполняет все действия и возвращает toggle, которые используется в функции onOptionsItemSelected()
      setAndonsVisibility(false);
      if(arguments != null) //был ли сделан правилно логин и возвратил ли он оттуда номер пульта
      {
            position = arguments.getString("Должность");
            login = arguments.getString("Логин пользователя");
            DatabaseReference userRef = database.getReference("Users/" + login);
            setAndonStates();
            initPultRefListener();
            userRef.addValueEventListener(pultRefListener);
      }
      else {
            Toast.makeText(getApplicationContext(), "Ошибка, постарайтесь зайти снова", Toast.LENGTH_LONG).show();
      }
    }

    private void initPultRefListener() {
        pultRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnap) {
                nomerPulta = Integer.parseInt(userSnap.child("pultNo").getValue().toString());
                pultRef = database.getReference("Pults/" + nomerPulta);
                //инициализируем listener базы данных, чтобы считать данные оттуда
                //пока данные не пришли с базы, в pultInfo будет показываться "Загрузка данных"
                pultRef.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot messages) {
                                setTitle("Пульт " + nomerPulta);
                                for (DataSnapshot oneMessage : messages.getChildren()){
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    String key = "";
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
                                    if(val == 1) {
                                        if (indexOfChild % 2 == 0) {
                                            andons[indexOfChild].setCompoundDrawablesWithIntrinsicBounds(R.drawable.qrcode_drawable, 0, 0, 0);
                                        } else {
                                            andons[indexOfChild].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.qrcode_drawable, 0);
                                        }
                                    }
                                    else
                                    {
                                        andons[indexOfChild].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

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
                                setAndonsVisibility(true);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Failed to read value
                                Log.e("БАЗА ДАННЫХ", "Ошибка работы с базой данных", error.toException());
                            }
                        });
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
    }

    private void setAndonStates()
    {
        DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference("Urgent_problems");
        urgentProblemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot urgentProblemsSnap) {
                for(DataSnapshot urgentProblemSnap : urgentProblemsSnap.getChildren())
                {
                    UrgentProblem urgentProblem = urgentProblemSnap.getValue(UrgentProblem.class);
                    String operatorLogin = urgentProblem.getOperatorLogin();
                    if(operatorLogin.equals(login))
                    {
                        String whoIsNeededLogin = urgentProblem.getWhoIsNeededLogin();
                        int whoIsNeededIndex = 0;
                        switch(whoIsNeededLogin) //switch from keys to indices
                        {
                            case "repair":
                                whoIsNeededIndex = 0;
                                break;
                            case "quality":
                                whoIsNeededIndex = 1;
                                break;
                            case "raw":
                                whoIsNeededIndex = 2;
                                break;
                            case "master":
                                whoIsNeededIndex = 3;
                                break;
                        }
                        qrRandomCode[whoIsNeededIndex] = urgentProblem.getQrRandomCode();
                        btnBlocked[whoIsNeededIndex] = true;
                        btn_condition[whoIsNeededIndex] = 1;
                        DatabaseReference thisUrgentProblem =  FirebaseDatabase.getInstance().getReference("Urgent_problems/" + urgentProblemSnap.getKey());
                        final int finalWhoIsNeededIndex = whoIsNeededIndex;
                        thisUrgentProblem.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot thisUrgentProblemSnap) {
                                if(!thisUrgentProblemSnap.exists())
                                {
                                    btnBlocked[finalWhoIsNeededIndex] = false;
                                    btn_condition[finalWhoIsNeededIndex]++;
                                    updateButton(finalWhoIsNeededIndex);
                                    andons[finalWhoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        if (whoIsNeededIndex % 2 == 0) {
                            andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(R.drawable.qrcode_drawable, 0, 0, 0);
                        } else {
                            andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.qrcode_drawable, 0);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setAndonsVisibility(boolean visible) {
      int visibilityState;
      if(visible)
          visibilityState = View.VISIBLE;
      else
          visibilityState = View.INVISIBLE;
      for(Button andon : andons)
      {
          andon.setVisibility(visibilityState);
      }
    }

    // find all variables

    protected void find_objects(){ //инициализация всех объектов layout
        andons[0] = findViewById(R.id.repair_btn);
        andons[1] = findViewById(R.id.quality_btn);
        andons[2] = findViewById(R.id.raw_btn);
        andons[3] = findViewById(R.id.master_btn);

        // set on click listener for variables
        for(Button andon : andons)
            andon.setOnTouchListener(this);
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Загрузка данных...");
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.activity_main);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        //ниже действия, выполняемые при нажатиях на элементы нав бара
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
                        openQuest.putExtra("Логин пользователя", login);
                        openQuest.putExtra("Должность", position);
                        startActivity(openQuest);
                        break;
                    case R.id.about: //инфа про приложение и компанию и иинструкции может
//                        Intent openAbout = new Intent(getApplicationContext(), About.class);
//                        startActivity(openAbout);
                        Toast.makeText(getApplicationContext(), "Приложение создано Akfa R&D в 2020 году в Ташкенте.",Toast.LENGTH_SHORT).show();break;
                    case R.id.log_out: //возвращение в логин page
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.clear();
                        editor.commit();
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
        return toggle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item)) return true;
        else return super.onOptionsItemSelected(item);
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
                        processCallForSpecialist(0);
                        updateButton(0);
                        break;

                    case R.id.quality_btn:
                        btn_condition[1]++;
                        processCallForSpecialist(1);
                        updateButton(1);
                        break;

                    case R.id.raw_btn:
                        btn_condition[2]++;
                        processCallForSpecialist(2);
                        updateButton(2);
                        break;

                    case R.id.master_btn:
                        btn_condition[3]++;
                        processCallForSpecialist(3);
                        updateButton(3);
                        break;
                }
                break;
        }
        return true;
    }
    //запиши в базу данныъ новое состояние одной конкретной кнопки


    private void updateButton(int signalTypeIndex)
    { //signalTypeIndex - тип сигнала (Ремонт, мастер, отк, сырье)
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

    private void processCallForSpecialist(int signalTypeIndex)
    {
        if(btn_condition[signalTypeIndex] == 1)
        {
            //startDialogFragment
            DialogFragment dialogFragment = new ChooseProblematicStationDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Логин пользователя", login);
            bundle.putInt("Вызвать специалиста", signalTypeIndex);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "Выбор участка");
        }
        else if(btnBlocked[signalTypeIndex])
        {
            //если кнопка заблокирована(спец еще не пришел), высветить QR Code
            btn_condition[signalTypeIndex]--; //вернуть в состояние "специалист не пришел, ПРОБЛЕМА"
            //Открыть диалог с QR Кодом
            DialogFragment dialogFragment = new QRCodeDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Код", qrRandomCode[signalTypeIndex]);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "QR Код");
        }
    }

    String[] qrRandomCode = new String[numOfButtons];
    @Override
    public void submitStationNo(int stationNo, String equipmentLineName, String shopName, String operatorLogin, final int whoIsNeededIndex) {
        //вбить экстренную проблему в базу, QR генерируется внутри самого диалога
        DatabaseReference dbRef = database.getReference();
        DatabaseReference thisUrgentProblem = dbRef.child("Urgent_problems").push();
        qrRandomCode[whoIsNeededIndex] = GenerateRandomString.randomString(3);
        thisUrgentProblem.setValue(new UrgentProblem(stationNo, equipmentLineName, shopName, operatorLogin, positionTypes[whoIsNeededIndex], qrRandomCode[whoIsNeededIndex]));
        //задать состояние кнопки блокированным
        btnBlocked[whoIsNeededIndex] = true;
        thisUrgentProblem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot thisUrgentProblemSnap) {
                if(!thisUrgentProblemSnap.exists())
                {
                    btnBlocked[whoIsNeededIndex] = false;
                    andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    btn_condition[whoIsNeededIndex]++;
                    updateButton(whoIsNeededIndex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(whoIsNeededIndex % 2 == 0)
        {
            andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(R.drawable.qrcode_drawable, 0, 0, 0);
        }
        else
        {
            andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.qrcode_drawable, 0);
        }
    }

    @Override
    public void onDialogCanceled(int whoIsNeededIndex) {
        btn_condition[whoIsNeededIndex]--;
        updateButton(whoIsNeededIndex);
//        andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public void onQRCodeDialogCanceled(int whoIsNeededIndex) {
        btn_condition[whoIsNeededIndex]--;
        updateButton(whoIsNeededIndex);
        btn_condition[whoIsNeededIndex]++;
        updateButton(whoIsNeededIndex);
    }
}

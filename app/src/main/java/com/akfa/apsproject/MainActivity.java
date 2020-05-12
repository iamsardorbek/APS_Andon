package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.Date;

//----------------PULT----------------//
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, ChooseProblematicStationDialog.ChooseProblematicStationDialogListener, QRCodeDialog.QRCodeDialogListener { //здесь пульты
    // all variables
    int numOfButtons = 4;
    private Button[] andons = new Button[numOfButtons];
    public Integer[] btnCondition = new Integer[numOfButtons];
    private boolean[] btnBlocked = new boolean[numOfButtons];
    private String[] positionTypes = {"repair", "quality", "raw", "master"};
    private String nomerPulta;
    private String login, position; //inter-activity strings
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pultRef;
    private ActionBarDrawerToggle toggle;
    ValueEventListener pultRefListener;
    private final String DETECTED = "DETECTED", SPECIALIST_CAME = "SPECIALIST_CAME", SOLVED = "SOLVED";
    String[] qrRandomCode = new String[numOfButtons]; //для сохранения актуальных QR кодов
    private int [][] andonDrawableReferences = {{R.drawable.remont_button, R.drawable.remont_button_animation, R.drawable.remont_button_alert},
            {R.drawable.otk_button, R.drawable.otk_button_animation, R.drawable.otk_button_alert},
            {R.drawable.materials_button, R.drawable.materials_button_animation, R.drawable.materials_button_alert},
            {R.drawable.master_button, R.drawable.master_button_animation, R.drawable.master_button_alert}};

  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Bundle arguments = getIntent().getExtras(); //аргументы переданные с других активити
      initInstances(); //инициализация всех layout элементов
      toggle = setUpNavBar(); //setUpNavBar выполняет все действия и возвращает toggle, которые используется в функции onOptionsItemSelected()
      setAndonsVisibility(false); //спрятать все кнопки-андоны
      if(arguments != null) //был ли сделан правилно логин и возвратил ли он оттуда номер пульта, или передал ли предыдущий активити аргументы
      {
            position = arguments.getString("Должность");
            login = arguments.getString("Логин пользователя");
            //создать ссылку на ветку пользователя для получения номера пульта
            DatabaseReference userRef = database.getReference("Users/" + login);
            initPultRefListener(); //инициализировать valuelistener для пульта этого юзера
            userRef.addValueEventListener(pultRefListener);
            setAndonStates();
      }
      else Toast.makeText(getApplicationContext(), "Ошибка, постарайтесь зайти снова", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initInstances(){
        //инициализация всех объектов layout и их listeners
        andons[0] = findViewById(R.id.repair_btn);
        andons[1] = findViewById(R.id.quality_btn);
        andons[2] = findViewById(R.id.raw_btn);
        andons[3] = findViewById(R.id.master_btn);

        // set on click listener for variables
        for(Button andon : andons)
            andon.setOnTouchListener(this);
    }

    //нужна ли эта функция вообще?
    private void initPultRefListener() { //инициализирует листенер состояний кнопок в БД в ветки "Pults"
        pultRefListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot userSnap) {
                nomerPulta = userSnap.child("pultNo").getValue().toString(); //считать номер пульта юзера, концепт номера пульта надо менять
                //инициализируем listener базы данных именно этого пульта, чтобы считать оттуда данные
                //пока данные не пришли с базы, в pultInfo будет показываться "Загрузка данных"
                pultRef = database.getReference("Pults/" + nomerPulta); //ссылка именно к этому пульту
                pultRef.addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot pultButtonStates) {
                        setTitle("Пульт " + nomerPulta); //app bar текст задаем. напр.: "Пульт 1"
                        //цикл ниже пройдется по каждой кнопке этого пульта
                        for (DataSnapshot buttonStateSnap : pultButtonStates.getChildren()){
                            //ветка отдельной кнопки (мастер, ремонт, отк, сырье) именно этого пульта
                            String whoIsNeededPosition = buttonStateSnap.getKey();
                            int whoIsNeededIndex = renderWhoIsNeededIndex(whoIsNeededPosition);
                            int buttonState = Integer.parseInt(buttonStateSnap.getValue().toString());

                            setAndonBackground(whoIsNeededIndex, buttonState); //синхронизовать внешнее состояние данной кнопки с состоянием в БД
                        }
                        setAndonsVisibility(true); //когда уже считали все данные с БД, сделать элементы видимыми

                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
    }
//can you merge these two function listeners?
    private void setAndonStates()
    {
        //функция срабатывает при запуске активити единожды, ОДНАКО имеется внутри асинхронный листенер
        //инициализируй состояния кнопок в зависимости от ветки Urgent_problems
        DatabaseReference urgentProblemsRef = database.getReference("Urgent_problems"); //ссылка на все срочные проблемы
        urgentProblemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot urgentProblemsSnap) {
                for(DataSnapshot urgentProblemSnap : urgentProblemsSnap.getChildren())
                { //рассмотрим по одному каждую проблему (aka Query)
                    UrgentProblem urgentProblem = urgentProblemSnap.getValue(UrgentProblem.class); //считать ветку в объект срочная проблема
                    String operatorLogin = urgentProblem.getOperator_login();
                    if(operatorLogin.equals(login) && urgentProblemSnap.child("status").getValue().toString().equals(DETECTED))
                    { //если проблема относится к данному пользователю, но специалист еще не пришел (состояние 1), но оператор сообщил  о проблеме уже до этого
                        String whoIsNeededLogin = urgentProblem.getWho_is_needed_login();
                        int whoIsNeededIndex = renderWhoIsNeededIndex(whoIsNeededLogin); //какая кнопка было нажата (кого вызвали?)

                        qrRandomCode[whoIsNeededIndex] = urgentProblem.getQr_random_code(); //считай с БД qr code этой срочной проблемы для reality check (мастер на самом деле подходит на линию и ознакамливается?)
                        btnBlocked[whoIsNeededIndex] = true; //заблокируй кнопку
                        btnCondition[whoIsNeededIndex] = 1; //смени состояние кнопки в мигающее
                        updateButton(whoIsNeededIndex);
                        //если update button значок QR сам соотвественно появится, потому что вызовется pultRef Listener
//                        setQRIconOnAndonButton(1, whoIsNeededIndex); //поставь значок QR в край кнопки (значит кнопка в мигающем состоянии)

                        //---- ВНИМАНИЕ, НИЖЕ ОБЪЯВЛЯЕТСЯ АСИНХРОННЫЙ СЛУШАТЕЛЬ ИЗМЕНЕНИЙ ИМЕННО НА ВЕТКЕ ЭТОЙ СРОЧНОЙ ПРОБЛЕМЫ, ПРИ ПРИХОДЕ СПЕЦИАЛИСТА, ----//
                        //---- СЛУШАТЕЛЬ (LISTENER) ПЕРЕВОДИТ КНОПКУ В СОСТОЯНИЕ 2, РАБЛОКИРУЕТ ЕЕ И ОБНОВЛЯЕТ БД ----//
                        DatabaseReference thisUrgentProblem =  database.getReference("Urgent_problems/" + urgentProblemSnap.getKey());
                        thisUrgentProblem.child("status").addValueEventListener(getUrgentProblemStatusListener(whoIsNeededIndex));
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private int renderWhoIsNeededIndex(String whoIsNeededLogin)
    {//переход от названий должностей в номера
        switch(whoIsNeededLogin)
        {
            case "repair":
                return 0;
            case "quality":
                return 1;
            case "raw":
                return 2;
            case "master":
                return 3;
        }
        return 0;
    }

    private void setAndonBackground(int andonIndex, int buttonState)
    {//set up the button background behaviour
        // behaviour depends on the button index, its condition value number
        //that's why there are two switch statements
        int drawableReference = andonDrawableReferences[andonIndex][buttonState];
        switch(buttonState)
        {
            case 0:
            case 2:
                andons[andonIndex].setBackgroundResource(drawableReference);
                setQRIconOnAndonButton(buttonState, andonIndex); //в зависимости от состояния кнопки, вставь/убери иконку QR
                break;
            case 1:
                andons[andonIndex].setBackgroundResource(drawableReference);
                AnimationDrawable problemAlert = (AnimationDrawable) andons[andonIndex].getBackground();
                problemAlert.start();
                setQRIconOnAndonButton(buttonState, andonIndex); //в зависимости от состояния кнопки, вставь/убери иконку QR
                break;
        }
        btnCondition[andonIndex] = buttonState;
    }

    private void setQRIconOnAndonButton(int buttonState, int whoIsNeededIndex)
    {       //в зависимости от buttonState состояния кнопки, вставить/убрать код QR
        if(buttonState == 1) {
            //следующие 4 строки определяют, куда поставить значок QR - справа или слева (зависит от четности)
            if (whoIsNeededIndex % 2 == 0) //четный слева
                andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(R.drawable.qrcode_drawable, 0, 0, 0);
            else //нечетный справа
                andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.qrcode_drawable, 0);
        }
        else
        {
            andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void setAndonsVisibility(boolean visible) {
      //измени видимость кнопок
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

    private ValueEventListener getUrgentProblemStatusListener(final int whoIsNeededIndex)
    {
        //---- ВНИМАНИЕ, НИЖЕ ОБЪЯВЛЯЕТСЯ АСИНХРОННЫЙ СЛУШАТЕЛЬ ИЗМЕНЕНИЙ СТАТУСА (РЕШЕНА, СПЕЦ ПРИШЕЛ, ОБНАРУЖЕНА) ЭТОЙ СРОЧНОЙ ПРОБЛЕМЫ, ПРИ ПРИХОДЕ СПЕЦИАЛИСТА, ----//
        //---- СЛУШАТЕЛЬ (LISTENER) ПЕРЕВОДИТ КНОПКУ В СОСТОЯНИЕ 2, РАБЛОКИРУЕТ ЕЕ И ОБНОВЛЯЕТ БД ----//
        ValueEventListener urgentProblemListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot thisUrgentProblemStatusSnap) {
                if(thisUrgentProblemStatusSnap.getValue().toString().equals(SPECIALIST_CAME)) //если специалист пришел
                {
                    btnBlocked[whoIsNeededIndex] = false; //разблокируй кнопку
                    btnCondition[whoIsNeededIndex] = 2; //переведи состояние кнопки в состояние "специалист пришел"
                    updateButton(whoIsNeededIndex); //внеси изменения в БД
                    //убери значок QR с кнопки
                    //автоматом уберется в pultRef Listener
                    //setQRIconOnAndonButton(2, whoIsNeededIndex);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        return urgentProblemListener;
        //----КОНЕЦ ОБЪЯВЛЕНИЯ АСИНХРОННОГО СЛУШАТЕЛЯ БД----//
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
                        finish();
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

    @Override public boolean onOptionsItemSelected(MenuItem item) {
      //связано с навигейшн бар
        if(toggle.onOptionsItemSelected(item)) return true;
        else return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View button, MotionEvent event) {
      //обработка касаний - зажато (DOWN для красоты) , отпущено (UP)
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
                        btnCondition[0]++;
                        updateButton(0);
                        processCallForSpecialist(0);
                        break;

                    case R.id.quality_btn:
                        btnCondition[1]++;
                        updateButton(1);
                        processCallForSpecialist(1);
                        break;

                    case R.id.raw_btn:
                        btnCondition[2]++;
                        updateButton(2);
                        processCallForSpecialist(2);
                        break;

                    case R.id.master_btn:
                        btnCondition[3]++;
                        updateButton(3);
                        processCallForSpecialist(3);
                        break;
                }
                break;
        }
        return true;
    }

    private void updateButton(int whoIsNeededIndex)
    { //запиши в базу данных новое состояние одной конкретной кнопки
        //whoIsNeededIndex - тип сигнала (Ремонт, мастер, отк, сырье)
        // check status and drop for 0 if more than 2
        if ((btnCondition[whoIsNeededIndex] >= 3)) //всего 3 состояния (нейтральное - порядок, мигает - срочная проблема ждет решения, горит - специалист работает
            //если больше 2, переведи в 0
            btnCondition[whoIsNeededIndex] = 0;

        //занеси новое состояние в базу данных
        pultRef = database.getReference("Pults/" + nomerPulta); //ссылка именно к этому пульту
        pultRef.child(positionTypes[whoIsNeededIndex]).setValue(btnCondition[whoIsNeededIndex]);
    }

    private void processCallForSpecialist(final int whoIsNeededIndex)
    {//обработай нажатие на кнопку, проверяя заблокирована ли она, и реши, какой диалог показать или что дальше
        Log.i("TAQ", "processCallForSpecialist: btnCondition[whoIsNeededIndex] = " + btnCondition[whoIsNeededIndex] + "\tbtnBlocked[whoIsNeededIndex] = " + btnBlocked[whoIsNeededIndex]
                + "\twhoIsNeededIndex = " + whoIsNeededIndex);
        if(btnCondition[whoIsNeededIndex] == 1 /*&& !btnBlocked[whoIsNeededIndex]*/)
        {//хочет вызвать специалиста
            //startDialogFragment для выбора проблемного участка и вызова специалиста
            DialogFragment dialogFragment = new ChooseProblematicStationDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Логин пользователя", login);
            bundle.putInt("Вызвать специалиста", whoIsNeededIndex);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "Выбор участка");
        }
        else if(btnBlocked[whoIsNeededIndex] && btnCondition[whoIsNeededIndex] == 2)
        {
            //если кнопка заблокирована(спец еще не пришел), высветить QR Code
            btnCondition[whoIsNeededIndex]--; //вернуть в состояние "специалист не пришел, ПРОБЛЕМА"
            updateButton(whoIsNeededIndex);
            //Открыть диалог с QR Кодом
            DialogFragment dialogFragment = new QRCodeDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Код", qrRandomCode[whoIsNeededIndex]); //
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "QR Код");
        }
        else if(btnCondition[whoIsNeededIndex] == 0 && !btnBlocked[whoIsNeededIndex])
        {
            //хочет нажать для индикации того, что проблема решена (Из горящего состояния перевести в нейтральное)

            //---получить данные о дате и времени---//
            final String dateSolved;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            dateSolved = sdf.format(new Date());
            final String timeSolved;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
            timeSolved = sdf1.format(new Date());
            //---конец данные о дате и времени---//

            final DatabaseReference urgentProblemsRef = database.getReference("Urgent_problems");
            urgentProblemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot urgentProblemsSnap) {
                    for(DataSnapshot urgentProblemSnap : urgentProblemsSnap.getChildren())
                    {
                        String operatorLogin = urgentProblemSnap.child("operator_login").getValue().toString();
                        String whoIsNeededLogin = urgentProblemSnap.child("who_is_needed_login").getValue().toString();
                        //проверка (Query) выбор срочной проблемы с тем же логином оператора, специалиста и проблема, которая не решена еще
                        if(operatorLogin.equals(login) && whoIsNeededLogin.equals(positionTypes[whoIsNeededIndex]) && urgentProblemSnap.child("status").getValue().toString().equals(SPECIALIST_CAME)) {
                            //если специалист пришел, но еще не решил проблему (status = SPECIALIST_CAME
                            String thisProbKey = urgentProblemSnap.getKey();
                            urgentProblemsRef.child(thisProbKey).child("date_solved").setValue(dateSolved);
                            urgentProblemsRef.child(thisProbKey).child("time_solved").setValue(timeSolved);
                            urgentProblemsRef.child(thisProbKey).child("status").setValue(SOLVED);
                            return;
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    @Override
    public void submitStationNo(int stationNo, String equipmentLineName, String shopName, String operatorLogin, final int whoIsNeededIndex) {
      //состояние btnCondition[whoIsNeededIndex] уже задано
        //интерфейс функция которая вызывается при успешном сообщении о существовании проблемы
        //вбить экстренную проблему в базу, QR генерируется внутри самого диалога
        DatabaseReference dbRef = database.getReference();
        DatabaseReference thisUrgentProblem = dbRef.child("Urgent_problems").push();
        qrRandomCode[whoIsNeededIndex] = GenerateRandomString.randomString(3);
        // ----получи дату и время в строки dateSolved, timeSolved----//
        final String dateDetected;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        dateDetected = sdf.format(new Date());
        final String timeDetected;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
        timeDetected = sdf1.format(new Date());
        //----считал дату и время----//
        //вбить обнаруженную срочную проблему в базу
        thisUrgentProblem.setValue(new UrgentProblem(stationNo, equipmentLineName, shopName, operatorLogin, positionTypes[whoIsNeededIndex], qrRandomCode[whoIsNeededIndex],
                dateDetected, timeDetected, DETECTED));
        btnBlocked[whoIsNeededIndex] = true; //задать состояние кнопки блокированным

        //следующие 1 строка определяют, куда поставить значок QR - справа или слева (зависит от четности)
//        setQRIconOnAndonButton(1, whoIsNeededIndex);
        //здесь же добавить БД слушатель, чтобы реагировал позднее на изменения в БД (specialist_came, solved)
        thisUrgentProblem.child("status").addValueEventListener(getUrgentProblemStatusListener(whoIsNeededIndex));
    }

    @Override
    public void onDialogCanceled(int whoIsNeededIndex) {
      //если диалог выбора проблемного участка отменили/закрыли
        //возврати мигающую кнопку в нейтральное состояние
        btnCondition[whoIsNeededIndex]--;
        updateButton(whoIsNeededIndex);
//        andons[whoIsNeededIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public void onQRCodeDialogCanceled(int whoIsNeededIndex) {
        //чтобы кнопка не  зависла в состоянии ACTION_DOWN (серый фон при нажатии), переключим ее состояние на один вперед и обратно, чтобы
        //pultRefListener отреагировал и вернул кнопку в нормализованное незажатое состояние
        int thisAndonCondition = btnCondition[whoIsNeededIndex];
        setAndonBackground(whoIsNeededIndex, thisAndonCondition);
        updateButton(whoIsNeededIndex);
//        if(tmp == 0 || tmp == 1)
//            btnCondition[whoIsNeededIndex] = 2;
//        else
//            btnCondition[whoIsNeededIndex] = 0;
//        updateButton(whoIsNeededIndex);
//        btnCondition[whoIsNeededIndex] = tmp;
//        updateButton(whoIsNeededIndex);
    }
}

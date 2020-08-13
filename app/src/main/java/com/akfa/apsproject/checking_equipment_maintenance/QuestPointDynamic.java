package com.akfa.apsproject.checking_equipment_maintenance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.monitoring_activities.FactoryCondition;
import com.akfa.apsproject.Login;
import com.akfa.apsproject.monitoring_activities.MaintenanceCheck;
import com.akfa.apsproject.pult_and_urgent_problems.PultActivity;
import com.akfa.apsproject.QRScanner;
import com.akfa.apsproject.R;
import com.akfa.apsproject.SettingsActivity;
import com.akfa.apsproject.pult_and_urgent_problems.UrgentProblemsList;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//----------ОПЕРАТОР/МАСТЕР ОТМЕЧВАЕТ СОСТОЯНИЕ КАЖДОГО ПУНКТА ЛИНИИ (ПОРЯДОК/ПРОБЛЕМА) И ПЕРЕХОД В QR SCANNER----------//
/*DISCLAIMER: ключевые слова MaintenanceProblem, problems, упоминающиеся в этом классе относятся к проверками обнаруженным при профилактических проверках Maintenance_problems*/
//"point" - "пункт"
public class QuestPointDynamic extends AppCompatActivity implements View.OnTouchListener
{
    private final int RADIO_GROUP_ID = 5000, REQUEST_IMAGE_CAPTURE  = 1, REQUEST_REQUIRED_STATION_PROBLEM_IMAGE_CAPTURE = 28, REQUEST_REQUIRED_IMAGE_CAPTURE = 27;
    public static String checkDuration; //длительность проверки в формате ММ:СС
    private long startTimeMillis;
    private int pointNo, numOfPoints = 0, numOfSubpoints = 0, problemsCount, problemsOnThisStation = 0, shopNo, equipmentNo, photoIterator = 0;
    private boolean[] photographedProblems; //для проверки, сфоткали ли все проблемы
    private boolean mustTakePic = false;
    private int noSubpointMustTakePic;
    private String shopName, equipmentName, pointName;
    File currentPicFile;
    List<String> problemPushKeysOfTheWholeCheck, subpointDescriptions; //на случай если нажмет назад, чтобы удалить все проблемы занесенные в БД
    //layout views
    ActionBarDrawerToggle toggle;
    private LinearLayout scrollLinearLayout; //в него добав-ся радиокнопки
    Button nextPoint, pointDeactivated;
    TextView equipmentNameTextView, pointInfoTextView; //инфа про линию и пункт
    //Firebase
    FirebaseDatabase db;
    DatabaseReference shopRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_point_dynamic);
        try {
            initInstances(); //иниц views, переменные, БД
            setEquipmentData(); //иниц кол-во пунктов, участков, назв-е линии
            toggle = setUpNavBar();
        }
        catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getApplicationContext());
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initInstances() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance();
        shopRef = db.getReference().child("Shops/" + QuestListOfEquipment.shopNoGlobal);
        nextPoint = findViewById(R.id.next_point);
        nextPoint.setOnTouchListener(this);
        nextPoint.setVisibility(View.GONE);
        pointDeactivated = findViewById(R.id.point_deactivated);
        pointDeactivated.setVisibility(View.GONE);
        equipmentNameTextView = findViewById(R.id.equipmentName);
        pointInfoTextView = findViewById(R.id.nomer_punkta);
        pointNo = Objects.requireNonNull(getIntent().getExtras()).getInt(getString(R.string.nomer_punkta_textview_text));
        Log.e("pointNo = ", String.valueOf(pointNo));
        subpointDescriptions = new ArrayList<>();
        if(pointNo == 1) //если проверка тока началась, иниц кол-во проблем к 0, начни отсчитывать продолжительность проверки и открой лист для сохранения ключей репортнутых проблем в БД
        {
            startTimeMillis = System.currentTimeMillis(); //эта фигня работает только для последнего активити
            problemsCount = 0;
            problemPushKeysOfTheWholeCheck = new ArrayList<>();
        }
        else //если это >1 участка, получи интер-активити данные о процессе проверки
        {
            Bundle arguments = getIntent().getExtras();
            startTimeMillis = arguments.getLong("startTimeMillis");
            problemsCount = arguments.getInt("Количество обнаруженных проблем");
            problemPushKeysOfTheWholeCheck = arguments.getStringArrayList("Коды проблем");
        }
        //общие интер-активти данные
        pointInfoTextView.setText(getString(R.string.nomer_point_textview) + pointNo);
        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        shopNo = getIntent().getExtras().getInt("Номер цеха");
        equipmentNo = getIntent().getExtras().getInt("Номер линии");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //дейсвтие при нажатиях на кнопку (отсканировать QR код)
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                nextPoint.setBackgroundResource(R.drawable.edit_red_accent_pressed); //эффект нажатия
                break;
            case MotionEvent.ACTION_UP: //когда уже отпустил, октрой qr
                //запуск QR сканера отсканировав  qr код 1-го пункта любой линии
                nextPoint.setBackgroundResource(R.drawable.edit_red_accent);
                if (AllRadiosChecked(numOfSubpoints)) //все радиогруппы были отмечены?
                {
                    saveCheckingData(numOfSubpoints);
                    //checks points' count and refreshes the activity
                }
                else { Toast.makeText(getApplicationContext(), R.string.select_the_state_of_each_subpoint, Toast.LENGTH_LONG).show(); }
                break;
        }
        return false;
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).show();
        setTitle(getString(R.string.maintenance_check));
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = findViewById(R.id.quest_activity_point_dynamic);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nv);
        View headerView = navigationView.getHeaderView(0);
        TextView userInfo = headerView.findViewById(R.id.user_info);
        userInfo.setText(UserData.login);
//        здесь адаптируем меню в нав баре в зависимости от уровня доступа пользователя: мастер/оператор, у ремонтника нет прав проверки
        navigationView.getMenu().clear();
        switch(UserData.position){
            case "operator":
                navigationView.inflateMenu(R.menu.operator_menu);
                break;
            case "master":
                navigationView.inflateMenu(R.menu.master_menu);
                break;
            //other positions shouldn't be able to access checking page at all
            //if some changes, u can add a case
        }

        //ниже действия, выполняемые при нажатиях на элементы нав бара
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.check_equipment)
                {
                    drawerLayout.closeDrawer(GravityCompat.START); //когда нажали на саму проверку, нав бар просто закрывается
                    Toast.makeText(getApplicationContext(), R.string.check_in_progress, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    AlertDialog diaBox = askOptionOnNavigationBarClicked(id);
                    diaBox.show();
                }
                return true;
            }
        });
        return toggle;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog askOptionOnNavigationBarClicked(final int menuItemId)
    {//тут если юзер захочет выйти из проверки, данные должны стереться, поэтому выводится диалог для этого
        return new AlertDialog.Builder(this).setTitle(getString(R.string.finish_check)).setMessage(getString(R.string.r_u_sure_u_wanna_finish_check))
            .setIcon(R.drawable.close)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @SuppressWarnings("deprecation")
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
                        for(String problemPushKey1 : problemPushKeysOfTheWholeCheck)
                        { //удали все проблемы, о которых репортнул юзер в текущей проверке
                            problemsRef.child(problemPushKey1).setValue(null);
                            StorageReference problemPicRef = mStorageRef.child("problem_pictures/" + problemPushKey1 + ".jpg");
                            problemPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //file deleted successfully
                                }
                            });

                        }
                        //после удаления данных, сделай переход на запрашиваемый активити
                        switch(menuItemId)
                        {
                            case R.id.urgent_problems:
                                Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                startActivity(openUrgentProblemsList);
                                break;
                            case R.id.pult:
                                Intent openMainActivity = new Intent(getApplicationContext(), PultActivity.class);
                                startActivity(openMainActivity);
                                break;
                            case R.id.web_monitoring:
                                Intent openFactoryCondition = new Intent(getApplicationContext(), FactoryCondition.class);
                                startActivity(openFactoryCondition);
                                break;
                            case R.id.about: //инфа про приложение и компанию и иинструкции может
                                Intent openAbout = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(openAbout);
                                break;
                            case R.id.log_out: //возвращение в логин page
                                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.clear();
                                editor.commit();
                                Intent logOut = new Intent(getApplicationContext(), Login.class);
                                logOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(logOut);
                                break;
                        }
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void setEquipmentData()
    {//иниц кол-во пунктов, участков, назв-е линии
        shopRef.addListenerForSingleValueEvent(new ValueEventListener() { //единожды из БД прочитай данные о конкретном участке и линии
            @SuppressLint("SetTextI18n")
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                // "/Equipment_lines/" + QuestMainActivity.childPositionG

                shopName = Objects.requireNonNull(shopSnap.child("shop_name").getValue()).toString();
                DataSnapshot equipmentSnap = shopSnap.child("Equipment_lines/" + QuestListOfEquipment.equipmentNoGlobal);
                equipmentName = Objects.requireNonNull(equipmentSnap.child("equipment_name").getValue()).toString();
                String equipmentNameMultilang = Objects.requireNonNull(equipmentSnap.child(getString(R.string.equipment_name)).getValue()).toString();
                equipmentNameTextView.setText(equipmentNameMultilang);

                //ссылка к этому участку
                DataSnapshot thisStationSnap = equipmentSnap.child("Points").child(Integer.toString(pointNo));
                //название участка
                Log.e("setEquipmentData pointNo = ", String.valueOf(pointNo));
                try { //если в БД не указано имя пункта point_name (забыли)
                    pointName = Objects.requireNonNull(thisStationSnap.child(getString(R.string.point_name)).getValue()).toString();
                }
                catch (NullPointerException npe) {
                    ExceptionProcessing.processException(npe);
                    pointName = "";
                }
                pointInfoTextView.setText(getString(R.string.nomer_point_textview) + pointNo + "\n " + pointName);
                //добавление пунктов этого участка
                int subpointNo = 1;
                while(thisStationSnap.child("subpoint_" + subpointNo).exists())
                {
                    String subpointDescription;
                    try {
                        subpointDescription = Objects.requireNonNull(thisStationSnap.child("subpoint_" + subpointNo).child(getString(R.string.description)).getValue()).toString();
                    }
                    catch (NullPointerException npe) { //на случай если неправильно заполнили или удалили определенный подпункт
                        ExceptionProcessing.processException(npe);
                        subpointDescription = getString(R.string.subpoint_no) + subpointNo;
                    }
                    subpointDescriptions.add(subpointDescription);
                    addRadioGroup(subpointNo, subpointDescription);
                    if(thisStationSnap.child("subpoint_" + subpointNo).child("must_take_pic").exists())
                    {
                        mustTakePic = true;
                        noSubpointMustTakePic = subpointNo;
                    }
                    subpointNo++;

                }
                numOfSubpoints = subpointNo-1;
                photographedProblems = new boolean[numOfSubpoints]; //количество пунктов было установлено, иниц массив photographedProblems
                //пока проблемы не обнаружены, задай, что все проблемы сфотканы уже
                Arrays.fill(photographedProblems, true); //заполни массив trues
                if(thisStationSnap.child("can_be_deactivated").exists())
                {
                    pointDeactivated.setVisibility(View.VISIBLE);
                }
                numOfPoints = Integer.parseInt(Objects.requireNonNull(equipmentSnap.child(getString(R.string.number_of_points)).getValue()).toString());
                if(pointNo == numOfPoints)
                {
                    nextPoint.setText(R.string.finish_check);
                }
                nextPoint.setVisibility(View.VISIBLE);
//                addRadioGroups(); //количество пунктов было установлено, добавь радиокнопки
                initClickListeners(); //радиокнопки были добавлены, поэтому можно разрешать кликать на "Следующий пункт"
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void addRadioGroup(int pointNo, String subpointDescription)
    {
        Context context = getApplicationContext(); //чтобы передать некоторым функциям как параметр
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL; //расположение посередине род элемента
        layoutParams.setMargins(10, 0, 10, 0); //ЭТИМ МОЖНО ЗАДАВАТЬ ОТСТУПЫ РАДИОКНОПОК
        RadioGroup rg = new RadioGroup(context); //create the RadioGroup
        rg.setId(RADIO_GROUP_ID + pointNo); // На данный момент (10.04) айдишки пунктов варируются 5000-5020
        //Id задается чтобы к элементу можно было обратиться позже в функции AllRadiosChecked
        rg.setOrientation(RadioGroup.HORIZONTAL);
        rg.setGravity(Gravity.CENTER_HORIZONTAL);
        rg.setWeightSum(2); //ДЛЯ ВЗАИМОРАСПОЛОЖЕНИЯ КНОПОК
        rg.setPadding(50, 20, 50, 20); //ВНУТРЕННИЙ ОТСТУП

        //-------Подпись Пункт №Х--------//
        @SuppressLint("ResourceType") String textColor = getResources().getString(R.color.text);
        TextView rgTitle = new TextView(context);
        rgTitle.setText(pointNo + ". " + subpointDescription);
        final int RADIO_GROUP_ELEMENT_ID = 6000;
        rgTitle.setId(RADIO_GROUP_ELEMENT_ID + pointNo * 10);
        rgTitle.setTextColor(Color.parseColor("#1F1C26")); //ЦВЕТ ТЕКСТА ЭТОГО TEXTVIEW
        rgTitle.setBackgroundColor(Color.parseColor(textColor)); //ФОН ЭТОГО TEXTVIEW (СЕЙЧАС БЕЛОВАТЫЙ)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 0, 0, 0); //РАСШИРЯЕТСЯ НА ВЕСЬ РОД VIEW (WIDTH)
        rgTitle.setLayoutParams(params);
        rgTitle.setGravity(Gravity.CENTER);
        rgTitle.setPadding(0, 30, 0, 30);
        scrollLinearLayout.addView(rgTitle);

        RadioButton[] rb = new RadioButton[2];
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(100, 0, 100, 0); //ОТСТУПЫ ПО СТОРОНАМ
        //-------Radiobutton для Проблемы--------//
        buttonParams.weight = 1; //ОБЕ КНОПКИ ДОЛЖНЫ ИМЕТЬ ОДИН РАЗМЕРЫ
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            assert inflater != null;
            rb[0] = (RadioButton) inflater.inflate(R.layout.problem_radiobutton, null); //ЗДЕСЬ МОЖНО ДАТЬ ССЫЛКУ НА XML ДИЗАЙН КНОПКИ
            rb[1] = (RadioButton) inflater.inflate(R.layout.no_problem_radiobutton, null); //ЗДЕСЬ МОЖНО ДАТЬ ССЫЛКУ НА XML ДИЗАЙН КНОПКИ

        }
        catch (AssertionError ae)
        {//если тот xml файл был потерян или проблема с inflater
            ExceptionProcessing.processException(ae);
            rb[0] = new RadioButton(context);
            rb[1] = new RadioButton(context);
        }
        rb[0].setText(R.string.problem);
        rb[0].setId(RADIO_GROUP_ELEMENT_ID + (pointNo * 10) + 1);
        rb[0].setLayoutParams(buttonParams);
        rb[0].setTextSize(15);
        //-------Radiobutton для Порядка--------//
        rb[1].setText(R.string.ok_order);
        rb[1].setTextSize(15);
        rb[1].setId(RADIO_GROUP_ELEMENT_ID + (pointNo * 10) + 2);
        rb[1].setLayoutParams(buttonParams);

        //-------Добавим все созданные объекты в scrollLinearLayout и инициализируем click listener--------//
        rg.addView(rb[0], 0, layoutParams);
        rg.addView(rb[1], 1, layoutParams);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId % 10 == 1)
                {
                    RadioButton rbProblem = findViewById(checkedId), rbNoProblem = findViewById(checkedId+1);
                    rbProblem.setBackground(getDrawable(R.drawable.problem_radiobutton_checked));
                    rbNoProblem.setBackground(getDrawable(R.drawable.no_problem_radiobutton));
                }
                else
                {
                    RadioButton rbProblem = findViewById(checkedId-1), rbNoProblem = findViewById(checkedId);
                    rbProblem.setBackground(getDrawable(R.drawable.problem_radiobutton));
                    rbNoProblem.setBackground(getDrawable(R.drawable.no_problem_radiobutton_checked));
                }
            }
        });
        scrollLinearLayout.addView(rg);
    }

    private void initClickListeners()
    {//задает listener кнопки след пункт
        pointDeactivated.setOnClickListener(new Button.OnClickListener() {
            @Override public void onClick(View v) {
                if (pointNo >= numOfPoints) { //если это последний пункт и проблем обнаружено не было
                    //переход на новое окно - QuestEndOfChecking - итоги проверки и следующие шаги
                    startEndOfChecking();
                }
                else qrStart(pointNo, equipmentNo, shopNo); //если все в порядке (проблем на этом участке нет) - запусти qr scanner
            }
        });
    }

    @Override
    public void onBackPressed() {
        if((pointNo) > 1) {
            AlertDialog diaBox = AskOption();
            diaBox.show();
        }
        else { super.onBackPressed(); }
    }

    private AlertDialog AskOption()
    { //конструирует диалог выходящий при желании пользователя остнаовить проверку
        return new AlertDialog.Builder(this).setTitle(getString(R.string.finish_check)).setMessage(getString(R.string.r_u_sure_u_wanna_finish_check))
                .setIcon(R.drawable.close)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
                        for(String problemPushKey1 : problemPushKeysOfTheWholeCheck)
                        {
                            problemsRef.child(problemPushKey1).setValue(null);
                            StorageReference problemPicRef = mStorageRef.child("problem_pictures/" + problemPushKey1 + ".jpg");
                            problemPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //file deleted successfully
                                }
                            });

                        }
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private boolean AllRadiosChecked(int numOfRadioGroups)
    { //проверка: все пункты должны быть отмечены
        RadioGroup rg;
        for(int i = 1; i <= numOfRadioGroups; i++)
        {
            rg = findViewById(RADIO_GROUP_ID + i);
            if(rg.getCheckedRadioButtonId() == -1)
            {
                return false;
            }
        }
        return true;
    }


    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    public void startEndOfChecking()
    {
        long endTimeMillis = System.currentTimeMillis();
        //считают продолжительность проверки
        long durationMillis = endTimeMillis - startTimeMillis;
        checkDuration = String.format("%02d" + getString(R.string.minutes) + ", " + "%02d" + getString(R.string.seconds), TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis)));
        Intent intent = new Intent(getApplicationContext(), QuestEndOfChecking.class);
        intent.putExtra("Количество обнаруженных проблем", problemsCount);

        String date, time;
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy"); //в firebase нельзя в пути ставить точки
        date = sdf.format(new Date());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference thisMaintenanceCheckRef = dbRef.child("Maintenance_checks/" + date).push();
        sdf = new SimpleDateFormat("HH:mm");
        time = sdf.format(new Date());
        sdf = new SimpleDateFormat("dd.MM.yyyy"); //но в value можно дать данные с точками
        date = sdf.format(new Date());
        MaintenanceCheck thisMaintenanceCheck = new MaintenanceCheck(UserData.login, checkDuration, equipmentName, shopName, time, date, shopNo, equipmentNo, problemsCount);
        thisMaintenanceCheckRef.setValue(thisMaintenanceCheck);
        startActivity(intent);
        finish();
        pointNo = 0;
    }

    public void qrStart(int pointNo, int equipmentNumber, int shopNumber) {//запустить qr SCANNER
        pointNo++; //подготовим для следующего окна PointDynamic
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopNumber);
        intent.putExtra("Номер линии", equipmentNumber);
        intent.putExtra(getString(R.string.nomer_punkta_textview_text), pointNo);
        intent.putExtra("Количество пунктов", numOfPoints);
        intent.putExtra("startTimeMillis", startTimeMillis);
        intent.putExtra("Действие", "Открой PointDynamic");
        intent.putExtra("Количество обнаруженных проблем", problemsCount);
        intent.putStringArrayListExtra("Коды проблем", (ArrayList<String>) problemPushKeysOfTheWholeCheck);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); //не сохранять qr сканер в tray
        startActivity(intent);
        finish(); //закрой это окошко
    }

    String problemPushKey; //айдишка текущей проблемы, по которой мы проходимся в цикле ниже
    List<String> problemPushKeys = new ArrayList<>(); //айдишки всех обнаруженных проблем
    @SuppressLint({"ResourceType", "SimpleDateFormat"})
    private void saveCheckingData(int numOfRadioGroups)
    { //this function is called in case all radiogroups are checked and
        //the user hits "next point".
        RadioGroup rg;
        //Проблемы - название таблицы для проблем
        DatabaseReference problemsRef = db.getReference().child("Maintenance_problems");
        for(int i = 1; i <= numOfRadioGroups; i++) //проверь каждую радиогруппу, если отмечено проблема, попроси сфоткать
        {
            rg = findViewById(RADIO_GROUP_ID + i);
            if(rg.getCheckedRadioButtonId() % 10 == 1) //case of a problem, not photographed
            {
                photographedProblems[i-1] = false; //даем знать, что эта проблема еще не сфоткана
                //мы в цикле проходимся индексами 1, 2, 3; не начинаем счет с 0, 1, 2, но в photographedProblems нужно обращаться к элементам через 0, 1,2
                problemsCount++; //итерируем кол-во проблем
                problemsOnThisStation++; //кол-во проблем на именно этом участке
                //i - номер пункта с проблемой
                //pointNo - номер участка
                //QuestMainActivity.shopNoGlobal - the number of the equipment (номер линии)
                //QuestMainActivity.equipmentNoGlobal - the number of the shop (номер цеха)

                //время-дата
                String date, time;
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                date = sdf.format(new Date());
                sdf = new SimpleDateFormat("HH:mm");
                time = sdf.format(new Date());
                //конец время-дата

                DatabaseReference newProbRef = problemsRef.push(); //создай подветку в Maintenance_problems с уник айди
                //занеси в БД данные об этой проблеме
                newProbRef.setValue(new MaintenanceProblem(date, time, shopName, equipmentName, QuestListOfEquipment.shopNoGlobal, QuestListOfEquipment.equipmentNoGlobal, pointNo, i, pointName, subpointDescriptions.get(i-1)));
                problemPushKey = newProbRef.getKey(); //запиши этот айди в переменную
                problemPushKeys.add(problemPushKey); //добавь этот айди в лист проблем именно на этом участке
                problemPushKeysOfTheWholeCheck.add(problemPushKey); //добавь этот айди в лист проблем во время всей проверки этой линии
                //сфоткайте первую проблему, следующие проблемы фоткаются через запуск камеры через onActivityResult
                if(problemsOnThisStation == 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.take_pic_of_point) + i, Toast.LENGTH_LONG).show();
                    if(noSubpointMustTakePic == 1)
                    {
                        mustTakePic = false;
                        dispatchTakePictureIntent(problemPushKey, REQUEST_REQUIRED_STATION_PROBLEM_IMAGE_CAPTURE); //запуск интент камеры
                    }
                    else {
                        dispatchTakePictureIntent(problemPushKey, REQUEST_IMAGE_CAPTURE); //запуск интент камеры
                    }
                }
            }
        }

        if(mustTakePic)
        {
            mustTakePic = false;
            //время-дата
            String date, time;
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            date = sdf.format(new Date());
            sdf = new SimpleDateFormat("HH.mm.SS");
            time = sdf.format(new Date());
            //конец время-дата
            String photoName = equipmentName + ", " + pointName + " | " + date + "_" + time;
            //noPointMustTakePic
            Toast.makeText(getApplicationContext(), R.string.take_pic_of_monitor_here, Toast.LENGTH_LONG).show();
            dispatchTakePictureIntent(photoName, REQUEST_REQUIRED_IMAGE_CAPTURE);
        }
        else if (pointNo >= numOfPoints && numOfUnphotographedProblem() == -1) { //если это последний пункт и проблем обнаружено не было
            //переход на новое окно - QuestEndOfChecking - итоги проверки и следующие шаги
            startEndOfChecking();
        }
        else if(pointNo < numOfPoints && numOfUnphotographedProblem() == -1)
            qrStart(pointNo, equipmentNo, shopNo); //если все в порядке (проблем на этом участке нет) - запусти qr scanner
    }

    private int numOfUnphotographedProblem() //проходится по массиву photographedProblems и возвращает индекс следующей несфотографированной проблемы
    {
        for(int i = 0; i < numOfSubpoints; i++)
            if(!photographedProblems[i])
                return i;//номер подпункта (они начинаются с 1, а не с 0)
        return -1;
    }

    String currentPhotoPath; //the string of uri of where file is located, используется в процессе сохранения переданной из CameraIntent фотки
    private File createImageFile(String problemPushKey) {
        // Create an image file name - our case will be the id of problem
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        //интер-активити перемен-е + имя картинки
        String currentFileName = problemPushKey + ".jpg"; //название фотки
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //директория для пользования только твоим прилдожнием
        File image = new File(storageDir, currentFileName);
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(String problemPushKey, int requestCode) { //запуск камеры
//        if(isCameraUsebyApp()) Log.i("TAG", "Камера исп др приложением");
//        else Log.i("TAG", "Камера свободна");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = createImageFile(problemPushKey); //создай файл с названием сордержащим айди проблемы
            // Continue only if the File was successfully created
            //непосредственно запуск активити камеры
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getString(R.string.package_name), photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            currentPicFile = photoFile;
            try {
                startActivityForResult(takePictureIntent, requestCode);
            }
            catch (Exception e)
            {
                ExceptionProcessing.processException(e, getString(R.string.camera_error), getApplicationContext());
                qrStart(pointNo, equipmentNo, shopNo);
            }
        }
    }

//    public boolean isCameraUsebyApp() {
//        Camera camera = null;
//        try {
//            camera = android.hardware.Camera.open();
//        } catch (RuntimeException e) {
//            return true;
//        } finally {
//            if (camera != null) camera.release();
//        }
//        return false;
//    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //обработка результата с камеры
        super.onActivityResult(requestCode, resultCode, data);
        boolean problemOnPictureRequiringStation = false;
        if(requestCode == REQUEST_REQUIRED_STATION_PROBLEM_IMAGE_CAPTURE)
        {//если так, сохрани картинку в обоих папках
            problemOnPictureRequiringStation = true;
        }
        if ((requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) || problemOnPictureRequiringStation) {
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference probPicRef = mStorageRef.child("problem_pictures/" + file.getLastPathSegment());
            //загрузи фотку-файл в Firebase Storage
            probPicRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("ResultOfMethodCallIgnored")
                        @Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), R.string.picture_uploaded_successfully, Toast.LENGTH_LONG).show();
                            File picToDelete = new File(currentPhotoPath);
                            picToDelete.delete(); //удали фотку из памяти телефона (чтобы память не засорялась)
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            exception.printStackTrace();
                            Toast.makeText(getApplicationContext(), R.string.pic_upload_error, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //progressbar, можно использовать если будем показывать прогресс загрузки фотки в БД
                        }
                    });
            photographedProblems[numOfUnphotographedProblem()] = true; //отметь эту несфотографированную фотку сфотографированной
            photoIterator++; //итерируй, чтобы перейти к след проблеме
            if(numOfUnphotographedProblem() != -1)
            { //если еще не сфоткали все проблемы, снова запусти камеру
                String problemPushKey = problemPushKeys.get(photoIterator);
                Toast.makeText(getApplicationContext(), getString(R.string.take_pic_of_point) + (numOfUnphotographedProblem()+1), Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent(problemPushKey, REQUEST_IMAGE_CAPTURE);
            }
            else
            {
                if(mustTakePic)
                {
                    mustTakePic = false;
                    //время-дата
                    String date, time;
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    date = sdf.format(new Date());
                    sdf = new SimpleDateFormat("HH.mm.SS");
                    time = sdf.format(new Date());
                    //конец время-дата
                    String photoName = equipmentName + ", " + pointName + " | " + date + "_" + time;
                    Toast.makeText(getApplicationContext(), R.string.take_pic_of_monitor_here, Toast.LENGTH_LONG).show();
                    dispatchTakePictureIntent(photoName, REQUEST_REQUIRED_IMAGE_CAPTURE);
                }
                else
                {
                    if (pointNo >= numOfPoints) { //если это последний пункт и все сфоткали
                    //переход на окно QuestEndOfChecking - итоги проверки и следующие шаги
                    startEndOfChecking();
                    }
                    else
                    {//если все сфоткали, запусти QR
                        qrStart(pointNo, equipmentNo, shopNo);
                    }
                }
            }
        }
        if((requestCode == REQUEST_REQUIRED_IMAGE_CAPTURE  && resultCode == RESULT_OK) || problemOnPictureRequiringStation)
        {
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            String date;
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            date = sdf.format(new Date());
            StorageReference probPicRef = mStorageRef.child("required_pictures/" + date + "/" + shopName + "/" + equipmentName + "/" + file.getLastPathSegment());
            //загрузи фотку-файл в Firebase Storage
            probPicRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), R.string.picture_uploaded_successfully, Toast.LENGTH_LONG).show();
                        File picToDelete = new File(currentPhotoPath);
                        picToDelete.delete(); //удали фотку из памяти телефона (чтобы память не засорялась)
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            exception.printStackTrace();
                            Toast.makeText(getApplicationContext(), R.string.pic_upload_error, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //progressbar, можно использовать если будем показывать прогресс загрузки фотки в БД
                        }
                    });
            if (pointNo >= numOfPoints) { //если это последний пункт и все сфоткали
                //переход на окно QuestEndOfChecking - итоги проверки и следующие шаги
                startEndOfChecking();
            }
            else
            {//если все сфоткали, запусти QR
                qrStart(pointNo, equipmentNo, shopNo);
            }
        }
    }
}

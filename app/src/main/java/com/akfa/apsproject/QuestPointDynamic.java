package com.akfa.apsproject;

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
/*DISCLAIMER: ключевые слова MaintenanceProblem, problems, упоминающиеся в этом классе относятся к проверками обнаруженным при профилактических проверках Maintenance_problems*/
public class QuestPointDynamic extends AppCompatActivity
{
    private final int RADIO_GROUP_ID = 5000;
    //"station" - "участок"
    private int stationNo, numOfStations = 0, numOfPunkts = 0, problemsCount, problemsOnThisStation = 0, shopNo, equipmentNo, photoIterator = 0;
    private long startTimeMillis, endTimeMillis, durationMillis;
    public static String checkDuration;
    private LinearLayout scrollLinearLayout;
    Button nextPoint;
    TextView equipmentNameTextView, nomerPunktaTextView;
    //Firebase
    FirebaseDatabase db;
    DatabaseReference shopRef;
    private boolean[] photographedProblems;
    private String employeeLogin, employeePosition, shopName, equipmentName, currentFileName;
    static final int REQUEST_IMAGE_CAPTURE  = 1;
    File currentPicFile;
    private StorageReference mStorageRef;
    List<String> problemPushKeysOfTheWholeCheck;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_point_dynamic);
        initInstances();
        setEquipmentData();
        toggle = setUpNavBar();
    }

    private void initInstances() {
        getSupportActionBar().hide();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance();
        shopRef = db.getReference().child("Shops/" + QuestMainActivity.shopNoGlobal);
        nextPoint = findViewById(R.id.nextPoint);
        equipmentNameTextView = findViewById(R.id.equipmentName);
        nomerPunktaTextView = findViewById(R.id.nomer_punkta);
        stationNo = getIntent().getExtras().getInt("Номер пункта");
        if(stationNo == 1)
        {
            startTimeMillis = System.currentTimeMillis(); //эта фигня работает только для последнего активити
            problemsCount = 0;
            problemPushKeysOfTheWholeCheck = new ArrayList<>();
        }
        else
        {
            Bundle arguments = getIntent().getExtras();
            startTimeMillis = arguments.getLong("startTimeMillis");
            problemsCount = arguments.getInt("Количество обнаруженных проблем");
            problemPushKeysOfTheWholeCheck = arguments.getStringArrayList("Коды проблем");
        }
        nomerPunktaTextView.setText(getString(R.string.nomer_station_textview) + stationNo);
        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");
        shopNo = getIntent().getExtras().getInt("Номер цеха");
        equipmentNo = getIntent().getExtras().getInt("Номер линии");
    }

    private ActionBarDrawerToggle setUpNavBar() {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        setTitle("Проверка линий");
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
//        здесь адаптируем меню в нав баре в зависимости от уровня доступа пользователя: мастер/оператор, у ремонтника нет прав проверки
        navigationView.getMenu().clear();
        switch(employeePosition){
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
                    Toast.makeText(getApplicationContext(), "Проверка линии уже в процессе", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    AlertDialog diaBox = askOptionOnNavigationBarClicked(id, drawerLayout);
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

    private AlertDialog askOptionOnNavigationBarClicked(final int menuItemId, final DrawerLayout drawerLayout)
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this).setTitle("Закончить проверку").setMessage("Вы уверены, что хотите закончить проверку? Данные не будут сохранены.")
                .setIcon(R.drawable.close)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
                        for(String problemPushKey : problemPushKeysOfTheWholeCheck)
                        {
                            problemsRef.child(problemPushKey).setValue(null);
                            StorageReference problemPicRef = mStorageRef.child("problem_pictures/" + problemPushKey + ".jpg");
                            problemPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //file deleted successfully
                                }
                            });

                        }
                        switch(menuItemId)
                        {
                            case R.id.urgent_problems:
                                Intent openUrgentProblemsList = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                openUrgentProblemsList.putExtra("Логин пользователя", employeeLogin);
                                openUrgentProblemsList.putExtra("Должность", employeePosition);
                                startActivity(openUrgentProblemsList);
                                break;
                            case R.id.pult:
                                Intent openMainActivity = new Intent(getApplicationContext(), PultActivity.class);
                                openMainActivity.putExtra("Логин пользователя", employeeLogin);
                                openMainActivity.putExtra("Должность", employeePosition);
                                startActivity(openMainActivity);
                                break;
                            case R.id.web_monitoring:
                                Intent openFactoryCondition = new Intent(getApplicationContext(), FactoryCondition.class);
                                openFactoryCondition.putExtra("Логин пользователя", employeeLogin);
                                openFactoryCondition.putExtra("Должность", employeePosition);
                                startActivity(openFactoryCondition);
                                break;
                            case R.id.about: //инфа про приложение и компанию и иинструкции может
//                        Intent openAbout = new Intent(getApplicationContext(), About.class);
//                        startActivity(openAbout);
                                Toast.makeText(getApplicationContext(), "Приложение создано Akfa R&D в 2020 году в Ташкенте.",Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.log_out: //возвращение в логин page
                                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.clear();
                                editor.commit();
                                Intent logOut = new Intent(getApplicationContext(), Login.class);
                                logOut.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(logOut);
                                break;
                        }
                        finish();
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private void setEquipmentData()
    {
        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot shopSnap) {
                // "/Equipment_lines/" + QuestMainActivity.childPositionG
                shopName = shopSnap.child("shop_name").getValue().toString();
                DataSnapshot equipmentSnap = shopSnap.child("Equipment_lines/" + QuestMainActivity.equipmentNoGlobal);
                equipmentName = equipmentSnap.child("equipment_name").getValue().toString();
                equipmentNameTextView.setText(getString(R.string.equipment_name_textview) + " " + equipmentName);
                //простое кастование не получается, поэтому приходится писать больше кода
                Long longNumOfPoints = new Long((long) equipmentSnap.child("number_of_punkts").getValue());
                numOfStations = longNumOfPoints.intValue();
                Long longNumOfSubpoints = Long.valueOf((long) equipmentSnap.child(Integer.toString(stationNo)).getValue());
                numOfPunkts = longNumOfSubpoints.intValue();
                photographedProblems = new boolean[numOfPunkts];
                for(int i = 0; i < photographedProblems.length; i++)
                {
                    photographedProblems[i] = true;
                }
                addRadioGroups();
                initClickListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addRadioGroups() {
        for (int i = 1; i <= numOfPunkts; i++) {
            //creates radiobuttons for a given point with count stationNo
            Context context = getApplicationContext(); //чтобы передать некоторым функциям как параметр
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            layoutParams.setMargins(20, 0, 20, 0);
            RadioGroup rg = new RadioGroup(context); //create the RadioGroup
            rg.setId(RADIO_GROUP_ID + i); // На данный момент (10.04) айдишки пунктов варируются 5000-5020
            //Id задается чтобы к элементу можно было обратиться позже в функции AllRadiosChecked
            rg.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL
            rg.setGravity(Gravity.CENTER_HORIZONTAL);
            rg.setWeightSum(2);
            rg.setPadding(50, 20, 50, 20);

            //-------Подпись Пункт №Х--------//
            @SuppressLint("ResourceType") String textColor = getResources().getString(R.color.text);
            TextView rgTitle = new TextView(context);
            rgTitle.setText("Пункт № " + i);
            int RADIO_GROUP_ELEMENT_ID = 6000;
            rgTitle.setId(RADIO_GROUP_ELEMENT_ID + i * 10);
            rgTitle.setTextColor(Color.parseColor("#1F1C26"));
            rgTitle.setBackgroundColor(Color.parseColor(textColor));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, 0, 0, 0);
            rgTitle.setLayoutParams(params);
            rgTitle.setGravity(Gravity.CENTER);
            rgTitle.setPadding(0, 30, 0, 30);
            scrollLinearLayout.addView(rgTitle);

            RadioButton[] rb = new RadioButton[2];
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.setMargins(300, 0, 300, 0);
            //-------Radiobutton для Проблемы--------//
            buttonParams.weight = 1;
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rb[0] = (RadioButton) inflater.inflate(R.layout.problem_radiobutton, null);
            rb[0].setText("ПРОБЛЕМА");
            rb[0].setId(RADIO_GROUP_ELEMENT_ID + (i * 10) + 1);
            rb[0].setLayoutParams(buttonParams);
            rb[0].setTextSize(15);
            //-------Radiobutton для Порядка--------//
//            rb[1] = new RadioButton(context);
            rb[1] = (RadioButton) inflater.inflate(R.layout.no_problem_radiobutton, null);
            rb[1].setText("ПОРЯДОК");
            rb[1].setTextSize(15);
            rb[1].setId(RADIO_GROUP_ELEMENT_ID + (i * 10) + 2);
            rb[1].setLayoutParams(buttonParams);
            //-------Добавим все созданные объекты в layout--------//
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
        stationNo++; //подготовим для следующего окна PointDynamic
    }

    private void initClickListeners()
    {
        nextPoint.setOnClickListener(new Button.OnClickListener(){
        @SuppressLint("DefaultLocale") @Override public void onClick(View v) {
            if (AllRadiosChecked(numOfPunkts)) //все радиогруппы были отмечены?
            {
                saveCheckingData(numOfPunkts);
                //checks points' count and refreshes the activity
                if (stationNo > numOfStations) {
                    Toast.makeText(getApplicationContext(), "Конец линии", Toast.LENGTH_LONG).show();
                    //переход на новое окно - QuestEndOfChecking - итоги проверки и следующие шаги
                    stationNo = 0;
                    endTimeMillis = System.currentTimeMillis();
                    durationMillis = endTimeMillis - startTimeMillis;
                    checkDuration = String.format("%02d мин, %02d сек", TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                            TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis)));
                    Intent intent = new Intent(getApplicationContext(), QuestEndOfChecking.class);
                    intent.putExtra("Количество обнаруженных проблем", problemsCount);
                    intent.putExtra("Должность", employeePosition);
                    intent.putExtra("Логин пользователя", employeeLogin);
                    startActivity(intent);
                    finish();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Заполните состояние каждого пункта", Toast.LENGTH_LONG).show();
            }
        }
        });
    }

    @Override
    public void onBackPressed() {
        if((stationNo-1) > 1) {
            AlertDialog diaBox = AskOption();
            diaBox.show();
        }
        else
        {
            finish();
            super.onBackPressed();
        }
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this).setTitle("Закончить проверку").setMessage("Вы уверены, что хотите закончить проверку? Данные не будут сохранены.")
                .setIcon(R.drawable.close)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
                        StorageReference problemsPicRef = mStorageRef.child("problem_pictures");
                        for(String problemPushKey : problemPushKeysOfTheWholeCheck)
                        {
                            problemsRef.child(problemPushKey).setValue(null);
                            StorageReference problemPicRef = mStorageRef.child("problem_pictures/" + problemPushKey + ".jpg");
                            problemPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //file deleted successfully
                                }
                            });
                            finish();
                    }}
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
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

    public void qrStart(int stationNo, int equipmentNumber, int shopNumber) {
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopNumber);
        intent.putExtra("Номер линии", equipmentNumber);
        intent.putExtra("Номер пункта", stationNo);
        intent.putExtra("Количество пунктов", numOfStations);
        intent.putExtra("startTimeMillis", startTimeMillis);
        intent.putExtra("Открой PointDynamic", "да");
        intent.putExtra("Логин пользователя", employeeLogin);
        intent.putExtra("Количество обнаруженных проблем", problemsCount);
        intent.putExtra("Должность", employeePosition);
        intent.putStringArrayListExtra("Коды проблем", (ArrayList<String>) problemPushKeysOfTheWholeCheck);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    private int numOfUnphotographedProblem()
    {
        for(int i = 0; i < numOfPunkts; i++)
        {
            if(!photographedProblems[i])
                return i;//номер пункта (они начинаются с 1, а не с 0)
        }
        return -1;
    }

    String problemPushKey;
    List<String> problemPushKeys = new ArrayList<>();
    @SuppressLint("ResourceType")
    private void saveCheckingData(int numOfRadioGroups)
    { //this function is called in case all radiogroups are checked and
        //the user hits "next point".
        RadioGroup rg;
        //Проблемы - название таблицы для проблем
        DatabaseReference problemsRef = db.getReference().child("Maintenance_problems");
        for(int i = 1; i <= numOfRadioGroups; i++)
        {
            rg = findViewById(RADIO_GROUP_ID + i);
            if(rg.getCheckedRadioButtonId() % 10 == 1) //case of a problem, not photographed
            {
                photographedProblems[i-1] = false;
                problemsCount++;
                problemsOnThisStation++;
                //i - номер пункта с проблемой
                //stationNo - номер участка
                //QuestMainActivity.childPositionG - the number of the equipment (номер линии)
                //QuestMainActivity.groupPositionG - the number of the shop (номер цеха)
                String date, time;
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                date = sdf.format(new Date());
                sdf = new SimpleDateFormat("HH:mm");
                time = sdf.format(new Date());
                DatabaseReference newProbRef = problemsRef.push();
                problemPushKey = newProbRef.getKey();
                problemPushKeys.add(problemPushKey);
                problemPushKeysOfTheWholeCheck.add(problemPushKey);
                Log.i("MMp problemPushKey", problemPushKey);
                newProbRef.setValue(new MaintenanceProblem(employeeLogin, date, time, shopName, equipmentName, QuestMainActivity.shopNoGlobal, QuestMainActivity.equipmentNoGlobal, stationNo, i));
                //сфоткайте проблему, следующие проблемы фоткаются через запуск камеры через onActivityResult
                if(problemsOnThisStation == 1) {
                    Toast.makeText(getApplicationContext(), "Сфотографируйте проблему пункта " + i, Toast.LENGTH_LONG).show();
                    dispatchTakePictureIntent(problemPushKey);
                }

            }
        }
        //если все в порядке
        if(numOfUnphotographedProblem() == -1)   qrStart(stationNo, equipmentNo, shopNo);


    }

    String currentPhotoPath; //the string of uri of where file is located
    private File createImageFile(String problemPushKey) throws IOException {
        // Create an image file name - our case will be the id of problem
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String problemKeyID = problemPushKey; //инициализируй эту переменную к unique key проблемы
        currentFileName = problemKeyID + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //директория для пользования только твоим прилдожнием
        File image = new File(storageDir, currentFileName);
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(String problemPushKey) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(problemPushKey);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getApplicationContext(), "Error creating the file", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getString(R.string.package_name), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentPicFile = photoFile;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference probPicRef = mStorageRef.child("problem_pictures/" + file.getLastPathSegment());
            probPicRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "Фотография загружена успешно", Toast.LENGTH_LONG).show();
                            File picToDelete = new File(currentPhotoPath);
                            picToDelete.delete();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            exception.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Ошибка загрузки файла", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //progressbar
                        }
                    });
            photographedProblems[numOfUnphotographedProblem()] = true;
            photoIterator++;
            if(numOfUnphotographedProblem() != -1)
            {
                String problemPushKey = problemPushKeys.get(photoIterator);
                Toast.makeText(getApplicationContext(), "Сфотографируйте проблему пункта " + (numOfUnphotographedProblem()+1), Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent(problemPushKey);
            }
            else
            {
                qrStart(stationNo, equipmentNo, shopNo);
            }

        }
    }
}

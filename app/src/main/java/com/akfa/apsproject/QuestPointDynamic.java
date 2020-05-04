package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class QuestPointDynamic extends AppCompatActivity
{
    private final int RADIO_GROUP_ID = 5000;
    private int nomerPunkta, numOfPoints = 0, numOfSubpoints = 0, problemsCount = 0, subpointNumForDialogTitle;
    private long startTimeMillis, endTimeMillis, durationMillis;
    private DialogInterface.OnClickListener dialogClickListener;
    public static String checkDuration;
    private LinearLayout scrollLinearLayout;
    Button nextPoint;
    TextView equipmentNameTextView, nomerPunktaTextView;
    //Firebase
    FirebaseDatabase db;
    DatabaseReference equipmentRef;
    private boolean[] photographedProblem;
    private String employeeLogin, employeePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_point_dynamic);
        initInstances();
    }

    private void initInstances() {
        getSupportActionBar().hide();
        db = FirebaseDatabase.getInstance();
        equipmentRef = db.getReference().child("Shops/" + QuestMainActivity.groupPositionG +
                "/Equipment_lines/" + QuestMainActivity.childPositionG);
        nextPoint = findViewById(R.id.nextPoint);
        equipmentNameTextView = findViewById(R.id.equipmentName);
        nomerPunktaTextView = findViewById(R.id.nomer_punkta);
        nomerPunkta = getIntent().getExtras().getInt("Номер пункта");
        if(nomerPunkta == 1)
        {
            startTimeMillis = System.currentTimeMillis(); //эта фигня работает только для последнего активити
            problemsCount = 0;
        }
        else
        {
            Bundle arguments = getIntent().getExtras();
            startTimeMillis = arguments.getLong("startTimeMillis");
            problemsCount = arguments.getInt("Количество обнаруженных проблем");
        }
        nomerPunktaTextView.setText(getString(R.string.nomer_punkta_textview) + nomerPunkta);
        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        employeePosition = getIntent().getExtras().getString("Должность");
        setEquipmentData();
    }

    private void setEquipmentData()
    {
        equipmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot equipmentSnapshot) {
                String equipmentName = (String) equipmentSnapshot.child("equipment_name").getValue();
                equipmentNameTextView.setText(getString(R.string.equipment_name_textview) + " " + equipmentName);
                //простое кастование не получается, поэтому приходится писать больше кода
                Long longNumOfPoints = new Long((long) equipmentSnapshot.child("number_of_punkts").getValue());
                numOfPoints = longNumOfPoints.intValue();
                Long longNumOfSubpoints = Long.valueOf((long) equipmentSnapshot.child(Integer.toString(nomerPunkta)).getValue());
                numOfSubpoints = longNumOfSubpoints.intValue();
                photographedProblem = new boolean[numOfSubpoints];
                addRadioGroups();
                initClickListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addRadioGroups() {
        for (int i = 1; i <= numOfSubpoints; i++) {
            //creates radiobuttons for a given point with count nomerPunkta
            Context context = getApplicationContext(); //чтобы передать некоторым функциям как параметр
            LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            RadioGroup rg = new RadioGroup(context); //create the RadioGroup
            rg.setId(RADIO_GROUP_ID + i); // На данный момент (10.04) айдишки подпунктов варируются 5000-5020
            //Id задается чтобы к элементу можно было обратиться позже в функции AllRadiosChecked
            rg.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL

            //-------Подпись Пункт №Х--------//
            @SuppressLint("ResourceType") String textColor = getResources().getString(R.color.text);
            TextView rgTitle = new TextView(context);
            rgTitle.setText("Подпункт № " + i);
            int RADIO_GROUP_ELEMENT_ID = 6000;
            rgTitle.setId(RADIO_GROUP_ELEMENT_ID + i * 10);
            rgTitle.setTextColor(Color.parseColor(textColor));
            rgTitle.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            rg.addView(rgTitle, 0, layoutParams);

            RadioButton[] rb = new RadioButton[2];
            //-------Radiobutton для Проблемы--------//
            rb[0] = new RadioButton(context);
            rb[0].setText("Проблема");
            rb[0].setId(RADIO_GROUP_ELEMENT_ID + (i * 10) + 1);
            rb[0].setTextColor(Color.parseColor(textColor));
            rb[0].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            //-------Radiobutton для Порядка--------//
            rb[1] = new RadioButton(context);
            rb[1].setText("Порядок");
            rb[1].setId(RADIO_GROUP_ELEMENT_ID + (i * 10) + 2);
            rb[1].setTextColor(Color.parseColor(textColor));
            rb[1].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            //-------Добавим все созданные объекты в layout--------//
            rg.addView(rb[0], 1, layoutParams);
            rg.addView(rb[1], 2, layoutParams);
            scrollLinearLayout.addView(rg);
        }
        nomerPunkta++; //подготовим для следующего окна PointDynamic
    }

    private void initClickListeners()
    {
        nextPoint.setOnClickListener(new Button.OnClickListener(){
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(View v) {
            if(nomerPunkta <= numOfPoints) {
                //checks points' count and refreshes the activity
                //нужна проверка: все радиогруппы были отмечены?
                if(AllRadiosChecked(numOfSubpoints))
                {
                    saveCheckingData(numOfSubpoints);
                    qrStart(nomerPunkta, QuestMainActivity.childPositionG, QuestMainActivity.groupPositionG);
                    /*if(allProbsPhotographed())
                    {
                    }
                    else
                    {
                        Toast.makeText(PointDynamic.this,
                                "Сфоткайте все", Toast.LENGTH_SHORT).show();
                    }*/
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Заполните состояние каждого подпункта", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Конец линии", Toast.LENGTH_LONG).show();
                //переход на новое окно - QuestEndOfChecking - итоги проверки и следующие шаги
                nomerPunkta = 0;
                endTimeMillis = System.currentTimeMillis();
                durationMillis = endTimeMillis - startTimeMillis;
                checkDuration = String.format("%02d мин, %02d сек", TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                        TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis))
                );
                Intent intent = new Intent(getApplicationContext(), QuestEndOfChecking.class);
                intent.putExtra("Количество обнаруженнных проблем", problemsCount);
                startActivity(intent);
            }
        }
    });

        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    startCameraApp();
                }
            }
        };
    }

    private boolean AllRadiosChecked(int numOfRadioGroups)
    { //проверка: все подпункты должны быть отмечены
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

        public void qrStart(int nomerPunkta, int equipmentNumber, int shopNumber) {
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopNumber);
        intent.putExtra("Номер линии", equipmentNumber);
        intent.putExtra("Номер пункта", nomerPunkta);
        intent.putExtra("Количество пунктов", numOfPoints);
        intent.putExtra("startTimeMillis", startTimeMillis);
        intent.putExtra("Открой PointDynamic", "да");
        intent.putExtra("Логин пользователя", employeeLogin);
        intent.putExtra("Количество обнаруженных проблем", problemsCount);
        intent.putExtra("Должность", employeePosition);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private boolean allProbsPhotographed()
    {
        for(int i = 0; i < numOfSubpoints; i++)
        {
            if(!photographedProblem[i])
                return false;
        }
        return true;
    }

    @SuppressLint("ResourceType")
    private void saveCheckingData(int numOfRadioGroups)
    { //this function is called in case all radiogroups are checked and
        //the user hits "next point".
        RadioGroup rg;
        //Проблемы - название таблицы для проблем
        DatabaseReference problemsRef = db.getReference().child("Problems");
        for(int i = 1; i <= numOfRadioGroups; i++)
        {
            rg = findViewById(RADIO_GROUP_ID + i);
            if(rg.getCheckedRadioButtonId() % 10 == 1 && !photographedProblem[i-1]) //case of a problem, not photographed
            {
                problemsCount++;
                //i - номер подпункта с проблемой
                //nomerPunkta - номер пункта
                //QuestMainActivity.childPositionG - the number of the equipment (номер линии)
                //QuestMainActivity.groupPositionG - the number of the shop (номер цеха)
                String date, time;
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                date = sdf.format(new Date());
                sdf = new SimpleDateFormat("HH:mm z");
                time = sdf.format(new Date());
                DatabaseReference newProbRef = problemsRef.push();
                newProbRef.setValue(new Problem(employeeLogin, date, time,
                        QuestMainActivity.groupPositionG, QuestMainActivity.childPositionG, nomerPunkta, i));
                //прямо здесь надо выводить диалог с последующим вызовом камеры

                /*subpointNumForDialogTitle = i;
                showDialog(DIALOG_EXIT_FOR_CAMERA);*/ //и дальше запустится камера и сохранит фотку в хранилище

                //надо сделать такое - если уже чувак сфоткал и результат был норм,
                //в след нажатии "Следующий пункт", не будет херни типа сфоткайте проблему
                //бесконечно
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        int DIALOG_EXIT_FOR_CAMERA = 0;
        if(id == DIALOG_EXIT_FOR_CAMERA)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Переход в приложение Камера");
            adb.setCancelable(false);
            adb.setMessage("Сфотографируйте проблему подпункта " + subpointNumForDialogTitle);
            adb.setIcon(R.drawable.camera_icon);
            adb.setPositiveButton("OK", dialogClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    private Uri generateFileUri() {
        //В объект File directory помещаем созданную исключительно для нашего приложения
        //директорию "ImgVidRec файлы" предназначенную для медиафайлов этого приложения
        //это должно работать на всех Android API
        File directory = new File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Фотографии проблем");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory.getPath() + "/" + "photo_"
                + System.currentTimeMillis() + ".jpg");
        Log.d("Сгенерированный файл:", "fileName = " + file);
        return Uri.fromFile(file);
    }

    public void startCameraApp()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        int REQUEST_CODE_PHOTO = 1;
        startActivityForResult(intent, REQUEST_CODE_PHOTO); //запускает активити из которого можно получить результаты
                                                            //эта конфигурация функции работает, взял ее
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (intent == null) {
                Log.e("Ошибка с интент", "Intent is null");
            } else {
                Log.d("Дир фотки", "Photo uri: " + intent.getData());
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object obj = intent.getExtras().get("data");
                    if (obj instanceof Bitmap) {
                        Bitmap bitmap = (Bitmap) obj;
                        Log.d("Размер фотки", "bitmap " + bitmap.getWidth() + " x "
                                + bitmap.getHeight());
                        photographedProblem[subpointNumForDialogTitle-1] = true;
                        //ivPhoto.setImageBitmap(bitmap); //вывести сделанную картинку в рамку внутри activity
                    }
                }
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            photographedProblem[subpointNumForDialogTitle-1] = false;
        }
    }
}

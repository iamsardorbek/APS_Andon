package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class QuestPointDynamic extends AppCompatActivity
{
    private final int RADIO_GROUP_ID = 5000;
    private int nomerPunkta, numOfPoints = 0, numOfSubpoints = 0, problemsCount, problemsOnThisPunkt = 0, shopNo, equipmentNo;
    private long startTimeMillis, endTimeMillis, durationMillis;
    public static String checkDuration;
    private LinearLayout scrollLinearLayout;
    Button nextPoint;
    TextView equipmentNameTextView, nomerPunktaTextView;
    //Firebase
    FirebaseDatabase db;
    DatabaseReference shopRef;
    private boolean[] photographedProblems;
    private String employeeLogin, employeePosition;
    private String shopName;
    private String equipmentName;
    static final int REQUEST_IMAGE_CAPTURE  = 1;
    File currentPicFile;
    String currentFileName;
    private int photoIterator = 0;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_point_dynamic);
        initInstances();
        setEquipmentData();
    }

    private void initInstances() {
        getSupportActionBar().hide();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance();
        shopRef = db.getReference().child("Shops/" + QuestMainActivity.groupPositionG);
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
        shopNo = getIntent().getExtras().getInt("Номер цеха");
        equipmentNo = getIntent().getExtras().getInt("Номер линии");
    }

    private void setEquipmentData()
    {
        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot shopSnap) {
                // "/Equipment_lines/" + QuestMainActivity.childPositionG
                shopName = shopSnap.child("shop_name").getValue().toString();
                DataSnapshot equipmentSnap = shopSnap.child("Equipment_lines/" + QuestMainActivity.childPositionG);
                equipmentName = equipmentSnap.child("equipment_name").getValue().toString();
                equipmentNameTextView.setText(getString(R.string.equipment_name_textview) + " " + equipmentName);
                //простое кастование не получается, поэтому приходится писать больше кода
                Long longNumOfPoints = new Long((long) equipmentSnap.child("number_of_punkts").getValue());
                numOfPoints = longNumOfPoints.intValue();
                Long longNumOfSubpoints = Long.valueOf((long) equipmentSnap.child(Integer.toString(nomerPunkta)).getValue());
                numOfSubpoints = longNumOfSubpoints.intValue();
                photographedProblems = new boolean[numOfSubpoints];
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
            rgTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
        @SuppressLint("DefaultLocale") @Override public void onClick(View v) {
            if (AllRadiosChecked(numOfSubpoints)) //все радиогруппы были отмечены?
            {
                saveCheckingData(numOfSubpoints);
                //checks points' count and refreshes the activity
                if (nomerPunkta > numOfPoints) {
                    Toast.makeText(getApplicationContext(), "Конец линии", Toast.LENGTH_LONG).show();
                    //переход на новое окно - QuestEndOfChecking - итоги проверки и следующие шаги
                    nomerPunkta = 0;
                    endTimeMillis = System.currentTimeMillis();
                    durationMillis = endTimeMillis - startTimeMillis;
                    checkDuration = String.format("%02d мин, %02d сек", TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                            TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis)));
                    Intent intent = new Intent(getApplicationContext(), QuestEndOfChecking.class);
                    intent.putExtra("Количество обнаруженных проблем", problemsCount);
                    intent.putExtra("Должность", employeePosition);
                    intent.putExtra("Логин пользователя", employeeLogin);
                    startActivity(intent);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Заполните состояние каждого подпункта", Toast.LENGTH_LONG).show();
            }
        }
        });
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

    private int numOfUnphotographedProblem()
    {
        for(int i = 0; i < numOfSubpoints; i++)
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
        DatabaseReference problemsRef = db.getReference().child("Problems");
        for(int i = 1; i <= numOfRadioGroups; i++)
        {
            rg = findViewById(RADIO_GROUP_ID + i);
            if(rg.getCheckedRadioButtonId() % 10 == 1) //case of a problem, not photographed
            {
                photographedProblems[i-1] = false;
                problemsCount++;
                problemsOnThisPunkt++;
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
                problemPushKey = newProbRef.getKey();
                problemPushKeys.add(problemPushKey);
                Log.i("MMp problemPushKey", problemPushKey);
                newProbRef.setValue(new Problem(employeeLogin, date, time, shopName, equipmentName, QuestMainActivity.groupPositionG, QuestMainActivity.childPositionG, nomerPunkta, i));
                //сфоткайте проблему, следующие проблемы фоткаются через запуск камеры через onActivityResult
                if(problemsOnThisPunkt == 1) {
                    Toast.makeText(getApplicationContext(), "Сфотографируйте проблему пункта " + i, Toast.LENGTH_LONG).show();
                    dispatchTakePictureIntent(problemPushKey);
                }

            }
        }
        //если все в порядке
        if(numOfUnphotographedProblem() == -1)   qrStart(nomerPunkta, QuestMainActivity.childPositionG, QuestMainActivity.groupPositionG);


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
                qrStart(nomerPunkta, QuestMainActivity.childPositionG, QuestMainActivity.groupPositionG);
            }

        }
    }
}

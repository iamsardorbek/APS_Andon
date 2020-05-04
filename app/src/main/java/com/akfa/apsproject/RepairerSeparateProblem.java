package com.akfa.apsproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class RepairerSeparateProblem extends AppCompatActivity {
    Button takePic, problemSolved;
    ImageView problemPic;
    Button.OnClickListener clickListener;
    private String IDOfTheProblem;
    private boolean qrResultedSuccess;
    final int REQUEST_CODE_PHOTO = 1;
    private final int DIALOG_EXIT_FOR_CAMERA = 0;

    DatabaseReference problemsRef, thisProblemRef;
    private int nomerPunkta, equipmentNo, shopNo;
    private String equipmentName, shopName;
    private String employeeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairer_activity_separate_problem);
        initInstances();

    }

    private void initInstances() {
        getSupportActionBar().hide();
        problemsRef = FirebaseDatabase.getInstance().getReference().child("Problems");
        IDOfTheProblem = getIntent().getExtras().getString("ID проблемы в таблице Problems");
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        thisProblemRef = problemsRef.child(IDOfTheProblem);
        thisProblemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot problemDataSnapshot) {
                TextView problemDescription = findViewById(R.id.problemDescription);
                Problem problem = problemDataSnapshot.getValue(Problem.class);
                String probDescripText = "Информация про проблему:\n" + problem.getShop_name() + "\n" + problem.getEquipment_line_name() + "\nПункт №" + problem.getPoint()
                        + "\nПодпункт №" + problem.getSubpoint() + "\nОбнаружено сотрудником: " + problem.getDetected_by_employee() + "\nДата и Время обнаружения:" + problem.getDate() + " в " + problem.getTime();
                nomerPunkta = problem.getPoint();
                equipmentName = problem.getEquipment_line_name();
                equipmentNo = problem.getEquipment_line_no();
                shopNo = problem.getShop_no();
                shopName = problem.getShop_name();
                problemDescription.setText(probDescripText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        takePic = findViewById(R.id.takePic);
        problemSolved = findViewById(R.id.problemSolved);
        problemPic = findViewById(R.id.problemPic);
        clickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId())
                {
                    case R.id.takePic:
                        startCameraApp();
                        break;
                    case R.id.problemSolved:
                        qrStart(nomerPunkta, equipmentNo, shopNo);
                        finish();
                }
            }
        };
        problemSolved.setOnClickListener(clickListener);
        takePic.setOnClickListener(clickListener);
        qrResultedSuccess = false;
    }

    private void startCameraApp()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        startActivityForResult(intent, REQUEST_CODE_PHOTO); //запускает активити из которого можно получить результаты
        //эта конфигурация функции работает, взял ее
    }

    private void qrStart(int nomerPunkta, int equipmentNo, int shopNo) {
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopName);
        intent.putExtra("Номер линии", equipmentName);
        intent.putExtra("Номер пункта", nomerPunkta);
        intent.putExtra("Открой PointDynamic", "нет");
        intent.putExtra("Логин пользователя", employeeLogin);
        intent.putExtra("ID проблемы в таблице Problems", IDOfTheProblem);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, QRScanner.CHECK_PERSON_ON_SPOT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data == null) {
                Log.e("Ошибка с интент", "Intent is null");
            } else {
                Log.d("Дир фотки", "Photo uri: " + data.getData());
                Bundle bndl = data.getExtras();
                if (bndl != null) {
                    Object obj = data.getExtras().get("data");
                    if (obj instanceof Bitmap) {
                        Bitmap bitmap = (Bitmap) obj;
                        Log.d("Размер фотки", "bitmap " + bitmap.getWidth() + " x " + bitmap.getHeight());
                        //ivPhoto.setImageBitmap(bitmap); //вывести сделанную картинку в рамку внутри activity
                    }
                }
            }
        }
        else if(resultCode == QRScanner.QR_OK)
        {
            qrResultedSuccess = true;
            Toast.makeText(getApplicationContext(), "Проблема исправлена!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), RepairersProblemsList.class);
//надо перестартовать старый таск ProblemsListForRepairers
            startActivity(intent);
        }
        else
        {
            qrResultedSuccess = false;
        }
    }

    private Uri generateFileUri() {
        //В объект File directory помещаем созданную исключительно для нашего приложения
        //директорию "ImgVidRec файлы" предназначенную для медиафайлов этого приложения
        //это должно работать на всех Android API
        File directory = new File(
                
                getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Фотографии проблем");
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory.getPath() + "/" + "photo_"
                + System.currentTimeMillis() + ".jpg");
        Log.d("Сгенерированный файл:", "fileName = " + file);
        return Uri.fromFile(file);
    }
}

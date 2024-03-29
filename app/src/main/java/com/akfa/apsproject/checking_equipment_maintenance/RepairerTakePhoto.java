package com.akfa.apsproject.checking_equipment_maintenance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

//----------ПОСЛЕ ОТСКАНИРОВАНИЯ РЕМОНТНИКОМ QR ПРОБЛЕМНОГО УЧАСТКА, ЭТОТ АКТИВИТИ  СПРАШИВАЕТ, ---------//
// ---------ХОЧЕТ ЛИ ОН СФОТКАТЬ РЕШЕНИЕ, ЕСЛИ ДА - ЗАПУСК КАМЕРЫ, ЕСЛИ НЕТ - ВОЗВРАТ В REPAIRERS PROBLEMS LIST---------//
public class RepairerTakePhoto extends AppCompatActivity implements View.OnTouchListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1; //код для камера активити
    String problemPushKey;
    Button takePic, dontTakePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_take_photo);
        try {
            initInstances();
        }
        catch (NullPointerException npe)
        {
            ExceptionProcessing.processException(npe);
            finish();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances()
    {
        problemPushKey = Objects.requireNonNull(getIntent().getExtras()).getString("ID проблемы в таблице Maintenance_problems");
        takePic = findViewById(R.id.take_pic);
        dontTakePic = findViewById(R.id.dont_take_pic);
        takePic.setOnTouchListener(this);
        dontTakePic.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                button.setBackgroundResource(R.drawable.edit_red_accent_pressed);
                break;
            case MotionEvent.ACTION_UP:
                //ГЛАВНЫЕ ДЕЙСТВИЯ ЭТОГО АКТИВИТИ:
                //ЕСЛИ НАЖАЛИ TAKE_PIC - ОТКРОЙ КАМЕРУ, ЕСЛИ НАЖАЛИ DONT_TAKE_PIC - ЗАКРОЙ АКТИВИТИ
                button.setBackgroundResource(R.drawable.edit_red_accent);
                switch(button.getId())
                {
                    case R.id.take_pic:
                        dispatchTakePictureIntent(problemPushKey);
                        break;
                    case R.id.dont_take_pic:
                        //проблема решена! аутпут
                        finish();
                        Toast.makeText(getApplicationContext(), R.string.problem_solved_successfully, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
        return false;
    }

    String currentPhotoPath; //the string of uri of where file is located

    private File createImageFile(String problemPushKey) {
        // Create an image file name - our case will be the id of problem
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String currentFileName = problemPushKey + "_SOLVED.jpg";
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
            File photoFile = createImageFile(problemPushKey);
            // Continue only if the File was successfully created
            //ЗАПУСТИ КАМЕРУ
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getString(R.string.package_name), photoFile); //создай файл в памяти телефона
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
            finish(); //если получил фотку, закрывай уже это активити
            //загрузка фотки в БД
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            StorageReference probPicRef = mStorageRef.child("solved_problem_pictures/" + file.getLastPathSegment());
            probPicRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), R.string.picture_uploaded_successfully, Toast.LENGTH_LONG).show();
                    File picToDelete = new File(currentPhotoPath);
                    picToDelete.delete();
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
//                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        //progressbar
                    }
                });

        }
    }
}

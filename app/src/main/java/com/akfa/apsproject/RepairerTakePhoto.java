package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class RepairerTakePhoto extends AppCompatActivity implements View.OnTouchListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Button takePic, dontTakePic;
    String problemPushKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_take_photo);
        problemPushKey = getIntent().getExtras().getString("ID проблемы в таблице Maintenance_problems");
        takePic = findViewById(R.id.take_pic);
        dontTakePic = findViewById(R.id.dont_take_pic);
        takePic.setOnTouchListener(this);
        dontTakePic.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                button.setBackgroundResource(R.drawable.edit_red_accent_pressed);
                break;
            case MotionEvent.ACTION_UP:
                button.setBackgroundResource(R.drawable.edit_red_accent);
                switch(button.getId())
                {
                    case R.id.take_pic:
                        dispatchTakePictureIntent(problemPushKey);
                        break;
                    case R.id.dont_take_pic:
                        //проблема решена! аутпут
                        finish();

                        Toast.makeText(getApplicationContext(), "Проблема успешно решена", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
        return false;
    }

    String currentPhotoPath; //the string of uri of where file is located

    private File createImageFile(String problemPushKey) throws IOException {
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
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
            finish();
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            StorageReference probPicRef = mStorageRef.child("solved_problem_pictures/" + file.getLastPathSegment());
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

        }
    }
}

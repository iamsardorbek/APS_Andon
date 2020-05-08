package com.akfa.apsproject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QRScanner extends AppCompatActivity {
    public static final int CHECK_PERSON_ON_SPOT = 1, QR_OK = 13;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView, directionsTextView;
    BarcodeDetector barcodeDetector;
    Bundle arguments;
    private String codeToDetect, shouldOpenPointDynamic;
    private int equipmentNumber, shopNumber, nomerPunkta, numOfPoints, problemsCount;
    private boolean detectedOnce = false;
    private long startTimeMillis;
    private String employeeLogin, employeePosition;
    List<EquipmentLine> equipmentLineList = new ArrayList<>();
    List<String> problemPushKeysOfTheWholeCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_q_r_scanner);
        getSupportActionBar().hide();
        initInstances();
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(640, 480).build();
        requestCameraPermission(); //в случае разрешения, стартует QR сканер
    }

    private void initInstances()
    {
        arguments = getIntent().getExtras();
        equipmentNumber = arguments.getInt("Номер линии");
        shopNumber = arguments.getInt("Номер цеха");
        Log.w("QRScanner equiNo", String.valueOf(equipmentNumber));
        Log.w("QRScanner shopNo", String.valueOf(shopNumber));
        nomerPunkta = arguments.getInt("Номер пункта");
        shouldOpenPointDynamic = arguments.getString("Открой PointDynamic");
        //на самом деле в адрес пункта нужно заложить полные названия цеха и линии, но пока на номерах
        numOfPoints = arguments.getInt("Количество пунктов");
        startTimeMillis = arguments.getLong("startTimeMillis");
        employeeLogin = arguments.getString("Логин пользователя");
        employeePosition = arguments.getString("Должность");
        problemsCount = arguments.getInt("Количество обнаруженных проблем");
        problemPushKeysOfTheWholeCheck = arguments.getStringArrayList("Коды проблем");
        surfaceView = findViewById(R.id.camerapreview);
        textView = findViewById(R.id.textView);
        directionsTextView = findViewById(R.id.directionsTextView);
        directionsTextView.setVisibility(View.INVISIBLE);
        //codeToDetect, возьми данные из таблицы "QRCodes"
        if(!shouldOpenPointDynamic.equals("другое")) {
            DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference().child("Shops/" + shopNumber);
            shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot shop) {
                    String shopName = shop.child("shop_name").getValue().toString();
                    String equipmentName = shop.child("Equipment_lines/" + equipmentNumber + "/equipment_name").getValue().toString();
                    String directionsText = "Подойдите к\n" + shopName + "\n" + equipmentName + "\nПункт №" + nomerPunkta;
                    directionsTextView.setText(directionsText);
                    codeToDetect = shop.child("Equipment_lines/" + equipmentNumber + "/QR_codes/qr_" + nomerPunkta).getValue().toString();
                    directionsTextView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        else
        {
            DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference().child("Shops");
            shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot shops) {
                    for(DataSnapshot shop : shops.getChildren())
                    {
                        DataSnapshot shopEquipmentLines = shop.child("Equipment_lines");
                        for(DataSnapshot equipmentLine : shopEquipmentLines.getChildren()) {
                            String codeToDetect = equipmentLine.child("QR_codes/qr_1").getValue().toString();
                            int shopNo = Integer.parseInt(shop.getKey().toString());
                            int equipmentLineNo = Integer.parseInt(equipmentLine.getKey().toString());
                            EquipmentLine equipmentLineObject = new EquipmentLine(shopNo, equipmentLineNo, codeToDetect);
                            equipmentLineList.add(equipmentLineObject);
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    private void requestCameraPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
        }
        else
        {
            qrScanCameraON();
        }
    }

    private void qrScanCameraON()
    {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override public void release() {

            }

            @Override public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size()!=0)
                {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            {
                                String codeFromQR = qrCodes.valueAt(0).displayValue;
                                if(shouldOpenPointDynamic.equals("да") || shouldOpenPointDynamic.equals("нет")) {
                                    if (!detectedOnce) {
                                        if (areDetectedAndPassedCodesSame(codeFromQR, codeToDetect)) {
                                            vibration(500);
                                            //создаем интент, в него заносим код успешного распознования
                                            //после окончания finish(), интент сам найдет родительский активити и отдаст результат в
                                            //onActivityResult
                                            if (shouldOpenPointDynamic.equals("да")) { //когда идет переход Verification->QR->PointDynamic
                                                Intent intent = new Intent(getApplicationContext(), QuestPointDynamic.class);
                                                intent.putExtra("Номер пункта", nomerPunkta);
                                                intent.putExtra("Количество пунктов", numOfPoints);
                                                intent.putExtra("startTimeMillis", startTimeMillis);
                                                intent.putExtra("Логин пользователя", employeeLogin);
                                                intent.putExtra("Номер цеха", shopNumber);
                                                intent.putExtra("Номер линии", equipmentNumber);
                                                intent.putExtra("Количество обнаруженных проблем", problemsCount);
                                                intent.putExtra("Должность", employeePosition);
                                                intent.putStringArrayListExtra("Коды проблем", (ArrayList<String>) problemPushKeysOfTheWholeCheck);
                                                startActivity(intent);
                                            } else if (shouldOpenPointDynamic.equals("нет")) {
                                                Bundle arguments = getIntent().getExtras();
                                                String problemKey = arguments.getString("ID проблемы в таблице Problems");
                                                DatabaseReference problemRef = FirebaseDatabase.getInstance().getReference().child("Problems/" + problemKey);
                                                problemRef.child("solved").setValue(true);
                                                problemRef.child("solved_by").setValue(employeeLogin);
                                                String date, time;
                                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                                date = sdf.format(new Date());
                                                sdf = new SimpleDateFormat("HH:mm z");
                                                time = sdf.format(new Date());
                                                problemRef.child("date_solved").setValue(date);
                                                problemRef.child("time_solved").setValue(time);
                                                Intent openProblemsList = new Intent(getApplicationContext(), RepairersProblemsList.class);
                                                setResult(QR_OK, openProblemsList); //Если нету extra данных
                                                startActivity(openProblemsList);
                                            }
                                            finish();
                                        } else {
                                            textView.setText("Вы не в том месте");
                                        }
                                    }
                                }
                                else if (shouldOpenPointDynamic.equals("другое")) {
                                    if (!detectedOnce) {
                                        EquipmentLine equipmentLine = detectedCodeAmongInitialPunkts(codeFromQR);
                                        if(equipmentLine != null)
                                        {
                                            vibration(500);
                                            Intent intent = new Intent(getApplicationContext(), QuestPointDynamic.class);
                                            intent.putExtra("Номер пункта", 1);
                                            intent.putExtra("Номер цеха", equipmentLine.getShopNo());
                                            intent.putExtra("Номер линии", equipmentLine.getEquipmentNo());
                                            intent.putExtra("Логин пользователя", employeeLogin);
                                            intent.putExtra("Количество обнаруженных проблем", problemsCount);
                                            intent.putExtra("Должность", employeePosition);
                                            intent.putStringArrayListExtra("Коды проблем", (ArrayList<String>) problemPushKeysOfTheWholeCheck);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else
                                        {
                                            textView.setText("Подойдите к 1-пункту линии, которую вы хотите проверить");
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(nomerPunkta > 1) {
            AlertDialog diaBox = AskOption();
            diaBox.show();
        }
        else
        {
            super.onBackPressed();
        }
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this).setTitle("Закончить проверку").setMessage("Вы уверены, что хотите закончить проверку? Данные не будут сохранены.")
                .setIcon(R.drawable.close)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Problems");
                        for(String problemPushKey : problemPushKeysOfTheWholeCheck)
                        {
                            problemsRef.child(problemPushKey).setValue(null);
                            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference problemPicRef = mStorageRef.child("problem_pictures/" + problemPushKey + ".jpg");
                            problemPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //file deleted successfully
                                }
                            });
                            finish();
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

    private EquipmentLine detectedCodeAmongInitialPunkts(String codeFromQR) {
        for(EquipmentLine equipmentCurrent : equipmentLineList)
        {
            if(codeFromQR.equals(equipmentCurrent.getStartQRCode()))
            {
                detectedOnce = true;
                return equipmentCurrent;
            }
        }
        return null;
    }

    private boolean areDetectedAndPassedCodesSame(String fromQRScanner, String fromDatabase)
    {
        if(fromQRScanner.equals(fromDatabase))
        {
            detectedOnce = true;
            return true;
        }
        return false;
    }

    private void vibration(int milliseconds)
    {
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }

}

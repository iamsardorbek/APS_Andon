package com.akfa.apsproject;

import android.Manifest;
import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class QRScanner extends AppCompatActivity {
    public static final int CHECK_PERSON_ON_SPOT = 1, QR_OK = 13;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView, directionsTextView;
    BarcodeDetector barcodeDetector;
    Bundle arguments;
    private String codeToDetect;
    private String shouldOpenPointDynamic;
    private int equipmentNumber, shopNumber, nomerPunkta;
    private boolean detectedOnce = false;
    private int numOfPoints;
    private long startTimeMillis;
    private String employeeLogin, employeePosition;
    private int problemsCount;

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
        nomerPunkta = arguments.getInt("Номер пункта");
        shouldOpenPointDynamic = arguments.getString("Открой PointDynamic");
        //на самом деле в адрес пункта нужно заложить полные названия цеха и линии, но пока на номерах
        numOfPoints = arguments.getInt("Количество пунктов");
        startTimeMillis = arguments.getLong("startTimeMillis");
        employeeLogin = arguments.getString("Логин пользователя");
        employeePosition = arguments.getString("Должность");
        problemsCount = arguments.getInt("Количество обнаруженных проблем");
        Log.i("problemsCount", Integer.toString(problemsCount));
        surfaceView = findViewById(R.id.camerapreview);
        textView = findViewById(R.id.textView);
        directionsTextView = findViewById(R.id.directionsTextView);
        directionsTextView.setVisibility(View.INVISIBLE);
        //codeToDetect, возьми данные из таблицы "QRCodes"
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
        Log.i("qrScanCameraON", "Даю коллбэк surfaceView getholder");
        //проблема здесь
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.i("qrScanCameraON", "surfaceCreated ретурн делаю");
                    return;
                }
                try {
                    cameraSource.start(holder);
                    Log.i("qrScanCameraON", "try cameraSource.start успешен");
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
                                if(!detectedOnce) {
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
                                            intent.putExtra("Количество обнаруженных проблем", problemsCount);
                                            Log.i("Кол-во проблем", String.valueOf(problemsCount));
                                            intent.putExtra("Должность", employeePosition);
                                            startActivity(intent);
                                        } else if (shouldOpenPointDynamic.equals("нет")) {
                                            Bundle arguments = getIntent().getExtras();
                                            String problemKey = arguments.getString("ID проблемы в таблице Problems");
                                            DatabaseReference problemRef = FirebaseDatabase.getInstance().getReference().child("Problems/" + problemKey);
                                            problemRef.child("solved").setValue(true);
                                            problemRef.child("solved_by").setValue(employeeLogin);
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
                        }
                    });
                }
            }
        });
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

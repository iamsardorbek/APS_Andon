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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;

public class QRScanner extends AppCompatActivity {
    public static final int CHECK_PERSON_ON_SPOT = 1, QR_OK = 13;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView, directionsTextView;
    BarcodeDetector barcodeDetector;
    Bundle arguments;
    private String codeToDetect, addressPunkta, shouldOpenPointDynamic;
    private int equipmentNumber, shopNumber, nomerPunkta;
    private boolean detectedOnce = false;
    private int numOfPoints;
    private long startTimeMillis;
    private String employeeLogin;
    private int problemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_q_r_scanner);
        initInstances();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480).build();
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
        addressPunkta = "Цех №" + shopNumber + "\nЛиния №"
                + equipmentNumber + "\nПункт №" + nomerPunkta;
        numOfPoints = arguments.getInt("Количество пунктов");
        startTimeMillis = arguments.getLong("startTimeMillis");
        employeeLogin = arguments.getString("Логин пользователя");
        problemsCount = arguments.getInt("Количество обнаруженных проблем");
        //ИНИЦИАЛИЗИРОВАТЬ ПЕРЕМЕННУЮ! codeToDetect, возьми данные из таблицы "QRCodes"
        //Используй аргументы переданные в arguments
        codeToDetect = arguments.getString("Код пункта"); //getEncodedString
        //arguments.getString("Код пункта");
        surfaceView = findViewById(R.id.camerapreview);
        textView = findViewById(R.id.textView);
        directionsTextView = findViewById(R.id.directionsTextView);
        directionsTextView.setText("Подойдите к\n" + addressPunkta);
    }

    private void requestCameraPermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                qrScanCameraON();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast toast = Toast.makeText(getApplicationContext(), "Разрешите использование камеры", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
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
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size()!=0)
                {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            {
                                String codeFromQR = qrCodes.valueAt(0).displayValue;
                                if(areDetectedAndPassedCodesSame(codeFromQR, codeToDetect) && detectedOnce)
                                {
                                    vibration(600);
                                    Toast.makeText(getApplicationContext(), "ВЫ НА МЕСТЕ!", Toast.LENGTH_SHORT).show();
                                    textView.setText(qrCodes.valueAt(0).displayValue);

                                    //создаем интент, в него заносим код успешного распознования
                                    //после окончания finish(), интент сам найдет родительский активити и отдаст результат в
                                    //onActivityResult
                                    if(shouldOpenPointDynamic.equals("да"))
                                    { //когда идет переход Verification->QR->PointDynamic
                                        Intent intent = new Intent(getApplicationContext(), QuestPointDynamic.class);
                                        intent.putExtra("Номер пункта", nomerPunkta);
                                        intent.putExtra("Количество пунктов", numOfPoints);
                                        intent.putExtra("startTimeMillis", startTimeMillis);
                                        intent.putExtra("Логин пользователя", employeeLogin);
                                        intent.putExtra("Количество обнаруженных проблем", problemsCount);

                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(); //intent говорят можно и убрать вообще, и просто возвратить setResult(QR_OK)
                                        setResult(QR_OK, intent); //Если нету extra данных
                                    }
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Неверный код", Toast.LENGTH_SHORT).show();
                                    textView.setText(qrCodes.valueAt(0).displayValue
                                            + "\nВы не в том месте!\nИдите в пункт, указанный выше.");
                                    //textView.setText("Вы не в том месте!\nИдите на пункт" + addressPunkta);
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

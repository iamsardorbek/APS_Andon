package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

//--------ЗДЕСЬ ПОКАЗЫВАЕТСЯ ЧЕРТЕЖ ЛИНИИ И 1-Й ПУНКТ, КУДА ЮЗЕР ДОЛЖЕН НАПРАВИТЬСЯ---------//
//--------ОТКРЫВАЕТСЯ ПРИ НАЖАТИИ НА ЭЛЕМЕНТ EXPANDABLE LIST VIEW (ЛИНИЮ) В QUEST MAIN ACTIVITY---------//
public class QuestMachineLayoutActivity extends AppCompatActivity implements View.OnTouchListener {
    Button qrScan;
    ImageView equipmentLayout;
    private int shopNo, equipmentNo;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_layout);
        initInstances();
        final DatabaseReference equipmentLayoutRef = FirebaseDatabase.getInstance().getReference("Shops/" + shopNo + "/Equipment_lines/" + equipmentNo + "/layout_start_pic_name");
        equipmentLayoutRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot equipmentLayoutSnap) {
                //setTitle()
                try {
                    String equipmentLayoutPicName = Objects.requireNonNull(equipmentLayoutSnap.getValue()).toString(); //получи название картинки содержащей чертеж оборудования
                    StorageReference equipmentLayoutFolder = FirebaseStorage.getInstance().getReference( "equipment_layouts"); //загрузи ту картинку из Storage - > equipment_layouts
                    StorageReference equipmentLayoutPic = equipmentLayoutFolder.child(equipmentLayoutPicName);
                    Glide.with(getApplicationContext()).load(equipmentLayoutPic).into(equipmentLayout); //поставь картинку в ImageView
                }catch (NullPointerException npe) {
                    ExceptionProcessing.processException(npe);
                    TextView exceptionTextView = findViewById(R.id.exception_text); //сообщение, что скачать картинку не удалось
                    exceptionTextView.setVisibility(View.VISIBLE);
                    equipmentLayout.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances() {
        qrScan = findViewById(R.id.qr_scan);
        equipmentLayout = findViewById(R.id.equipment_layout);
        try {
            shopNo = Objects.requireNonNull(getIntent().getExtras()).getInt("Номер цеха");
            equipmentNo = getIntent().getExtras().getInt("Номер линии");
        } catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe);
            TextView exceptionTextView = findViewById(R.id.exception_text); //сообщение, что скачать картинку не удалось
            exceptionTextView.setVisibility(View.VISIBLE);
            equipmentLayout.setVisibility(View.GONE);
        }
        qrScan.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //дейсвтие при нажатиях на кнопку (отсканировать QR код)
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                qrScan.setBackgroundResource(R.drawable.edit_red_accent_pressed); //эффект нажатия
                break;
            case MotionEvent.ACTION_UP: //когда уже отпустил, октрой qr
                //запуск QR сканера отсканировав  qr код 1-го пункта любой линии

                qrScan.setBackgroundResource(R.drawable.edit_red_accent);
                Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                intent.putExtra("Номер цеха", shopNo);
                intent.putExtra("Номер линии", equipmentNo);
                int INITIAL_POINT_NUMBER_FOR_QR = 1;
                intent.putExtra(getString(R.string.nomer_punkta_textview_text), INITIAL_POINT_NUMBER_FOR_QR);
                intent.putExtra("Действие", "Открой PointDynamic");
                intent.putExtra(getString(R.string.nomer_punkta_textview_text), 1);
                int problemsCount = 0;
                intent.putExtra("Количество обнаруженных проблем", problemsCount);
                startActivity(intent);
                break;
        }
        return false;
    }
}

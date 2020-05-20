package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//--------ЗДЕСЬ ПОКАЗЫВАЕТСЯ ЧЕРТЕЖ ЛИНИИ И 1-Й УЧАСТОК, КУДА ЮЗЕР ДОЛЖЕН НАПРАВИТЬСЯ---------//
//--------ОТКРЫВАЕТСЯ ПРИ НАЖАТИИ НА ЭЛЕМЕНТ EXPANDABLE LIST VIEW (ЛИНИЮ) В QUEST MAIN ACTIVITY---------//
public class MachineLayoutActivity extends AppCompatActivity {
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
                String equipmentLayoutPicName = equipmentLayoutSnap.getValue().toString(); //получи название картинки содержащей чертеж оборудования
                StorageReference equipmentLayoutFolder = FirebaseStorage.getInstance().getReference( "equipment_layouts"); //загрузи ту картинку из Storage - > equipment_layouts
                StorageReference equipmentLayoutPic = equipmentLayoutFolder.child(equipmentLayoutPicName);
                Glide.with(getApplicationContext()).load(equipmentLayoutPic).into(equipmentLayout); //поставь картинку в ImageView
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void initInstances() {
        qrScan = findViewById(R.id.qr_scan);
        equipmentLayout = findViewById(R.id.equipment_layout);
        shopNo = getIntent().getExtras().getInt("Номер цеха");
        equipmentNo = getIntent().getExtras().getInt("Номер линии");

        qrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                intent.putExtra("Номер цеха", shopNo);
                intent.putExtra("Номер линии", equipmentNo);
                int INITIAL_POINT_NUMBER_FOR_QR = 1;
                intent.putExtra("Номер участка", INITIAL_POINT_NUMBER_FOR_QR);
                intent.putExtra("Открой PointDynamic", "да");
                String login = getIntent().getExtras().getString("Логин пользователя");
                intent.putExtra("Логин пользователя", login); //передавать логин пользователя взятый из Firebase
                int problemsCount = 0;
                intent.putExtra("Количество обнаруженных проблем", problemsCount);
                String position = getIntent().getExtras().getString("Должность");
                intent.putExtra("Должность", position);
                startActivity(intent);
            }
        });
    }
}

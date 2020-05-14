package com.akfa.apsproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class RepairerSeparateProblem extends AppCompatActivity {
    Button problemSolved;
    ImageView problemPic;
    Button.OnClickListener clickListener;
    private String IDOfTheProblem;
    final int REQUEST_CODE_PHOTO = 1;

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
        problemsRef = FirebaseDatabase.getInstance().getReference().child("Maintenance_problems");
        IDOfTheProblem = getIntent().getExtras().getString("ID проблемы в таблице Maintenance_problems");
        employeeLogin = getIntent().getExtras().getString("Логин пользователя");
        thisProblemRef = problemsRef.child(IDOfTheProblem);
        thisProblemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot problemDataSnapshot) {
                MaintenanceProblem problem = problemDataSnapshot.getValue(MaintenanceProblem.class);
                TextView shopNameTextView = findViewById(R.id.shop_name);
                TextView equipmentNameTextView = findViewById(R.id.equipment_name);
                TextView stationNo = findViewById(R.id.station_no);
                TextView pointNo = findViewById(R.id.point_no);
                TextView employeeLogin = findViewById(R.id.employee_login);
                TextView date = findViewById(R.id.date);
                shopNameTextView.setText(problem.getShop_name());
                equipmentNameTextView.setText(problem.getEquipment_line_name());
                stationNo.setText(Integer.toString(problem.getStation_no()));
                pointNo.setText(Integer.toString(problem.getPoint_no()));
                employeeLogin.setText(problem.getDetected_by_employee());
                date.setText(problem.getDate() + " " + problem.getTime());
                nomerPunkta = problem.getStation_no();
                equipmentName = problem.getEquipment_line_name();
                equipmentNo = problem.getEquipment_line_no();
                shopNo = problem.getShop_no();
                shopName = problem.getShop_name();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        problemSolved = findViewById(R.id.problemSolved);
        problemPic = findViewById(R.id.problemPic);
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("problem_pictures");
        Log.i("IDOfTheProblem", IDOfTheProblem);
        StorageReference singlePicRef = mStorageRef.child(IDOfTheProblem + ".jpg");
        Glide.with(getApplicationContext()).load(singlePicRef).into(problemPic); //load the pic from FB top imageview
        clickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId())
                {
                    case R.id.problemSolved:
                        qrStart(nomerPunkta, equipmentNo, shopNo);
                        finish();
                }
            }
        };
        problemSolved.setOnClickListener(clickListener);
    }

    private void qrStart(int nomerPunkta, int equipmentNo, int shopNo) {
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopNo);
        intent.putExtra("Номер линии", equipmentNo);
        intent.putExtra("Номер пункта", nomerPunkta);
        intent.putExtra("Открой PointDynamic", "ремонтник");
        intent.putExtra("Логин пользователя", employeeLogin);
        intent.putExtra("ID проблемы в таблице Maintenance_problems", IDOfTheProblem);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}

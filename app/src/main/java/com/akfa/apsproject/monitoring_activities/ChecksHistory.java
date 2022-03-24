package com.akfa.apsproject.monitoring_activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.akfa.apsproject.R;
import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.InitNavigationBar;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChecksHistory extends AppCompatActivity {
    private final int ID_CHECKS = 5000;
    private final int ID_CHECKED_EQUIPMENT = 5000, ID_NOT_CHECKED_EQUIPMENT = 6000;
    LinearLayout linearLayout;
    View.OnClickListener textviewClickListener;
    ActionBarDrawerToggle toggle;
    final List<MaintenanceCheck> maintenanceChecks = new ArrayList<>();
    private int totalChecksCount = 0, todayNotCheckedEquipmentCount = 0, todayChecksCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checks_history);
        setTitle(getString(R.string.loading_data)); //если нет проблем, надо сделать: нету проблем
        initInstances();
        toggle = InitNavigationBar.setUpNavBar(ChecksHistory.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.checks_history, R.id.activity_checks_history);
    }

    private void initInstances() {
        linearLayout = findViewById(R.id.linearLayout);
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View textView) {
                int checkIndex = textView.getId() - ID_CHECKS;
                Intent openSeparateCheckDetails = new Intent(getApplicationContext(), SeparateCheckDetails.class);
                openSeparateCheckDetails.putExtra("Номер цеха", maintenanceChecks.get(checkIndex).getShop_no());
                openSeparateCheckDetails.putExtra("Номер линии", maintenanceChecks.get(checkIndex).getEquipment_no());
                openSeparateCheckDetails.putExtra("Дата", maintenanceChecks.get(checkIndex).getDate_finished());
                startActivity(openSeparateCheckDetails);
            }
        };
        addEquipmentChecksData();
    }

    private void addEquipmentChecksData()
    {
        DatabaseReference maintenanceChecksRef = FirebaseDatabase.getInstance().getReference("Maintenance_checks");
        maintenanceChecksRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override public void onDataChange(@NonNull DataSnapshot maintenanceChecksSnap) {
                linearLayout.removeAllViews();
                maintenanceChecks.clear();
                totalChecksCount = 0;
                setTitle(getString(R.string.checks_history_submenu));


                for(final DataSnapshot oneDaySnap : maintenanceChecksSnap.getChildren())
                {
                    try {
                    String date = oneDaySnap.getKey();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                    Date d = null;
                    try { d = sdf.parse(date); } catch (ParseException e) { ExceptionProcessing.processException(e); }
                    sdf.applyPattern("dd.MM.yyyy");
                    String dateStorageFormat = sdf.format(d);

                    final TextView dateTextView = new TextView(getApplicationContext());
                    dateTextView.setText(dateStorageFormat);
                    dateTextView.setPadding(25, 25, 25, 25);
                    dateTextView.setTextColor(Color.parseColor(getString(R.color.text)));
                    dateTextView.setTextSize(16);
                    dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    dateTextView.setPaintFlags(dateTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(20, 20, 20, 8);
                    dateTextView.setLayoutParams(params);
                    dateTextView.setClickable(false);

                    DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref));
                    shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot shopsSnap) {
                            linearLayout.addView(dateTextView);
                            populateTheUIListWithTextview(oneDaySnap, shopsSnap);
//                            if(shopsSnap.exists()) {
//                            }
//                            else {
//                                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref));
//                                shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override public void onDataChange(@NonNull DataSnapshot shopsSnap) {
//                                        populateTheUIListWithTextview(oneDaySnap, shopsSnap);
//                                    }
//                                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
//                                });
//                            }
                        }

                        @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });


//                    for(DataSnapshot singleMaintenanceCheck : oneDaySnap.getChildren()) {
//                        MaintenanceCheck maintenanceCheck = singleMaintenanceCheck.getValue(MaintenanceCheck.class);
//                        maintenanceChecks.add(maintenanceCheck);
//
//                        //инициализация TEXTVIEW
//                        String equipmentInfo = maintenanceCheck.getShop_name() + "\n" + maintenanceCheck.getEquipment_name() + "\nПроверено: " + maintenanceCheck.getChecked_by() + "\nКоличество проблем: " +
//                                maintenanceCheck.getNum_of_detected_problems() + "\n" + maintenanceCheck.getTime_finished() + "\nПотраченное время: " + maintenanceCheck.getDuration();
//                        TextView equipmentInfoTextView = new TextView(getApplicationContext());
//                        equipmentInfoTextView.setText(equipmentInfo);
//                        equipmentInfoTextView.setPadding(25, 25, 25, 25);
//                        equipmentInfoTextView.setId(ID_CHECKS + totalChecksCount);
//                        equipmentInfoTextView.setTextColor(Color.parseColor(getString(R.color.text)));
//                        equipmentInfoTextView.setTextSize(14);
//                        params.setMargins(20, 25, 20, 25);
//                        equipmentInfoTextView.setLayoutParams(params);
//                        equipmentInfoTextView.setClickable(true);
//                        equipmentInfoTextView.setBackgroundResource(R.drawable.list_group_layout);
//
//                        equipmentInfoTextView.setOnClickListener(textviewClickListener);
//                        //добавить textview в layout
//                        linearLayout.addView(equipmentInfoTextView);
//                        totalChecksCount++; //итерируй для уникализации айдишек textview и обращения к лист элементам
//                    }
                        } catch (Exception e) {
                        ExceptionProcessing.processException(e);}

                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint("ResourceType")
    private void populateTheUIListWithTextview(DataSnapshot oneDaySnap, DataSnapshot shopsSnap)
    {
        for(DataSnapshot shopSnap : shopsSnap.getChildren())
        {
            int shopNo = Integer.parseInt(Objects.requireNonNull(shopSnap.getKey()));
            for(DataSnapshot equipmentSnap : shopSnap.child("Equipment_lines").getChildren())
            {
                int equipmentNo = Integer.parseInt(Objects.requireNonNull(equipmentSnap.getKey()));
                boolean thisEquipmentChecked = false;
                for(DataSnapshot singleMaintenanceCheckSnap : oneDaySnap.getChildren())
                {
                    MaintenanceCheck maintenanceCheck = singleMaintenanceCheckSnap.getValue(MaintenanceCheck.class);
                    if(Objects.requireNonNull(maintenanceCheck).getEquipment_no() == equipmentNo && maintenanceCheck.getShop_no() == shopNo)
                    {
                        thisEquipmentChecked = true;
                        //инициализация TEXTVIEW
                        String checkInfo = shopNo + "\n" + equipmentNo + "\n" + getString(R.string.checked_by) + maintenanceCheck.getChecked_by() + getString(R.string.num_of_problems) +
                                maintenanceCheck.getNum_of_detected_problems() + getString(R.string.end_time) + maintenanceCheck.getTime_finished() + "\n" + getString(R.string.checkingDuration) + maintenanceCheck.getDuration();
                        TextView equipmentInfoTextView = new TextView(getApplicationContext());
                        equipmentInfoTextView.setText(checkInfo);
                        PointDataRetriever.setTextOfACheck(getBaseContext(), equipmentInfoTextView, shopNo, equipmentNo, maintenanceCheck.getChecked_by(), maintenanceCheck.getNum_of_detected_problems(), maintenanceCheck.getTime_finished(), maintenanceCheck.getDuration(), PointDataRetriever.EQUIPMENT_CHECKED);
                        equipmentInfoTextView.setPadding(25, 25, 25, 25);
                        equipmentInfoTextView.setId(ID_CHECKED_EQUIPMENT + todayChecksCount);
                        equipmentInfoTextView.setTextColor(Color.BLACK);
                        equipmentInfoTextView.setTextSize(13);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(20, 25, 20, 25);
                        equipmentInfoTextView.setLayoutParams(params);
                        equipmentInfoTextView.setClickable(true);
                        equipmentInfoTextView.setBackgroundResource(R.drawable.checked_equipment_background);
                        equipmentInfoTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.accept_button, 0);

                        equipmentInfoTextView.setOnClickListener(textviewClickListener);
                        //добавить textview в layout
                        maintenanceChecks.add(maintenanceCheck);
                        linearLayout.addView(equipmentInfoTextView);
                        todayChecksCount++; //итерируй для уникализации айдишек textview и обращения к лист элементам
                        break;
                    }
                }
                if(!thisEquipmentChecked)
                {
//                                    problemIDs.add(problemDataSnapshot.getKey()); //добавь айди данной проблемы в лист

                    //инициализация TEXTVIEW
                    String equipmentInfo = shopNo + "\n" + equipmentNo;
                    TextView equipmentInfoTextView;
                    equipmentInfoTextView = new TextView(getApplicationContext());
                    equipmentInfoTextView.setText(equipmentInfo);
                    PointDataRetriever.setTextOfACheck(getBaseContext(), equipmentInfoTextView, shopNo, equipmentNo, "", -1, "", "", PointDataRetriever.EQUIPMENT_NOT_CHECKED);
                    equipmentInfoTextView.setPadding(25, 25, 25, 25);
                    equipmentInfoTextView.setId(ID_NOT_CHECKED_EQUIPMENT + todayNotCheckedEquipmentCount);
                    equipmentInfoTextView.setTextColor(Color.parseColor(getString(R.color.text)));
                    equipmentInfoTextView.setTextSize(13);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(20, 25, 20, 25);
                    equipmentInfoTextView.setLayoutParams(params);
                    equipmentInfoTextView.setClickable(false);
                    equipmentInfoTextView.setBackgroundResource(R.drawable.unchecked_equipment_background);
                    equipmentInfoTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.decline_button, 0);

                    //добавить textview в layout
                    linearLayout.addView(equipmentInfoTextView);
                    todayNotCheckedEquipmentCount++; //итерируй для уникализации айдишек textview и обращения к лист элементам
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}

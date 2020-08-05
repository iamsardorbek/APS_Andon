package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TodayChecks extends AppCompatActivity {
    private final int ID_CHECKED_EQUIPMENT = 5000, ID_NOT_CHECKED_EQUIPMENT = 6000;
    LinearLayout linearLayout;
    View.OnClickListener textviewClickListener;
    ActionBarDrawerToggle toggle;
    private int todayChecksCount = 0, todayNotCheckedEquipmentCount = 0;
    final List<MaintenanceCheck> maintenanceChecks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_checks);
        setTitle(getString(R.string.loading_data)); //если нет проблем, надо сделать: нету проблем
        initInstances();
        toggle = InitNavigationBar.setUpNavBar(TodayChecks.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()),  R.id.today_checks, R.id.activity_today_checks);

    }

    private void initInstances() {
        linearLayout = findViewById(R.id.linearLayout);
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View textView) {
                int checkIndex = textView.getId() - ID_CHECKED_EQUIPMENT;
                Intent openSeparateCheckDetails = new Intent(getApplicationContext(), SeparateCheckDetails.class);
                openSeparateCheckDetails.putExtra("Название цеха", maintenanceChecks.get(checkIndex).getShop_name());
                openSeparateCheckDetails.putExtra("Название линии", maintenanceChecks.get(checkIndex).getEquipment_name());
                openSeparateCheckDetails.putExtra("Дата", maintenanceChecks.get(checkIndex).getDate_finished());

                startActivity(openSeparateCheckDetails);
            }
        };
        addEquipmentChecksData();
    }

    private void addEquipmentChecksData()
    {
        final String date;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        date = sdf.format(new Date());
        DatabaseReference todayMaintenanceChecksRef = FirebaseDatabase.getInstance().getReference("Maintenance_checks/" + date);
        todayMaintenanceChecksRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull final DataSnapshot todayMaintenanceChecksSnap) {
                linearLayout.removeAllViews();
                maintenanceChecks.clear();
                todayChecksCount = 0;
                todayNotCheckedEquipmentCount = 0;
                setTitle(getString(R.string.today_checks_submenu));

                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference("Shops");
                shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("ResourceType") @Override public void onDataChange(@NonNull DataSnapshot shopsSnap) {

                        for(DataSnapshot shopSnap : shopsSnap.getChildren())
                        {
                            try { //если сущ проблема, незаметно юзеру будет сообщено в crashlytics
                                String shopName = Objects.requireNonNull(shopSnap.child("shop_name").getValue()).toString();
                                for(DataSnapshot equipmentSnap : shopSnap.child("Equipment_lines").getChildren())
                                {
                                    String equipmentName = Objects.requireNonNull(equipmentSnap.child("equipment_name").getValue()).toString();
                                    boolean thisEquipmentChecked = false;
                                    for(DataSnapshot singleMaintenanceCheckSnap : todayMaintenanceChecksSnap.getChildren())
                                    {
                                        MaintenanceCheck maintenanceCheck = singleMaintenanceCheckSnap.getValue(MaintenanceCheck.class);
                                        if(equipmentName.equals(Objects.requireNonNull(maintenanceCheck).getEquipment_name()) && maintenanceCheck.getShop_name().equals(shopName))
                                        {
                                            thisEquipmentChecked = true;
                                            //инициализация TEXTVIEW
                                            String equipmentInfo = shopName + "\n" + equipmentName + getString(R.string.checked_by) + maintenanceCheck.getChecked_by() + getString(R.string.num_of_problems) +
                                                    maintenanceCheck.getNum_of_detected_problems() + getString(R.string.end_time) + maintenanceCheck.getTime_finished() + getString(R.string.time_spent) + maintenanceCheck.getDuration();
                                            TextView equipmentInfoTextView = new TextView(getApplicationContext());
                                            equipmentInfoTextView.setText(equipmentInfo);
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
                                        String equipmentInfo = shopName + "\n" + equipmentName;
                                        TextView equipmentInfoTextView;
                                        equipmentInfoTextView = new TextView(getApplicationContext());
                                        equipmentInfoTextView.setText(equipmentInfo);
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
                            } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}

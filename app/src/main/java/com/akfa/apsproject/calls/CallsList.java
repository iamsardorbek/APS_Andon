package com.akfa.apsproject.calls;

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

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.InitNavigationBar;
import com.akfa.apsproject.QRScanner;
import com.akfa.apsproject.R;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

//----------ПОКАЗЫВАЕТ, АКТИВНЫЕ ВЫЗОВЫ ДАННОГО ПОЛЬЗОВАТЕЛЯ ДРУГИМИ СПЕЦИАЛИСТАМИ--------//

public class CallsList extends AppCompatActivity {
    private static final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;


    private ActionBarDrawerToggle toggle;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_or_master_calls_list);
        setTitle(getString(R.string.no_calls));
        linearLayout = findViewById(R.id.linearLayout);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull final DataSnapshot userSnap) {
                DatabaseReference callsRef = FirebaseDatabase.getInstance().getReference("Calls");
                callsRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("ResourceType")
                    @Override public void onDataChange(@NonNull DataSnapshot callsSnap) {
                        linearLayout.removeAllViews(); //для обновления данных удали все результаты предыдущего поиска
                        problemCount = 0;
                        if(callsSnap.getValue() == null) //если ветка Calls пуста/не сущ -> дай знать, что все проблемы уже решены
                            setTitle(getString(R.string.no_calls));
                        else {
                            setTitle(getString(R.string.no_calls));
                            for (final DataSnapshot singleCallSnap : callsSnap.getChildren()) {
                                try{
                                    final Call thisCall = singleCallSnap.getValue(Call.class);

                                    String whoIsNeededPosition = Objects.requireNonNull(thisCall).getWho_is_needed_position();
                                    String callEquipmentName = thisCall.getEquipment_name();
                                    String callShopName = thisCall.getShop_name();
                                    boolean callIsComplete = thisCall.getComplete();

                                    if (whoIsNeededPosition.equals(UserData.position) && !callIsComplete) {
                                        //условия query: должность вызванного и этого юзера соотвествуют, это цех, за который ответственен данный мастер/оператор,
                                        // а также этот вызов еще не был удовлетворен (тобиш вызываемый еще не пришел)
                                        boolean operatorIsResponsibleForThisEquipment = false, masterIsResponsibleForThisShop = false;
                                        final String equipmentName;
                                        final String shopName;
                                        if (UserData.position.equals("operator")) //если оператор, нам нужно проверить, это линия, за которую он отвечает?
                                        //если да - добавь этот вызов в список, если нет - не добавляй/не показывай этот вызов оператору
                                        {
                                            shopName = userSnap.child("shop_name").getValue().toString();
                                            equipmentName = userSnap.child("equipment_name").getValue().toString();
                                            if (callEquipmentName.equals(equipmentName) && callShopName.equals(shopName) ) { //если оператор отвесвенен за данную линию
                                                operatorIsResponsibleForThisEquipment = true;
                                            }
                                        }
                                        else if (UserData.position.equals("master")) //если мастер, нам нужно проверить, это цех, за который он отвечает?
                                        //если да - добавь этот вызов в список, если нет - не добавляй/не показывай этот вызов мастеру
                                        {
                                            final String shopNameRepairer = userSnap.child("shop_name").getValue().toString();
                                            if (callShopName.equals(shopNameRepairer)) { //если мастер отвесвенен за данный цех
                                                masterIsResponsibleForThisShop = true;
                                            }
                                        }

                                        if ((operatorIsResponsibleForThisEquipment && UserData.position.equals("operator")) || (UserData.position.equals("master") && masterIsResponsibleForThisShop)
                                                || UserData.position.equals("repair")) { //если оператор ответсвенен за эту лини. или это цех мастера или это просто ремонтник
                                            //----СОЗДАНИЕ TEXTVIEW, ВНЕСЕНИЕ ДАННЫХ В НЕГО И ИНИЦИАЛИЗАЦИЯ ПАРАМЕТРОВ----//
                                            setTitle(getString(R.string.waiting_for_you_in_the_following_places));
                                            TextView callInfo;
                                            callInfo = new TextView(getApplicationContext());
                                            //данные об этой проблеме запишем в строку callInfoFromDB
                                            String callInfoFromDB = getString(R.string.shop) + thisCall.getShop_name() + "\n" + getString(R.string.equipmentLine) + thisCall.getEquipment_name() + "\n" + getString(R.string.nomer_point_textview) + thisCall.getPoint_no() + "\n"
                                                    + getString(R.string.date_time_of_call) + thisCall.getDate_called() + " " + thisCall.getTime_called() + getString(R.string.called_by) + thisCall.getCalled_by();
                                            callInfo.setText(callInfoFromDB);
                                            PointDataRetriever.setTextOfCallsList(getBaseContext(), callInfo, thisCall.getShop_no(), thisCall.getEquipment_no(), thisCall.getPoint_no(), thisCall.getDate_called() + " " + thisCall.getTime_called(), thisCall.getCalled_by());
                                            callInfo.setPadding(25, 25, 25, 25);
                                            callInfo.setId(ID_TEXTVIEWS + problemCount);
                                            callInfo.setTextColor(Color.parseColor(getString(R.color.text)));
                                            callInfo.setTextSize(13);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            params.setMargins(20, 25, 20, 25);

                                            callInfo.setLayoutParams(params);
                                            callInfo.setClickable(true);
                                            callInfo.setBackgroundResource(R.drawable.list_group_layout);

                                        callInfo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                                                openQR.putExtra("Действие", "реагирование на вызов"); //описание действия для QR сканера
                                                openQR.putExtra("Номер цеха", thisCall.getShop_no());
                                                openQR.putExtra("Номер линии", thisCall.getEquipment_no());
                                                openQR.putExtra(getString(R.string.nomer_punkta_textview_text), thisCall.getPoint_no());
                                                openQR.putExtra("Код вызова", singleCallSnap.getKey());
                                                startActivity(openQR);
                                            }
                                        });
                                        //----КОНЕЦ ИНИЦИАЛИЗАЦИИ TEXTVIEW ДЛЯ СРОЧНОЙ ПРОБЛЕМЫ----//
                                        linearLayout.addView(callInfo); //добавить textview в linearLayout
                                        problemCount++; //итерировать для уникализации айдишек textviews

                                        }
                                    }
                                } catch (NullPointerException npe) {
                                    ExceptionProcessing.processException(npe);
                                }

                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

            }

            @Override public void onCancelled (@NonNull DatabaseError databaseError){ }
        });

        toggle = InitNavigationBar.setUpNavBar(CallsList.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.calls, R.id.activity_operator_or_master_calls_list); //инициализация navigation bar
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//функция нужная, чтобы нав бар работал
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

}

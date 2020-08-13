package com.akfa.apsproject.checking_equipment_maintenance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akfa.apsproject.calls.Call;
import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.QRScanner;
import com.akfa.apsproject.R;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.akfa.apsproject.general_data_classes.UserData;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

//--------ПОКАЗЫВАЕТ БОЛЬШЕ ДЕТАЛЕЙ ПРО ТО ПРОБЛЕМУ, ЧЕМ В REPAIRERS PROBLEM LIST. ПРИ НАЖАТИИ НА КНОПКУ "ПРОБЛЕМА РЕШЕНА"-------
//--------ОТКРЫВАЕТ QR И ДАЕТ ВОЗМОЖНОСТЬ ПРИКРЕПИТЬ ФОТКУ РЕШЕНИЯ---------//
public class RepairerSeparateProblem extends AppCompatActivity implements View.OnTouchListener {
    Button problemSolved, callOperator;
    ImageView problemPic;

    private String IDOfTheProblem;
    private int nomerPunkta, equipmentNo, shopNo;
    private MaintenanceProblem problem;
    private boolean callForOperatorOpen = false;

    DatabaseReference problemsRef, thisProblemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairer_activity_separate_problem);
        setTitle(getString(R.string.separate_problem_title));
        try {
            initInstances();
        }
        catch (NullPointerException npe)
        {
            ExceptionProcessing.processException(npe, getString(R.string.the_prob_was_solved_or_incomplete_data), getApplicationContext(), RepairerSeparateProblem.this);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances() {
        problemsRef = getInstance().getReference().child("Maintenance_problems"); //ссылка к ТО пробам
        IDOfTheProblem = Objects.requireNonNull(getIntent().getExtras()).getString("ID проблемы в таблице Maintenance_problems"); //айди именно текущей проблемы


        thisProblemRef = problemsRef.child(Objects.requireNonNull(IDOfTheProblem));
        thisProblemRef.addListenerForSingleValueEvent(new ValueEventListener() { //единожды загрузим данные про текущ пробу
            @SuppressLint("SetTextI18n")
            @Override public void onDataChange(@NonNull DataSnapshot problemDataSnapshot) {
                problem = problemDataSnapshot.getValue(MaintenanceProblem.class); //считай данные пробы в объект
                //на месте иниц views и задай их текст
                TextView shopNameTextView = findViewById(R.id.shop_name);
                TextView equipmentNameTextView = findViewById(R.id.equipment_name);
                TextView pointName = findViewById(R.id.point_no);
                TextView subpointDescription = findViewById(R.id.subpoint_no);
                TextView employeeLogin = findViewById(R.id.employee_login);
                TextView date = findViewById(R.id.date);
                shopNameTextView.setText(problem.getShop_name());
                equipmentNameTextView.setText(problem.getEquipment_line_name());
                try {
                    pointName.setText(problem.getPoint_name());
                    subpointDescription.setText(problem.getSubpoint_description());
                }
                catch (NullPointerException npe)
                {
                    ExceptionProcessing.processException(npe);
                    pointName.setText(problem.getPoint_no());
                    subpointDescription.setText(Integer.toString(problem.getSubpoint_no()));
                }
                employeeLogin.setText(problem.getDetected_by_employee());
                date.setText(problem.getDate() + " " + problem.getTime());
                PointDataRetriever.fillInPointDataToTextViews(getBaseContext(), shopNameTextView, equipmentNameTextView, pointName, subpointDescription, problem.getShop_no(), problem.getEquipment_line_no(), problem.getPoint_no(), problem.getSubpoint_no());

                //переменные для передачи в QR Scanner
                nomerPunkta = problem.getPoint_no();
                equipmentNo = problem.getEquipment_line_no();
                shopNo = problem.getShop_no();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        DatabaseReference callsRef = getInstance().getReference("Calls");
        callsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot callsSnap) {
                for(DataSnapshot singleCallSnap : callsSnap.getChildren()) {
                    if(singleCallSnap.child("problem_key").exists()) { //если это вызов прямо из RepairersSeparateProblem (без разницы какой ремонтник вызвал)
                        try {
                            String problemKey = Objects.requireNonNull(singleCallSnap.child("problem_key").getValue()).toString();
                            boolean isCallComplete = (boolean) singleCallSnap.child("complete").getValue();
                            if (problemKey.equals(IDOfTheProblem) && !isCallComplete) //если оператор еще не пришел, а если он уже пришел и потенциально ушел, можно его вызвать снова
                            {
                                String thisCallKey = singleCallSnap.getKey();
                                DatabaseReference callRef = getInstance().getReference("Calls/" + thisCallKey);
                                callRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot callSnap) {
                                        boolean callSnapComplete = (boolean) callSnap.child("complete").getValue();
                                        if (callSnapComplete) { //когда оператор прибыл позже, после первоначальной инициализации этого листенера
                                            callOperator.setBackgroundResource(R.drawable.call_closed_button);
                                            callOperator.setText(R.string.operator_arrived);
                                            Resources r = getApplicationContext().getResources();
                                            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            params.setMargins(px, 2 * px, px, 0);
                                            callOperator.setLayoutParams(params);
                                            callOperator.setClickable(true); //теперь если вдруг уйдет, можно вызывать снова
                                            callForOperatorOpen = false; //вызов уже закрыт, можно вызывать снова
                                        } else {
                                            callForOperatorOpen = true;
                                            callOperator.setClickable(false); //если есть уже активный вызов оператора, еще раз вызвать его нельзя, а то БД заполнится
                                            callOperator.setBackgroundResource(R.drawable.call_opened_button);
                                            callOperator.setText(R.string.operator_is_called);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            params.setMargins(5, 40, 5, 40);
                                            params.gravity = Gravity.CENTER;
                                            callOperator.setLayoutParams(params);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }catch (NullPointerException npe){
                            ExceptionProcessing.processException(npe);
                        }
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //загрузим фотку с Storage в ImageView с помощью Glide
        problemPic = findViewById(R.id.problemPic);
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("problem_pictures");
        StorageReference singlePicRef = mStorageRef.child(IDOfTheProblem + ".jpg");
        Glide.with(getApplicationContext()).load(singlePicRef).into(problemPic); //load the pic from FB top imageview

        //действия при нажатии кнопки
        problemSolved = findViewById(R.id.problemSolved);
        problemSolved.setOnTouchListener(this);
        callOperator = findViewById(R.id.call_operator);
        callOperator.setOnTouchListener(this);
        if(UserData.position.equals("head"))
        {
            problemSolved.setVisibility(View.GONE);
            callOperator.setVisibility(View.GONE);
        }
    }

    private void qrStart(int nomerPunkta, int equipmentNo, int shopNo) { //ЗАПУСК QR SCANNER
        Intent intent = new Intent(getApplicationContext(), QRScanner.class);
        intent.putExtra("Номер цеха", shopNo);
        intent.putExtra("Номер линии", equipmentNo);
        intent.putExtra(getString(R.string.nomer_punkta_textview_text), nomerPunkta);
        intent.putExtra("Действие", "ремонтник");
        intent.putExtra("ID проблемы в таблице Maintenance_problems", IDOfTheProblem);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) { //обработка нажатия с эффектом
        if(v.isClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: //затемнена кнопка
                    v.setBackgroundResource(R.drawable.edit_red_accent_pressed);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundResource(R.drawable.edit_red_accent);
                    switch (v.getId()) {
                        case R.id.call_operator: //если вызов оператора
                            if (!callForOperatorOpen) {
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference newCallRef = dbRef.child("Calls").push(); //создать ветку нового вызова
                                //дата-время
                                final String dateCalled;
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                dateCalled = sdf.format(new Date());
                                final String timeCalled;
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                                timeCalled = sdf1.format(new Date());
                                //----считал дату и время----//
                                newCallRef.setValue(new Call(dateCalled, timeCalled, UserData.login, "operator", problem.getShop_no(), problem.getEquipment_line_no(),
                                        problem.getPoint_no(), problem.getEquipment_line_name(), problem.getShop_name(), false, IDOfTheProblem));

                                newCallRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot callSnap) {
                                        try {
                                            boolean callSnapComplete = (boolean) callSnap.child("complete").getValue();
                                            if (callSnapComplete) {
                                                callOperator.setBackgroundResource(R.drawable.call_closed_button);
                                                callOperator.setText(R.string.operator_arrived);
                                                callOperator.setClickable(true); //теперь если вдруг уйдет, можно вызывать снова
                                                callForOperatorOpen = false;
                                            } else {
                                                callForOperatorOpen = true;
                                                callOperator.setClickable(false); //если есть уже активный вызов оператора, еще раз вызвать его нельзя, а то БД заполнится
                                                callOperator.setBackgroundResource(R.drawable.call_opened_button);
                                                callOperator.setText(R.string.operator_is_called);
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                params.setMargins(5, 40, 5, 40);
                                                params.gravity = Gravity.CENTER;
                                                callOperator.setLayoutParams(params);
                                            }
                                        }
                                        catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                            break;
                        case R.id.problemSolved:
                            qrStart(nomerPunkta, equipmentNo, shopNo); //открыть QR Scanner
                            finish();
                            break;
                    }
                    break;
            }
        }
        return false;
    }

}

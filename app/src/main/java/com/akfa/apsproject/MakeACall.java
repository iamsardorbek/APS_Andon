package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MakeACall extends AppCompatActivity implements View.OnTouchListener {
    private Button callMaster, callOperator, callRepairer;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_a_call);
        try {
            initInstances();
        } catch (AssertionError ae){
            ExceptionProcessing.processException(ae, getResources().getString(R.string.program_issue_toast), getApplicationContext());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances() {
        setTitle("Вызвать сотрудника");
        Bundle args = getIntent().getExtras();
        assert args != null;
        String whoIsCalled = args.getString("Кого вызываем");
        callOperator = findViewById(R.id.call_operator);
        callRepairer = findViewById(R.id.call_repairer);
        callMaster = findViewById(R.id.call_master);
        //мастер можно вызвать оператора и ремонтника
        //сырье и отк могут вызвать мастер и оператора
        if(UserData.position.equals("master"))
        {
            callMaster.setVisibility(View.INVISIBLE);
        }
        else if(UserData.position.equals("quality") || UserData.position.equals("raw"))
        {
            callRepairer.setVisibility(View.INVISIBLE);
        }
        callOperator.setOnTouchListener(this);
        callRepairer.setOnTouchListener(this);
        callMaster.setOnTouchListener(this);
        if(whoIsCalled != null)
        { //если этот параметра не нулл, значит этот активити вызван диалогом после выбора кого вызвать
            String shopName = args.getString("Название цеха");
            String equipmentName = args.getString("Название линии");
            int pointNo = args.getInt(getString(R.string.nomer_punkta_textview_text));
            DialogFragment dialogFragment = new ConfirmCallDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Название цеха", shopName);
            bundle.putString("Название линии", equipmentName);
            bundle.putInt(getString(R.string.nomer_punkta_textview_text), pointNo);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "Подтвердить вызов");
        }

        DatabaseReference callsRef = FirebaseDatabase.getInstance().getReference("Calls");
        callsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot callsSnap) {
                for(DataSnapshot callSnap : callsSnap.getChildren())
                {
                    try {
                        Call call = callSnap.getValue(Call.class);
                        String callCalledBy = Objects.requireNonNull(call).getCalled_by();
                        boolean callComplete = call.getComplete();
                        if (!callComplete && callCalledBy.equals(UserData.login)) {
                            String callWhoIsNeededPosition = call.getWho_is_needed_position();
                            switch (callWhoIsNeededPosition) {
                                case "operator":
                                    callOperator.setBackgroundResource(R.drawable.call_opened_button);
                                    callOperator.setClickable(false);
                                    callOperator.setText(R.string.operator_is_called);
                                    break;
                                case "repair":
                                    callRepairer.setBackgroundResource(R.drawable.call_opened_button);
                                    callRepairer.setClickable(false);
                                    callRepairer.setText(R.string.repairer_is_called);
                                    break;
                                case "master":
                                    callMaster.setBackgroundResource(R.drawable.call_opened_button);
                                    callMaster.setClickable(false);
                                    callMaster.setText(R.string.master_is_called);
                                    break;
                            }
                            DatabaseReference activeCallRef = FirebaseDatabase.getInstance().getReference("Calls/" + callSnap.getKey());
                            activeCallRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot activeCallSnap) {
                                    Call call = activeCallSnap.getValue(Call.class);
                                    boolean callComplete = Objects.requireNonNull(call).getComplete();
                                    if (callComplete) {
                                        String callWhoIsNeededPosition = call.getWho_is_needed_position();
                                        switch (callWhoIsNeededPosition) {
                                            case "operator":
                                                callOperator.setBackgroundResource(R.drawable.call_closed_button);
                                                callOperator.setClickable(true);
                                                callOperator.setText(R.string.operator_arrived);
                                                break;
                                            case "repair":
                                                callRepairer.setBackgroundResource(R.drawable.call_closed_button);
                                                callRepairer.setClickable(true);
                                                callRepairer.setText(R.string.repairer_arrived);
                                                break;
                                            case "master":
                                                callMaster.setBackgroundResource(R.drawable.call_closed_button);
                                                callMaster.setClickable(true);
                                                callMaster.setText(R.string.master_arrived);
                                                break;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    } catch (NullPointerException npe) {
                        ExceptionProcessing.processException(npe);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        toggle = InitNavigationBar.setUpNavBar(MakeACall.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.make_a_call, R.id.activity_make_a_call);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                String whoIsCalled = "";
                switch (button.getId())
                {
                    case R.id.call_master:
                        whoIsCalled = "master";
                        break;
                    case R.id.call_operator:
                        whoIsCalled = "operator";

                        break;
                    case R.id.call_repairer:
                        whoIsCalled = "repair";
                        break;
                }
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                openQR.putExtra("Действие", "определи адрес"); //описание действия для QR сканера
                openQR.putExtra("Кого вызываем", whoIsCalled);
                startActivity(openQR);
                finish();
                break;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

}

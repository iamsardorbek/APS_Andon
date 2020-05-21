package com.akfa.apsproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MakeACall extends AppCompatActivity implements View.OnTouchListener {
    private Button callMaster, callOperator, callRepairer;
    private String employeePosition, employeeLogin, shopName, equipmentName, whoIsCalled;
    private int stationNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_a_call);
        initInstances();
    }

    private void initInstances() {
        Bundle args = getIntent().getExtras();
        employeeLogin = args.getString("Логин пользователя");
        employeePosition = args.getString("Должность");
        whoIsCalled = args.getString("Кого вызываем");
        callOperator = findViewById(R.id.call_operator);
        callRepairer = findViewById(R.id.call_repairer);
        callMaster = findViewById(R.id.call_master);
        if(employeeLogin.equals("master"))
        {
            callMaster.setVisibility(View.INVISIBLE);
        }
        else if(employeeLogin.equals("quality") || employeeLogin.equals("raw"))
        {
            callRepairer.setVisibility(View.INVISIBLE);
        }
        callOperator.setOnTouchListener(this);
        callRepairer.setOnTouchListener(this);
        callMaster.setOnTouchListener(this);
        if(whoIsCalled != null)
        {
            shopName = args.getString("Название цеха");
            equipmentName = args.getString("Название линии");
            stationNo = args.getInt("Номер участка");
            DialogFragment dialogFragment = new ConfirmCallDialog();
            Bundle bundle = new Bundle();
            bundle.putString("Название цеха", shopName);
            bundle.putString("Название линии", equipmentName);
            bundle.putInt("Номер участка", stationNo);
            bundle.putString("Вызываемый специалист", whoIsCalled);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "Подтвердить вызов");
        }
    }

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
                        whoIsCalled = "repairer";
                        break;
                }
                Intent openQR = new Intent(getApplicationContext(), QRScanner.class);
                openQR.putExtra("Открой PointDynamic", "определи адрес"); //описание действия для QR сканера
                openQR.putExtra("Должность", employeePosition);
                openQR.putExtra("Кого вызываем", whoIsCalled);
                openQR.putExtra("Логин пользователя", employeeLogin); //передавать логин пользователя взятый из Firebase
                startActivity(openQR);
                finish();
                break;
        }
        return false;
    }
}

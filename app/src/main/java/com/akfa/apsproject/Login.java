package com.akfa.apsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private TextView loading;
    private EditText accessCode;
    private Button checkThisPoint;
    private Spinner spinnerPults;
    private String correctAccessCode = "akfa";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initInstances();
        checkThisPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessCode.getText().toString().equals(correctAccessCode)
                        && !spinnerPults.getSelectedItem().toString().equals(getString(R.string.spinner_pults_prompt)))
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Номер пункта", spinnerPults.getSelectedItem().toString());
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Неверный код доступа или не выбран пульт", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void initInstances()
    {
        spinnerPults = findViewById(R.id.spinner_pults);
        accessCode = findViewById(R.id.access_code);
        checkThisPoint = findViewById(R.id.check_this_point);
        loading = findViewById(R.id.loading);
        //невидимы пока с базы не загрузил данные
        spinnerPults.setVisibility(View.INVISIBLE);
        accessCode.setVisibility(View.INVISIBLE);
        checkThisPoint.setVisibility(View.INVISIBLE);
        setSpinnerPults();
    }

    private void setSpinnerPults()
    {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot wholeDB) {
                ArrayList<String> pultsList = new ArrayList<String>();
                pultsList.add(getString(R.string.spinner_pults_prompt));
                for(DataSnapshot pult : wholeDB.getChildren())
                {
                    pultsList.add(pult.getKey());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, pultsList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPults.setAdapter(arrayAdapter);
                spinnerPults.setVisibility(View.VISIBLE);
                accessCode.setVisibility(View.VISIBLE);
                checkThisPoint.setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

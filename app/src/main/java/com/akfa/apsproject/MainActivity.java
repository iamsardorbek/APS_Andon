package com.akfa.apsproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int GET_NOMER_PUNKTA = 5;
    // all variables
    private Button repair_btn, quality_btn, raw_btn, master_btn, openFactoryConditionActivity;
    private TextView punktInfo;
    private ImageView settings;
    private ImageView[] andons = new ImageView[4];
    private String nomerPunkta;
    private Integer[] btn_condition = new Integer[4];
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pultRef;

    protected void crutch() {
        for (int i = 0; i < 4; i++) {
            btn_condition[i] = 0;
        }
    }


  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Bundle arguments = getIntent().getExtras();
      find_objects();
      crutch();
      if(arguments != null) {
            nomerPunkta = arguments.getString("Номер пункта");
            pultRef = database.getReference(nomerPunkta);
            punktInfo.setText("Подождите пока загрузятся данные про пульт");
            pultRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot messages) {
                    int i = 0;
                    for (DataSnapshot oneMessage : messages.getChildren()){
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String key = oneMessage.getKey();
                        long val = (long) oneMessage.getValue();
                        switch((int) val){
                            case 0:
                                andons[i].setImageResource(R.drawable.grey48_fine);
                                break;
                            case 1:
                                Glide.with(getApplicationContext()).load(R.drawable.problem).into(andons[i]);
                                break;
                            case 2:
                                andons[i].setImageResource(R.drawable.yellow48_repairing);
                                break;
                        }
                        Log.d("FB Listener",  key + " " + val);
                        i++;
                    }
                    punktInfo.setText(nomerPunkta);
                    master_btn.setVisibility(View.VISIBLE);
                    raw_btn.setVisibility(View.VISIBLE);
                    quality_btn.setVisibility(View.VISIBLE);
                    repair_btn.setVisibility(View.VISIBLE);
                    andons[0].setVisibility(View.VISIBLE);
                    andons[1].setVisibility(View.VISIBLE);
                    andons[2].setVisibility(View.VISIBLE);
                    andons[3].setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("FB read", "Failed to read value.", error.toException());
                }
            });
        }
        else
        {
            master_btn.setVisibility(View.INVISIBLE);
            raw_btn.setVisibility(View.INVISIBLE);
            quality_btn.setVisibility(View.INVISIBLE);
            repair_btn.setVisibility(View.INVISIBLE);
            andons[0].setVisibility(View.INVISIBLE);
            andons[1].setVisibility(View.INVISIBLE);
            andons[2].setVisibility(View.INVISIBLE);
            andons[3].setVisibility(View.INVISIBLE);
            punktInfo.setText("Пункт не выбран, нажмите\nна логотип чтобы выбрать.");
        }
    }





    // find all variables
    protected void find_objects(){
        repair_btn=findViewById(R.id.repair_btn);
        quality_btn=findViewById(R.id.quality_btn);
        raw_btn=findViewById(R.id.raw_btn);
        master_btn=findViewById(R.id.master_btn);
        openFactoryConditionActivity = findViewById(R.id.open_factory_condition);
        punktInfo = findViewById(R.id.punkt_info);
        settings = findViewById(R.id.settings);
        andons[0] = findViewById(R.id.repair_andon);
        andons[1] = findViewById(R.id.quality_andon);
        andons[2] = findViewById(R.id.raw_andon);
        andons[3] = findViewById(R.id.master_andon);

        // set on click listener for variables
        repair_btn.setOnClickListener(this);
        quality_btn.setOnClickListener(this);
        raw_btn.setOnClickListener(this);
        master_btn.setOnClickListener(this);
        settings.setOnClickListener(this);
        openFactoryConditionActivity.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.repair_btn:
                btn_condition[0]++;
                updateButtons();
                break;

            case R.id.quality_btn:
                btn_condition[1]++;
                updateButtons();
                break;

            case R.id.raw_btn:
                btn_condition[2]++;
                updateButtons();
                break;

            case R.id.master_btn:
                btn_condition[3]++;
                updateButtons();
                break;

            case R.id.open_factory_condition:
                startActivity(new Intent(getApplicationContext(), FactoryCondition.class));
                break;
            case R.id.settings:
                startActivityForResult(new Intent(getApplicationContext(), Login.class), GET_NOMER_PUNKTA);
                master_btn.setVisibility(View.INVISIBLE);
                raw_btn.setVisibility(View.INVISIBLE);
                quality_btn.setVisibility(View.INVISIBLE);
                repair_btn.setVisibility(View.INVISIBLE);
                andons[0].setVisibility(View.INVISIBLE);
                andons[1].setVisibility(View.INVISIBLE);
                andons[2].setVisibility(View.INVISIBLE);
                andons[3].setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_CANCELED)
        {

        }
        else if(resultCode == RESULT_OK)
        {
            Bundle args;
            if (intent != null) {
                args = intent.getExtras();

                nomerPunkta = args.getString("Номер пункта");
                //DatabaseReference
            }
        }
    }

    private void updateButtons()
    {
        // check status and drop for 0 if more than 2
        for (int ii = 0; ii <= 3; ii++)
            if ((btn_condition[ii] >= 3)){
                btn_condition[ii]=0;
            }

        // Write a message to the database
        pultRef.child("Ремонт").setValue(btn_condition[0]);
        pultRef.child("ОТК").setValue(btn_condition[1]);
        pultRef.child("Сырье").setValue(btn_condition[2]);
        pultRef.child("Мастер").setValue(btn_condition[3]);
    }
}

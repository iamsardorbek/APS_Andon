package com.akfa.apsproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class RepairersProblemsList extends AppCompatActivity {
    private final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;
    private List<String> problemIDs;
    LinearLayout linearLayout;
    View.OnClickListener textviewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairers_activity_problems_list);
        linearLayout = findViewById(R.id.linearLayout);
        problemIDs = new ArrayList<>();
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nomerProblemy = v.getId() - ID_TEXTVIEWS;
                Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                String IDOfSelectedProblem = problemIDs.get(nomerProblemy);
                intent.putExtra("ID проблемы в таблице Problems", IDOfSelectedProblem);
                startActivity(intent);
            }
        };
        addProblemsFromDatabase();
    }
    private void addProblemsFromDatabase() {
        //на самом деле нужно взять количество строк в таблице problems
        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference().child("Проблемы");
        problemsRef.orderByChild("solved").equalTo(false).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot problemDataSnapshot, @Nullable String prevChildKey) {
                Problem problem = problemDataSnapshot.getValue(Problem.class);
                problemIDs.add(problemDataSnapshot.getKey());
                String problemInfoFromDB = "Цех: " + problem.getShop() + "\nОборудование: " + problem.getEquipment_line()
                        + "\nПункт №" + problem.getPoint() + "\nПодпункт №" + problem.getSubpoint();
                TextView problemsInfo;
                problemsInfo = new TextView(getApplicationContext());
                problemsInfo.setText(problemInfoFromDB);
                problemsInfo.setId(ID_TEXTVIEWS + problemCount);
                problemsInfo.setTextColor(Color.BLACK);
                problemsInfo.setTextSize(15);
                problemsInfo.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                problemsInfo.setClickable(true);
                problemsInfo.setOnClickListener(textviewClickListener);
                linearLayout.addView(problemsInfo);
                problemCount++;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

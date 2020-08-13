package com.akfa.apsproject.checking_equipment_maintenance;

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
import com.akfa.apsproject.R;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//----------ПОКАЗЫВАЕТ СПИСОК ТО ПРОБЛЕМ НА ЗАВОДЕ-------//
//----------ПРИ НАЖАТИИ НА TextView С ПРОБЛЕМОЙ, ОТКРЫВАЕТ RepairersSeparateProblem---------//
//---------layout xml пустой почти, потому что элементы динамически добавляются, заголовок задается программно---------//
public class RepairersMaintenanceProblemsList extends AppCompatActivity {
    private final int ID_TEXTVIEWS = 5000;
    private int problemCount = 0;
    private List<String> problemIDs;
    ActionBarDrawerToggle toggle;
    LinearLayout linearLayout;
    View.OnClickListener textviewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairers_activity_problems_list);
        setTitle(getString(R.string.loading_data)); //если нет проблем, надо сделать: нету проблем
        initInstances();
        toggle = InitNavigationBar.setUpNavBar(RepairersMaintenanceProblemsList.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.problems_list, R.id.repairers_activity);
        addProblemsFromDatabase();
    }
    private void initInstances()
    {
        linearLayout = findViewById(R.id.linearLayout);
        problemIDs = new ArrayList<>();
        //к каждому textview проблемы будет прикреплен этот listener
        textviewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nomerProblemy = v.getId() - ID_TEXTVIEWS;
                Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                String IDOfSelectedProblem = problemIDs.get(nomerProblemy);
                intent.putExtra("ID проблемы в таблице Maintenance_problems", IDOfSelectedProblem);
                startActivity(intent);
            }
        };
    }

    private void addProblemsFromDatabase() {
        //----САМОЕ ГЛАВНОЕ ЭТОГО АКТИВИТИ----//
        //на самом деле нужно взять количество строк в таблице problems
        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference().child("Maintenance_problems"); //ссылка на проблемы ТО
        problemsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override public void onDataChange(@NonNull DataSnapshot problemsSnap) {
                linearLayout.removeAllViews(); //для обновления данных удали все результаты предыдущего поиска
                if(problemsSnap.getValue() == null)
                {
                    setTitle(getString(R.string.all_probs_solved));
                }
                else
                {
                    for(DataSnapshot problemDataSnapshot : problemsSnap.getChildren())
                    { //пройдись по всем проблемах в ветке
                        try {

                            MaintenanceProblem problem = problemDataSnapshot.getValue(MaintenanceProblem.class); //считай в объект
                            if(!Objects.requireNonNull(problem).solved)
                            {
                                setTitle(getString(R.string.problems_on_equipment_lines));
                                problemIDs.add(problemDataSnapshot.getKey()); //добавь айди данной проблемы в лист
                                addAMaintenanceProblem(problem);
                            }
                        } catch (NullPointerException npe) {
                            ExceptionProcessing.processException(npe);
                        }
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @SuppressLint("ResourceType")
    private void addAMaintenanceProblem(MaintenanceProblem problem)
    {
        //инициализация TEXTVIEW
        String problemInfoFromDB = getString(R.string.shop) + problem.getShop_name() + "\n" + getString(R.string.equipment_name_textview) + problem.getEquipment_line_name() + "\n"
                + getString(R.string.nomer_point_textview) + problem.getPoint_no() + "\n" + getString(R.string.subpoint_no) + problem.getSubpoint_no() + getString(R.string.date_time_detection) + problem.getDate() + " " + problem.getTime();

        TextView problemsInfo;
        problemsInfo = new TextView(getApplicationContext());
        problemsInfo.setText(problemInfoFromDB);
        PointDataRetriever.setTextOfProblemInList(getBaseContext(), problemsInfo, problem.getShop_no(), problem.getEquipment_line_no(), problem.getPoint_no(), problem.getSubpoint_no(), problem.getTime() + " " + problem.getDate());
        problemsInfo.setPadding(25, 25, 25, 25);
        problemsInfo.setId(ID_TEXTVIEWS + problemCount);
        problemsInfo.setTextColor(Color.parseColor(getString(R.color.text)));
        problemsInfo.setTextSize(13);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 25, 20, 25);
        problemsInfo.setLayoutParams(params);
        problemsInfo.setClickable(true);
        problemsInfo.setBackgroundResource(R.drawable.list_group_layout);
        problemsInfo.setOnClickListener(textviewClickListener);
        //добавить textview в layout
        linearLayout.addView(problemsInfo);
        problemCount++; //итерируй для уникализации айдишек textview и обращения к лист элементам
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}

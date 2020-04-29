package com.akfa.apsproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class QuestMainActivity extends AppCompatActivity  {

    static int childPositionG, groupPositionG;
//    uz.akfa.questaps.EquipmentLine liteyniy[4];
//    equipmentLines[0] = new uz.akfa.questaps.EquipmentLine()

    ExpandableListView expandableListView; //для выпадающего списка
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    private MainAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest_activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        expandableListView = findViewById(R.id.equipment_list);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new MainAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(adapter);
        initListData();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
//                Toast.makeText(MainActivity.this, Integer.toString(childPosition), Toast.LENGTH_SHORT);
                Log.i("Pressed child", "#" + childPosition);
                groupPositionG = groupPosition;
                childPositionG = childPosition;
                Intent intent = new Intent(getApplicationContext(), QuestVerification.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                return false;
            }
        });
    }

    //добавить данные в раскрывающийся список
    private void initListData() {
        listGroup.add(getString(R.string.group1));
        listGroup.add(getString(R.string.group2));
        String[] array;

        List<String> list1 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group1);
        Collections.addAll(list1, array);

        List<String> list2 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group2);
        Collections.addAll(list2, array);

        listItem.put(listGroup.get(0), list1);
        listItem.put(listGroup.get(1), list2);
        adapter.notifyDataSetChanged();
    }

}


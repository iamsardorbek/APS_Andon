package com.akfa.apsproject;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ChooseProblematicStationDialog extends DialogFragment implements View.OnTouchListener {
    Button confirm, cancel;
    private int numOfStations;
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_choose_problematic_station, container, false);
        confirm = view.findViewById(R.id.confirm);
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnTouchListener(this);
        confirm.setOnTouchListener(this);

        Bundle bundle = getArguments();
        String login = bundle.getString("Логин пользователя");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + login);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnap) {
                final String equipmentName = userSnap.child("equipment_name").getValue().toString();
                final String shopName = userSnap.child("shop_name").getValue().toString();
                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference("Shops");
                shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shopsSnap) {
                        for(DataSnapshot shopSnap : shopsSnap.getChildren())
                        {
                            if(shopSnap.child("shop_name").getValue().toString().equals(shopName))
                            {
                                for(DataSnapshot equipmentSnap : shopSnap.child("Equipment_lines").getChildren())
                                {
                                    if(equipmentSnap.child("equipment_name").getValue().toString().equals(equipmentName))
                                    {
                                        numOfStations = Integer.parseInt(equipmentSnap.child("number_of_punkts").getValue().toString());
                                        List<String> spinnerArray =  new ArrayList<String>();
                                        for(int i = 1; i <=numOfStations; i++) {
                                            spinnerArray.add("Участок №" + i);
                                        }


                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, spinnerArray);

                                        adapter.setDropDownViewResource(R.layout.spinner_item);
                                        Spinner spinnerStations = view.findViewById(R.id.spinner_stations);
                                        spinnerStations.setAdapter(adapter);
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        return view;
    }

    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_UP:
                switch (button.getId())
                {
                    case R.id.cancel:
                        Log.i("TAG", "Confirmed");
                        getDialog().dismiss();
                        break;
                    case R.id.confirm:
                        Log.i("TAG", "Confirmed");
                        getDialog().dismiss();
                        break;
                }
                break;
        }

        return false;
    }
}

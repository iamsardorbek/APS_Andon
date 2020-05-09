package com.akfa.apsproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseProblematicStationDialog extends DialogFragment implements View.OnTouchListener {
    private Button confirm, cancel;
    private int numOfStations;
    private ChooseProblematicStationDialogListener listener;
    private List<String> spinnerArray =  new ArrayList<String>();
    private String equipmentLineName, shopName;
    private Spinner spinnerStations;
    private int whoIsNeededIndex; //master/raw/repair/quality
    private String operatorLogin;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_choose_problematic_station, container, false);
        confirm = view.findViewById(R.id.confirm);
        cancel = view.findViewById(R.id.cancel);
        spinnerStations = view.findViewById(R.id.spinner_stations);
        cancel.setOnTouchListener(this);
        confirm.setOnTouchListener(this);
        spinnerArray.add("Нажмите сюда для выбора участка");

        Bundle bundle = getArguments();
        operatorLogin = bundle.getString("Логин пользователя");
        whoIsNeededIndex = bundle.getInt("Вызвать специалиста");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + operatorLogin);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnap) {
                equipmentLineName = userSnap.child("equipment_name").getValue().toString();
                shopName = userSnap.child("shop_name").getValue().toString();
                DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference("Shops");
                shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shopsSnap) {
                        for(DataSnapshot shopSnap : shopsSnap.getChildren())
                        {
                            if(shopSnap.child("shop_name").getValue().toString().equals(shopName))
                            {
//                                shopName = shopSnap.child("shop_name").getValue().toString();
                                for(DataSnapshot equipmentSnap : shopSnap.child("Equipment_lines").getChildren())
                                {
                                    if(equipmentSnap.child("equipment_name").getValue().toString().equals(equipmentLineName))
                                    {
                                        numOfStations = Integer.parseInt(equipmentSnap.child("number_of_punkts").getValue().toString());
                                        for(int i = 1; i <=numOfStations; i++) {
                                            spinnerArray.add("Участок №" + i);
                                        }


                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, spinnerArray);

                                        adapter.setDropDownViewResource(R.layout.spinner_item);

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
                        if(spinnerStations.getSelectedItem().toString().equals("Нажмите сюда для выбора участка"))
                        {
                            Toast.makeText(getView().getContext(), "Выберите участок", Toast.LENGTH_SHORT);
                        }
                        else
                        {
                            int stationNo = spinnerStations.getSelectedItemPosition();
                            Log.i("TAG", "Confirmed. StationNo = " + stationNo);

                            listener.submitStationNo(stationNo, equipmentLineName, shopName, operatorLogin, whoIsNeededIndex);
                            getDialog().dismiss();
                        }
                        break;
                }
                break;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ChooseProblematicStationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ChooseProblematicStationDialogListener");
        }
    }

    public interface ChooseProblematicStationDialogListener {
        void submitStationNo(int stationNo, String equipmentLineName, String shopName, String operatorLogin, int whoIsNeededIndex);
    }
}

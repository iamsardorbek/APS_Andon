package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ConfirmCallDialog extends DialogFragment implements View.OnTouchListener{
    private String whoIsCalled, shopName, equipmentName;
    private int pointNo;
    private Button confirm;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_confirm_call, container, false); //связать с xml файлом
        try {
            initInstances(view);
        } catch (AssertionError ae) {
            ExceptionProcessing.processException(ae, getResources().getString(R.string.program_issue_toast), getContext());
            try {
                Objects.requireNonNull(getDialog()).dismiss();
            }
            catch (NullPointerException npe1) {
                ExceptionProcessing.processException(npe1, getResources().getString(R.string.program_issue_toast), getContext());
                Intent intent = new Intent(getContext(), MakeACall.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        }
        return view;
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initInstances(final View view) {
        Bundle bundle = getArguments();
        assert bundle != null;
        whoIsCalled = bundle.getString("Вызываемый специалист");
        shopName = bundle.getString("Название цеха");
        equipmentName = bundle.getString("Название линии");
        pointNo = bundle.getInt("Номер пункта");

        TextView whoIsCalledTextview = view.findViewById(R.id.who_is_called_position);
        TextView shopNameTextview = view.findViewById(R.id.shop);
        TextView equipmentNameTextview = view.findViewById(R.id.equipment);
        TextView pointNoTextView = view.findViewById(R.id.point_no);


        whoIsCalledTextview.setText(whoIsCalled);
        shopNameTextview.setText(shopName);
        equipmentNameTextview.setText(equipmentName);
        pointNoTextView.setText(Integer.toString(pointNo));

        confirm = view.findViewById(R.id.confirm);
        confirm.setOnTouchListener(this);
        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnTouchListener(this);

        DatabaseReference callsRef = FirebaseDatabase.getInstance().getReference("Calls");
        callsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot callsSnap) {
                for(DataSnapshot callSnap : callsSnap.getChildren())
                {
                    try {

                        Call call = callSnap.getValue(Call.class);
                        boolean callComplete = Objects.requireNonNull(call).getComplete();
                        String callCalledBy = call.getCalled_by();
                        String callWhoIsNeededPosition = call.getWho_is_needed_position();
                        String callShopName = call.getShop_name();
                        String callEquipmentName = call.getEquipment_name();
                        int callPointNo = call.getPoint_no();
                        if(!callComplete && callCalledBy.equals(UserData.login) && callEquipmentName.equals(equipmentName) && callShopName.equals(shopName) && pointNo == callPointNo
                                && callWhoIsNeededPosition.equals(whoIsCalled))
                        {
                            confirm.setVisibility(View.INVISIBLE);
                            TextView title = view.findViewById(R.id.title);
                            title.setText("У вас уже есть активный вызов с данными параметрами");
                        }
                    } catch (NullPointerException npe) {
                        ExceptionProcessing.processException(npe);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: //эффект нажатия
                switch (button.getId()) {
                    case R.id.cancel:
                        button.setBackgroundResource(R.drawable.red_rectangle_pressed);
                        break;
                    case R.id.confirm:
                        button.setBackgroundResource(R.drawable.green_rectangle_pressed);
                }
                break;
            case MotionEvent.ACTION_UP: //что делать при клике
                switch (button.getId())
                {
                    case R.id.cancel: //закрыть диалог и сообщить об этом PultActivity через интерфейс
                        button.setBackgroundResource(R.drawable.red_rectangle);
//                        listener.onDialogCanceled(whoIsNeededIndex);
                        try {
                            Objects.requireNonNull(getDialog()).dismiss();
                        }
                        catch (NullPointerException npe) {
                            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getContext());
                            Intent intent = new Intent(getContext(), MakeACall.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                        break;
                    case R.id.confirm:
                        button.setBackgroundResource(R.drawable.green_rectangle);
//                        if(spinnerStations.getSelectedItem().toString().equals("Нажмите сюда для выбора пункта")) //если юзер не открыл spinner и не выбрал пункт
//                            Toast.makeText(getView().getContext(), "Выберите пункт", Toast.LENGTH_SHORT).show();
//                        else
//                        {
//                            int pointNo = spinnerStations.getSelectedItemPosition(); //какой пункт выбрали (индекс выбранного элемента спиннера)
//                            listener.submitPointNo(pointNo, equipmentLineName, shopName, whoIsNeededIndex); //передай в интерфейс функцию данные
//                            getDialog().dismiss(); // и закрой диалог
//                        }
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference callRef = dbRef.child("Calls").push();
                        //дата-время
                        final String dateCalled;
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        dateCalled = sdf.format(new Date());
                        final String timeCalled;
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                        timeCalled = sdf1.format(new Date());
                        //----считал дату и время----//
                        callRef.setValue(new Call(dateCalled, timeCalled, UserData.login, whoIsCalled, pointNo, equipmentName, shopName, false));
                        try {
                            Objects.requireNonNull(getDialog()).dismiss();
                        }
                        catch (NullPointerException npe) {
                            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getContext());
                            Intent intent = new Intent(getContext(), MakeACall.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                        break;
                }
                break;
        }
        return false;
    }
}

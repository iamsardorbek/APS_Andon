package com.akfa.apsproject.pult_and_urgent_problems;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.R;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//-------- ДИАЛОГ ВЫБОРА И ИНФОРМИРОВАНИЯ О СРОЧНОЙ НЕПОЛАДКЕ/ПРОБЛЕМЕ ОБНАРУЖЕННОЙ ОПЕРАТОРОМ В PULT ACTIVITY --------//
//--------ГЛАВНЫЕ ЭЛЕМЕНТЫ: SPINNER С НОМЕРАМИ УЧАСТКОВ ДЛЯ ВЫБОРА, --------//
public class ChooseProblematicPointDialog extends DialogFragment implements View.OnTouchListener {
    private ChooseProblematicStationDialogListener listener; //для передачи данных PultActivity через и интерфейс

    private int numOfStations;
    private int whoIsNeededIndex; //master/raw/repair/quality
    private String equipmentLineName, shopName;
    private List<String> spinnerArray =  new ArrayList<>();

    private Spinner spinnerStations;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_choose_problematic_point, container, false); //связать с xml файлом
        try {

            initInstances(view);
            spinnerArray.add(getString(R.string.click_here_to_select_point));

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnap) {
                    //по аккаунту юзера (подветки equipment_name, shop_name) найти кол-во участков
                    try {
                        equipmentLineName = userSnap.child("equipment_name").getValue().toString();
                        shopName = userSnap.child("shop_name").getValue().toString();
                        DatabaseReference shopsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.shops_ref));
                        shopsRef.addListenerForSingleValueEvent(new ValueEventListener() { //единожды пройдись пока не найдешь нужный цех
                            @Override
                            public void onDataChange(@NonNull DataSnapshot shopsSnap) {
                                for (DataSnapshot shopSnap : shopsSnap.getChildren()) {
                                    try {

                                        if (shopSnap.child("shop_name").getValue().toString().equals(shopName)) {
//                                shopName = shopSnap.child("shop_name").getValue().toString();
                                            for (DataSnapshot equipmentSnap : shopSnap.child("Equipment_lines").getChildren()) // пройдись пока не найдешь нужную линию
                                            {
                                                try {

                                                    if (equipmentSnap.child("equipment_name").getValue().toString().equals(equipmentLineName)) //нашел нужную линию
                                                    {
                                                        numOfStations = Integer.parseInt(equipmentSnap.child(getString(R.string.number_of_points)).getValue().toString()); //искомое кол-во участков
                                                        //ниже: заполни спиннер
                                                        for (int i = 1; i <= numOfStations; i++) {
                                                            spinnerArray.add(getString(R.string.nomer_point_textview) + i);
                                                        }
                                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, spinnerArray);
                                                        adapter.setDropDownViewResource(R.layout.spinner_item);
                                                        spinnerStations.setAdapter(adapter);
                                                        return;
                                                    }
                                                } catch (NullPointerException npe) {
                                                    ExceptionProcessing.processException(npe);
                                                }
                                            }
                                        }
                                    } catch (NullPointerException npe) {
                                        ExceptionProcessing.processException(npe);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    } catch (NullPointerException npe) {
                        ExceptionProcessing.processException(npe, getString(R.string.user_data_incomplete), getContext());
                        try {// и закрой диалог
                            Objects.requireNonNull(getDialog()).dismiss();
                        } catch (NullPointerException npe1) {ExceptionProcessing.processException(npe1);}
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (AssertionError ae) {
            ExceptionProcessing.processException(ae, getResources().getString(R.string.program_issue_toast), getContext());
            try {// и закрой диалог
                Objects.requireNonNull(getDialog()).dismiss();
            } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
        }
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInstances(View view)
    {
        Button confirm = view.findViewById(R.id.confirm);
        Button cancel = view.findViewById(R.id.cancel);
        spinnerStations = view.findViewById(R.id.spinner_points);
        cancel.setOnTouchListener(this);
        confirm.setOnTouchListener(this);

        Bundle bundle = getArguments();
        assert bundle != null;
        whoIsNeededIndex = bundle.getInt("Вызвать специалиста");
    }

    public interface ChooseProblematicStationDialogListener { //интерфейс чтобы PultActivity и диалог могли общаться
        void submitPointNo(int pointNo, String equipmentLineName, String shopName, int whoIsNeededIndex);
        void onDialogCanceled(int whoIsNeededIndex);
    }

    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN: //эффект нажатия
                switch (button.getId())
                {
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
                        listener.onDialogCanceled(whoIsNeededIndex);
                        try {
                            Objects.requireNonNull(getDialog()).dismiss();
                        } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                        break;
                    case R.id.confirm: //
                        button.setBackgroundResource(R.drawable.green_rectangle);
                        if(spinnerStations.getSelectedItem().toString().equals(getString(R.string.click_here_to_select_point))) //если юзер не открыл spinner и не выбрал пункт
                            Toast.makeText(getContext(), getString(R.string.choose_point), Toast.LENGTH_SHORT).show();
                        else
                        {
                            int pointNo = spinnerStations.getSelectedItemPosition(); //какой пункт выбрали (индекс выбранного элемента спиннера)
                            listener.submitPointNo(pointNo, equipmentLineName, shopName, whoIsNeededIndex); //передай в интерфейс функцию данные
                            try {// и закрой диалог
                                Objects.requireNonNull(getDialog()).dismiss();
                            } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                        }
                        break;
                }
                break;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            return new Dialog(Objects.requireNonNull(getActivity()), getTheme()){
                @Override public void onBackPressed() {
                    listener.onDialogCanceled(whoIsNeededIndex); // при нажатии на кнопку назад дай об ээтом знать PultActivity
                    try {// и закрой диалог
                        Objects.requireNonNull(getDialog()).dismiss();
                    } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                }
            };

        }
        catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getContext());
            try {// и закрой диалог
                Objects.requireNonNull(getDialog()).dismiss();
            } catch (NullPointerException npe1) {ExceptionProcessing.processException(npe1);}
            return null; //просто чтобы NPE or warning не было, возвращаем пустой объект
        }
    }

    @Override
    public void onAttach(@NonNull Context context) { //attach listener to this dialog
        super.onAttach(context);
        try { listener = (ChooseProblematicStationDialogListener) context; }
        catch (ClassCastException e) { throw new ClassCastException(context.toString() + "must implement ChooseProblematicStationDialogListener"); }
    }
}

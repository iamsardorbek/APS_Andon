package com.akfa.apsproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChooseProblematicStationDialog extends DialogFragment implements View.OnTouchListener {
    Button confirm, cancel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_choose_problematic_station, container, false);
        confirm = view.findViewById(R.id.confirm);
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnTouchListener(this);
        confirm.setOnTouchListener(this);
        return view;
    }

    @Override
    public boolean onTouch(View button, MotionEvent event) {
        switch (button.getId())
        {
            case R.id.cancel:

                break;
            case R.id.confirm:

                break;
        }
        return false;
    }
}

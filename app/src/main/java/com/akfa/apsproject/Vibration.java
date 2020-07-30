package com.akfa.apsproject;

import android.content.Context;
import android.os.Vibrator;

import java.util.Objects;

public class Vibration {
    private static final int VIBRATION_DURATION = 500;
    @SuppressWarnings("deprecation")
    public static void vibration(Context context)
    {
        //вибрирует
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            Objects.requireNonNull(vibrator).vibrate(VIBRATION_DURATION);
        }
        catch (NullPointerException npe)
        {ExceptionProcessing.processException(npe);}
    }
}

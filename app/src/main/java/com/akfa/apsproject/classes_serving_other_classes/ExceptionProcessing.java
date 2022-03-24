package com.akfa.apsproject.classes_serving_other_classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.akfa.apsproject.BackgroundService;
import com.akfa.apsproject.SplashActivity;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

//-----------ОБРАБОТКА ОШИБОК для SAFE CODE ------------//
//здесь caught exception отдается в crashlytics и выводится тост для успокоения пользователя
//а приложение отматывается назад на сплаш скрин (типа рестарт) - чуть небезопасно, т.е. может создать infinite loop если и в сплэш есть ошибка
public class ExceptionProcessing {
    public static void processException(Throwable throwable, String toastMsg, Context context, Activity activity)
    {
        throwable.printStackTrace();
        addUserDataToRecord();
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
        activity.finish();
    }

    public static void processException(Throwable throwable, String toastMsg, Context context, BackgroundService backgroundService)
    {
        throwable.printStackTrace();
        addUserDataToRecord();
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
        backgroundService.stopSelf();
    }

    public static void processException(Throwable throwable, String toastMsg, Context context) {
        throwable.printStackTrace();
        addUserDataToRecord();
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
    }

    public static void processException(Throwable throwable, Context context) {
        throwable.printStackTrace();
        addUserDataToRecord();
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
    }

    public static void processException(Throwable throwable) {
        throwable.printStackTrace();
        addUserDataToRecord();
        FirebaseCrashlytics.getInstance().recordException(throwable);
    }

    private static void addUserDataToRecord()
    {
        if(!UserData.login.equals(""))
        {
            FirebaseCrashlytics.getInstance().recordException(new Exception("Login: " + UserData.login + "\nPosition: " + UserData.position));
        }
    }
}

package com.akfa.apsproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

//-----------ОБРАБОТКА ОШИБОК для SAFE CODE ------------//
//здесь caught exception отдается в crashlytics и выводится тост для успокоения пользователя
//а приложение отматывается назад на сплаш скрин (типа рестарт) - чуть небезопасно, т.е. может создать infinite loop если и в сплэш есть ошибка
public class ExceptionProcessing {
    public static void processException(Throwable throwable, String toastMsg, Context context, Activity activity)
    {
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
        activity.finish();
    }

    public static void processException(Throwable throwable, String toastMsg, Context context, BackgroundService backgroundService)
    {
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
        backgroundService.stopSelf();
    }

    public static void processException(Throwable throwable, Context context) {
        FirebaseCrashlytics.getInstance().recordException(throwable);
        Intent openSplash = new Intent(context, SplashActivity.class);
        context.startActivity(openSplash);
    }

    public static void processException(Throwable throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable);
    }
}

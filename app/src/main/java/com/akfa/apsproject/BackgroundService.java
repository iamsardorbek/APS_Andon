package com.akfa.apsproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//-------- ФОНОВЫЙ СЕРВИС ПРОВЕРКИ НАЛИЧИЯ НОВООБНАРУЖЕННЫХ ПРОБЛЕМ И НЕПОЛАДОК. ПРИ НОВЫХ ПРОБЛЕМАХ В ЗАВИС ОТ ДОЛЖНОСТИ ВЫВОДИТ УВЕДОМЛЕНИЯ--------//
//--------РАБ ДАЖЕ ПРИ ЗАКРЫТОМ ПРИЛОЖЕНИИ--------//
public class BackgroundService extends Service {
    private static final String CHANNEL_ID = "com.akfa.apsproject", MAINTENANCE_PROBS_CHANNEL_ID = "maintenance problems channel"; //название канала уведомлений
    private final int RUNNABLE_REFRESH_TIME = 10000; //периодичность уведомлений вызовов/сроч проблем
    List<String> urgentProblems = new ArrayList<>(); //для хранения данных об уже обнаруженных срочных проблемах
    List<Integer> urgentProblemsIDs = new ArrayList<>(), callsIDs = new ArrayList<>();
    List<String> maintenanceProblems = new ArrayList<>(); //для хранения данных об уже обнаруженных простых проблемах
    private long notificationCount = 2, maintanceProblemsNotificationsCount = 100;
    private boolean stopped = false;
    @Override
    public void onCreate() {//при создании  сервиса
        createNotificationChannel();
//        startForegroundWithNotification();

        super.onCreate();
    }

    @Override //при запуске сервиса
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            createMaintenanceProbsNotificationChannel();
            startForegroundWithNotification();

            if (!UserData.position.equals("operator") && !UserData.position.equals("head")) { //работает у всех кроме оператора, потому что он сам сообщает про срочные и ТО проблемы
                //те же дейсвтия повтори с ТО проблемами
                DatabaseReference maintenanceProblemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
                maintenanceProblemsRef.addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot maintenanceProbsSnap) {
                        if (maintenanceProbsSnap.exists()) {

                            for (DataSnapshot maintenanceProbSnap : maintenanceProbsSnap.getChildren()) {
                                boolean thisMaintenanceProbIsSolved = (boolean) maintenanceProbSnap.child("solved").getValue();
                                String thisMaintenanceProbKey = maintenanceProbSnap.getKey();
                                String detectedByEmployee = maintenanceProbSnap.child("detected_by_employee").getValue().toString();
                                String whoIsNeededPosition = "repair"; //проблемами ТО занимаются только ремонтники (repairers)
                                if (!UserData.login.equals(detectedByEmployee) && !thisMaintenanceProbIsSolved && !maintenanceProblems.contains(thisMaintenanceProbKey) && whoIsNeededPosition.equals(UserData.position))
                                {
                                    //получить UID проблемы
                                    //если эта об этой проблеме ТО еще уведомление не выводилось
                                    //create and show a notification here
                                    String shopName = maintenanceProbSnap.child("shop_name").getValue().toString();
                                    String equipmentName = maintenanceProbSnap.child("equipment_line_name").getValue().toString();
                                    String pointNo = maintenanceProbSnap.child(getString(R.string.point_no)).getValue().toString();
                                    String maintenanceProblemInfo = shopName + "\n" + equipmentName + "\nПункт №" + pointNo;
                                    Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                                    intent.putExtra("ID проблемы в таблице Maintenance_problems", thisMaintenanceProbKey);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MAINTENANCE_PROBS_CHANNEL_ID)
                                            .setSmallIcon(R.drawable.aps_icon)
                                            .setContentTitle(getResources().getString(R.string.maintenance_problem))
                                            .setContentText(maintenanceProblemInfo)
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText(maintenanceProblemInfo))
                                            .setContentIntent(pendingIntent)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    // notificationId is a unique int for each notification that you must define
                                    Objects.requireNonNull(notificationManager).notify((int) maintanceProblemsNotificationsCount, builder.build());
                                    maintenanceProblems.add(thisMaintenanceProbKey);
                                    maintanceProblemsNotificationsCount++;
                                }
                            }

                            //укороченная версия уведомлений
//                        Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        String notificationText = "Существуют " + maintanceProblemsNotificationsCount + " проблем на заводе. Нажмите сюда, чтобы с ними ознакомиться.";
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MAINTENANCE_PROBS_CHANNEL_ID)
//                        .setSmallIcon(R.drawable.aps_icon)
//                        .setContentTitle("Проблемы ТО")
//                        .setContentText(notificationText)
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(maintanceProblemsNotificationsCount + " проблем на заводе"))
//                        .setContentIntent(pendingIntent)
//                        .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                        // notificationId is a unique int for each notification that you must define
//                        notificationManager.notify((int) maintanceProblemsNotificationsCount, builder.build());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }


            final Handler handler = new Handler();
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    if (!UserData.position.equals("head")) {
                        AudioManager mobilemode = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        Objects.requireNonNull(mobilemode).setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        mobilemode.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        mobilemode.setStreamVolume(AudioManager.STREAM_SYSTEM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                    }

//                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                for(int i = 2; i <= notificationCount; i++)
//                {
//                    mNotificationManager.cancel(i);
//                }
                    notificationCount = 2; //foreground notification имеет айди 1, поэтому последующие айди начинаются с 2
                    if (!stopped) { //stopped булеан, который дает знать, надо дальше высвечивать или нет
                        if (!UserData.position.equals("operator")) { //работает у всех кроме оператора, потому что он сам сообщает про срочные и ТО проблемы
                            DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference("Urgent_problems"); //мониторить срочные проблемы ( с пультов)
                            urgentProblemsRef.addListenerForSingleValueEvent(new ValueEventListener() { //привязываем слушатель к срочным проблемаам
                                @Override
                                public void onDataChange(@NonNull DataSnapshot urgentProbsSnap) {
                                    if (urgentProbsSnap.exists()) {
                                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        for (int urgentProblemID : urgentProblemsIDs) {
                                            Objects.requireNonNull(mNotificationManager).cancel(urgentProblemID);
                                        }
                                        urgentProblemsIDs = new ArrayList<>();
                                        for (DataSnapshot urgentProbSnap : urgentProbsSnap.getChildren()) { //пройдись по проблемам и если есть DETECTED проблемы, о которых ты еще не вывел уведомления, сообщи о них в новом уведомлении
                                            String thisUrgentProbStatus = urgentProbSnap.child("status").getValue().toString();
                                            final String thisUrgentProbKey = urgentProbSnap.getKey();
                                            String whoIsNeededPosition = urgentProbSnap.child("who_is_needed_position").getValue().toString();

                                            if (thisUrgentProbStatus.equals("DETECTED") && whoIsNeededPosition.equals(UserData.position)) {

                                                //получить UID проблемы
                                                //если эта об этой срочной проблеме еще уведомление не выводилось
                                                //create and show a notification here
                                                final String shopName = urgentProbSnap.child("shop_name").getValue().toString();
                                                String equipmentName = urgentProbSnap.child("equipment_name").getValue().toString();
                                                String pointNo = urgentProbSnap.child(getString(R.string.point_no)).getValue().toString();
                                                final String urgentProblemShortInfo = shopName + "\n" + equipmentName + "\nПункт №" + pointNo;

                                                if (UserData.position.equals("master")) {
                                                    final DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                                                    masterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot masterSnap) {
                                                            String masterShopName = masterSnap.child("shop_name").getValue().toString();
                                                            if (masterShopName.equals(shopName)) {
                                                                Intent intent = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                        .setSmallIcon(R.drawable.aps_icon)
                                                                        .setContentTitle(getResources().getString(R.string.urgent_problem))
                                                                        .setContentText(urgentProblemShortInfo)
                                                                        .setStyle(new NotificationCompat.BigTextStyle()
                                                                                .bigText(urgentProblemShortInfo))
                                                                        .setContentIntent(pendingIntent)
                                                                        .setPriority(NotificationCompat.PRIORITY_MAX);
                                                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                // notificationId is a unique int for each notification that you must define
                                                                notificationManager.notify((int) notificationCount, builder.build());
                                                                urgentProblems.add(thisUrgentProbKey); //добавь эту проблему в список уже сообщенных
                                                                Vibration.vibration(getApplicationContext());
                                                                urgentProblemsIDs.add((int) notificationCount);
                                                                notificationCount++;
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        }
                                                    });
                                                } else {
                                                    Intent intent = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                            .setSmallIcon(R.drawable.aps_icon)
                                                            .setContentTitle(getResources().getString(R.string.urgent_problem))
                                                            .setContentText(urgentProblemShortInfo)
                                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                                    .bigText(urgentProblemShortInfo))
                                                            .setContentIntent(pendingIntent)
                                                            .setPriority(NotificationCompat.PRIORITY_MAX);
                                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                    // notificationId is a unique int for each notification that you must define
                                                    notificationManager.notify((int) notificationCount, builder.build());
                                                    urgentProblems.add(thisUrgentProbKey); //добавь эту проблему в список уже сообщенных
                                                    Vibration.vibration(getApplicationContext());
                                                    urgentProblemsIDs.add((int) notificationCount);
                                                    notificationCount++;
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                        //запустим листенеры для Вызовов Calls
                        if (UserData.position.equals("operator") || UserData.position.equals("master") || UserData.position.equals("repair")) {
                            DatabaseReference callsRef = FirebaseDatabase.getInstance().getReference("Calls");

                            callsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot callsSnap) {
                                    if (callsSnap.exists()) {
                                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        for (int callID : callsIDs) {
                                            if (mNotificationManager != null) {
                                                mNotificationManager.cancel(callID);
                                            }
                                        }
                                        callsIDs = new ArrayList<>();

                                        for (final DataSnapshot callSnap : callsSnap.getChildren()) {
                                            boolean callComplete = (boolean) callSnap.child("complete").getValue();
                                            String whoIsNeededPosition = callSnap.child("who_is_needed_position").getValue().toString();
                                            if (!callComplete && whoIsNeededPosition.equals(UserData.position)) {
                                                final String callShopName = callSnap.child("shop_name").getValue().toString();
                                                switch (UserData.position) {
                                                    case "operator":
                                                        DatabaseReference operatorRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                                                        operatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                                String operatorEquipmentName = userSnap.child("equipment_name").getValue().toString();
                                                                String operatorShopName = userSnap.child("shop_name").getValue().toString();
                                                                String callEquipmentName = callSnap.child("equipment_name").getValue().toString();
                                                                if (operatorEquipmentName.equals(callEquipmentName) && operatorShopName.equals(callShopName)) {
                                                                    String pointNo = callSnap.child(getString(R.string.point_no)).getValue().toString();
                                                                    String calledByLogin = callSnap.child("called_by").getValue().toString();
                                                                    String callInfo = calledByLogin + getResources().getString(R.string.calling_you_to) + callShopName + "\n" + callEquipmentName + "\n"+ getResources().getString(R.string.nomer_point_textview) + pointNo;
                                                                    Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                            .setSmallIcon(R.drawable.aps_icon)
                                                                            .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                            .setContentText(callInfo)
                                                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                                                    .bigText(callInfo))
                                                                            .setContentIntent(pendingIntent)
                                                                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                    // notificationId is a unique int for each notification that you must define
                                                                    Objects.requireNonNull(notificationManager).notify((int) notificationCount, builder.build());
                                                                    Vibration.vibration(getApplicationContext());
                                                                    callsIDs.add((int) notificationCount);
                                                                    notificationCount++;
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            }
                                                        });
                                                        break;
                                                    case "master":
                                                        DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                                                        masterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                                String operatorShopName = userSnap.child("shop_name").getValue().toString();

                                                                if (operatorShopName.equals(callShopName)) {
                                                                    String callEquipmentName = callSnap.child("equipment_name").getValue().toString();
                                                                    String pointNo = callSnap.child(getString(R.string.point_no)).getValue().toString();
                                                                    String calledByLogin = callSnap.child("called_by").getValue().toString();

                                                                    String callInfo = calledByLogin + getResources().getString(R.string.calling_you_to) + callShopName + "\n" + callEquipmentName + "\n" + getResources().getString(R.string.nomer_point_textview) + pointNo;
                                                                    Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                            .setSmallIcon(R.drawable.aps_icon)
                                                                            .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                            .setContentText(callInfo)
                                                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                                                    .bigText(callInfo))
                                                                            .setContentIntent(pendingIntent)
                                                                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                    // notificationId is a unique int for each notification that you must define
                                                                    notificationManager.notify((int) notificationCount, builder.build());
                                                                    Vibration.vibration(getApplicationContext());
                                                                    callsIDs.add((int) notificationCount);
                                                                    notificationCount++;
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            }
                                                        });
                                                        break;
                                                    case "repair":
                                                        String callEquipmentName = callSnap.child("equipment_name").getValue().toString();
                                                        String pointNo = callSnap.child(getString(R.string.point_no)).getValue().toString();
                                                        String calledByLogin = callSnap.child("called_by").getValue().toString();

                                                        String callInfo = calledByLogin + getResources().getString(R.string.calling_you_to) + callShopName + "\n" + callEquipmentName + "\n" + getResources().getString(R.string.nomer_point_textview) + pointNo;
                                                        Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                .setSmallIcon(R.drawable.aps_icon)
                                                                .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                .setContentText(callInfo)
                                                                .setStyle(new NotificationCompat.BigTextStyle()
                                                                        .bigText(callInfo))
                                                                .setContentIntent(pendingIntent)
                                                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                        // notificationId is a unique int for each notification that you must define
                                                        notificationManager.notify((int) notificationCount, builder.build());
                                                        Vibration.vibration(getApplicationContext());
                                                        callsIDs.add((int) notificationCount);
                                                        notificationCount++;
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        if (!stopped) {
                            handler.postDelayed(this, RUNNABLE_REFRESH_TIME);
                        }
                    }
                }
            };
            handler.postDelayed(runnableCode, RUNNABLE_REFRESH_TIME);
        }
        catch (NullPointerException npe){ExceptionProcessing.processException(npe, getResources().getString(R.string.database_npe_toast), getApplicationContext(), this);}
        catch (AssertionError ae) {ExceptionProcessing.processException(ae, getResources().getString(R.string.no_notifications_func_or_tech_problem), getApplicationContext(), this);}
        return super.onStartCommand(intent, flags, startId);
    }

    @Override public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancelAll();
        stopped = true;
        super.onDestroy();
    }
    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.aps_notifications); //название канала уведомлений (показывается в настройках
            String description = getString(R.string.notifications_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createMaintenanceProbsNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.problems_list_submenu); //название канала уведомлений (показывается в настройках
            String description = getString(R.string.maintenance_probs_notif_channel);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(MAINTENANCE_PROBS_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundWithNotification()
    {
        // система android требует, чтобы вы вывели фоновое уведомление если вы запускаете фоновый независимый сервис
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        switch (UserData.position)
        {
            case "operator":
                intent = new Intent(getApplicationContext(), PultActivity.class);
                break;
            case "master":
                intent = new Intent(getApplicationContext(), QuestListOfEquipment.class); //actually there should be the FactoryCondition.class, but it is incomplete yet
                break;
            case "repair":
            case "raw":
            case "quality":
                intent = new Intent(getApplicationContext(), UrgentProblemsList.class);
                break;
            case "head":
                intent = new Intent(getApplicationContext(), TodayChecks.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(getString(R.string.app_launched))
                .setContentText(getString(R.string.notifs_will_be_made_online))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notifs_will_be_made_online)))
                .setSmallIcon(R.drawable.aps_icon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }
}

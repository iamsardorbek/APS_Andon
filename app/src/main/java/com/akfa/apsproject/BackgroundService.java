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

import com.akfa.apsproject.calls.Call;
import com.akfa.apsproject.calls.CallsList;
import com.akfa.apsproject.checking_equipment_maintenance.QuestListOfEquipment;
import com.akfa.apsproject.checking_equipment_maintenance.RepairerSeparateProblem;
import com.akfa.apsproject.checking_equipment_maintenance.RepairersMaintenanceProblemsList;
import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.akfa.apsproject.classes_serving_other_classes.Vibration;
import com.akfa.apsproject.general_data_classes.UserData;
import com.akfa.apsproject.monitoring_activities.TodayChecks;
import com.akfa.apsproject.pult_and_urgent_problems.PultActivity;
import com.akfa.apsproject.pult_and_urgent_problems.UrgentProblemsList;
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
                        try {
                            if (maintenanceProbsSnap.exists()) {
                                maintanceProblemsNotificationsCount = 0;
                                for (DataSnapshot maintenanceProbSnap : maintenanceProbsSnap.getChildren()) {
                                    boolean thisMaintenanceProbIsSolved = (boolean) maintenanceProbSnap.child("solved").getValue();
                                    String thisMaintenanceProbKey = maintenanceProbSnap.getKey();
                                    String detectedByEmployee = Objects.requireNonNull(maintenanceProbSnap.child("detected_by_employee").getValue()).toString();
                                    String whoIsNeededPosition = "repair"; //проблемами ТО занимаются только ремонтники (repairers)
                                    if (!UserData.login.equals(detectedByEmployee) && !thisMaintenanceProbIsSolved && !maintenanceProblems.contains(thisMaintenanceProbKey) && whoIsNeededPosition.equals(UserData.position)) {
                                        //получить UID проблемы
                                        //если эта об этой проблеме ТО еще уведомление не выводилось
                                        //create and show a notification here
//                                        String shopNo = Objects.requireNonNull(maintenanceProbSnap.child("shop_no").getValue()).toString();
//                                        String equipmentLineNo = Objects.requireNonNull(maintenanceProbSnap.child("equipment_line_no").getValue()).toString();
//                                        String pointNo = Objects.requireNonNull(maintenanceProbSnap.child(getString(R.string.point_no)).getValue()).toString();
//                                        String maintenanceProblemInfo = shopNo + "\n" + equipmentLineNo + "\nПункт №" + pointNo;
//                                        Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
//                                        intent.putExtra("ID проблемы в таблице Maintenance_problems", thisMaintenanceProbKey);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//
//                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MAINTENANCE_PROBS_CHANNEL_ID)
//                                                .setSmallIcon(R.drawable.aps_icon)
//                                                .setContentTitle(getResources().getString(R.string.maintenance_problem))
//                                                .setContentText(maintenanceProblemInfo)
//                                                .setStyle(new NotificationCompat.BigTextStyle()
//                                                        .bigText(maintenanceProblemInfo))
//                                                .setContentIntent(pendingIntent)
//                                                .setPriority(NotificationCompat.PRIORITY_HIGH);
//                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                                        // notificationId is a unique int for each notification that you must define
//                                        PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, maintanceProblemsNotificationsCount, Integer.parseInt(shopNo), Integer.parseInt(equipmentLineNo), Integer.parseInt(pointNo));
//                                        maintenanceProblems.add(thisMaintenanceProbKey);
                                        maintanceProblemsNotificationsCount++;
                                    }
                                }
                                if(maintanceProblemsNotificationsCount != 0) {
                                    //укороченная версия уведомлений
                                    Intent intent = new Intent(getApplicationContext(), RepairersMaintenanceProblemsList.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    String notificationText = maintanceProblemsNotificationsCount + getString(R.string.maintenance_problems_the_factory) + getString(R.string.click_notif_to_get_info);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MAINTENANCE_PROBS_CHANNEL_ID)
                                            .setSmallIcon(R.drawable.aps_icon)
                                            .setContentTitle(getString(R.string.problems_list_submenu))
                                            .setContentText(maintanceProblemsNotificationsCount + getString(R.string.maintenance_problems_the_factory))
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText(notificationText))
                                            .setContentIntent(pendingIntent)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    // notificationId is a unique int for each notification that you must define
                                    Objects.requireNonNull(notificationManager).notify((int) maintanceProblemsNotificationsCount, builder.build());
                                }
                            }
                        }
                        catch (NullPointerException npe){ ExceptionProcessing.processException(npe);}
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
                                    try {
                                        if (urgentProbsSnap.exists()) {
                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            for (int urgentProblemID : urgentProblemsIDs) {
                                                Objects.requireNonNull(mNotificationManager).cancel(urgentProblemID);
                                            }
                                            urgentProblemsIDs = new ArrayList<>();
                                            for (DataSnapshot urgentProbSnap : urgentProbsSnap.getChildren()) { //пройдись по проблемам и если есть DETECTED проблемы, о которых ты еще не вывел уведомления, сообщи о них в новом уведомлении
                                                String thisUrgentProbStatus = Objects.requireNonNull(urgentProbSnap.child("status").getValue()).toString();
                                                final String thisUrgentProbKey = urgentProbSnap.getKey();
                                                String whoIsNeededPosition = Objects.requireNonNull(urgentProbSnap.child("who_is_needed_position").getValue()).toString();

                                                if (thisUrgentProbStatus.equals("DETECTED") && whoIsNeededPosition.equals(UserData.position)) {

                                                    //получить UID проблемы
                                                    //если эта об этой срочной проблеме еще уведомление не выводилось
                                                    //create and show a notification here
                                                    final String shopNo = Objects.requireNonNull(urgentProbSnap.child("shop_no").getValue()).toString();
                                                    final String equipmentNo = Objects.requireNonNull(urgentProbSnap.child("equipment_no").getValue()).toString();
                                                    final String pointNo = Objects.requireNonNull(urgentProbSnap.child(getString(R.string.point_no)).getValue()).toString();

                                                    if (UserData.position.equals("master")) {
                                                        final DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                                                        masterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot masterSnap) {
                                                                String masterShopName = Objects.requireNonNull(masterSnap.child("shop_name").getValue()).toString();
                                                                if (masterShopName.equals(shopNo)) {
                                                                    Intent intent = new Intent(getApplicationContext(), UrgentProblemsList.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                            .setSmallIcon(R.drawable.aps_icon)
                                                                            .setContentTitle(getResources().getString(R.string.urgent_problem))
                                                                            .setContentIntent(pendingIntent)
                                                                            .setPriority(NotificationCompat.PRIORITY_MAX);
                                                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                    assert notificationManager != null;
                                                                    // notificationId is a unique int for each notification that you must define
                                                                    PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, notificationCount, Integer.parseInt(shopNo), Integer.parseInt(equipmentNo), Integer.parseInt(pointNo), "");
                                                                    urgentProblems.add(thisUrgentProbKey); //добавь эту проблему в список уже сообщенных
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
                                                                .setContentIntent(pendingIntent)
                                                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                        // notificationId is a unique int for each notification that you must define
                                                        assert notificationManager != null;
                                                        PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, notificationCount, Integer.parseInt(shopNo), Integer.parseInt(equipmentNo), Integer.parseInt(pointNo), "");
                                                        urgentProblems.add(thisUrgentProbKey); //добавь эту проблему в список уже сообщенных
                                                        urgentProblemsIDs.add((int) notificationCount);
                                                        notificationCount++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    catch (NullPointerException | AssertionError e){ ExceptionProcessing.processException(e);}
                                }

                                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
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
                                            try {
                                                final Call call = callSnap.getValue(Call.class);
                                                assert call != null;
                                                boolean callComplete = call.getComplete();
                                                String whoIsNeededPosition = call.getWho_is_needed_position();
                                                if (!callComplete && whoIsNeededPosition.equals(UserData.position)) {
                                                    final String callShopName = call.getShop_name();
                                                    switch (UserData.position) {
                                                        case "operator":
                                                            DatabaseReference operatorRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                                                            operatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                                    try {
                                                                        String operatorEquipmentName = Objects.requireNonNull(userSnap.child("equipment_name").getValue()).toString();
                                                                        String operatorShopName = Objects.requireNonNull(userSnap.child("shop_name").getValue()).toString();
                                                                        String callEquipmentName = call.getEquipment_name();
                                                                        if (operatorEquipmentName.equals(callEquipmentName) && operatorShopName.equals(callShopName)) {
                                                                            int callPointNo = call.getPoint_no();
                                                                            String calledByLogin = call.getCalled_by();
                                                                            int callShopNo = call.getShop_no();;
                                                                            int callEquipmentNo = call.getEquipment_no();
                                                                            Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                                    .setSmallIcon(R.drawable.aps_icon)
                                                                                    .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                                    .setContentIntent(pendingIntent)
                                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                            // notificationId is a unique int for each notification that you must define
                                                                            PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, notificationCount, callShopNo, callEquipmentNo, callPointNo, calledByLogin + getString(R.string.calling_you_to));
                                                                            callsIDs.add((int) notificationCount);
                                                                            notificationCount++;
                                                                        }
                                                                    } catch (NullPointerException npe) {
                                                                        ExceptionProcessing.processException(npe);
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
                                                                    try {
                                                                        String masterShopName = Objects.requireNonNull(userSnap.child("shop_name").getValue()).toString();

                                                                        if (masterShopName.equals(callShopName)) {
                                                                            int callPointNo = call.getPoint_no();
                                                                            String calledByLogin = call.getCalled_by();
                                                                            int callShopNo = call.getShop_no();;
                                                                            int callEquipmentNo = call.getEquipment_no();
                                                                            Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                                    .setSmallIcon(R.drawable.aps_icon)
                                                                                    .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                                    .setContentIntent(pendingIntent)
                                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                            // notificationId is a unique int for each notification that you must define

                                                                            PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, notificationCount, callShopNo, callEquipmentNo, callPointNo, calledByLogin + getString(R.string.calling_you_to));
                                                                            callsIDs.add((int) notificationCount);
                                                                            notificationCount++;
                                                                        }
                                                                    } catch (NullPointerException | AssertionError thr) {
                                                                        ExceptionProcessing.processException(thr);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                            break;
                                                        case "repair":
                                                            try {
                                                                int callPointNo = call.getPoint_no();
                                                                String calledByLogin = call.getCalled_by();
                                                                int callShopNo = call.getShop_no();;
                                                                int callEquipmentNo = call.getEquipment_no();
                                                                Intent intent = new Intent(getApplicationContext(), CallsList.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                                        .setSmallIcon(R.drawable.aps_icon)
                                                                        .setContentTitle(getResources().getString(R.string.you_are_being_called))
                                                                        .setContentIntent(pendingIntent)
                                                                        .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                // notificationId is a unique int for each notification that you must define
                                                                PointDataRetriever.setNotificationText(getBaseContext(), builder, notificationManager, notificationCount, callShopNo, callEquipmentNo, callPointNo, calledByLogin + getString(R.string.calling_you_to));
                                                                callsIDs.add((int) notificationCount);
                                                                notificationCount++;
                                                            } catch (NullPointerException npe) {
                                                                ExceptionProcessing.processException(npe);
                                                            }
                                                            break;
                                                    }
                                                }
                                            }
                                            catch (NullPointerException | AssertionError e) {ExceptionProcessing.processException(e);}
                                        }
                                    }

                                }
                                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
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
        catch (NullPointerException npe){
            ExceptionProcessing.processException(npe, getResources().getString(R.string.database_npe_toast), getApplicationContext(), this);}
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

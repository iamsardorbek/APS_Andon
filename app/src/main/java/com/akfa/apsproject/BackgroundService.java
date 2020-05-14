package com.akfa.apsproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BackgroundService extends Service {
    private static final String CHANNEL_ID = "com.akfa.apsproject";
    List<String> urgentProblems = new ArrayList<String>(); //для хранения данных об уже обнаруженных срочных проблемах
    List<String> maintenanceProblems = new ArrayList<String>(); //для хранения данных об уже обнаруженных простых проблемах

    @Override
    public void onCreate() {
        createNotificationChannel();
        startForegroundWithNotification();
        super.onCreate();
    }

    @Override //при запуске сервиса
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference("Urgent_problems");
        DatabaseReference maintenanceProblemsRef = FirebaseDatabase.getInstance().getReference("Maintenance_problems");
        final String employeePosition = intent.getExtras().getString("Должность");
        urgentProblemsRef.addValueEventListener(new ValueEventListener() { //привязываем слушатель к срочным проблемаам

            @Override public void onDataChange(@NonNull DataSnapshot urgentProbsSnap) {
                for(DataSnapshot urgentProbSnap : urgentProbsSnap.getChildren())
                {
                    String thisUrgentProbStatus = urgentProbSnap.child("status").getValue().toString();
                    String thisUrgentProbKey = urgentProbSnap.getKey();
                    String whoIsNeededPosition = urgentProbSnap.child("who_is_needed_login").getValue().toString();

                    if(thisUrgentProbStatus.equals("DETECTED") && !urgentProblems.contains(thisUrgentProbKey) && whoIsNeededPosition.equals(employeePosition)) {
                         //получить UID проблемы
                         //если эта об этой срочной проблеме еще уведомление не выводилось
                        //create and show a notification here
                        String shopName = urgentProbSnap.child("shop_name").getValue().toString();
                        String equipmentName = urgentProbSnap.child("equipment_name").getValue().toString();
                        String stationNo = urgentProbSnap.child("station_no").getValue().toString();
                        String urgentProblemShortInfo = shopName + "\n" + equipmentName + "\n Участок №" + stationNo;
                        Intent intent = new Intent(getApplicationContext(), UrgentProblemsList.class);
                        intent.putExtra("Логин пользователя", "master");
                        intent.putExtra("Должность", "master");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.aps_icon)
                                .setContentTitle("Срочная проблема")
                                .setContentText(urgentProblemShortInfo)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(urgentProblemShortInfo))
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(1, builder.build());
                        urgentProblems.add(thisUrgentProbKey);
                    }
                }

            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        maintenanceProblemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot maintenanceProbsSnap) {
                for(DataSnapshot maintenanceProbSnap : maintenanceProbsSnap.getChildren())
                {
                    boolean thisMaintenanceProbIsSolved = (boolean) maintenanceProbSnap.child("solved").getValue();
                    String thisMaintenanceProbKey = maintenanceProbSnap.getKey();
                    String whoIsNeededPosition = "repair"; //проблемами ТО занимаются только ремонтники (repairers)
                    if(!thisMaintenanceProbIsSolved && !maintenanceProblems.contains(thisMaintenanceProbKey) && whoIsNeededPosition.equals(employeePosition)) {
                        //получить UID проблемы
                        //если эта об этой проблеме ТО еще уведомление не выводилось
                        //create and show a notification here
                        String shopName = maintenanceProbSnap.child("shop_name").getValue().toString();
                        String equipmentName = maintenanceProbSnap.child("equipment_line_name").getValue().toString();
                        String stationNo = maintenanceProbSnap.child("station_no").getValue().toString();
                        String maintenanceProblemInfo = shopName + "\n" + equipmentName + "\n Участок №" + stationNo;
                        Intent intent = new Intent(getApplicationContext(), RepairerSeparateProblem.class);
                        intent.putExtra("Логин пользователя", "marat1980");
                        intent.putExtra("ID проблемы в таблице Maintenance_problems", thisMaintenanceProbKey);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.aps_icon)
                                .setContentTitle("Проблема ТО")
                                .setContentText(maintenanceProblemInfo)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(maintenanceProblemInfo))
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(1, builder.build());
                        maintenanceProblems.add(thisMaintenanceProbKey);
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return super.onStartCommand(intent, flags, startId);

    }

    @Override public void onDestroy() {
        super.onDestroy();

    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "APS Notifications";
            String description = "Notifications about problems with the equipment on the factory";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundWithNotification()
    {
        Intent intent = new Intent(getApplicationContext(), SplashNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Приложение APS запущено")
                .setContentText("Уведомления о проблемах на линиях будут показываться в режиме реального времени")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Уведомления о проблемах на линиях будут показываться в режиме реального времени"))
                .setSmallIcon(R.drawable.aps_icon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }
}

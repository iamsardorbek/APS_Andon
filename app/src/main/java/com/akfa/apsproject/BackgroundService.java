package com.akfa.apsproject;

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
    List<String> urgentProblems = new ArrayList<String>(); //для хранения данных об уже обнаруженных проблемах
    @Override //при запуске сервиса
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "BG Service Started", Toast.LENGTH_SHORT).show();
        createNotificationChannel();
        DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference("Urgent_problems");
        DatabaseReference problemsRef = FirebaseDatabase.getInstance().getReference("Problems");
//        while(true) //сервис работает всегда в фоне
//        {
        Toast.makeText(getApplicationContext(), "IN infinite loop", Toast.LENGTH_SHORT).show();

        urgentProblemsRef.addValueEventListener(new ValueEventListener() { //привязываем слушатель к срочным проблемаам

            @Override public void onDataChange(@NonNull DataSnapshot urgentProbsSnap) {
                for(DataSnapshot urgentProbSnap : urgentProbsSnap.getChildren())
                {
                    String thisUrgentProbStatus = urgentProbSnap.child("status").getValue().toString();
                    String thisUrgentProbKey = urgentProbSnap.getKey();
                    if(thisUrgentProbStatus.equals("DETECTED") && !urgentProblems.contains(thisUrgentProbKey)) {
                         //получить UID проблемы
                         //если эта об этой срочной проблеме еще уведомление не выводилось
                        //create and show a notification here
                        String shopName = urgentProbSnap.child("shop_name").getValue().toString();
                        String equipmentName = urgentProbSnap.child("equipment_name").getValue().toString();
                        String stationNo = urgentProbSnap.child("station_no").getValue().toString();
                        String whoIsNeededPosition = urgentProbSnap.child("who_is_needed_login").getValue().toString();
                        String urgentProblemShortInfo = "Обнаружена срочная проблема в цехе " + shopName + ", с участком №" + stationNo + " линии " + equipmentName
                                + "\nНужен " + whoIsNeededPosition;
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
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(1, builder.build());
                        urgentProblems.add(thisUrgentProbKey);
                        Toast.makeText(getApplicationContext(), "Должен вывести уведомление", Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        return super.onStartCommand(intent, flags, startId);
//        }
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
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
}

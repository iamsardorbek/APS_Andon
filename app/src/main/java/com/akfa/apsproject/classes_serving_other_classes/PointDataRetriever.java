package com.akfa.apsproject.classes_serving_other_classes;

import com.akfa.apsproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class PointDataRetriever {

    public static final int EQUIPMENT_NOT_CHECKED = 0, EQUIPMENT_CHECKED = 1, SEPARATE_CHECK_DETAILS = 2;

    public static void setTextOfProblemInList(final Context context, final TextView textView, int shopNo, final int equipmentNo, final int pointNo, final int subpointNo, final String date_time_detected)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {

                    final String shopName, equipmentName, pointName, subPointName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    pointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + context.getString(R.string.point_name)).getValue()).toString();
                    String problemInfoFromDB = context.getString(R.string.shop) + shopName + "\n" + context.getString(R.string.equipmentLine) + equipmentName + "\n"
                            + context.getString(R.string.nomer_point_textview) + pointName + "\n";

                    if(subpointNo != -1) { //maintenance prob list
                        subPointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + "subpoint_" + subpointNo + "/" + context.getString(R.string.subpoint_description)).getValue()).toString();
                        problemInfoFromDB += context.getString(R.string.subpoint_no) + subPointName + "\n" + context.getString(R.string.date_time_detection) + date_time_detected;
                        Log.i("PointDataRetriever", "entered not -1");
                    }
                    else //urgent problem list
                    {
                        Log.i("PointDataRetriever", "entered -1");
                        problemInfoFromDB += context.getString(R.string.date_time_detection) + date_time_detected;
                    }
                    textView.setText(problemInfoFromDB);
                }
                catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    public static void fillInPointDataToTextViews(final Context context, final TextView shopTextView, final TextView equipmentTextView,
                  final TextView pointTextView, final TextView subpointTextView, int shopNo, final int equipmentNo, final int pointNo, final int subpointNo)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {

                    final String shopName, equipmentName, pointName, subPointName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    shopTextView.setText(shopName);
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    equipmentTextView.setText(equipmentName);
                    pointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + context.getString(R.string.point_name)).getValue()).toString();
                    pointTextView.setText(pointName);
                    if (subpointTextView != null) {
                        subPointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + "subpoint_" + subpointNo + "/" + context.getString(R.string.subpoint_description)).getValue()).toString();
                        subpointTextView.setText(subPointName);
                    }
                }
                catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public static void setTextOfCallsList(final Context context, final TextView textView, int shopNo, final int equipmentNo, final int pointNo, final String dateTime, final String calledBy)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {

                    final String shopName, equipmentName, pointName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    pointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + context.getString(R.string.point_name)).getValue()).toString();
                    String callInfo = context.getString(R.string.shop) + shopName + "\n" + context.getString(R.string.equipmentLine) + equipmentName + "\n"
                            + context.getString(R.string.nomer_point_textview) + pointName + "\n" + context.getString(R.string.date_time_of_call) + dateTime + context.getString(R.string.called_by) + calledBy;
                    textView.setText(callInfo);
                }
                catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    public static void setTextOfACheck(final Context context, final TextView textView, int shopNo, final int equipmentNo, final String checked_by, final int num_of_detected_problems, final String time_or_date_finished, final String duration, final int textviewType)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {

                    final String shopName, equipmentName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    String equipmentInfo;
                    switch (textviewType)
                    {
                        case EQUIPMENT_NOT_CHECKED:
                            equipmentInfo = shopName + "\n" + equipmentName;
                            break;
                        case EQUIPMENT_CHECKED:
                            equipmentInfo = shopName + "\n" + equipmentName + "\n" + context.getString(R.string.checked_by) + checked_by + context.getString(R.string.num_of_problems) +
                                    num_of_detected_problems + context.getString(R.string.end_time) + time_or_date_finished + context.getString(R.string.time_spent) + duration;
                            break;
                        case SEPARATE_CHECK_DETAILS:
                            equipmentInfo = shopName + "\n" + equipmentName + "\n" + time_or_date_finished;
                            break;
                        default:
                            throw new IllegalArgumentException("Unexpected value: " + textviewType);
                    }
                    textView.setText(equipmentInfo);
                }
                catch (NullPointerException | IllegalArgumentException e) {ExceptionProcessing.processException(e);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    public static void setNotificationText(final Context context, final NotificationCompat.Builder builder, final NotificationManager notificationManager, final long maintanceProblemsNotificationsCount,
                                           int shopNo, final int equipmentNo, final int pointNo, final String preppedText)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {
                    final String shopName, equipmentName, pointName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    pointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + context.getString(R.string.point_name)).getValue()).toString();
                    String equipmentInfo;
                    equipmentInfo = preppedText + shopName + "\n" + equipmentName + "\n" + pointName;
                    builder.setContentText(shopName)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(equipmentInfo));
                    Objects.requireNonNull(notificationManager).notify((int) maintanceProblemsNotificationsCount, builder.build());
                    Vibration.vibration(context);
                }
                catch (NullPointerException e) {ExceptionProcessing.processException(e);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    public static void setQRDirections(final Context context, final TextView textView, int shopNo, final int equipmentNo, final int pointNo)
    {
        DatabaseReference shopNameRef = getInstance().getReference(context.getString(R.string.shops_ref) + "/" + shopNo);
        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot shopSnap) {
                try {

                    final String shopName, equipmentName, pointName, subPointName;
                    shopName = Objects.requireNonNull(shopSnap.child(context.getString(R.string.shop_name)).getValue()).toString();
                    equipmentName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/" + context.getString(R.string.equipment_name)).getValue()).toString();
                    pointName = Objects.requireNonNull(shopSnap.child("Equipment_lines/" + equipmentNo + "/Points/" + pointNo + "/" + context.getString(R.string.point_name)).getValue()).toString();
                    String directionsText = context.getString(R.string.go_to) + shopName + "\n" + equipmentName + "\n" + context.getString(R.string.nomer_point_textview) + pointNo + "\n" + pointName;
                    textView.setText(directionsText);
                    textView.setVisibility(View.VISIBLE); //данные получены и directions составлены, сделаем текствью видимым
                }
                catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

    }
}

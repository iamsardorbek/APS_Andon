package com.akfa.apsproject.monitoring_activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akfa.apsproject.R;
import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.PointDataRetriever;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class SeparateCheckDetails extends AppCompatActivity {
    LinearLayout linearLayout;
    TextView checkInfo;
    private String date;
    private int shopNo, equipmentNo;
    Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_separate_check_details);
        setTitle(getString(R.string.maintenance_check_details)); //если нет проблем, надо сделать: нету проблем
        try {
            initInstances();
        } catch (AssertionError ae) {
            ExceptionProcessing.processException(ae, getResources().getString(R.string.activity_called_outside_or_wrong_intent_call), getApplicationContext(), SeparateCheckDetails.this);
        }
    }
    @SuppressLint("SetTextI18n")
    private void initInstances() {
        args = getIntent().getExtras();
        assert args != null;
        shopNo = args.getInt("Номер цеха");
        equipmentNo = args.getInt("Номер линии");
        date = args.getString("Дата");
        linearLayout = findViewById(R.id.linearLayout);
        checkInfo = findViewById(R.id.check_info);
        checkInfo.setText(shopNo + "\n" + equipmentNo + "\n" + date);
        PointDataRetriever.setTextOfACheck(getBaseContext(), checkInfo, shopNo, equipmentNo, "", -1, date, "", PointDataRetriever.SEPARATE_CHECK_DETAILS);
        initImages();
    }

    private void initImages()
    {
        StorageReference todayRequiredPics = FirebaseStorage.getInstance().getReference("required_pictures/" + date + "/" + shopNo + "/" + equipmentNo);
        todayRequiredPics.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @SuppressLint ("ResourceType") @Override public void onSuccess(ListResult listResult) {
                for (StorageReference picture : listResult.getItems()) {

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(20, 25, 20, 10);
                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setLayoutParams(params);
                    imageView.setBackgroundColor(Color.parseColor(getString(R.color.borders)));
                    imageView.setPadding(4, 4, 4, 4);
                    Glide.with(getApplicationContext()).load(picture).into(imageView); //load the pic from FB top imageview
                    linearLayout.addView(imageView);

                    //фото какого участка это?
                    int separatorIndex = picture.getName().indexOf("|");
                    String neededPictureName = picture.getName().substring(0, separatorIndex);

                    TextView dateTextView = new TextView(getApplicationContext());
                    dateTextView.setText(neededPictureName);
                    dateTextView.setPadding(25, 25, 25, 25);
                    dateTextView.setTextColor(Color.parseColor(getString(R.color.text)));
                    dateTextView.setTextSize(16);
                    dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    dateTextView.setPaintFlags(dateTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                    dateTextView.setLayoutParams(params);
                    dateTextView.setClickable(false);
                    dateTextView.setGravity(Gravity.CENTER);
                    linearLayout.addView(dateTextView);
                }
            }
        })
            .addOnFailureListener(new OnFailureListener() {
                @Override public void onFailure(@NonNull Exception e) {
                    Log.i("TAQ", "Failure Storage");
                    ExceptionProcessing.processException(e);
                }
            });

    }
}

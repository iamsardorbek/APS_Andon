package com.akfa.apsproject.pult_and_urgent_problems;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.akfa.apsproject.classes_serving_other_classes.ExceptionProcessing;
import com.akfa.apsproject.classes_serving_other_classes.GenerateRandomString;
import com.akfa.apsproject.R;
import com.akfa.apsproject.general_data_classes.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;


//------ПОКАЗЫВАЕТ QR КОД НА ПУЛЬТЕ ОПЕРАТОРА, ЧТОБЫ ПОДОШЕДШИЙ СПЕЦИАЛИСТ ОТСКАНИРОВАЛ (ДОКАЗАТЕЛЬСТВО ЧТО ОН ПРИШЕЛ НА САМОМ ДЕЛЕ / REALITY CHECK) ------//
//------ТАКЖЕ ЧЕРЕЗ 15 СЕКУНД ПОСЛЕ СВОЕГО ЗАПУСКА, ЗАПУСКАЕТ RUNNABLE, ОБНОВЛЯЮЩИЙ КАЖДЫЕ 15 СЕК QR ТЕКУЩЕЙ СРОЧНОЙ ПРОБЛЕМЫ (ЧТОБЫ ИЗБЕЖАТЬ ТОГО, ЧТО ОПЕРАТОР ОТПРАВЛЯЕТ ЧЕРЕЗ ТГ СКРИН QR И МАСТЕР НЕ ПРИХОДИТ)------//
public class QRCodeDialog extends DialogFragment {
    private static final int BACK_PRESSED = 1, SPECIALIST_CAME = 2;
    private QRCodeDialogListener listener; //обрабатывает и передает данные в pultActivity с помощью интерфейса

    private static final long QR_REFRESH_TIME = 15000; //in millis
    private int whoIsNeededIndex; //
    private boolean runnableStarted = false; //чтобы в нужное время останавливать runnable обновляющий QR Код


    //относ к runnable обновления QR кода
    private Handler handler = new Handler();
    private Runnable runnableCode;
    private boolean stopped = false;

    View view; //view всего диалога
    ImageView qrCode;
    Button back;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_qr_code, container, false);
        try {
            qrCode = view.findViewById(R.id.qr_code);
            back = view.findViewById(R.id.back);
            Bundle arguments = getArguments();
            final String nomerPulta, whoIsNeeded;
            final int shopNo, equipmentNo;
            shopNo = Objects.requireNonNull(arguments).getInt("Номер цеха");
            equipmentNo = arguments.getInt("Номер линии");
            nomerPulta = arguments.getString("Номер пульта");
            whoIsNeededIndex = arguments.getInt("Вызвать специалиста");
            whoIsNeeded = arguments.getString("Должность вызываемого специалиста");

            final DatabaseReference urgentProblemsRef = FirebaseDatabase.getInstance().getReference("Urgent_problems");
            //этот слушатель БД единожды находит рассматриваемую оператором срочную проблему, находить в ней пару "qr_random_code : value"
            //и устанавливает многоразовый слушатель именно на эту пару (для каждого обновления QR кода)
            urgentProblemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot urgentProblemsSnap) {
                    for (final DataSnapshot urgentProblemSnap : urgentProblemsSnap.getChildren()) //пройди по срочным проблемам, пока не найдешь нужную нам сейчас
                    {
                        try {
                            UrgentProblem urgProb = Objects.requireNonNull(urgentProblemSnap.getValue(UrgentProblem.class));
                            if (urgProb.getShop_no() == (shopNo) && urgProb.getEquipment_no() == (equipmentNo) && UserData.login.equals(urgProb.getOperator_login())
                                    && urgProb.getStatus().equals("DETECTED") && urgProb.getPult_no().equals(nomerPulta) && urgProb.getWho_is_needed_position().equals(whoIsNeeded)) { //проблема с теми же данными, и к которой еще специалист не пришел
                                if (!runnableStarted) { //если runnable не запустил еще, запусти
                                    initializeRunnable(urgentProblemSnap.getKey());
                                    stopped = false;
                                    handler.postDelayed(runnableCode, QR_REFRESH_TIME); //runnable запустится через QR_REFRESH_TIME мс
                                }
                                final DatabaseReference urgentProblemRef = urgentProblemSnap.getRef();
                                urgentProblemRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot urgentProblemInnerSnap) {
                                        String status = Objects.requireNonNull(urgentProblemInnerSnap.child("status").getValue()).toString();
                                        if (status.equals("SPECIALIST_CAME")) {
                                            closeDialog(SPECIALIST_CAME);
                                            urgentProblemRef.removeEventListener(this);
                                        } else {
                                            String qrCodeString = Objects.requireNonNull(urgentProblemSnap.child("qr_random_code").getValue()).toString();
                                            generateQrBitmap(qrCodeString); //к сожалению, контент image view с QR не обновляется
                                        }
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                                DatabaseReference urgentProblemQrRandomCodeRef = urgentProblemsRef.child(Objects.requireNonNull(urgentProblemSnap.getKey())).child("qr_random_code");
                                urgentProblemQrRandomCodeRef.addValueEventListener(new ValueEventListener() { //прислушивайся к изменениям в БД
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot qrRandomCodeSnap) {
                                        String qrCodeString = Objects.requireNonNull(urgentProblemSnap.child("qr_random_code").getValue()).toString();
                                        generateQrBitmap(qrCodeString); //к сожалению, контент image view с QR не обновляется
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                                return;
                            }
                        } catch (NullPointerException npe) {
                            ExceptionProcessing.processException(npe);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            back.setOnTouchListener(backButtonListener); //установи listener кликов кнопки
        } catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getContext());
        }
        return view;
    }

    public interface QRCodeDialogListener { //иниц интерфейса
        void onQRCodeDialogCanceled(int whoIsNeededIndex, int andonState);
    }

    // Define the code block to be executed

    private void initializeRunnable(final String urgentProblemKey)
    {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                if(!stopped) {
                    DatabaseReference urgentProblemQRCodeRef = FirebaseDatabase.getInstance().getReference("Urgent_problems/" + urgentProblemKey + "/qr_random_code");
                    String randomlyGeneratedCode = GenerateRandomString.randomString(3);
                    urgentProblemQRCodeRef.setValue(randomlyGeneratedCode);
                    // Repeat this the same runnable code block again another 2 seconds
                    // 'this' is referencing the Runnable object
                    handler.postDelayed(this, QR_REFRESH_TIME);
                }
            }
        };
    }

    View.OnTouchListener backButtonListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //что делать, если юзер нажал кнопку назад
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN: //эффект зажатия
                    back.setBackgroundResource(R.drawable.red_rectangle_pressed);
                    break;
                case MotionEvent.ACTION_UP: //действие при клике
                    back.setBackgroundResource(R.drawable.red_rectangle);
                    closeDialog(BACK_PRESSED);
                    break;
            }
            return false;
        }
    };

    public void closeDialog(int code)
    {
        if(!stopped) //чтобы повторно не вызывал dismiss() пустого диалога (already dismissed dialog)
            switch (code)
            {
                case BACK_PRESSED:
                    stopped = true; //чтобы runnable остановился
                    listener.onQRCodeDialogCanceled(whoIsNeededIndex, 1); //отправим через интерфейс диалога в PultActivity сообщение, что нажали "назад" или специалист пришел
                    try {// и закрой диалог
                        Objects.requireNonNull(getDialog()).dismiss();
                    } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                    break;
                case SPECIALIST_CAME:
                    stopped = true; //чтобы runnable остановился
                    listener.onQRCodeDialogCanceled(whoIsNeededIndex, 2); //отправим через интерфейс диалога в PultActivity сообщение, что нажали "назад" или специалист пришел
                    try {// и закрой диалог
                        Objects.requireNonNull(getDialog()).dismiss();
                    } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                    break;

            }

    }

    private void generateQrBitmap(String qrCodeString)
    {
        //из строки qrCodeString сгенерировать QR Код
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeString, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        }
        catch (WriterException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            return new Dialog(Objects.requireNonNull(getActivity()), getTheme()){
                @Override public void onBackPressed() {
                    listener.onQRCodeDialogCanceled(whoIsNeededIndex, 1); // при нажатии на кнопку назад дай об ээтом знать PultActivity
                    try {// и закрой диалог
                        Objects.requireNonNull(getDialog()).dismiss();
                    } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                }
            };
        }
        catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe, getResources().getString(R.string.program_issue_toast), getContext());
            try {// и закрой диалог
                Objects.requireNonNull(getDialog()).dismiss();
            } catch (NullPointerException npe1) {ExceptionProcessing.processException(npe1);}
            return null; //просто чтобы NPE or warning не было, возвращаем пустой объект
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { //прикрепи интерфейс к диалогу
            listener = (QRCodeDialog.QRCodeDialogListener) context;
        } catch (ClassCastException e) { throw new ClassCastException(context.toString() + "must implement QRCodeDialogListener"); }
    }
}

package com.akfa.apsproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeDialog extends DialogFragment {
    ImageView qrCode;
    Button back;
    private QRCodeDialogListener listener;
    private int whoIsNeededIndex;
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_qr_code, container, false);
        qrCode = view.findViewById(R.id.qr_code);
        back = view.findViewById(R.id.back);
        String qrCodeString = getArguments().getString("Код"); //encode in the QR code
        whoIsNeededIndex = getArguments().getInt("Вызвать специалиста");
        Log.i("TAG", "Encoded text" + qrCodeString);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeString, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            Log.i("TAG", "ImageViewID" + qrCode.getId());
            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_UP:
                        listener.onQRCodeDialogCanceled(whoIsNeededIndex);
                        getDialog().dismiss();
                        break;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
                listener.onQRCodeDialogCanceled(whoIsNeededIndex);
                getDialog().dismiss();
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (QRCodeDialog.QRCodeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement QRCodeDialogListener");
        }
    }

    public interface QRCodeDialogListener {
        void onQRCodeDialogCanceled(int whoIsNeededIndex);
    }
}

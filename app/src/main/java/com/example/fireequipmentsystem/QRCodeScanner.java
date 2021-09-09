package com.example.fireequipmentsystem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRCodeScanner extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView textView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    Global gv;
    String placetmp = "none";


    final ExecutorService service = Executors.newSingleThreadExecutor();
    HashMap<String, Boolean> id;
    Vibrator myVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        getPermissionsCamera();
        gv = (Global) getApplicationContext();
        myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        surfaceView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.textView);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(400, 400).setAutoFocusEnabled(true).build();


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                    return;
                try {
                    cameraSource.start(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0) {
                    final String QRvalue = qrCodes.valueAt(0).displayValue;
                    switch (gv.intentfrom) {
                        case ("inventory"): {
                            if (gv.idset.containsKey(QRvalue)) {
                                if (!gv.idset.get(QRvalue)) {
                                    gv.idset.put(QRvalue, true);
                                    myVibrator.vibrate(100);
                                    Snackbar.make(getWindow().getDecorView().getRootView(), QRvalue, Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                }
                            }
                            break;
                        }


                        case ("lend"): {
                            if (!gv.idset.containsKey(QRvalue)) {
                                gv.idset.put(QRvalue, false);
                                myVibrator.vibrate(100);
                                Snackbar.make(getWindow().getDecorView().getRootView(), QRvalue, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                            }

                            break;
                        }

                        case ("itemback"): {

                            if (gv.placeset.values().contains(QRvalue) && !QRvalue.equals(placetmp)) {
                                placetmp = QRvalue;
                                myVibrator.vibrate(300);
                                Snackbar.make(getWindow().getDecorView().getRootView(), QRvalue, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                            }

                            if (gv.idset.keySet().contains(QRvalue) && gv.placeset.get(QRvalue).equals(placetmp)) {
                                if (!gv.idset.get(QRvalue)) {
                                    gv.idset.put(QRvalue, true);
                                    Snackbar.make(getWindow().getDecorView().getRootView(), QRvalue, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                }

                            }


                            break;
                        }


                    }


                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            setResult(1);
        }
        return super.onKeyDown(keyCode, event);
    }

    //相機權限
    public void getPermissionsCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }


}
package com.mulin.larlock.larlock;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRScanner extends AppCompatActivity {

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;

    private SharedPreferences recordData;
    private static final String POST = "POST";
    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private Boolean check;

    private String RSBuildIP="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:" + "QRScan onCreate");
        setContentView(R.layout.activity_qrscan);
        getPermissionCamera();
        surfaceView = (SurfaceView)findViewById(R.id.qrscanView);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource=new CameraSource.Builder(this,barcodeDetector).setRequestedPreviewSize(300,300).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)
                        !=PackageManager.PERMISSION_GRANTED)
                    return;
                try{
                    cameraSource.start(surfaceHolder);
                }catch (IOException e){
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
                final SparseArray<Barcode> qrCodes=detections.getDetectedItems();
                if(qrCodes.size()!=0){
                    Log.d("QRscan",qrCodes.valueAt(0).displayValue);
                    barcodeDetector.release();
                    updaterelationship(qrCodes.valueAt(0).displayValue);
                }
            }
        });

    }

    private void updaterelationship(final String qrstring)
    {
        final String session =recordData.getString("sessionid","");
        service.submit(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder params = new FormBody.Builder();
                params.add("token", qrstring);
                FormBody formBody = params.build();
                Request request = new Request.Builder()
                        .url(RSBuildIP)
                        .addHeader("cookie",session)
                        .method(POST, formBody)
                        .build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    Log.d("QRscan",resStr);

                    JSONObject array = new JSONObject(resStr);

                    check=array.getBoolean("isOk");
//                    if(check)
//                    {
//
//                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(check)
                            {
                                Log.d("QRscan","success Build");
                                final android.support.v7.app.AlertDialog.Builder firstLoginDialog1 = new android.support.v7.app.AlertDialog.Builder(QRScanner.this);
                                firstLoginDialog1.setTitle("已成功加入監護行列")
                                        .setMessage("已成功加入監護行列")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                            else
                            {
                                Log.d("QRscan","failure Build");
                                final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(QRScanner.this);
                                firstLoginDialog.setTitle("QRCode已過期或網路錯誤")
                                        .setMessage("請稍後再做輸入")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("QRscan","exception");
                    e.printStackTrace();
                }
            }
        });
    }


    //偵測相機有沒有打開 QRcode掃描用
    private void getPermissionCamera()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"QRScan onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"QRScan onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"QRScan onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"QRScan onDestroy");
        super.onDestroy();
    }
}

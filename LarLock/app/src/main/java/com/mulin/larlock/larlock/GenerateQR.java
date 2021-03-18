package com.mulin.larlock.larlock;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class GenerateQR extends AppCompatActivity {

    private ImageView qrcode;
    private String QRstring;
    private Boolean title;


    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private SharedPreferences recordData;
    private JSONObject jsonObject;
    private String QRIP="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:" + "generateQR onCreate");
        setContentView(R.layout.activity_beguardqr);
        qrcode=(ImageView)findViewById(R.id.imageQR);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
    }

    private void getQrtoken()
    {
        service.submit(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder params = new FormBody.Builder();
//                Log.d("TTT", "onClick: A");
                String session =recordData.getString("sessionid","");
                Request request = new Request.Builder()
                        .url(QRIP)
                        .addHeader("cookie",session)
                        .build();
//                Log.d("TTT", "onClick: C");
                try {
//                                Log.d("TTT", "onClick: B");
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    Log.d("QRcode",resStr);

                    JSONObject array = new JSONObject(resStr);
                    title=array.getBoolean("isOk");
                    if(title)
                    {
                        QRstring=array.getString("data");
                        Log.d("QRcode", URLDecoder.decode(QRstring, "UTF-8"));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(title)
                            {
                                getQRcode();
                            }
                            else
                            {
                                Log.d("QRcode","failure getString");
                                final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(GenerateQR.this);
                                firstLoginDialog.setTitle("錯誤代碼")
                                        .setMessage("錯誤代碼")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                                finish();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("QRcode","exception");
                    e.printStackTrace();
                }
            }
        });
    }

    private void getQRcode()
    {
        BarcodeEncoder encoder = new BarcodeEncoder();
        try{
            Bitmap bit = encoder.encodeBitmap(QRstring,BarcodeFormat.QR_CODE,250,250);
            qrcode.setImageBitmap(bit);
        }catch (WriterException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"generateQR onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"generateQR onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"generateQR onResume");
        super.onResume();
        getQrtoken();
        //getQRcode();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"generateQR onDestroy");
        super.onDestroy();
    }
}

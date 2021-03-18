package com.mulin.larlock.larlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BLE_Service extends Service{

    private byte [] powerOpenCmd={}; //電門開啟
    final Handler mHandler=new Handler();
    private SharedPreferences recordData;
    private String bleMac="";
    Runnable runnable;
    Notification notification;
    private int counterCounter=0;
    private boolean firstStart=true;
    private boolean connectFlag=true;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {

        Intent inetnet = new Intent(this, Main_Page.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,inetnet,PendingIntent.FLAG_UPDATE_CURRENT);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O) {
            NotificationChannel larlockchannel = new NotificationChannel("Channel1", "LarLockChannel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(larlockchannel);
            notification=new NotificationCompat.Builder(this,"Channel1")
                    .setContentTitle("LarLock背景程式")
                    .setContentText("靠近解鎖服務")
                    .setSmallIcon(R.drawable.ic_action_user)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        else {
            notification = new Notification.Builder(this)
                    .setContentTitle("LarLock背景程式")
                    .setContentText("靠近解鎖服務")
                    .setSmallIcon(R.drawable.ic_action_user)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        bleMac = Main_Page.mClientBTConnect.getBTDevice();
        Main_Page.ble_search.Near_Start_Scanning();
        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        runnable=new Runnable() {
            @Override
            public void run() {
                    Log.d("Test","Message0");
                   // Log.d("bleMac",Main_Page.mClientBTConnect.getBTDevice());
                    Log.d("Rssi:",Integer.toString(BLE_SEARCH.bleRssi));
                try{
                    if(BLE_SEARCH.bleRssi>=0 && BLE_SEARCH.bleRssi<65 )
                    {
                        Log.d("Test","Message1");
                        Main_Page.ble_search.Near_Stop_Scanning();
                        if(ClientBTConnect.ConnectState==0) {
                            Log.d("Test", "Message2");
                            Main_Page.mClientBTConnect.connectBTDevice(bleMac);
                        }
                        else if(ClientBTConnect.ConnectState==2 && connectFlag)
                        {
                            Log.d("Test","Message3");
                            connectFlag=false;
                            //Main_Page.mClientBTConnect.sendData(powerOpenCmd);
                        }
                    }
                    else
                    {
                        Log.d("Test","Message4");
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("錯誤" + e.toString());
                }
                mHandler.postDelayed(this,100);
            }
        };
        mHandler.postDelayed(runnable,1000);
        //mHandler.post(runnable);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        //BLE_Page.mGatt.disconnect();
        //BLE_Page.mGatt=null;
        //BLE_Page.ConnectState=0;
        //BLE_Page.SendStatus=false;
        if(ClientBTConnect.ConnectState==2) {
            Main_Page.mClientBTConnect.disConnectBTDevice();
        }
       // ClientBTConnect.ConnectState=0;
        //ClientBTConnect.SendStatus=false;
        stopForeground(true);
        mHandler.removeCallbacks(runnable);
    }

    /*private boolean getRssi()
    {
        return Main_Page.mClientBTConnect.getRemoteRssi();
    }*/
}

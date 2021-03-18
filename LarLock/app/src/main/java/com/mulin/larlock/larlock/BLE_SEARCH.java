package com.mulin.larlock.larlock;


import android.app.AlertDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

public  class BLE_SEARCH{

    public static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothLeScanner mBleScanner;
    private static ScanSettings mBleSetting;

    //private ArrayAdapter<String> BLE_Name_Adapter=null;
    private Set<String> BLE_Address=new LinkedHashSet<>();
    private Set<String> BLE_Name=new LinkedHashSet<>();
    private  String[] BLE_Address_Array=null;
    private  String[] BLE_Name_Array=null;

    private List<HashMap<String,String>> mapList=new ArrayList<>();
    private ListAdapter listAdapter=null;

    private CallBack2 mCallback2;

    private Dialog waitdialog;
    private Context mContext;
    private View view;

    private String bleMac;
    public static int bleRssi=-1;
    private boolean nearScanning;

    public BLE_SEARCH()
    {
        mCallback2=new CallBack2();
        bleMac=Main_Page.mClientBTConnect.getBTDevice();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void search(final View v, final Context context){
        Handler handler = new Handler();
        Runnable runnable = null;
        this.view=v;
        this.mContext=context;
        try {
            runnable=new Runnable() {

                @Override
                public void run() {
                    mBleScanner.stopScan(mCallback2);
                    waitdialog.dismiss();
                    showlist();
                }
            };
            //mCallback2=new CallBack2();
            BLE_Name.clear();
            BLE_Address.clear();
            mapList.clear();
            mBleScanner.startScan(null, mBleSetting, mCallback2);
            waitdialog= ProgressDialog.show(context,"掃描中","請等待3秒...",true);
            handler.postDelayed(runnable,3000);
        }
        catch (IllegalStateException e)
        {
            handler.removeCallbacks(runnable);
        }
    }

    public void Near_Start_Scanning()
    {
        Log.d("BLE_SEARCH"," Near_Start_Scanning");
        mBleScanner.startScan(null, mBleSetting, mCallback2);
        nearScanning=true;
    }

    public void Near_Stop_Scanning()
    {
        if(nearScanning) {
            Log.d("BLE_SEARCH", " Near_Stop_Scanning");
            mBleScanner.stopScan(mCallback2);
            nearScanning=false;
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class CallBack2 extends ScanCallback
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(result==null||result.getDevice()==null|| TextUtils.isEmpty(result.getDevice().getName()))
                return;
            //Log.d("Found:",result.getDevice().getAddress());
            //Log.d("Targe:",Main_Page.mClientBTConnect.getBTDevice());
            BLE_Address.add(result.getDevice().getAddress());
            BLE_Address_Array=BLE_Address.toArray(new String[BLE_Address.size()]);
            BLE_Name.add(result.getDevice().getName());
            if(result.getDevice().getAddress().equals(bleMac))
            {
                //bleMac=result.getDevice().getAddress();
                bleRssi=-result.getRssi();
                //Log.d("Rssi:",Integer.toString(bleRssi));
            }
            BLE_Name_Array=BLE_Name.toArray(new String[BLE_Name.size()]);

        }
    }

    private void showlist()
    {
        if(BLE_Name.isEmpty())
            Toast.makeText(mContext,"周遭沒有藍牙裝置",Toast.LENGTH_SHORT).show();
        else if(!(BLE_Name.isEmpty())) {
            for (int i = 0; i < BLE_Name_Array.length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Address_list", BLE_Address_Array[i]);
                hashMap.put("Name_list", BLE_Name_Array[i]);
                mapList.add(hashMap);
            }
            listAdapter = new SimpleAdapter(mContext, mapList, android.R.layout.simple_list_item_2, new String[]{"Name_list", "Address_list"}, new int[]{android.R.id.text1, android.R.id.text2});

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("搜尋到的藍牙裝置");
            builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                        Main_Page.mClientBTConnect.setBTDevice(BLE_Address_Array[i]);
                        Main_Page.mClientBTConnect.connectBTDevice(BLE_Address_Array[i]);
                        Toast.makeText(mContext, "正在連接" + BLE_Name_Array[i], Toast.LENGTH_LONG).show();

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    //------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void initbluetooh(Context context, AppCompatActivity activity) {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBleSetting=new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();
        }
        if (mBluetoothAdapter == null)//不支持藍芽
        {
            activity.finish();
            return;
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))//不支持BLE藍芽
        {
            activity.finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) { //判斷藍芽是否打開
            Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //沒打開有請求
            activity.startActivityForResult(btEnable, 1);
        }
        while (mBleScanner==null)
            mBleScanner=mBluetoothAdapter.getBluetoothLeScanner();

    }

}

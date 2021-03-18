package com.mulin.larlock.larlock;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ClientBTConnect extends Thread {
    private final int BT_DATA = 1;
    private final String TAG = ClientBTConnect.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private Context mContext;
    private IMonitorDataView view;
    private BTHandler handler = new BTHandler();
    Intent gattService;

    public static int ConnectState;
    public static boolean SendStatus=false;
    public static boolean Servicestate=false;
    private static boolean connectFlag=true;
    public static int Rssi=0;

    public ClientBTConnect(Context context, IMonitorDataView view) {
        this.mContext = context;
        this.view = view;
        gattService = new Intent(mContext, BluetoothLeService.class);
       // mContext.bindService(gattService, mServiceConnection, mContext.BIND_AUTO_CREATE);
    }


    public void setBTDevice(String address) {
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.TANGRAM_BT_DEVICE, MODE_PRIVATE);
        preferences.edit().putString(Constant.BT_DEVICE, address).commit();
    }

    public String getBTDevice() {
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.TANGRAM_BT_DEVICE, MODE_PRIVATE);
        return preferences.getString(Constant.BT_DEVICE, Constant.NO_DEVICE);
    }

    public IntentFilter regReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constant.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constant.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constant.ACTION_DATA_WRITE);
        intentFilter.addAction(Constant.ACTION_GATT_CONNECT_TIMEOUT);
        intentFilter.addAction(Constant.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    public void closeBluetoothLeService() {
        this.mBluetoothLeService = null;
        mContext.unbindService(mServiceConnection);
    }

    public BluetoothLeService getBluetoothLeService() {return this.mBluetoothLeService; }

    public boolean connectBTDevice(String address) {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && Build.VERSION.SDK_INT > 21) {
            return false;
        } else {
            //TODO 判斷建立服務是否放在這
            if(mServiceConnection !=null && mBluetoothLeService!=null) {
                if(mBluetoothLeService.getBle_Connection() == Constant.BLE_STATE_DISCONNECTED) {
                    mBluetoothLeService.ble_Connect(address);
                }
                /*else{
                    if(mWriteCharacteristic !=null){
                        //sendData();           //待解
                    }
                }*/

            }
            else {
                gattService = new Intent(mContext, BluetoothLeService.class);
                mContext.bindService(gattService, mServiceConnection, mContext.BIND_AUTO_CREATE);
            }
        }

        return true;
    }

    public void disConnectBTDevice(){
        mBluetoothLeService.ble_DisConnect();
    }

    public ServiceConnection getServiceConnection() {
        return this.mServiceConnection;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.BleBinder) service).getService();
            if (!mBluetoothLeService.ble_Initialize()) {
                mBluetoothLeService = null;
            }
            if (!getBTDevice().equals(Constant.NO_DEVICE)) {
                mBluetoothLeService.ble_Connect(getBTDevice());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    public BroadcastReceiver getGattUpdateReceiver() {
        return this.mGattUpdateReceiver;
    }

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constant.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_GATT_CONNECTED");
                ConnectState=Constant.BLE_STATE_CONNECTED;
                Servicestate=true;
                Main_Page.firstautoconnect=false;
                Toast.makeText(context,"藍芽已連線",Toast.LENGTH_LONG).show();
            }
            if(Constant.ACTION_GATT_DISCONNECTED.equals(action))
            {
                Log.d(TAG,"onReceive: ACTION_GATT_DISCONNECTED");
                ConnectState=Constant.BLE_STATE_DISCONNECTED;
                Servicestate=false;
                SendStatus=false;
                Toast.makeText(context,"藍芽已斷線，請重新連線",Toast.LENGTH_LONG).show();
            }
            if (Constant.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED1");
                try {
                    if (mBluetoothLeService.getSupportedGattServices() != null) {
                        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
                        BluetoothGattCharacteristic gattCharacteristics = null;
                        for(BluetoothGattService gattService:gattServices){
                            String serviceUUID = gattService.getUuid().toString();
                            if(serviceUUID.equals(Constant.BLE_2640_SERVICE_UUID)){                 //掃到0000fff0服務 要再掃裡面的fff6
                                List<BluetoothGattCharacteristic> gattCharacteristicList=gattService.getCharacteristics();
                                for(BluetoothGattCharacteristic serviceCharacteristic:gattCharacteristicList) {     //掃fff6
                                    String CharacteristicUUID=serviceCharacteristic.getUuid().toString();
                                    if(CharacteristicUUID.equals(Constant.BLE_BDE_UUID)) {
                                        gattCharacteristics =serviceCharacteristic;
                                        Log.d(TAG, "Find Service:"+CharacteristicUUID);
                                    }
                                }
                            }
                        }
                        if (gattCharacteristics != null) {
                            final BluetoothGattCharacteristic characteristic = gattCharacteristics;
                           // final BluetoothGattCharacteristic characteristic1 = gattCharacteristics.get(gattCharacteristics.size() - 2);
                            final int charaProp = characteristic.getProperties();
                           // final int charaProp1 = characteristic1.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                Log.d(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED");
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            }
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                    //(charaProp1 | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                if(characteristic.getUuid().toString().equals(Constant.BLE_BDE_UUID)){
                                    mWriteCharacteristic = characteristic;
                                    SendStatus=true;
                                }
                                /*else if(characteristic1.getUuid().toString().equals(Constant.BLE_SEND_UUID)){
                                    mWriteCharacteristic = characteristic1;
                                }*/
                            }
                            //sendData();       //待解
                         /*   if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                               *//* if (mNotifyCharacteristic != null) {
                                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                                    mNotifyCharacteristic = null;
                                }*//*
                                Log.d(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED");
                                mBluetoothLeService.readCharacteristic(characteristic);
                            }*/

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Constant.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DATA_AVAILABLE1 ");

                try {
                   /* if (mBluetoothLeService.getSupportedGattServices() != null) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = mBluetoothLeService.getSupportedGattServices().get(mBluetoothLeService.getSupportedGattServices().size() - 1).getCharacteristics();
                        if (gattCharacteristics != null) {
                            final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(gattCharacteristics.size() - 1);
                            final int charaProp = characteristic.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0  ||
                                    (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {*/
                               /* if (mNotifyCharacteristic != null) {
                                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                                    mNotifyCharacteristic = null;
                                }*/

                                Log.d(TAG, "onCharacteristicReceive: ACTION_DATA_AVAILABLE " + DataTransform.byteArrayToStr(intent.getByteArrayExtra(Constant.EXTRA_DATA)) + " " + mNotifyCharacteristic.getUuid());
                                Message msg = handler.obtainMessage(BT_DATA);
                                Bundle bundle = new Bundle();
                                bundle.putByteArray("BT_RECEIVE_DATA", intent.getByteArrayExtra(Constant.EXTRA_DATA));
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            //}
                           /* if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            }*/
                        //}
                    //}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Constant.ACTION_DATA_WRITE.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DATA_WRITE");
            }else if(Constant.ACTION_GATT_CONNECT_TIMEOUT.equals(action)){
                //Message msg = handler.obtainMessage(BT_DATA);
                //Bundle bundle = new Bundle();
                //bundle.putString("BT_STATUS",Constant.ACTION_GATT_CONNECT_TIMEOUT);
                //msg.setData(bundle);
                //handler.sendMessage(msg);
                if(Main_Page.firstautoconnect==true)
                {
                    Toast.makeText(context,"開機自動連線逾時，請按右上角連線",Toast.LENGTH_LONG).show();
                    Main_Page.firstautoconnect=false;
                }
                else
                {
                    Toast.makeText(context,"連線逾時",Toast.LENGTH_LONG).show();
                }
                Log.d(TAG,"onReceive:ACTION_GATT_CONNECT_TIMEOUT");

            }
        }
    };

    public void sendData(byte[] value) {
        try {
           /* if (mBluetoothLeService.getSupportedGattServices() != null) {
                List<BluetoothGattCharacteristic> gattCharacteristics = mBluetoothLeService.getSupportedGattServices().get(mBluetoothLeService.getSupportedGattServices().size() - 1).getCharacteristics();
                if (gattCharacteristics != null) {
                    final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(gattCharacteristics.size() - 2);
                    final BluetoothGattCharacteristic characteristic1 = gattCharacteristics.get(gattCharacteristics.size()-1);
                    final int charaProp = characteristic.getProperties();
                    final int charaProp1 = characteristic1.getProperties();*/
                    //if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0 ||
                     //       (charaProp1 | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)> 0) {
                       /* if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                            mNotifyCharacteristic = null;
                        }*/
                        // bluetooth tx
                        Thread.sleep(150);
                        //byte[] value = DataTransform.strToByteArray("V");
                        mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, value);
                        Log.d(TAG, "onCharacteristicWriteSendData: " + mWriteCharacteristic.getUuid());
                    //}
                  /*  if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }*/
                //}
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class BTHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_DATA:
                    Bundle bundle = msg.getData();
                    view.updateComponetView(bundle);        //將藍芽接收到的資料傳出
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void bleNotify(final Context context)
    {

        final Handler mhandler=new Handler();
        final String bleMac;
        SharedPreferences recordData=context.getSharedPreferences("record",MODE_PRIVATE);
        //bleMac=recordData.getString("bleMac","");
        bleMac=getBTDevice();
        connectFlag=true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("藍芽未連接")
                .setMessage("按下重新連線來重新連接藍芽")
                .setPositiveButton("重新連線", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog notify = new ProgressDialog(context);
                        notify.setTitle("藍芽連線");
                        notify.setMessage("等待藍芽重新連線");
                        notify.show();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (!SendStatus) {
                                    if(connectFlag) {
                                        connectFlag=false;
                                        //BLE_Page.mGatt = BLE_Page.mBluetoothAdapter.getRemoteDevice(bleMac).connectGatt(context, false, BLE_Page.mGattCallBack);
                                        connectBTDevice(bleMac);
                                        mhandler.postDelayed(this, 3000);
                                    }
                                    else{
                                        notify.dismiss();
                                        try {
                                            //BLE_Page.mGatt.disconnect();
                                            disConnectBTDevice();
                                        }
                                        catch (NullPointerException e){
                                            new AlertDialog.Builder(context)
                                                    .setTitle("Warning")
                                                    .setMessage(e.toString())
                                                    .setPositiveButton("null pointer", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).show();
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(context,"連線逾時",Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    notify.dismiss();
                                }
                            }
                        };
                        mhandler.postDelayed(runnable, 1000);

                    }
                })
                .setNegativeButton("關閉", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }


//    //檢查用
//    public boolean CheckBLE(int select)
//    {
//        boolean result=false;
//        switch (select)
//        {
//            case 1:             //檢查BluetoothGatt
//                result=mBluetoothLeService.GattCheck();
//                break;
//            case 2:             //檢查BluetoothAdapter
//                result=mBluetoothLeService.AdapterCheck();
//                break;
//        }
//
//        return result;
//    }
}

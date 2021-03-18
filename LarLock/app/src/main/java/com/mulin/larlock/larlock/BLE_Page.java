package com.mulin.larlock.larlock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BLE_Page extends AppCompatActivity implements IMonitorDataView{
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 2;
    public static int ConnectState;

    public static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothLeScanner mBleScanner;
    public static ScanSettings mBleSetting;
    public static BluetoothGatt mGatt;

    private ArrayAdapter<String> BLE_Name_Adapter=null;
    private Set<String> BLE_Address=new LinkedHashSet<>();
    private Set<String> BLE_Name=new LinkedHashSet<>();
    private  String[] BLE_Address_Array=null;
    private ListView BLE_List=null;
    public static TextView connectText=null;
    private Toolbar toolBar=null;
    private ProgressBar progressBar;
    private ActionBar actionBar;
    private CallBack2 mCallback2;
    public static BluetoothGattCharacteristic SendCharacteristic=null;
    public static Boolean SendStatus=false;
    public static Boolean Servicestate=false;
    //public static String ReadData="";
    public static int []ReadData=new int[16];
    public static int Rssi=0;
    private static SharedPreferences recordData;
    private static boolean connectFlag=true;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_page);
        inittoolbar();
        BLE_List=(ListView)findViewById(R.id.listview1);
        BLE_Name_Adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        BLE_List.setAdapter(BLE_Name_Adapter);
        mBleSetting=new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        progressBar=(ProgressBar)findViewById(R.id.progressbar1);
        connectText=(TextView)findViewById(R.id.text10);
        mCallback2=new CallBack2();
        initbluetooh(this,this);
    }

    private void inittoolbar()
    {
        actionBar=getSupportActionBar();
        toolBar=(Toolbar)findViewById(R.id.toolbar2);
        toolBar.inflateMenu(R.menu.menu1_layout);
        //setSupportActionBar(toolBar);
        toolBar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.menu_search)
                {
                    search();
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void search() {
        Handler handler = new Handler();
        Runnable runnable = null;
        try {
            runnable=new Runnable() {

                @Override
                public void run() {

                    mBleScanner.stopScan(mCallback2);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            };
            progressBar.setVisibility(View.VISIBLE);
            BLE_Name.clear();
            BLE_Name_Adapter.clear();
            BLE_Address.clear();
            BLE_List.setOnItemClickListener(onItemClickListener);
            mBleScanner.startScan(null, mBleSetting, mCallback2);
            handler.postDelayed(runnable,3000);
        }
        catch (IllegalStateException e)
        {
            handler.removeCallbacks(runnable);
            progressBar.setVisibility(View.INVISIBLE);
            initbluetooh(this,this);
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mGatt.disconnect();
            mGatt=mBluetoothAdapter.getRemoteDevice(BLE_Address_Array[i]).connectGatt(BLE_Page.this,false,mGattCallBack);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void initbluetooh(Context context, AppCompatActivity activity) {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            registerBLEReceiver(activity,true);
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
    private static void registerBLEReceiver(Context context,Boolean registerFlag)
    {
        if(registerFlag==true) {
            IntentFilter stateChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            IntentFilter connectStateFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            IntentFilter disConnectStateFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(stateChangeReceiver, stateChangeFilter);
            context.registerReceiver(stateChangeReceiver, connectStateFilter);
            context.registerReceiver(stateChangeReceiver, disConnectStateFilter);
        }
        else
        {
            context.unregisterReceiver(stateChangeReceiver);
        }
    }
    public  static BroadcastReceiver stateChangeReceiver=new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                System.out.println("藍牙狀態:改變");
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            System.out.println("藍牙狀態:關閉");
                            mBluetoothAdapter=null;
                            ConnectState = STATE_DISCONNECTED;
                            SendStatus = false;
                            Servicestate = false;
                            if(mGatt!=null) {
                                mGatt.close();
                                mGatt = null;
                            }
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            System.out.println("藍牙狀態:開啟中");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            System.out.println("藍牙狀態:開啟");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            System.out.println("藍牙狀態:關閉中");
                            break;
                    }
                } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    System.out.println("藍牙狀態:已連線");
                    if (connectText != null)
                        connectText.setText("連接狀態:已連接");
                } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    System.out.println("藍牙狀態:未連線");
                    registerBLEReceiver(context,false);
                    if (connectText != null)
                        connectText.setText("連接狀態:未連接");
                }
        }
    };

    @Override
    public void updateComponetView(Bundle bundle) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class CallBack2 extends ScanCallback
    {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
                if(result==null||result.getDevice()==null|| TextUtils.isEmpty(result.getDevice().getName()))
                    return;
                BLE_Address.add(result.getDevice().getAddress());
                BLE_Address_Array=BLE_Address.toArray(new String[BLE_Address.size()]);
                if(BLE_Name.add(result.getDevice().getName()))
                    BLE_Name_Adapter.add(result.getDevice().getName());
                BLE_Name_Adapter.notifyDataSetChanged();
            }
    }

    public static BluetoothGattCallback mGattCallBack=new BluetoothGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState==STATE_DISCONNECTED) {
                Log.d("連接狀態","Test1");
                ConnectState=STATE_DISCONNECTED;
                SendStatus=false;
                Servicestate=false;
                mGatt.close();
                mGatt=null;
            }
            if(newState==STATE_CONNECTED) {
                Log.d("連接狀態","Test2");
                ConnectState=STATE_CONNECTED;
                Servicestate=true;
                mGatt.discoverServices();
            }
        }


        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d("MTU:",Integer.toString(mtu));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("連接狀態:服務");
            List<BluetoothGattService> gattservicelist=gatt.getServices();
            for(BluetoothGattService mGattService:gattservicelist)
            {
                List<BluetoothGattCharacteristic> gattCharacteristicslist = mGattService.getCharacteristics();
                for(BluetoothGattCharacteristic mGattCharacteristic:gattCharacteristicslist)
                {
                    if("0000fff6-0000-1000-8000-00805f9b34fb".equals(mGattCharacteristic.getUuid().toString()))
                    {
                        System.out.println("Test Find Service"+mGattCharacteristic.getUuid().toString());
                        boolean isEnableNotification =  BLE_Page.mGatt.setCharacteristicNotification(mGattCharacteristic, true);
                        if(isEnableNotification) {
                            List<BluetoothGattDescriptor> descriptorList = mGattCharacteristic.getDescriptors();
                            if(descriptorList != null && descriptorList.size() > 0) {
                                for(BluetoothGattDescriptor descriptor : descriptorList) {
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    BLE_Page.mGatt.writeDescriptor(descriptor);
                                }
                            }
                        }
                        System.out.println("UUIDTest");
                        SendCharacteristic=mGattCharacteristic;
                        SendStatus=true;
                    }
                }
            }

        }




        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d("Rssi:",Integer.toString(-rssi));
            Rssi=-rssi;
           /* double d;
            d=Math.pow(10.0,(Rssi-68)/10);
            System.out.println("Rssi d:"+d);*/
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("bytesArray",new String(characteristic.getValue()));
            byteToInt(characteristic.getValue());
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void bleNotify(final Context context)
        {

        final Handler mhandler=new Handler();
        final String bleMac;
        SharedPreferences recordData=context.getSharedPreferences("record",MODE_PRIVATE);
        bleMac=recordData.getString("bleMac","");
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
                                    if (!BLE_Page.SendStatus) {
                                        if(connectFlag) {
                                            connectFlag=false;
                                            BLE_Page.mGatt = BLE_Page.mBluetoothAdapter.getRemoteDevice(bleMac).connectGatt(context, false, BLE_Page.mGattCallBack);
                                            mhandler.postDelayed(this, 3000);
                                        }
                                        else{
                                            notify.dismiss();
                                            try {
                                                BLE_Page.mGatt.disconnect();
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


    private static void byteToInt(byte [] a)
    {
        for(int i=0;i<a.length;i++)
        {
            ReadData[i]=a[i]&0xFF;
        }
    }
    static public void Arrayinit()
    {
        for(int i=0;i<16;i++)
        {
            ReadData[i]=0;
        }
    }


    /*GPS Init*/
    private void GPSInit()
    {
        LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gps)
        {
            android.support.v7.app.AlertDialog.Builder locationBuilder=new android.support.v7.app.AlertDialog.Builder(this);
            locationBuilder.setTitle("定位未打開")
                           .setMessage("按下確定後跳轉到開啟定位介面，不打開搜尋不到藍牙裝置")
                           .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   Intent locationIntent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                   startActivityForResult(locationIntent,2);
                               }
                           })
                           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   finish();
                               }
                           })
                          .show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        GPSInit();
        super.onResume();
    }


}

package com.mulin.larlock.larlock;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

//TODO 藍牙關閉Adapter清空
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String BluetoothDeviceAddress;
    private IBinder bleBinder;
    private BluetoothDevice mBluetoothDevice;
    private StatusThread mStatusThread;



    private int mBluetoothConnction = Constant.BLE_STATE_DISCONNECTED;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("BluetoothLeService",Integer.toString(newState));
            if (newState == Constant.BLE_STATE_CONNECTED) {
                Log.i(TAG, "BluetoothGatt Connected");
                mBluetoothConnction = Constant.BLE_STATE_CONNECTED;
                broadcastUpdate(Constant.ACTION_GATT_CONNECTED);
                mBluetoothGatt.discoverServices();
            } else if (newState == Constant.BLE_STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothGatt Disconnected");
                mBluetoothConnction = Constant.BLE_STATE_DISCONNECTED;
                broadcastUpdate(Constant.ACTION_GATT_DISCONNECTED);
                mBluetoothGatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == Constant.BLE_SERVICE_SUCCESS) {
                Log.i(TAG, "BluetoothGatt Discovered Service:" + status);
                broadcastUpdate(Constant.ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "BluetoothGatt Discovered Service:" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constant.ACTION_DATA_AVAILABLE, characteristic);
                Log.d(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "BluetoothGatt CharacteristicWrite");
            broadcastUpdate(Constant.ACTION_DATA_WRITE, characteristic);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {           //接收到藍芽傳值
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "BluetoothGatt CharacteristicChanged");
            broadcastUpdate(Constant.ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d("Rssi:",Integer.toString(-rssi));
            ClientBTConnect.Rssi=-rssi;
           /* double d;
            d=Math.pow(10.0,(Rssi-68)/10);
            System.out.println("Rssi d:"+d);*/
        }
    };

    private BroadcastReceiver btStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF) {
                    mBluetoothAdapter = null;
                }
            }
        }
    };

    /*ble function*/
    public boolean ble_Initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean ble_Connect(String deviceAddress) {
        if (mBluetoothAdapter == null || deviceAddress == null) {
            Log.w(TAG, "Bluetooth not initialize or unspecified deviceAddress");
            return false;
        }
        if(mBluetoothConnction == Constant.BLE_STATE_DISCONNECTED) {
            mBluetoothConnction = Constant.BLE_STATE_CONNECTING;
            mStatusThread = new StatusThread();
            mStatusThread.start();
        }
        /*Previous Connected Device*/
        if (mBluetoothAdapter != null && BluetoothDeviceAddress != null && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            mBluetoothGatt.connect();
            return true;
        }

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (mBluetoothDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        BluetoothDeviceAddress = deviceAddress;
        return true;
    }

    public void ble_DisConnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or mBluetoothGatt is null");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    public void BLE_Close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public int getBle_Connection(){
        return mBluetoothConnction;
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or mBluetoothGatt is null");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or mBluetoothGatt is null");
            return;
        }
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or mBluetoothGatt is null");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

            boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            if (isEnableNotification) {
                List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                if (descriptorList != null && descriptorList.size() > 0) {
                    for (BluetoothGattDescriptor descriptor : descriptorList) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                }
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }


    /*Send BroadCast*/
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if ((Constant.BLE_SEND_UUID.equals(characteristic.getUuid().toString()) ||
                Constant.BLE_BDE_UUID.equals(characteristic.getUuid().toString()))&&
                intent.getAction().equals(Constant.ACTION_DATA_WRITE)) {
            Log.i(TAG, "BLE SendData:" + DataTransform.byteArrayToHexStr(characteristic.getValue()));
            intent.putExtra(Constant.EXTRA_DATA, characteristic.getValue());
        } else if ((Constant.BLE_RECERIVER_UUID.equals(characteristic.getUuid().toString()) ||
                Constant.BLE_BDE_UUID.equals(characteristic.getUuid().toString())) &&
                    intent.getAction().equals(Constant.ACTION_DATA_AVAILABLE)) {
            Log.i(TAG, "BLE ReadData:" + DataTransform.byteArrayToHexStr(characteristic.getValue()));
            intent.putExtra(Constant.EXTRA_DATA, characteristic.getValue());
        }
        sendBroadcast(intent);
    }


    /*Service LifeCycle*/
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class BleBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        bleBinder = new BleBinder();
        mStatusThread = new StatusThread();
        return bleBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        BLE_Close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private class StatusThread extends Thread{
        @Override
        public void run() {
            super.run();
            if(mBluetoothConnction == Constant.BLE_STATE_CONNECTING){
                try {
                    Thread.sleep(5000);
                    if(mBluetoothConnction == Constant.BLE_STATE_CONNECTING){
                        broadcastUpdate(Constant.ACTION_GATT_CONNECT_TIMEOUT);
                        mBluetoothConnction = Constant.BLE_STATE_DISCONNECTED;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    //檢查用
//    public boolean GattCheck()
//    {
//        if(mBluetoothGatt!=null)
//            return true;
//        else
//            return false;
//    }
//    public boolean AdapterCheck()
//    {
//        if(mBluetoothAdapter!=null)
//            return true;
//        else
//            return false;
//    }

}

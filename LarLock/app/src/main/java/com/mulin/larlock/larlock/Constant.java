package com.mulin.larlock.larlock;

import java.util.UUID;

public class Constant {

 /*   public final static String WIFI_PASSWORD = "123456789";
    public final static String SERVER = "120.113.96.29"; //"192.168.1.24";
    public final static int PORT = 666;
    public final static int SERVER_TIMEOUT = 6000;
    public final static String TANGRAM_FILTER = "Tangram";

    // title, subtitle
    public final static String TITLE_ACTIONBAR = "title";
    public final static String SUBTITLE_ACTIONBAR = "subtitle";

    // for Demo
    public final static String VIBRATION_ONBOARD = "Vibration.(g)-Onboard";
    public final static String VIBRATION_EXTERNAL = "Vibration.(g)-External";
    public final static String RPM_ONBOARD= "Rpm.(r/min)";
    public final static String TEMPERATURE = "Temperature.(°C)";
    public final static String TEMPERATURE_ONBOARD = "Temperature.(°C)-OnBO";
    public final static String TEMPERATURE_PT100 = "Temperature.(°C)-PT100";
    public final static String NOISE_ONBOARD = "Noise.(dB)-Onboard";
    public final static String NOISE = "Noise.(dB)";
    public final static String HUMIDITY = "Humidity.(%RH)";
    public final static String CURRENTSENSOR = "Current Sensor.(Amp)";
    public final static String VOLTAGE = "Voltage.(V)";
    public final static String IO = "IO-Input.";
    public final static String IOOUT = "IO-Output.";
    public final static String IO1 = "IO1-Input.";
    public final static String IO2 = "IO2-Input.";
    public final static String IO3 = "IO3-Input.";
    public final static String IO1_AI = "IO-1(AI)";
    public final static String IO2_AI = "IO-2(AI)";
    public final static String IO3_AI = "IO-3(AI)";
    public final static String IO4_AI = "IO-4(AI)";
    public final static String IO1_DI = "IO-1(DI)";
    public final static String IO2_DI = "IO-2(DI)";
    public final static String IO3_DI = "IO-3(DI)";
    public final static String IO4_DI = "IO-4(DI)";
    public final static String TEMP1 = "Temperature.(°C)-1";
    public final static String TEMP2 = "Temperature.(°C)-2";
    public final static String TEMP3 = "Temperature.(°C)-3";
    public final static String TEMP4 = "Temperature.(°C)-4";
    public final static String TEMP5 = "Temperature.(°C)-5";
    public final static String STRAIN_GAUGE = "Strain_Gauge.(mm/mm)";
    public final static String DAC = "DAC.(mV)";
    public final static String ADC = "ADC.(V)";
    public final static String GENERAL_SENSORS = "General";
    public final static String HOLD_WELL = "HOLD WELL";
    public final static String ZHEN_HONG = "ZHEN_HONG";
    public final static String ZHEN_SUN = "ZHEN_SUN";
    public final static String YUAN_JUNG_FONG = "YUAN_JUNG_FONG";
    public final static String EXACT = "Exact";
    public final static String THETA = "Theta";
    public final static String CTM = "CTM";
    public final static String SANKYO = "SANKYO";*/

    //ble status define
    public static final int BLE_STATE_DISCONNECTED = 0;
    public static final int BLE_STATE_CONNECTING = 1;
    public static final int BLE_STATE_CONNECTED = 2;
    public static final int BLE_SERVICE_SUCCESS = 0;

    //ble broadcast's string define
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECT_TIMEOUT = "com.example.bluetooth.le.ACTION_GATT_CONNECT_TIMEOUT";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE = "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    //ble uuid define
    public static String BLE_2640_SERVICE_UUID = "";
    public static String BLE_RECERIVER_UUID = "";
    public static String BLE_SEND_UUID = "";
    public static String BLE_BDE_SERVICE_UUID = "";
    public static String BLE_BDE_UUID = "";



    //firstlogin
    public final static boolean firstlogin=true;
    //bluetooth device
    public final static String TANGRAM_BT_DEVICE = "SimpleBLEPeripher  ";
    public final static String BT_DEVICE = "Device_Address";
    public final static String NO_DEVICE = "Device_Null";

    // client server define rule;
    public final static String CONNECTIONSTATUS = "connection_ok";
    public final static String REQUEST_SERVER_READY_STATUS = "^";
    public final static String RESPONSE_SERVER_READY_STATUS = "server_ready";
    public final static String REQUEST_WHICH_SENSOR_COMMAND = "$";
    public final static String REQUEST_ALARM = "!";

    // sensor type
    public final static String SENSOR_VIBRATION = "Vib";
    public final static String SENSOR_NOISE = "Noise";
    public final static String SENSOR_HUMIDITY = "Humi";
    public final static String SENSOR_TEMPERATURE = "Temp";
    public final static String SENSOR_CURRENT = "Current";


    // server receive formats in Bundle
    public final static String RECEIVE_DATA = "receive_data";
    public final static String STREAM = "stream";
    public final static String LENGTH = "length";

    // sensor detail bundle
    public final static String TARGET_ITEM = "sensor_item";

    // server stream data length
    public final static int DATA_LENGTH = 768;

    // byte location
//    public final static int LOCATION0 = 0;
//    public final static int LOCATION1 = 1;
//    public final static int LOCATION2 = 2;
//    public final static int LOCATION3 = 3;
//    public final static int LOCATION4 = 4;
//    public final static int LOCATION5 = 5;
//    public final static int LOCATION6 = 6;
//    public final static int LOCATION7 = 7;
//    public final static int LOCATION8 = 8;
//    public final static int LOCATION9 = 9;
//    public final static int LOCATION10 = 10;
//    public final static int LOCATION11 = 11;
//    public final static int LOCATION12 = 12;
//    public final static int LOCATION13 = 13;
//    public final static int LOCATION14 = 14;
//    public final static int LOCATION15 = 15;
//    public final static int LOCATION16 = 16;
//    public final static int LOCATION17 = 17;
//    public final static int LOCATION18 = 18;
//    public final static int LOCATION19 = 19;
//    public final static int LOCATION20 = 20;
//    public final static int LOCATION21 = 21;
//    public final static int LOCATION22 = 22;
//    public final static int LOCATION23 = 23;
//    public final static int LOCATION24 = 24;
//    public final static int LOCATION25 = 25;
//    public final static int LOCATION26 = 26;
//    public final static int LOCATION27 = 27;
//    public final static int LOCATION28 = 28;
//    public final static int LOCATION29 = 29;
//    public final static int LOCATION30 = 30;
//    public final static int LOCATION31 = 31;
//    public final static int LOCATION32 = 32;
//    public final static int LOCATION33 = 33;

}

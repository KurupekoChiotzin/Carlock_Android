package com.mulin.larlock.larlock;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class Main_Page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IMonitorDataView{
    private byte [] loginCmd={}; //登入指令
    private byte [] logoutCmd={}; //登入指令
    private byte [] VerifyErrorCmd={};//驗證碼長度不符合或取消輸入驗證碼
    private Toolbar toolbar;
    private Button loginBtn;
    private Button nearOpenBtn;
    private Boolean nearOpen;
    private Spinner userSpinner;
    private ArrayAdapter<CharSequence> userAdapter;
    EncryprtionAndStore encryprtionAndStore;
    private SharedPreferences recordData;
    private Login loginThread;
    public static final int BLE_INTENT=1;
    public static int tabNum=4;
    private String user_Array[];
    private String loginState="";
    private Boolean firstLogin;
    private String bleMac="";
    public int ACCESS_COARSE_LOCATION_REQUEST=1;

    private IntentFilter BLE_intent;
    private BroadcastReceiver BLE_BoardRe;

    public static  BLE_SEARCH ble_search;
    public static ClientBTConnect mClientBTConnect;
    public static int[] ReadData=new int[16];
    private boolean BLE_register=false;
    private boolean AutoConnectBLE=false;
    public static boolean firstautoconnect=false;
    private boolean Nearopenflag=false;
    public static boolean loginflag=false;
    static public Boolean type=false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:"+"Main onCreate");
        setContentView(R.layout.activity_navigation_login_page);
        loginBtn=(Button)findViewById(R.id.member_loginbtn);
        nearOpenBtn=(Button)findViewById(R.id.nearOpenBtn);
        loginBtn.setOnClickListener(btnLister);
        nearOpenBtn.setOnClickListener(btnLister);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
        mClientBTConnect=new ClientBTConnect(this,this);
        ble_search=new BLE_SEARCH();
        registerBLEReceiver(true);
        Arrayinit();
        appDataLoding();
        checkpermission();
        EditText editText=new EditText(Main_Page.this);
        drawerLayout=(DrawerLayout) findViewById(R.id.activity_navigation_login_page);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        System.out.println("TestLogin");

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void appDataLoding() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_layout);
        firstLogin = recordData.getBoolean("firstLogin", true);
        Log.d("fitstLogin",Boolean.toString(firstLogin));
        //bleMac=recordData.getString("bleMac","");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_connect) {
                    ble_search.search(item.getActionView(), Main_Page.this);
                }
                return false;
            }
        });
        if(firstLogin==false)
        {
            AutoConnectBLE=true;
            bleMac=mClientBTConnect.getBTDevice();
            System.out.println("AutoConnect_bleMAC = " + bleMac);
            mClientBTConnect.connectBTDevice(bleMac);
            AutoConnectBLE=false;
        }
    }

    public void checkpermission()
    {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(Build.VERSION.SDK_INT>=23&&permission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},ACCESS_COARSE_LOCATION_REQUEST);
        }

    }


    public void updatelogin()       //登入後更新帳號資訊
    {
        View headerLayout = navigationView.getHeaderView(0);
        TextView headerText = headerLayout.findViewById(R.id.logintHeader);
        loginflag=recordData.getBoolean("login",false);
        if(loginflag)
        {
            String account="";
            account=recordData.getString("account","");
            type=recordData.getBoolean("type",false);
            headerText.setText(account);
            if(type)
            {
                navigationView.getMenu().findItem(R.id.guard).setVisible(true);
                navigationView.getMenu().findItem(R.id.beguard).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            }
            else
            {
                navigationView.getMenu().findItem(R.id.guard).setVisible(false);
                navigationView.getMenu().findItem(R.id.beguard).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            }
        }
        else
        {
            navigationView.getMenu().findItem(R.id.guard).setVisible(false);
            navigationView.getMenu().findItem(R.id.beguard).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
            headerText.setText("LarLock，是您最好的選擇");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==ACCESS_COARSE_LOCATION_REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            else
            {
                new AlertDialog.Builder(Main_Page.this)
                        .setTitle("警告")
                        .setIcon(R.drawable.ic_action_user)
                        .setMessage("不開權限可能會造成藍芽無法搜尋，按取消後再次提示權限取得")
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkpermission();
                            }
                        }).show();
            }
            return;
        }


    }
    private Button.OnClickListener btnLister=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.member_loginbtn:
                    loginThread=new Login();
                    loginThread.start();
                    break;
                case R.id.nearOpenBtn:
                    nearOpenFunction();
                    break;

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==BLE_INTENT)
        {
            switch (resultCode)
            {
                case Navigation_Page.END_FORBACK:
                    finish();
                    break;
                case  Navigation_Page.ACCOUNT_LOGOUT:
                    if(ClientBTConnect.ConnectState==2) {          //if(BLE_Page.mGatt!=null)
                        mClientBTConnect.sendData(logoutCmd);
                    }
                    break;
            }

        }
    }
    private Boolean serviceIsOpen()
    {
        ActivityManager activityManager=(ActivityManager)this.getSystemService(this.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>serviceList=activityManager.getRunningServices(Integer.MAX_VALUE);
        for(int i=0;i<serviceList.size();i++)
        {
            ActivityManager.RunningServiceInfo serviceInfo=serviceList.get(i);
            ComponentName serviceName=serviceInfo.service;
            if(serviceName.getClassName().equals("com.mulin.larlock.larlock.BLE_Service"))
            {
                return true;
            }
        }
        return false;
    }

    private void nearOpenFunction()     //待解
    {
        Nearopenflag=true;
        registerBLEReceiver(false);
        Intent intent=new Intent(this,BLE_Service.class);
        if(ClientBTConnect.ConnectState==2)
        {
            mClientBTConnect.disConnectBTDevice();
            ClientBTConnect.ConnectState=0;
        }
        if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O)
        startForegroundService(intent);
        else
        startService(intent);
        finish();
    }

    @Override
    public void updateComponetView(Bundle bundle) {
            byteToInt(bundle.getByteArray("BT_RECEIVE_DATA"));
            Log.d("updateComponetVie", "Receive_Data");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.nav_login:
                Intent intent = new Intent(Main_Page.this, Member_Page.class);
                startActivityForResult(intent, BLE_INTENT);
                break;
            case R.id.nav_logout:
                navigationlogout();
                Log.d("Logout","test Logout");
                break;
            case R.id.beguard_qr:
                Intent intentqr = new Intent(Main_Page.this,GenerateQR.class);
                startActivityForResult(intentqr,BLE_INTENT);
                break;
            case R.id.guard_qradd:
                Intent intentqrscan = new Intent(Main_Page.this,QRScanner.class);
                startActivityForResult(intentqrscan,BLE_INTENT);
                break;
            case R.id.guard_view:
                Intent intentrslist=new Intent(Main_Page.this,RS_List.class);
                startActivityForResult(intentrslist,BLE_INTENT);
                break;
            case R.id.guard_cpw:
                Intent intentcpw1=new Intent(Main_Page.this,CreateForgetAT.class);
                intentcpw1.putExtra("select",3);
                startActivityForResult(intentcpw1,BLE_INTENT);
                break;
            case R.id.beguard_cpw:
                Intent intentcpw2=new Intent(Main_Page.this,CreateForgetAT.class);
                intentcpw2.putExtra("select",3);
                startActivityForResult(intentcpw2,BLE_INTENT);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public class Login extends Thread
    {
        final  EditText verifyedit=new EditText(Main_Page.this);
        final Handler mHandler=new Handler();
        private Boolean inputVerifyFlag=false;
        private byte [] writeCmd; //發送指令用
        Runnable runnable;
        ProgressDialog notify=null;
        public Login() {}

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            try {
                if(ble_search.mBluetoothAdapter==null)
                {
                    ble_search.initbluetooh(Main_Page.this,Main_Page.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main_Page.this,"開啟藍牙後請重新登入",Toast.LENGTH_LONG).show();
                        }
                    });
                    loginThread.stop();
                }
                runnable=new Runnable() {
                    @Override
                    public void run() {
                        if(inputVerifyFlag)
                        {
                                Arrayinit();
                                writeCmd = verifyedit.getText().toString().getBytes();
                                mClientBTConnect.sendData(writeCmd);
                                inputVerifyFlag = false;
                                mHandler.postDelayed(runnable, 500);
                        }
                        else if(ReadData[0]==0x00) {
                            tabNum = 1;
                            notify.dismiss();
                            Intent intent = new Intent(Main_Page.this, Navigation_Page.class);
                            startActivityForResult(intent, BLE_INTENT);
                            Arrayinit();
                        }
                        else if(ReadData[0]==0x00)
                        {
                            notify.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog=new android.support.v7.app.AlertDialog.Builder(Main_Page.this);
                                    firstLoginDialog.setTitle("驗證碼錯誤或超過時限未輸入")
                                            .setMessage("請重新登入在做輸入")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            });
                            Arrayinit();
                        }
                        else
                            mHandler.postDelayed(runnable,500);
                    }
                };
                if(ClientBTConnect.ConnectState==Constant.BLE_STATE_CONNECTED && ClientBTConnect.SendStatus)          //BLE_Page.SendStatus  待解
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notify=new ProgressDialog(Main_Page.this);
                            notify.setTitle("提示訊息");
                            notify.setMessage("登入中...");
                            notify.setCancelable(true);
                            notify.show();
                        }
                    });
                    mClientBTConnect.sendData(loginCmd);
                    Thread.sleep(3000);
                    if(!(ReadData[8]==0x01)){
                        notify.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Main_Page.this,"不明錯誤，請重登",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        if (ReadData[0] == 0x00) {
                            if (firstLogin) {
                                recordData.edit().putBoolean("firstLogin", false).commit();
                            }
                            tabNum = 2;
                            Arrayinit();
                            notify.dismiss();
                            Intent intent = new Intent(Main_Page.this, Navigation_Page.class);
                            startActivityForResult(intent, BLE_INTENT);
                        } else if (ReadData[0] == 0x00) {
                            notify.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final android.support.v7.app.AlertDialog.Builder verifyDialog = new android.support.v7.app.AlertDialog.Builder(Main_Page.this);
                                    verifyDialog.setTitle("請輸入驗證碼")
                                            .setMessage("輸入驗證碼後按確定")
                                            .setView(verifyedit)
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    notify.show();
                                                    inputVerifyFlag = true;
                                                    mHandler.postDelayed(runnable, 500);
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    mClientBTConnect.sendData(VerifyErrorCmd);
                                                    Arrayinit();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            });

                        }
                    }

                }
                else
                {
                    if(firstLogin)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final android.support.v7.app.AlertDialog.Builder firstLoginDialog=new android.support.v7.app.AlertDialog.Builder(Main_Page.this);
                                firstLoginDialog.setTitle("藍芽未連接")
                                        .setMessage("請按右上角連線進行連線")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        });


                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mClientBTConnect.bleNotify(Main_Page.this);
                            }
                        });
                    }

                }
            }
            catch (Exception e)
            {
                notify.dismiss();
                e.printStackTrace();
            }



        }


    }


    @Override
    protected void onStop() {
        System.out.println("生命週期:"+"Main onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"Main onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        System.out.println("生命週期:"+"Main onStart");
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"Main onResume");
        if(ble_search.mBluetoothAdapter==null)                         //藍芽初始化
        {
            ble_search.initbluetooh(this,this);     //偵測藍芽有沒有打開
            GPSInit();                                              //偵測GPS有沒有打開
            getPermissionCamera();                                  //偵測相機權限有沒有打開
        }
        nearOpen=recordData.getBoolean("nearOpen",false);
        if (nearOpen)
        {
            nearOpenBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            nearOpenBtn.setVisibility(View.INVISIBLE);
        }
        if(serviceIsOpen()) {
            Intent myservice = new Intent(Main_Page.this, BLE_Service.class);
            stopService(myservice);
        }
        updatelogin();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"Main onDestroy");
        if(!Nearopenflag) {
            registerBLEReceiver(false);     //ClientBTConnect廣播註消
            mClientBTConnect.closeBluetoothLeService();
        }
        super.onDestroy();
    }


    //---------------------
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

    //偵測相機有沒有打開 QRcode掃描用
    private void getPermissionCamera()
    {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
    }

    private static void byteToInt(byte [] a)        //將獲得的藍芽資料(狀態)轉成陣列
    {
        if(a != null) {             //避免接受訊息為空
            // Log.d("Receive Length","Receive key Length="+a.length);
            if(a.length<=16)
            {
                for (int i = 0; i < a.length; i++) {
                    ReadData[i] = a[i] & 0xFF;
                }
            }
            else if(a.length>16)
            {
                Log.d("Attemp_null_array", "ReadData Attempt to get length of null array Length="+a.length+"byte[]="+a);
            }
        }
    }
    static public void Arrayinit()                  //將狀態清空
    {
        for(int i=0;i<16;i++)
        {
            ReadData[i]=0;
        }
    }


    public void registerBLEReceiver(Boolean registerFlag)  //ClientBTConnect廣播註冊
    {
        if(registerFlag==true)
        {
            BLE_intent=mClientBTConnect.regReceiver();
            BLE_BoardRe=mClientBTConnect.getGattUpdateReceiver();
            registerReceiver(BLE_BoardRe,BLE_intent);
        }
        else
        {
            unregisterReceiver(BLE_BoardRe);
        }
    }

    private void navigationlogout()
    {
        recordData.edit().clear().commit();
        loginflag=false;
        updatelogin();
    }
}

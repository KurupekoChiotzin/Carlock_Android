package com.mulin.larlock.larlock;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Navigation_Page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener ,IMonitorDataView, LocationListener {
    /*Data Value Define*/
    private static byte [] switchStateCmd={};
    private byte[] OpenCarboxCmd={};       //開車廂指令
    public static final int END_FORBACK=1;
    public static final int ACCOUNT_LOGOUT=2;
    private Boolean backKeyFlag=false;
    /*UI Object Define*/
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ProgressDialog notify;
    /*Frament Define*/
    private Fragment tempFragment=null;
    private Fragment homeFrament=null;
    private Fragment funFrament=null;
    private Fragment descriptionFrament=null;
    private Fragment aboutFrament=null;

    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    LocationManager mgr;
    static final int MIN_TIME=5000;
    static final float MIN_DIST=0;
    static public boolean powerflag=false;

    private Boolean check;
    private static final String POST = "POST";
    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private SharedPreferences recordData;

    /*Other*/
    Intent myservice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:"+"Sub onCreate");
        setContentView(R.layout.activity_navigation_page);
        mgr=(LocationManager)getSystemService(LOCATION_SERVICE);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
        page_Init();
    }

    private void page_Init()
    {
        myservice=new Intent(Navigation_Page.this,BLE_Service.class);
        toolbar=(Toolbar)findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        drawer=(DrawerLayout)findViewById(R.id.activity_navigation_login_page);
        navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        new getSwitchState().start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                setResult(END_FORBACK);
                backKeyFlag=true;
                finish();
                break;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fun:
                if(funFrament==null)
                    funFrament=new Function_Page();
                switchFrament(funFrament);
                break;
            case R.id.action_description:
                break;
            case R.id.action_about:
                break;
            case R.id.action_logout:
                setResult(ACCOUNT_LOGOUT);
                finish();
                break;
            case R.id.action_openbox:
                OpenCarBox();;
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFrament(Fragment frament)
    {
        if(frament!=tempFragment)
        {
            if(!frament.isAdded())
            {
                if(tempFragment==null)
                    getSupportFragmentManager().beginTransaction().add(R.id.frament_container,frament).commit();
                else
                    getSupportFragmentManager().beginTransaction().hide(tempFragment).add(R.id.frament_container,frament).commit();
            }
            else
            {
                getSupportFragmentManager().beginTransaction().hide(tempFragment).show(frament).commit();
            }
            tempFragment=frament;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"Sub onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"Sub onPause");
        super.onPause();
        enableLocationUpdate(false);
    }


    @Override
    protected void onStop() {
        //finish();
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"Sub onResume");
        if(ClientBTConnect.ConnectState==0)
        {
            finish();
        }
        enableLocationUpdate(true);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"Sub onDestroy");
        super.onDestroy();
    }

    @Override
    public void updateComponetView(Bundle bundle) {

    }


    private class getSwitchState extends Thread
    {
        @Override
        public void run() {
            Looper.prepare();
            updateDataNotify(Navigation_Page.this);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(!(Main_Page.ReadData[8]==0x05))
            {
                try {
                    if(ClientBTConnect.ConnectState==2&&ClientBTConnect.SendStatus)
                    {
                        Main_Page.mClientBTConnect.sendData(switchStateCmd);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(Main_Page.ReadData[7]==0x01)
            {
                Power_frament.powerBtnFlag=true;
            }
            else if(Main_Page.ReadData[7]==0x00)
            {
                Power_frament.powerBtnFlag=false;
            }
            if(Main_Page.ReadData[9]==0x01)
            {
                Anti_frament.antiBtnFlag=true;
            }
            else if(Main_Page.ReadData[9]==0x00)
            {
                Anti_frament.antiBtnFlag=false;
            }
            notify.dismiss();
            Main_Page.Arrayinit();
            if(funFrament==null)
                funFrament=new Function_Page();
            switchFrament(funFrament);
        }
    }



    private  void updateDataNotify(final Context context)
    {
        final Handler mhandler=new Handler();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notify=new ProgressDialog(context);
                        notify.setTitle("更新資料");
                        notify.setMessage("擷取裝置相關資料");
                        notify.show();
                    }
                });
    }

    private void OpenCarBox()           //開車廂
    {
        if(ClientBTConnect.ConnectState==Constant.BLE_STATE_CONNECTED && ClientBTConnect.SendStatus) {
            Main_Page.mClientBTConnect.sendData(OpenCarboxCmd);
            Toast.makeText(this, "已開車廂", Toast.LENGTH_LONG).show();
            Log.d("OpenCarBox","Already Opened Carbox");
        }
    }

    //GPS
    private void updateLocation(final String lng,final String lat)
    {
        final String session =recordData.getString("sessionid","");
        service.submit(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder params = new FormBody.Builder();
                params.add("lng", lng);
                params.add("lat", lat);
                FormBody formBody = params.build();
                final Request request = new Request.Builder()
                        .url("https://www.usblab.nctu.me/carlock/carlock/public/updateLocation")
                        .addHeader("cookie",session)
                        .method(POST, formBody)
                        .build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    Log.d("updateLocation",resStr);

                    JSONObject array = new JSONObject(resStr);

                    check=array.getBoolean("isOk");
                    if(check)
                    {
                        Log.d("updateLocation","success");
                    }
                    else
                    {
                        Log.d("updateLocation","fail");
                    }
                } catch (Exception e) {
                    Log.d("updateLocation","exception");
                    e.printStackTrace();
                }
            }
        });
    }

    private void enableLocationUpdate(boolean isTurnOn) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isTurnOn) {
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled && !isNetworkEnabled) {
//                    Toast.makeText(this,"請確認已開啟定位功能!",Toast.LENGTH_LONG).show();
                    Log.d("enLocationUP", "fail");
                } else {
//                    Toast.makeText(this, "取得定位資訊中><", Toast.LENGTH_SHORT).show();
                    Log.d("enLocationUP", "fail");
                    if (isGPSEnabled)
                        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
                    if (isNetworkEnabled)
                        mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, this);
                }
            } else
                mgr.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String lng,lat;
        lng=String.valueOf(location.getLongitude());
        lat=String.valueOf(location.getLatitude());
        Log.d("onLocationChanged","Lng:"+lng+"Lat:"+lat);
        if(powerflag) {
            updateLocation(lng, lat);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}

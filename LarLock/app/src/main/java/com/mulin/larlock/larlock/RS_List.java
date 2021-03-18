package com.mulin.larlock.larlock;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RS_List extends AppCompatActivity {
    private ListView RSlist=null;
    private Boolean check;
    private Set<String> User_name=new LinkedHashSet<>();
    private Set<String> User_id=new LinkedHashSet<>();
    private ArrayAdapter<String> Uname_Adapter=null;
    private String[] Uid_Array=null;
    private String[] Uname_Array=null;
    private double Lng;
    private double Lat;

    private JSONObject jsonObject;
    private JSONArray jsonarray;

    private static final String POST = "POST";
    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private SharedPreferences recordData;

    private String search_RSnameIP="";
    private String requestLocationIP="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:" + "RS_List onCreate");
        setContentView(R.layout.activity_rslist);
        RSlist=(ListView)findViewById(R.id.listrslist);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
        initRSList();
    }

    private void initRSList()
    {
        final String session =recordData.getString("sessionid","");
        Uname_Adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        RSlist.setOnItemClickListener(onItemClickListener);
        RSlist.setAdapter(Uname_Adapter);
        service.submit(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder params = new FormBody.Builder();
                FormBody formBody = params.build();
                Request request = new Request.Builder()
                        .url(search_RSnameIP)
                        .addHeader("cookie",session)
                        .build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    Log.d("RS_List",resStr);

                    JSONObject array = new JSONObject(resStr);

                    check=array.getBoolean("isOk");
                    if(check)
                    {
                        jsonarray=array.getJSONArray("data");
                        for(int i=0;i<jsonarray.length();i++)
                        {
                            String tname;
                            String tid;
                            jsonObject=jsonarray.getJSONObject(i);
                            tname=jsonObject.getString("name");
                            tid=jsonObject.getString("id");
                            Log.d("RS_List",tname+":"+tid);
                            User_id.add(jsonObject.getString("id"));
                            User_name.add(jsonObject.getString("name"));
                        }

                        Log.d("RS_List","success array");
                        Uid_Array=User_id.toArray(new String[User_id.size()]);
                        Uname_Array=User_name.toArray(new String[User_name.size()]);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(check)
                            {
                                Log.d("RS_List","success getList");
                                Uname_Adapter.addAll(Uname_Array);
                                Uname_Adapter.notifyDataSetChanged();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("RS_List","exception");
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestLocation(final String tid,final int i)
    {
        service.submit(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder params = new FormBody.Builder();
                params.add("id", tid);
                FormBody formBody = params.build();
                Request request = new Request.Builder()
                        .url(requestLocationIP)
                        .method(POST, formBody)
                        .build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    Log.d("requestLocation",resStr);

                    JSONObject array = new JSONObject(resStr);

                    check=array.getBoolean("isOk");
                    if(check)
                    {
                        JSONObject temp=array.getJSONObject("data");
                        Lng=temp.getDouble("lng");
                        Lat=temp.getDouble("lat");
                        Log.d("requestLocation","Lng:"+Lng+",Lat:"+Lat);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(check)
                            {
                                Log.d("requestLocation","success");
                                String tname=Uname_Array[i];
                                Intent intent = new Intent(RS_List.this, Maps_Page.class);
                                intent.putExtra("lng",Lng);
                                intent.putExtra("lat",Lat);
                                intent.putExtra("name",tname);
                                startActivityForResult(intent, 1);
                            }
                            else {
                                Log.d("requestLocation", "fail");
                                final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(RS_List.this);
                                firstLoginDialog.setTitle("目前此用戶尚未上傳位置")
                                        .setMessage("請等用戶上傳位置")
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
                    Log.d("RS_List","exception");
                    e.printStackTrace();
                }
            }
        });
    }

    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tid;
                tid=Uid_Array[i];
                Log.d("RS_List select","id:"+tid);
                requestLocation(tid,i);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"RS_List onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"RS_List onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"RS_List onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"RS_List onDestroy");
        super.onDestroy();
    }
}



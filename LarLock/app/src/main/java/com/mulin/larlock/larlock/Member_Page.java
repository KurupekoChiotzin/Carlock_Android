package com.mulin.larlock.larlock;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Member_Page extends AppCompatActivity {

    private Button loginbtn,registerbtn,forgetpwdbtn;
    private EditText accountedit;
    private EditText pwdedit;
    private String account="";
    private String password="";
    private String name="";
    private Boolean type;
    private Boolean title;
    private static final String POST = "POST";

    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private SharedPreferences recordData;
    private JSONObject jsonObject;

    private String loginIP="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:"+"Member onCreate");
        setContentView(R.layout.activity_login_page);
        accountedit=(EditText)findViewById(R.id.pwdedit);
        pwdedit=(EditText)findViewById(R.id.pwd_edit);
        loginbtn=(Button)findViewById(R.id.member_loginbtn);
        registerbtn=(Button)findViewById(R.id.registerbtn);
        forgetpwdbtn=(Button)findViewById(R.id.forgetpwdbtn);
        loginbtn.setOnClickListener(membtnLister);
        registerbtn.setOnClickListener(membtnLister);
        forgetpwdbtn.setOnClickListener(membtnLister);
        recordData=getSharedPreferences("record",MODE_PRIVATE);
    }

    private Button.OnClickListener membtnLister=new Button.OnClickListener(){
      @Override
      public void onClick(View view)
      {
          switch(view.getId())
          {
              case R.id.member_loginbtn:
                  login();
                  break;
              case R.id.registerbtn:
                  Intent intent = new Intent(Member_Page.this, CreateForgetAT.class);
                  intent.putExtra("select",1);      //1為註冊
                  startActivityForResult(intent, 1);
                  break;
              case R.id.forgetpwdbtn:
                  Intent intent1 = new Intent(Member_Page.this, CreateForgetAT.class);
                  intent1.putExtra("select",2);      //2為遺失密碼
                  startActivityForResult(intent1, 1);
                  break;
          }
      }
    };


    private void login()
    {
        account = accountedit.getText().toString();
        password = pwdedit.getText().toString();
        if (account.equals("") || password.equals("") || account.equals(null) || password.equals(null)) {
            Log.d("Login", "onClick: nul");
            final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(Member_Page.this);
            firstLoginDialog.setTitle("登入失敗")
                    .setMessage("請重新登入在做輸入")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("account", account);
                    params.add("password", password);
                    FormBody formBody = params.build();
//                            Log.d("TTT", "onClick: A");
                    final Request request = new Request.Builder()
                            .url(loginIP)
                            .method(POST, formBody)
                            .build();
                    try {
//                                Log.d("TTT", "onClick: B");
                        final Response response = client.newCall(request).execute();
                        final String resStr = response.body().string();
                        Log.d("Login",resStr);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        t1.setText(resStr);
//                                    }
//                                });

                        JSONObject array = new JSONObject(resStr);

//                        for (int i = 0; i < array.length(); i++) {
//                            jsonObject = array.getJSONObject(i);
//                            title = jsonObject.getBoolean("isOk");
//                            //Log.d("Login", URLDecoder.decode(title, "UTF-8"));
//                        }
                        title=array.getBoolean("isOk");
                        if(title)
                        {
                            jsonObject=array.getJSONObject("data");
                            name=jsonObject.getString("name");
                            String temp =jsonObject.getString("type");
                            type=temp.equals("1");
                            //存取seesion
                            Headers headers =response.headers();
                            List cookies =headers.values("Set-Cookie");
                            String session = cookies.get(0).toString();
//                            Log.d("Login",session);       //檢查cookie header
                            String sessionid = session.substring(0,session.indexOf(";"));
                            recordData.edit().putString("sessionid",sessionid).commit();
                            Log.d("Login", URLDecoder.decode(name, "UTF-8"));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(title)
                                {
                                    Log.d("Login","success Login");

                                    recordData.edit().putString("account",account)
                                                     .putString("name",name)
                                                     .putBoolean("type",type)
                                                     .putBoolean("login",true).commit();
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog1 = new android.support.v7.app.AlertDialog.Builder(Member_Page.this);
                                    firstLoginDialog1.setTitle("登入成功")
                                            .setMessage("登入成功")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setCancelable(false)
                                            .show();

                                }
                                else
                                {
                                    Log.d("Login","failure Login");
                                    recordData.edit().putBoolean("login",false);
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(Member_Page.this);
                                    firstLoginDialog.setTitle("使用者名稱或密碼錯誤")
                                            .setMessage("請重新登入在做輸入")
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
                        Log.d("Login","exception");
                        e.printStackTrace();
                    }
                }
            });
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"Member onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"Member onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"Member onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"Member onDestroy");
        super.onDestroy();
    }
}

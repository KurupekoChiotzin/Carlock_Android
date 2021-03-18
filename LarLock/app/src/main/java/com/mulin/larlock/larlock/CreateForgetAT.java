package com.mulin.larlock.larlock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateForgetAT extends AppCompatActivity {

    private TextView acc_text,pwd_text,name_text,email_text,phone_text;
    private EditText acc_edit,pwd_edit,name_edit,email_edit,phone_edit;
    private int type=2;
    private Button crfgbtn;
    private RadioGroup usertype;
    private String account,password,name,email,phone;
    private int selectmod;
    private Boolean check;


    private static final String POST = "POST";
    private final OkHttpClient client= new OkHttpClient();
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private String registerIP="";
    private String forgetpwdIP="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("生命週期:" + "CreateForgetAT onCreate");
        setContentView(R.layout.activity_member_page);
        acc_edit=(EditText)findViewById(R.id.accedit);
        pwd_edit=(EditText)findViewById(R.id.pwdedit);
        name_edit=(EditText)findViewById(R.id.nameedit);
        email_edit=(EditText)findViewById(R.id.emailedit);
        phone_edit=(EditText)findViewById(R.id.phoneedit);
        acc_text=(TextView)findViewById(R.id.acctext);
        pwd_text=(TextView)findViewById(R.id.pwdtext);
        name_text=(TextView)findViewById(R.id.nametext);
        email_text=(TextView)findViewById(R.id.emailtext);
        phone_text=(TextView)findViewById(R.id.phonetext);
        usertype=(RadioGroup)findViewById(R.id.radioGroup1);
        crfgbtn=(Button)findViewById(R.id.crfgATbtn);
        crfgbtn.setOnClickListener(crfgListen);
        initPage();
    }

    private Button.OnClickListener crfgListen =new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.crfgATbtn:
                    if(selectmod==1)
                    {
                        register();
                    }
                    else if(selectmod==2)
                    {
                        forgetpwd();
                    }
                    else if(selectmod==3)
                    {
                        changepwd();
                    }
                    break;
            }
        }
    };

    private void initPage()
    {
        Intent intent = getIntent();
        selectmod =intent.getIntExtra("select",4);
        if(selectmod==1)        //1註冊   2遺失密碼
        {
            acc_text.setVisibility(View.VISIBLE);
            pwd_text.setVisibility(View.VISIBLE);
            name_text.setVisibility(View.VISIBLE);
            email_text.setVisibility(View.VISIBLE);
            phone_text.setVisibility(View.VISIBLE);
            acc_edit.setVisibility(View.VISIBLE);
            pwd_edit.setVisibility(View.VISIBLE);
            name_edit.setVisibility(View.VISIBLE);
            email_edit.setVisibility(View.VISIBLE);
            phone_edit.setVisibility(View.VISIBLE);
            usertype.setVisibility(View.VISIBLE);

        }
        else if(selectmod==2)
        {
            acc_text.setVisibility(View.VISIBLE);
            pwd_text.setVisibility(View.INVISIBLE);
            name_text.setVisibility(View.INVISIBLE);
            email_text.setVisibility(View.VISIBLE);
            phone_text.setVisibility(View.INVISIBLE);
            acc_edit.setVisibility(View.VISIBLE);
            pwd_edit.setVisibility(View.INVISIBLE);
            name_edit.setVisibility(View.INVISIBLE);
            email_edit.setVisibility(View.VISIBLE);
            phone_edit.setVisibility(View.INVISIBLE);
            usertype.setVisibility(View.INVISIBLE);
        }
        else if(selectmod==3)
        {
            acc_text.setVisibility(View.VISIBLE);
            pwd_text.setVisibility(View.VISIBLE);
            name_text.setVisibility(View.INVISIBLE);
            email_text.setVisibility(View.INVISIBLE);
            phone_text.setVisibility(View.INVISIBLE);
            acc_edit.setVisibility(View.VISIBLE);
            pwd_edit.setVisibility(View.VISIBLE);
            name_edit.setVisibility(View.INVISIBLE);
            email_edit.setVisibility(View.INVISIBLE);
            phone_edit.setVisibility(View.INVISIBLE);
            usertype.setVisibility(View.INVISIBLE);
        }
    }

    private void register()
    {
        account=acc_edit.getText().toString();
        password=pwd_edit.getText().toString();
        name=name_edit.getText().toString();
        email=email_edit.getText().toString();
        phone=phone_edit.getText().toString();
        switch (usertype.getCheckedRadioButtonId())
        {
            case R.id.guardianbtn:
                type=1;
                break;
            case R.id.underguardianbtn:
                type=0;
                break;
            default:
                type=2;
                break;
        }
        if(account.equals("")||email.equals("")||name.equals("")||email.equals("")||phone.equals("")||password.equals("")||
        account.equals(null)||email.equals(null)||name.equals(null)||email.equals(null)||phone.equals(null)||password.equals(null) || type==2)
        {
            Log.d("register", "onClick: null");
            final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
            firstLoginDialog.setTitle("資料錯誤")
                    .setMessage("資料格式不正確。請重新登入在做輸入")
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
            service.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("account", account);
                    params.add("passsword",password);
                    params.add("name",name);
                    params.add("email", email);
                    params.add("phone",phone);
                    params.add("type",String.valueOf(type));
                    FormBody formBody = params.build();
                    Request request = new Request.Builder()
                            .url(registerIP)
                            .method(POST, formBody)
                            .build();
                    try {
                        final Response response = client.newCall(request).execute();
                        final String resStr = response.body().string();
                        Log.d("register",resStr);

                        JSONObject array = new JSONObject(resStr);

                        check=array.getBoolean("isOk");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(check)
                                {
                                    Log.d("register","success register");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog1 = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog1.setTitle("建立帳戶成功")
                                            .setMessage("帳戶成功建立，請按返回鍵重新登入")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                    finish();
                                }
                                else
                                {
                                    Log.d("register","failure register");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog.setTitle("此帳戶已被註冊")
                                            .setMessage("請重新輸入帳號")
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
                        Log.d("register","exception");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void forgetpwd()
    {
        account=acc_edit.getText().toString();
        email=email_edit.getText().toString();
        if(account.equals("") || email.equals("") || account.equals(null) || email.equals(null))
        {
            Log.d("forgetpwd", "onClick: null");
            final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
            firstLoginDialog.setTitle("資料錯誤")
                    .setMessage("資料格式不正確。請重新登入在做輸入")
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
            service.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("account", account);
                    params.add("email", email);
                    FormBody formBody = params.build();
                    Request request = new Request.Builder()
                            .url("https://www.usblab.nctu.me/carlock/carlock/public/forgetpwd")
                            .method(POST, formBody)
                            .build();
                    try {
                        final Response response = client.newCall(request).execute();
                        final String resStr = response.body().string();
                        Log.d("forgetpwd",resStr);

                        JSONObject array = new JSONObject(resStr);

                        check=array.getBoolean("isOk");
                        if(check)
                        {
                            password=array.getString("data");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(check)
                                {
                                    Log.d("forgetpwd","success changepwd");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog1 = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog1.setTitle("新的密碼")
                                            .setMessage("新的密碼為:"+password)
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                    password="";
                                }
                                else
                                {
                                    Log.d("forgetpwd","failure changepwd");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog.setTitle("使用者名稱或信箱錯誤")
                                            .setMessage("請重新登入再做輸入")
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
                        Log.d("forgetpwd","exception");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void changepwd()
    {
        account=acc_edit.getText().toString();
        password=pwd_edit.getText().toString();
        if(account.equals("") || pwd_edit.equals("") || account.equals(null) || pwd_edit.equals(null))
        {
            Log.d("changepwd", "onClick: null");
            final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
            firstLoginDialog.setTitle("資料錯誤")
                    .setMessage("資料格式不正確。請重新登入在做輸入")
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
            service.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("account", account);
                    params.add("newpwd", password);
                    FormBody formBody = params.build();
                    Request request = new Request.Builder()
                            .url(forgetpwdIP)
                            .method(POST, formBody)
                            .build();
                    try {
                        final Response response = client.newCall(request).execute();
                        final String resStr = response.body().string();
                        Log.d("changepwd",resStr);

                        JSONObject array = new JSONObject(resStr);

                        check=array.getBoolean("isOk");
//                        if(check)
//                        {
//                            password=array.getString("data");
//                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(check)
                                {
                                    Log.d("changepwd","success changepwd");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog1 = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog1.setTitle("新的密碼")
                                            .setMessage("新的密碼為:"+password)
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                    password="";
                                }
                                else
                                {
                                    Log.d("changepwd","failure changepwd");
                                    final android.support.v7.app.AlertDialog.Builder firstLoginDialog = new android.support.v7.app.AlertDialog.Builder(CreateForgetAT.this);
                                    firstLoginDialog.setTitle("使用者名稱或信箱錯誤")
                                            .setMessage("請重新登入再做輸入")
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
                        Log.d("changepwd","exception");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("生命週期:"+"CreateForgetAT onStart");
    }

    @Override
    protected void onPause() {
        System.out.println("生命週期:"+"CreateForgetAT onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        System.out.println("生命週期:"+"CreateForgetAT onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("生命週期:"+"CreateForgetAT onDestroy");
        super.onDestroy();
    }
}

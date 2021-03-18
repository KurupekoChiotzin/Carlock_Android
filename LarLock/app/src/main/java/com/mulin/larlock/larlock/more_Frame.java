package com.mulin.larlock.larlock;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class more_Frame extends Fragment {
    private byte[] changeVerifyCmd={};     //換驗證碼指令
    private byte[] changeBTnameCmd={};     //換藍牙名稱指令
    private SharedPreferences recordData;
    private Switch nearOpenSwitch;
    private Button getVerifyBtn;
    private Button getBTNameBtn;
    private View view;
    private Boolean nearOpen;
    private String verifyNum=null;
    private changeVerifyNum changeVerifyNumThread=null;
    private changeBTname changeBTnameThread=null;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.more_frame,container,false);
        nearOpenSwitch=(Switch)view.findViewById(R.id.switch2);
        getVerifyBtn=(Button)view.findViewById(R.id.verifynumbtn);
        getBTNameBtn=(Button)view.findViewById(R.id.chbtnamebtn);
        getVerifyBtn.setOnClickListener(btnLister);
        getBTNameBtn.setOnClickListener(btnLister);
        recordData= getActivity().getSharedPreferences("record",MODE_PRIVATE);
        nearOpen=recordData.getBoolean("nearOpen",false);
        if(nearOpen)
            nearOpenSwitch.setChecked(true);
        nearOpenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nearOpenSwitch.isChecked())
                {
                    recordData.edit().putBoolean("nearOpen",true).commit();
                }
                else
                {
                    recordData.edit().putBoolean("nearOpen",false).commit();
                }
            }
        });
        return view;
    }
    private Button.OnClickListener btnLister=new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.verifynumbtn:
                    changeVerifyNumThread=new changeVerifyNum();
                    changeVerifyNumThread.start();
                    break;
                case R.id.chbtnamebtn:
                    changeBTnameThread=new changeBTname();
                    changeBTnameThread.start();
                    break;
            }

        }
    };
    private class changeVerifyNum extends Thread {                  //換驗證碼
        private Boolean verifySendFlag=false;
        private ProgressDialog notify=null;
        private byte [] writeCmd; //發送指令用
        final EditText verifyedit = new EditText(getActivity());
        final Handler mHandler = new Handler();
        Runnable runnable;


        public changeVerifyNum() {
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            try {
                //if(BLE_Page.mBluetoothAdapter==null)
                if(ClientBTConnect.ConnectState!=Constant.BLE_STATE_CONNECTED)
                {
                    changeVerifyNumThread.stop();
                    getActivity().finish();
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(verifySendFlag)
                        {
                            //BLE_Page.SendCharacteristic.setValue(changeVerifyCmd);
                            //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                            Main_Page.mClientBTConnect.sendData(changeVerifyCmd);                   //先傳要換驗證碼指令 再傳新驗證碼
                            /*while (!(Main_Page.ReadData[8] == 0x59)) {
                                try {
                                    Thread.sleep(500);
                                    //Log.d("debug",(String.valueOf(Main_Page.ReadData[8])));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            mHandler.postDelayed(runnable, 500);
                            if(Main_Page.ReadData[0] == 0x00) {
                                Main_Page.Arrayinit();
                                writeCmd = verifyedit.getText().toString().getBytes();
                                //BLE_Page.SendCharacteristic.setValue(writeCmd);
                                //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                                Main_Page.mClientBTConnect.sendData(writeCmd);
                                verifySendFlag = false;
                                mHandler.postDelayed(runnable, 500);
                            }
                        }
                        else if (Main_Page.ReadData[0] == 0x00) {
                            notify.dismiss();
                            final android.support.v7.app.AlertDialog.Builder rightChangeDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            rightChangeDialog.setTitle("成功訊息")
                                    .setMessage("修改驗證碼成功")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else if (Main_Page.ReadData[0] == 0x00) {
                            notify.dismiss();
                            final android.support.v7.app.AlertDialog.Builder errorChangeDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            errorChangeDialog.setTitle("錯誤訊息")
                                    .setMessage("請重新更改驗證碼")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }/*else if(Main_Page.ReadData[8] != 0x4E && Main_Page.ReadData[8] != 0x59) {

                            notify.dismiss();
                            final android.support.v7.app.AlertDialog.Builder errorChangeDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            errorChangeDialog.setTitle("錯誤訊息")
                                    .setMessage("認證錯誤，請重新更改驗證碼")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }*/
                        else
                            mHandler.postDelayed(runnable, 500);
                        Main_Page.Arrayinit();
                    }
                };
                if (ClientBTConnect.ConnectState == Constant.BLE_STATE_CONNECTED && ClientBTConnect.SendStatus) {
                    Main_Page.Arrayinit();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final android.support.v7.app.AlertDialog.Builder verifyDialog=new  android.support.v7.app.AlertDialog.Builder(getActivity());
                            verifyDialog.setTitle("請輸入欲修改驗證碼")
                                    .setMessage("輸入驗證碼後按確定")
                                    .setView(verifyedit)
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(verifyedit.length()!=8)
                                            {
                                                Toast.makeText(getActivity(),"密碼長度錯誤",Toast.LENGTH_LONG).show();
                                            }
                                            else
                                            {
                                                verifySendFlag=true;
                                                notify=new ProgressDialog(getActivity());
                                                notify.setTitle("提示訊息");
                                                notify.setMessage("更換驗證碼中...");
                                                notify.setCancelable(false);
                                                notify.show();
                                                mHandler.postDelayed(runnable, 300);
                                            }
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("VerityMessage:","Cancel Input Verity");
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }
                else
                {
                    //BLE_Page.bleNotify(getActivity());
                    Main_Page.mClientBTConnect.bleNotify(getActivity());
                }
            } catch (Exception e) {
            }
        }
    }


    private class changeBTname extends Thread {                  //換驗證碼
        private Boolean BTnameSendFlag=false;
        private ProgressDialog notify=null;
        private byte [] writeCmd; //發送指令用
        final EditText BTnameedit = new EditText(getActivity());
        final Handler mHandler = new Handler();
        Runnable runnable;


        public changeBTname() {
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            try {
                if(ClientBTConnect.ConnectState!=Constant.BLE_STATE_CONNECTED)
                {
                    changeVerifyNumThread.stop();
                    getActivity().finish();
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(BTnameSendFlag)
                        {
                            Main_Page.mClientBTConnect.sendData(changeBTnameCmd);                   //先傳要換驗證碼指令 再傳新驗證碼
                            mHandler.postDelayed(runnable, 500);
                            if(Main_Page.ReadData[0] == 0x00) {
                                Main_Page.Arrayinit();
                                writeCmd = BTnameedit.getText().toString().getBytes();
                                Main_Page.mClientBTConnect.sendData(writeCmd);
                                BTnameSendFlag = false;
                                mHandler.postDelayed(runnable, 500);
                            }
                        }
                        else if (Main_Page.ReadData[0] == 0x00) {
                            notify.dismiss();
                            final android.support.v7.app.AlertDialog.Builder rightChangeDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            rightChangeDialog.setTitle("成功訊息")
                                    .setMessage("修改藍牙名稱成功")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else if (Main_Page.ReadData[0] == 0x00) {
                            notify.dismiss();
                            final android.support.v7.app.AlertDialog.Builder errorChangeDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            errorChangeDialog.setTitle("錯誤訊息")
                                    .setMessage("請重新更改藍牙名稱")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        else
                            mHandler.postDelayed(runnable, 500);
                        Main_Page.Arrayinit();
                    }
                };
                if (ClientBTConnect.ConnectState == Constant.BLE_STATE_CONNECTED && ClientBTConnect.SendStatus) {
                    Main_Page.Arrayinit();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final android.support.v7.app.AlertDialog.Builder verifyDialog=new  android.support.v7.app.AlertDialog.Builder(getActivity());
                            verifyDialog.setTitle("請輸入欲修改藍牙名稱")
                                    .setMessage("輸入藍牙名稱後按確定")
                                    .setView(BTnameedit)
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(BTnameedit.length()==0)
                                            {
                                                Toast.makeText(getActivity(),"藍牙名稱長度錯誤",Toast.LENGTH_LONG).show();
                                            }
                                            else
                                            {
                                                BTnameSendFlag=true;
                                                notify=new ProgressDialog(getActivity());
                                                notify.setTitle("提示訊息");
                                                notify.setMessage("更換藍牙名稱中...");
                                                notify.setCancelable(false);
                                                notify.show();
                                                mHandler.postDelayed(runnable, 300);
                                            }
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("BTNameMessage:","Cancel Input BTName");
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }
                else
                {
                    Main_Page.mClientBTConnect.bleNotify(getActivity());
                }
            } catch (Exception e) {
            }
        }
    }

}

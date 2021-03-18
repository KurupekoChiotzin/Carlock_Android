package com.mulin.larlock.larlock;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.zip.Inflater;

public class ChangePassword_Fragment extends Fragment {

    private View view;
    private Spinner userSpinner;
    private Button changepswdBtn;
    private static EditText oldPasswordText;
    private static EditText newPasswordText;
    private static EditText checkNewPasswordText;
    private static String user_Array[];
    private ArrayAdapter<CharSequence> userAdapter;
     @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.changepassword_frame,container,false);
        userSpinner=(Spinner)view.findViewById(R.id.userspinner2);
        userAdapter=ArrayAdapter.createFromResource(view.getContext(),R.array.user_array,android.R.layout.simple_spinner_item);
        userSpinner.setAdapter(userAdapter);
        changepswdBtn=(Button)view.findViewById(R.id.changepswdbtn);
        changepswdBtn.setOnClickListener(btnLister);
        oldPasswordText=(EditText)view.findViewById(R.id.text6);
        newPasswordText=(EditText)view.findViewById(R.id.text7);
        checkNewPasswordText=(EditText)view.findViewById(R.id.text8);
        user_Array=getResources().getStringArray(R.array.user_array);
        return view;
    }

    private Button.OnClickListener btnLister=new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            int btnID=view.getId();
            if(btnID==R.id.changepswdbtn)
            {
                ChangePassword send=new ChangePassword();
                send.start();
            }
        }
    };

     public class ChangePassword extends Thread
     {
         String account;
         String oldPasswordStr;
         String newPasswordStr;
         String chaeckNewPasswordStr;
         byte [] cmd;

         public void ChangePassword()
         {
            /* account=user_Array[userSpinner.getSelectedItemPosition()];
             oldPasswordStr=oldPasswordText.getText().toString();
             newPasswordStr=newPasswordText.getText().toString();
             chaeckNewPasswordStr=checkNewPasswordText.getText().toString();*/
         }
         @Override
         public void run() {
             try {
                 account=user_Array[userSpinner.getSelectedItemPosition()];
                 oldPasswordStr=oldPasswordText.getText().toString();
                 newPasswordStr=newPasswordText.getText().toString();
                 chaeckNewPasswordStr=checkNewPasswordText.getText().toString();
                 if(ClientBTConnect.ConnectState==2&&ClientBTConnect.SendStatus)
                 {
                     if(account.equals("Administrator"))
                         cmd=account.getBytes();
                     else if(account.equals("Guest"))
                         cmd=account.getBytes();
                     //BLE_Page.mGatt.setCharacteristicNotification(BLE_Page.SendCharacteristic, true);
                     //BLE_Page.SendCharacteristic.setValue(cmd);
                     //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                     Main_Page.mClientBTConnect.sendData(cmd);
                     while (!Main_Page.ReadData.equals("OK"))
                     {
                         Thread.sleep(1000);
                     }
                     if(newPasswordStr.equals(chaeckNewPasswordStr))
                     {
                         cmd=(oldPasswordStr+"/"+newPasswordStr+"Key").getBytes();
                         //BLE_Page.mGatt.setCharacteristicNotification(BLE_Page.SendCharacteristic, true);
                         //BLE_Page.SendCharacteristic.setValue(cmd);
                         //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                         Main_Page.mClientBTConnect.sendData(cmd);
                     }
                     else
                     {
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 android.support.v7.app.AlertDialog.Builder newPasswordCheck=new android.support.v7.app.AlertDialog.Builder(getContext());
                                 newPasswordCheck.setTitle("錯誤")
                                         .setMessage("新密碼兩次輸入不一致")
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
                 }
                 else
                 {
                     getActivity().runOnUiThread(new Runnable() {
                         @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                         @Override
                         public void run() {
                            // BLE_Page.bleNotify(getContext());
                             Main_Page.mClientBTConnect.bleNotify(getContext());
                         }
                     });
                 }
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }


         }
     }
}

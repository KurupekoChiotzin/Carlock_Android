package com.mulin.larlock.larlock;

import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import static android.content.Context.MODE_PRIVATE;

public class Anti_frament extends Fragment{
    private byte [] antiOffCmd={}; //防盜關閉
    private byte [] antiOpenCmd={}; //防盜開啟
    private static byte [] switchStateCmd={};
    private ImageButton antiBtn=null;
    public static Boolean antiBtnFlag=true;
    private View view;
    private SharedPreferences recordData;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.anti_frame,container,false);
        antiBtn=(ImageButton)view.findViewById(R.id.imgbtn2);
        if(antiBtnFlag)
            antiBtn.setImageResource(R.drawable.lock_transopen);
        else
            antiBtn.setImageResource(R.drawable.lock_transclose);
        antiBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view)
            {
                //if(BLE_Page.mBluetoothAdapter==null)
                if(ClientBTConnect.ConnectState!=2)
                {
                    getActivity().finish();
                }
               else if(ClientBTConnect.ConnectState==2&&ClientBTConnect.SendStatus)
               {
                   if(antiBtnFlag)
                   {
                       antiBtn.setImageResource(R.drawable.lock_transopen);
                       ((TransitionDrawable) antiBtn.getDrawable()).startTransition(1500);
                       antiBtnFlag = false;
                       //BLE_Page.SendCharacteristic.setValue(antiOffCmd);
                       //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                       Main_Page.mClientBTConnect.sendData(antiOffCmd);
                   }
                   else
                   {
                       antiBtn.setImageResource(R.drawable.lock_transclose);
                       ((TransitionDrawable) antiBtn.getDrawable()).startTransition(1500);
                       antiBtnFlag = true;
                       //BLE_Page.SendCharacteristic.setValue(antiOpenCmd);
                       //BLE_Page.mGatt.writeCharacteristic(BLE_Page.SendCharacteristic);
                       Main_Page.mClientBTConnect.sendData(antiOpenCmd);
                   }
               }
               else
               {
                   //BLE_Page.bleNotify(getContext());
                   Main_Page.mClientBTConnect.bleNotify(getContext());
               }
            }
        });
        return view;
    }

}

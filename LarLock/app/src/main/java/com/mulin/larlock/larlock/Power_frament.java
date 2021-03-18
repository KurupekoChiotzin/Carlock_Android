package com.mulin.larlock.larlock;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Power_frament extends Fragment{
    private byte [] powerOffCmd={}; //電門關閉
    private byte [] powerOpenCmd={}; //電門開啟
    private ImageButton powerBtn=null;
    public static Boolean powerBtnFlag;
    private View view;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.power_frame,container,false);
        powerBtn=(ImageButton)view.findViewById(R.id.imgbtn1);
        if (powerBtnFlag)
            powerBtn.setImageResource(R.drawable.power_transoff);
        else
            powerBtn.setImageResource(R.drawable.power_transon);

        powerBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view)
            {
                if(ClientBTConnect.ConnectState!=2)
                {
                    getActivity().finish();
                }
                else if(ClientBTConnect.ConnectState==2&&ClientBTConnect.SendStatus)
                {
                    if(powerBtnFlag)
                    {
                        powerBtn.setImageResource(R.drawable.power_transoff);
                        ((TransitionDrawable) powerBtn.getDrawable()).startTransition(1000);
                        powerBtnFlag = false;
                        Main_Page.mClientBTConnect.sendData(powerOffCmd);
                        Navigation_Page.powerflag=false;
                    }
                    else
                    {
                        powerBtn.setImageResource(R.drawable.power_transon);
                        ((TransitionDrawable) powerBtn.getDrawable()).startTransition(1000);
                        powerBtnFlag = true;
                        Main_Page.mClientBTConnect.sendData(powerOpenCmd);
                        if(!Main_Page.type) {
                            Navigation_Page.powerflag = true;
                        }
                    }
                }
                else
                {
                    Main_Page.mClientBTConnect.bleNotify(getContext());
                }
            }
        });
        return view;

    }


}

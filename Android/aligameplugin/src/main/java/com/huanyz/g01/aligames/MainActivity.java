package com.huanyz.g01.aligames;

import android.os.Bundle;

import cn.uc.gamesdk.UCGameSdk;
import cn.uc.gamesdk.unity3d.SdkEventReceiverImpl;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity
{
    private SdkEventReceiverImpl mSdkEventReceiverImpl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSdkEventReceiverImpl = new SdkEventReceiverImpl();
        UCGameSdk.defaultSdk().registerSDKEventReceiver(this.mSdkEventReceiverImpl);
    }

    public void onDestroy() {
        UCGameSdk.defaultSdk().unregisterSDKEventReceiver(this.mSdkEventReceiverImpl);
        super.onDestroy();
    }
}


package com.huanyz.g01.qihoo;

import android.app.Application;

import com.qihoo.gamecenter.sdk.matrix.Matrix;

public class SDKMainApplication extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();

        // 此处必须先初始化360SDK
        Matrix.initInApplication(this);
    }
}

package com.mapdemo;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by ShiShow_xk on 2017/4/13.
 */
public class TApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        //注意：在SDK各功能组件使用之前都需要调用SDKInitializer.initialize(getApplicationContext());，因此我们建议该方法放在Application的初始化方法中
        SDKInitializer.initialize(getApplicationContext());
    }
}

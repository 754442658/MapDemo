package com.mapdemo.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by ShiShow_xk on 2017/4/13.
 */
public class MyOrientationListener implements SensorEventListener {
    //传感器管理者
    private SensorManager mSensorManager;
    //上下文
    private Context mContext;
    //传感器
    private Sensor mSensor;
    //方向传感器有三个坐标，现在只关注X
    private float mLastX;
    // 传感器的回调方法
    private OnOrientationListener onOrientationListener;

    //构造函数
    public MyOrientationListener(Context mContext) {
        this.mContext = mContext;
    }

    // 方向传感器是否正在监听
    public boolean isRun = false;

    //回掉方法
    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

    /**
     * 设置回调监听
     *
     * @param onOrientationListener
     */
    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener;
    }

    //开始监听
    public void start() {
        //获得传感器管理者
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {//是否支持
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if (mSensor != null) {//如果手机有方向传感器，精度可以自己去设置，注册方向传感器
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
            isRun = true;
        }
    }

    //结束监听
    public void stop() {
        //取消注册的方向传感器
        mSensorManager.unregisterListener(this);
        isRun = false;
    }

    /**
     * 传感器发生改变
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //判断返回的传感器类型是不是方向传感器
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            //只获取x的值
            float x = event.values[SensorManager.DATA_X];
            //为了防止经常性的更新
            if (Math.abs(x - mLastX) > 1.0) {
                if (onOrientationListener != null) {
                    onOrientationListener.onOrientationChanged(x);
                }
            }
            mLastX = x;
        }
    }

    /**
     * 当传感器精度发生改变，当前不用
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

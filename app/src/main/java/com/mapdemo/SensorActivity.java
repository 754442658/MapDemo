package com.mapdemo;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SensorActivity extends Activity implements SensorEventListener {
    @InjectView(R.id.tv)
    TextView tv;
    @InjectView(R.id.tv1)
    TextView tv1;

    SensorManager sensorManager;
    private float[] gravity = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        ButterKnife.inject(this);

        //获取传感器SensorManager对象
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //注册临近传感器
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        init();
    }

    /**
     * 测试加速传感器和重力传感器
     */
    private void testSensor() {

    }

    /**
     * 获取手机上的传感器列表
     */
    private void init() {
        // 获取手机可用传感器列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < sensors.size(); i++) {
            StringBuilder sb = new StringBuilder();
            Sensor s = sensors.get(i);
            sb.append("***************************\n");
            sb.append("名称:").append(s.getName()).append("\n");
            sb.append("类型:").append(s.getType()).append(" ");
            String type = "未知传感器";
            switch (s.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    type = "加速度传感器";
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    type = "温度传感器";
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    type = "TYPE_GAME_ROTATION_VECTOR";
                    break;
                case Sensor.TYPE_GRAVITY:
                    type = "重力传感器";
                    break;
                case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                    type = "TYPE_GEOMAGNETIC_ROTATION_VECTOR";
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    type = "陀螺仪传感器";
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    type = "TYPE_GYROSCOPE_UNCALIBRATED";
                    break;
                case Sensor.TYPE_HEART_RATE:
                    type = "TYPE_HEART_RATE";
                    break;
                case Sensor.TYPE_LIGHT:
                    type = "环境光线传感器";
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    type = "电磁场传感器";
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    type = "线性加速传感器";
                    break;
                case Sensor.TYPE_PRESSURE:
                    type = "压力传感器";
                    break;
                case Sensor.TYPE_PROXIMITY:
                    type = "临近传感器";
                    break;
                case Sensor.TYPE_ORIENTATION:
                    type = "方向传感器";
                    break;
                case Sensor.TYPE_TEMPERATURE:
                    type = "温度传感器";
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    type = "计步传感器";
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    type = "旋转向量传感器";
                    break;
            }
            sb.append(type).append("\n");
            sb.append("版本:").append(s.getVersion()).append("\n");
            sb.append("供应商:").append(s.getVendor()).append("\n");
            tv.append(sb.toString() + "\n");
        }
    }

    /**
     * 传感器数据变化时回调
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //判断传感器类别
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度传感器
                final float alpha = (float) 0.8;
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                String accelerometer = "加速度传感器\n" + "x:"
                        + (event.values[0] - gravity[0]) + "\n" + "y:"
                        + (event.values[1] - gravity[1]) + "\n" + "z:"
                        + (event.values[2] - gravity[2]);
                tv1.setText(accelerometer);
                //重力加速度9.81m/s^2，只受到重力作用的情况下，自由下落的加速度
                break;
            case Sensor.TYPE_GRAVITY://重力传感器
                gravity[0] = event.values[0];//单位m/s^2
                gravity[1] = event.values[1];
                gravity[2] = event.values[2];
                break;
            default:
                break;
        }
    }

    /**
     * 传感器精度变化时回调
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销传感器
        sensorManager.unregisterListener(this);
    }
}

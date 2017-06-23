package com.mapdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by ShiShow_xk on 2017/4/14.
 */
public class MyDialog extends Dialog {

    private String text;
    TextView tv;

    public MyDialog(Context context, String text) {
        super(context, android.R.style.Theme);
        this.setOwnerActivity((Activity) context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置没有提示栏
        this.text = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        tv = (TextView) findViewById(R.id.tv);
        initView();
        addListener();
    }

    private void initView() {
        tv.setText(text);
    }

    private void addListener() {
    }
}

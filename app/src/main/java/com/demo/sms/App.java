package com.demo.sms;

import android.app.Application;

import com.demo.sms.mms_util.MmsConfig;

/**
 * Created by SiKang on 2018/12/28.
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        MmsConfig.init(this);
    }
}

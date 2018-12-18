package com.demo.sms.service.phone_call;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telecom.InCallService;

import com.demo.sms.BuildConfig;
import com.demo.sms.service.utils.PreferencesManager;

/**
 * Created by SiKang on 2018/11/5.
 * 和SmsActivity同理，不过测试了几个手机，此Service都未启动，虽然已经被设为默认应用，但是系统的电话功能没有受到影响
 * 所以代码此代码未经过测试
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {
    IBinder systemService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            systemService = service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
        String defaultPkg = PreferencesManager.get().getDefaultSmsPackage();
        Intent systemIntent = new Intent(intent.getAction(), intent.getData());
        systemIntent.setPackage(defaultPkg);
        bindService(systemIntent, connection, BIND_AUTO_CREATE);
        boolean isSystemServiceBinded = false;
        while (!isSystemServiceBinded) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (systemService != null) {
                isSystemServiceBinded = true;
            }
        }
        return systemService;
    }

}

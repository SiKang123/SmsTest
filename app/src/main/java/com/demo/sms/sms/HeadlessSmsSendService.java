package com.demo.sms.sms;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.demo.sms.BuildConfig;
import com.demo.sms.utils.PreferencesManager;

/**
 * Created by SiKang on 2018/11/5.
 */
public class HeadlessSmsSendService extends Service {
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

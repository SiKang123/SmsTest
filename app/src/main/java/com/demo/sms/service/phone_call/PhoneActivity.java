package com.demo.sms.service.phone_call;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.demo.sms.BuildConfig;
import com.demo.sms.service.utils.PreferencesManager;

/**
 * Created by SiKang on 2018/11/5.
 * 和SmsActivity同理，不过测试了几个手机，此Activity都未启动，虽然已经被设为默认应用，但是系统的电话功能没有受到影响
 * 所以代码此代码为经过测试
 */
public class PhoneActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(this, "please try again", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //获取被替换之前的默认应用
            String defaultPkg = PreferencesManager.get().getDefaultSmsPackage();
            //恢复默认应用
            Intent defaultIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            defaultIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultPkg);
            startActivityForResult(defaultIntent,0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(getIntent().getAction(), getIntent().getData());
            intent.putExtras(getIntent().getExtras());
            intent.setPackage(PreferencesManager.get().getDefaultSmsPackage());
            startActivity(intent);
        }
        finish();
    }
}

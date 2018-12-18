package com.demo.sms.service.sms;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.demo.sms.BuildConfig;
import com.demo.sms.service.utils.PreferencesManager;

/**
 * Created by SiKang on 2018/11/5.
 * 短信启动页面
 * 当APP被用户设置为默认应用后，会被设置为短信功能的默认启动项，如果设备中发生了一个类似 Intent.ACTION_SENDTO 的短信相关的功能跳转，系统会直接调起此Activity，
 * 通过getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID) 方法可以清除启动项，调用此方法后，再发生如上所述的跳转时，
 * 系统会弹出一个可选列表，让用户重新选择要启动的短信APP
 */
public class SmsActivity extends FragmentActivity {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*如果clearPackagePreferredActivities() 方法被调用后，SmsActivity还是被创建了，表示用户在启动列表中，再次选择了本APP
         * 这时候隐私数据已经收集完成，暂时不再需要默认权限，且APP并没有准备短信处理功能，会让用户的短信功能瘫痪，所以在这里找到被替换之前的默认短信APP，申请将其还原回默认应用
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //获取被替换之前的默认应用
            String defaultPkg = PreferencesManager.get().getDefaultSmsPackage();
            //恢复默认应用
            Intent defaultIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            defaultIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultPkg);
            startActivityForResult(defaultIntent, 0);
        } else {
            Toast.makeText(this, "please try again!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //清除本APP的启动项
            getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
            //默认短信应用恢复后，copy 当前intent，转发给现在的默认应用处理
            Intent intent = new Intent(getIntent().getAction(), getIntent().getData());
            intent.putExtras(getIntent().getExtras());
            intent.setPackage(PreferencesManager.get().getDefaultSmsPackage());
            startActivity(intent);
        }
        finish();
    }
}

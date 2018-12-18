package com.demo.sms;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Toast;

import com.demo.sms.utils.PreferencesManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferencesManager.get().init(getApplicationContext());

        //申请默认短信,会弹出授权框，同意后会成为默认短信程序
        findViewById(R.id.default_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String defaultPkg = Telephony.Sms.getDefaultSmsPackage(MainActivity.this);
                    //保存被替换的短信应用包名
                    if (!defaultPkg.equals(getPackageName())) {
                        PreferencesManager.get().saveDefaultSmsPackage(defaultPkg);
                        //申请成为默认
                        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, new Intent());
                    }

                }
            }
        });

        //申请默认电话,会弹出授权框，同意后会成为默认电话程序
        findViewById(R.id.default_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TelecomManager tm = (TelecomManager) getSystemService(Activity.TELECOM_SERVICE);
                    String defaultPkg = tm.getDefaultDialerPackage();
                    if (!defaultPkg.equals(getPackageName())) {
                        PreferencesManager.get().saveDefaultCallPackage(defaultPkg);
                        //申请成为默认
                        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                        startActivityForResult(intent, 1);
                    } else {
                        onActivityResult(1, RESULT_OK, new Intent());
                    }

                }
            }
        });

        findViewById(R.id.send_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除默认启动项，清除后依然是默认程序，但是不会自动响应短信跳转
                getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
                //发起短信intent，清除启动项后，会让用户主动选择启动目标，用户点击系统短信，就可以回到系统短信APP
                Uri sms_uri = Uri.parse("smsto:13517596490");//设置号码
                Intent intent = new Intent(Intent.ACTION_SENDTO, sms_uri);//调用发短信Action
                intent.putExtra("sms_body", "testest");
                startActivity(intent);
            }
        });


        findViewById(R.id.start_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除默认启动项
                getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
                //发起电话Intent
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:13517596490");
                intent.setData(data);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    Toast.makeText(this, "默认短信设置成功，可以开始收集数据", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, "默认电话设置成功，可以开始收集数据", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            /*
             * 申请被拒绝，这里不要做重新申请的逻辑，有的手机会禁用这个申请，不会弹出授权框，直接自动拒绝，如果做了拒绝再申请，会无限循环
             * 这时可以引导用户去手动设置默认程序
             * */
            switch (requestCode) {
                case 0:
                    Toast.makeText(this, "默认短信申请被拒绝，引导用户手动设置", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, "默认电话设置被拒绝，引导用户手动设置", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}

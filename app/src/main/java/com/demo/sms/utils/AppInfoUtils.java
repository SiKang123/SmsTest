package com.demo.sms.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.content.PermissionChecker;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Created by SiKang on 2018/12/10.
 */
public class AppInfoUtils {

    /**
     * 获取应用程序名称
     */
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取AndroidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 判断是否存在某个包且可获取入口
     */
    public static boolean isPackageExist(Context context, String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (context != null) {
            try {
                if (!(TextUtils.isEmpty(packageName) || context.getPackageManager().getLaunchIntentForPackage(packageName) == null)) {
                    return true;
                }
            } catch (Exception unused) {
                return false;
            }
        }
        return false;
    }

    /**
     * 是否是默认短信应用
     */
    public static boolean isDefaultSmsApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultPkg = Telephony.Sms.getDefaultSmsPackage(context);
            return defaultPkg.equals(context.getPackageName());
        }
        return false;
    }

    /**
     * 是否是默认电话应用
     */
    public static boolean isDefaultCallApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager tm = (TelecomManager) context.getSystemService(Activity.TELECOM_SERVICE);
            return tm.getDefaultDialerPackage().equals(context.getPackageName());
        }
        return false;
    }

    /**
     * 是否是默认辅助应用
     */
    public static void setDefaultAssist(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


    @SuppressLint("MissingPermission")
    public static String getPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String[] permissions = new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE};
        for (String permission : permissions) {
            boolean isGranted = Build.VERSION.SDK_INT < 23 || PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
            if (!isGranted) {
                return "";
            }
        }
        return telephonyManager.getLine1Number();

    }
}

package com.demo.sms.service.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by SiKang on 2018/9/18.
 * SharePreferences 管理
 */
public class PreferencesManager {
    private static final String PREFERENCES_FILE_NAME = "base_info";
    //单例
    private static PreferencesManager preferencesManager;
    // 获取SharedPreferences对象
    private SharedPreferences mSharedPreferences;
    // 获取Editor对象
    private SharedPreferences.Editor mEditor;
    private Context context;

    private PreferencesManager() {

    }

    public void init(Context context) {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static PreferencesManager get() {
        if (preferencesManager == null) {
            synchronized (PreferencesManager.class) {
                if (preferencesManager == null) {
                    preferencesManager = new PreferencesManager();
                }
            }
        }
        return preferencesManager;
    }

    /**
     * 手机默认短信应用的包名
     */
    public void saveDefaultSmsPackage(String pkgName) {
        saveData("default_sms_pkg", pkgName);
    }

    public String getDefaultSmsPackage() {
        return getString("default_sms_pkg", "");
    }

    /**
     * 手机默认电话应用的包名
     */
    public void saveDefaultCallPackage(String pkgName) {
        saveData("default_call_pkg", pkgName);
    }

    public String getDefaultCallPackage() {
        return getString("default_call_pkg", "");
    }


    /**
     * 存取 install referrer
     */
    public void saveInstallReferrer(String referrer) {
        saveData("install_referrer", referrer);
    }

    public String getInstallReferrer() {
        return getString("install_referrer", "");
    }



    /**
     * 保存单条数据
     */
    public void saveData(String key, String data) {
        mEditor.putString(key, data);
        mEditor.commit();
    }

    /**
     * 保存单条数据(int)
     */
    public void saveData(String key, int data) {
        mEditor.putInt(key, data);
        mEditor.commit();
    }


    /**
     * 保存单条数据(boolean)
     */
    public void saveData(String key, boolean flag) {
        mEditor.putBoolean(key, flag);
        mEditor.commit();
    }


    /**
     * 删除一条数据
     */
    public void remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }

    /**
     * 保存多条数据
     */
    public void saveData(Map<String, String> map) {
        for (String key : map.keySet()) {
            mEditor.putString(key, map.get(key));
        }
        mEditor.commit();
    }

    /**
     * 根据key取指定字段
     */
    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    /**
     * 根据key取指定字段(int)
     */
    public int getInt(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    /**
     * 根据key取指定字段(boolean)
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * 清除数据
     */
    public void clearAll() {
        mEditor = mSharedPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }

}

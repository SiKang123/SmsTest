package com.demo.sms.mms_util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Carriers;
import android.text.TextUtils;
import android.util.Log;

import custom.google.android.mms.util.SqliteWrapper;

public class TransactionSettings {
    private static final String[] APN_PROJECTION = new String[]{"type", "mmsc", "mmsproxy", "mmsport"};
    private static final int COLUMN_MMSC = 1;
    private static final int COLUMN_MMSPORT = 3;
    private static final int COLUMN_MMSPROXY = 2;
    private static final int COLUMN_TYPE = 0;
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = true;
    private static final String TAG = "TransactionSettings";
    private String mProxyAddress;
    private int mProxyPort = -1;
    private String mServiceCenter;

    public TransactionSettings(Context context, String apnName) {
        String selection;
        String str = TAG;
        if (apnName != null) {
            selection = "apn='" + apnName + "'";
        } else {
            selection = null;
        }
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Uri.withAppendedPath(Carriers.CONTENT_URI, "current"), APN_PROJECTION, selection, null, null);
        String str2;
        if (cursor == null) {
            str2 = TAG;
            Log.e(str, "Apn is not found in Database!");
            return;
        }
        boolean sawValidApn = false;
        while (cursor.moveToNext() && TextUtils.isEmpty(this.mServiceCenter)) {
            String portString = null;
            try {
                if (isValidApnType(cursor.getString(0), "mms")) {
                    sawValidApn = true;
                    this.mServiceCenter = cursor.getString(1);
                    this.mProxyAddress = cursor.getString(2);
                    if (isProxySet()) {
                        portString = cursor.getString(3);
                        this.mProxyPort = Integer.parseInt(portString);
                    } else {
                        continue;
                    }
                }
            } catch (NumberFormatException e) {
                NumberFormatException e2 = e;
                if (TextUtils.isEmpty(portString)) {
                    Log.w(TAG, "mms port not set!");
                } else {
                    Log.e(TAG, "Bad port number format: " + portString, e2);
                }
            } catch (Throwable th) {
                cursor.close();
            }
        }
        cursor.close();
        if (sawValidApn && TextUtils.isEmpty(this.mServiceCenter)) {
            str2 = TAG;
            Log.e(str, "Invalid APN setting: MMSC is empty");
        }
    }

    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        this.mServiceCenter = mmscUrl;
        this.mProxyAddress = proxyAddr;
        this.mProxyPort = proxyPort;
    }

    public String getMmscUrl() {
        return this.mServiceCenter;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    public boolean isProxySet() {
        return (this.mProxyAddress == null || this.mProxyAddress.trim().length() == 0) ? false : true;
    }

    private static boolean isValidApnType(String types, String requestType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals("*")) {
                return true;
            }
        }
        return false;
    }
}

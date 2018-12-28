package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import custom.google.android.mms.util.PduCache;

@SuppressLint({"LogTagMismatch","WrongConstant"})
public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static final String TAG = "MmsSystemEventReceiver";
    private static MmsSystemEventReceiver sMmsSystemEventReceiver;


    private static void wakeUpService(Context context) {
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            Log.v(TAG, "wakeUpService: start transaction service ...");
        }
        context.startService(new Intent(context, TransactionService.class));
    }

    public void onReceive(Context context, Intent intent) {
        String str = TAG;
        String str2 = LogTag.TRANSACTION;
        String str3 = LogTag.TRANSACTION;
        if (Log.isLoggable(str2, 2)) {
            str3 = TAG;
            Log.v(str, "Intent received: " + intent);
        }
        String action = intent.getAction();
        if (action.equals("android.intent.action.CONTENT_CHANGED")) {
            PduCache.getInstance().purge((Uri) intent.getParcelableExtra("deleted_contents"));
        } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
            String state = intent.getStringExtra(TransactionService.STATE);
            str3 = LogTag.TRANSACTION;
            if (Log.isLoggable(str2, 2)) {
                str3 = TAG;
                Log.v(str, "ANY_DATA_STATE event received: " + state);
            }
            if (state.equals("CONNECTED")) {
                wakeUpService(context);
            }
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
//            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
        }
    }

    public static void registerForConnectionStateChanges(Context context) {
        unRegisterForConnectionStateChanges(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            Log.v(TAG, "registerForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver == null) {
            sMmsSystemEventReceiver = new MmsSystemEventReceiver();
        }
        context.registerReceiver(sMmsSystemEventReceiver, intentFilter);
    }

    public static void unRegisterForConnectionStateChanges(Context context) {
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            Log.v(TAG, "unRegisterForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver != null) {
            try {
                context.unregisterReceiver(sMmsSystemEventReceiver);
            } catch (IllegalArgumentException e) {
            }
        }
    }
}

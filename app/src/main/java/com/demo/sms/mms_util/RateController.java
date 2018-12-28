package com.demo.sms.mms_util;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.Telephony.Mms.Rate;
import android.util.Log;

import custom.google.android.mms.util.SqliteWrapper;

public class RateController {
    private static final int ANSWER_NO = 2;
    public static final int ANSWER_TIMEOUT = 20000;
    private static final int ANSWER_YES = 1;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final int NO_ANSWER = 0;
    private static final long ONE_HOUR = 3600000;
    private static final int RATE_LIMIT = 100;
    public static final String RATE_LIMIT_CONFIRMED_ACTION = "com.joker.mmsfolder.RATE_LIMIT_CONFIRMED";
    public static final String RATE_LIMIT_SURPASSED_ACTION = "com.joker.mmsfolder.RATE_LIMIT_SURPASSED";
    private static final String TAG = "RateController";
    private static RateController sInstance;
    private static boolean sMutexLock;
    private int mAnswer;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (RateController.RATE_LIMIT_CONFIRMED_ACTION.equals(intent.getAction())) {
                synchronized (this) {
                    RateController.this.mAnswer = intent.getBooleanExtra("answer", false) ? 1 : 2;
                    notifyAll();
                }
            }
        }
    };
    private final Context mContext;

    private RateController(Context context) {
        this.mContext = context;
    }

    public static void init(Context context) {
        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new RateController(context);
    }

    public static RateController getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("Uninitialized.");
    }

    public final void update() {
        ContentValues values = new ContentValues(1);
        values.put("sent_time", Long.valueOf(System.currentTimeMillis()));
        SqliteWrapper.insert(this.mContext, this.mContext.getContentResolver(), Rate.CONTENT_URI, values);
    }

    public final boolean isLimitSurpassed() {
        Cursor c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), Rate.CONTENT_URI, new String[]{"COUNT(*) AS rate"}, "sent_time>" + (System.currentTimeMillis() - ONE_HOUR), null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    boolean z = c.getInt(0) >= 100;
                    c.close();
                    return z;
                }
                c.close();
            } catch (Throwable th) {
                c.close();
            }
        }
        return false;
    }

    public synchronized boolean isAllowedByUser() {
        boolean z;
        while (sMutexLock) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        sMutexLock = true;
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter(RATE_LIMIT_CONFIRMED_ACTION));
        this.mAnswer = 0;
        try {
            Intent intent = new Intent(RATE_LIMIT_SURPASSED_ACTION);
//            intent.addFlags(268435456);
            this.mContext.startActivity(intent);
            if (waitForAnswer() == 1) {
                z = true;
            } else {
                z = false;
            }
        } finally {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            sMutexLock = false;
            notifyAll();
        }
        return z;
    }

    private synchronized int waitForAnswer() {
        int t = 0;
        while (this.mAnswer == 0 && t < ANSWER_TIMEOUT) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
            }
            t += 1000;
        }
        return this.mAnswer;
    }
}

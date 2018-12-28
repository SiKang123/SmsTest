package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import custom.google.android.mms.MmsException;
import custom.google.android.mms.pdu.NotificationInd;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.util.SqliteWrapper;

@SuppressLint("WrongConstant")
public class DownloadManager {
    private static final boolean DEBUG = false;
    private static final int DEFERRED_MASK = 4;
    private static final boolean LOCAL_LOGV = false;
    public static final int STATE_DOWNLOADING = 129;
    public static final int STATE_PERMANENT_FAILURE = 135;
    public static final int STATE_TRANSIENT_FAILURE = 130;
    public static final int STATE_UNSTARTED = 128;
    private static final String TAG = "DownloadManager";
    private static DownloadManager sInstance;
    private boolean mAutoDownload;
    private final Context mContext;
    private final Handler mHandler;
    private final SharedPreferences mPreferences;
    private final OnSharedPreferenceChangeListener mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//            if (MessagingPreferenceActivity.AUTO_RETRIEVAL.equals(key) || MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING.equals(key)) {
//                synchronized (DownloadManager.sInstance) {
//                    DownloadManager.this.mAutoDownload = DownloadManager.getAutoDownloadState(prefs);
//                }
//            }
        }
    };
    private final BroadcastReceiver mRoamingStateListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
//                boolean isRoaming = ServiceState.newFromBundle(intent.getExtras()).getRoaming();
//                synchronized (DownloadManager.sInstance) {
//                    DownloadManager.this.mAutoDownload = DownloadManager.getAutoDownloadState(DownloadManager.this.mPreferences, isRoaming);
//                }
            }
        }
    };

    private DownloadManager(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mPreferences.registerOnSharedPreferenceChangeListener(this.mPreferencesChangeListener);
        context.registerReceiver(this.mRoamingStateListener, new IntentFilter("android.intent.action.SERVICE_STATE"));
        this.mAutoDownload = getAutoDownloadState(this.mPreferences);
    }

    public boolean isAuto() {
        return this.mAutoDownload;
    }

    public static void init(Context context) {
        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new DownloadManager(context);
    }

    public static DownloadManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("Uninitialized.");
    }

    static boolean getAutoDownloadState(SharedPreferences prefs) {
        return getAutoDownloadState(prefs, isRoaming());
    }

    static boolean getAutoDownloadState(SharedPreferences prefs, boolean roaming) {
//        if (prefs.getBoolean(MessagingPreferenceActivity.AUTO_RETRIEVAL, true)) {
//            boolean alwaysAuto = prefs.getBoolean(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING, false);
//            if (!roaming || alwaysAuto) {
//                return true;
//            }
//        }
        return false;
    }

    static boolean isRoaming() {
//        return "true".equals(SystemProperties.get("gsm.operator.isroaming", null));
        return false;
    }

    public void markState(final Uri uri, int state) {
        try {
            if (((NotificationInd) PduPersister.getPduPersister(this.mContext).load(uri)).getExpiry() >= System.currentTimeMillis() / 1000 || state != STATE_DOWNLOADING) {
                if (state == STATE_PERMANENT_FAILURE) {
                    this.mHandler.post(new Runnable() {

                        public void run() {
                            try {
                                Toast.makeText(DownloadManager.this.mContext, DownloadManager.this.getMessage(uri), 1).show();
                            } catch (MmsException e) {
                                MmsException e2 = e;
                                Log.e(DownloadManager.TAG, e2.getMessage(), e2);
                            }
                        }
                    });
                } else if (!this.mAutoDownload) {
                    state |= 4;
                }
                ContentValues values = new ContentValues(1);
                values.put("st", Integer.valueOf(state));
                SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, values, null, null);
                return;
            }
            this.mHandler.post(new Runnable() {
                public void run() {
//                    Toast.makeText(DownloadManager.this.mContext, 2131231007, 1).show();
                }
            });
            SqliteWrapper.delete(this.mContext, this.mContext.getContentResolver(), uri, null, null);
        } catch (MmsException e) {
            MmsException e2 = e;
            Log.e(TAG, e2.getMessage(), e2);
        }
    }

    public void showErrorCodeToast(int errorStr) {
        final int errStr = errorStr;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(DownloadManager.this.mContext, errStr, 1).show();
                } catch (Exception e) {
                    Log.e(DownloadManager.TAG, "Caught an exception in showErrorCodeToast");
                }
            }
        });
    }

    private String getMessage(Uri uri) throws MmsException {
//        NotificationInd ind = (NotificationInd) PduPersister.getPduPersister(this.mContext).load(uri);
//        EncodedStringValue v = ind.getSubject();
//        String subject = v != null ? v.getString() : this.mContext.getString(2131231004);
//        String subject = v != null ? v.getString() : "";
//        v = ind.getFrom();
//        String from = v != null ? Contact.get(v.getString(), false).getName() : this.mContext.getString(2131231005);
//        return this.mContext.getString(2131231006, new Object[]{subject, from});
        return "";
    }

    public int getState(Uri uri) {
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, new String[]{"st"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int i = cursor.getInt(0) & -5;
                    return i;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return STATE_UNSTARTED;
    }
}

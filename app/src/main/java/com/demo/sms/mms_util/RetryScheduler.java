package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.provider.Telephony.Mms.Outbox;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.support.annotation.RequiresApi;
import android.util.Log;

import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.util.SqliteWrapper;
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
@SuppressLint({"LogTagMismatch", "WrongConstant"})
public class RetryScheduler implements Observer {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "RetryScheduler";
    private static RetryScheduler sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    private RetryScheduler(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public static RetryScheduler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RetryScheduler(context);
        }
        return sInstance;
    }

    private boolean isConnected() {
        return ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(2).isConnected();
    }


    public void update(Observable observable) {
        Transaction t;
        try {
            t = (Transaction) observable;
            if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                Log.v(TAG, "[RetryScheduler] update " + observable);
            }
            if ( (t instanceof RetrieveTransaction) || (t instanceof ReadRecTransaction) || (t instanceof SendTransaction)) {
                TransactionState state = t.getState();
                if (state.getState() == 2) {
                    Uri uri = state.getContentUri();
                    if (uri != null) {
                        scheduleRetry(uri);
                    }
                }
                t.detach(this);
            }
            if (isConnected()) {
                setRetryAlarm(this.mContext);
            }
        } catch (Throwable th) {
            if (isConnected()) {
                setRetryAlarm(this.mContext);
            }
        }
    }


    private void scheduleRetry(Uri uri) {
        long msgId = ContentUris.parseId(uri);
        Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, null, null, null);
        if (cursor != null) {
            Cursor c;
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    boolean isRetryDownloading;
                    int msgType = cursor.getInt(cursor.getColumnIndexOrThrow("msg_type"));
                    int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                    int errorType = 1;
                    DefaultRetryScheme defaultRetryScheme = new DefaultRetryScheme(this.mContext, retryIndex);
                    ContentValues values = new ContentValues(4);
                    long current = System.currentTimeMillis();
                    if (msgType == 130) {
                        isRetryDownloading = true;
                    } else {
                        isRetryDownloading = false;
                    }
                    boolean retry = true;
                    if (getResponseStatus(msgId) == 132) {
                        DownloadManager.getInstance().showErrorCodeToast(2131230850);
                        retry = false;
                    }
                    if (retryIndex >= defaultRetryScheme.getRetryLimit() || !retry) {
                        errorType = 10;
                        if (isRetryDownloading) {
                            c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, new String[]{"thread_id"}, null, null, null);
                            long threadId = -1;
                            if (c != null) {
                                if (c.moveToFirst()) {
                                    threadId = c.getLong(0);
                                }
                                c.close();
                            }
                            if (threadId != -1) {
//                                MessagingNotification.notifyDownloadFailed(this.mContext, threadId);
                            }
                            DownloadManager.getInstance().markState(uri, DownloadManager.STATE_PERMANENT_FAILURE);
                        } else {
                            ContentValues readValues = new ContentValues(1);
                            readValues.put("read", Integer.valueOf(0));
                            SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, readValues, null, null);
//                            MessagingNotification.notifySendFailed(this.mContext, true);
                        }
                    } else {
                        long retryAt = current + defaultRetryScheme.getWaitingInterval();
                        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                            Log.v(TAG, "scheduleRetry: retry for " + uri + " is scheduled at " + (retryAt - System.currentTimeMillis()) + "ms from now");
                        }
                        values.put("due_time", Long.valueOf(retryAt));
                        if (isRetryDownloading) {
                            DownloadManager.getInstance().markState(uri, DownloadManager.STATE_TRANSIENT_FAILURE);
                        }
                    }
                    values.put("err_type", Integer.valueOf(errorType));
                    values.put("retry_index", Integer.valueOf(retryIndex));
                    values.put("last_try", Long.valueOf(current));
                    SqliteWrapper.update(this.mContext, this.mContentResolver, PendingMessages.CONTENT_URI, values, "_id=" + cursor.getLong(cursor.getColumnIndexOrThrow("_id")), null);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
        }
    }

    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, Outbox.CONTENT_URI, null, "_id=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                respStatus = cursor.getInt(cursor.getColumnIndexOrThrow("resp_st"));
            }
            cursor.close();
            if (respStatus != 0) {
                Log.e(TAG, "Response status is: " + respStatus);
            }
            return respStatus;
        } catch (Throwable th) {
            cursor.close();
        }
        return 0;
    }

    public static void setRetryAlarm(Context context) {
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(Long.MAX_VALUE);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long retryAt = cursor.getLong(cursor.getColumnIndexOrThrow("due_time"));
                    ((AlarmManager) context.getSystemService("alarm")).set(1, retryAt, PendingIntent.getService(context, 0, new Intent(TransactionService.ACTION_ONALARM, null, context, TransactionService.class), 1073741824));
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "Next retry is scheduled at" + (retryAt - System.currentTimeMillis()) + "ms from now");
                    }
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }
}

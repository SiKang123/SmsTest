package com.demo.sms.mms_util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.demo.sms.utils.AppInfoUtils;

import java.io.IOException;

import custom.google.android.mms.MmsException;
import custom.google.android.mms.pdu.AcknowledgeInd;
import custom.google.android.mms.pdu.EncodedStringValue;
import custom.google.android.mms.pdu.PduComposer;
import custom.google.android.mms.pdu.PduParser;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.pdu.RetrieveConf;
import custom.google.android.mms.util.SqliteWrapper;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class RetrieveTransaction extends Transaction implements Runnable {
    static final int COLUMN_CONTENT_LOCATION = 0;
    static final int COLUMN_LOCKED = 1;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    static final String[] PROJECTION = new String[]{"ct_l", "locked"};
    private static final String TAG = "RetrieveTransaction";
    private final String mContentLocation;
    private boolean mLocked;
    private final Uri mUri;

    public RetrieveTransaction(Context context, int serviceId, TransactionSettings connectionSettings, String uri) throws MmsException {
        super(context, serviceId, connectionSettings);
        if (uri.startsWith("content://")) {
            this.mUri = Uri.parse(uri);
            String contentLocation = getContentLocation(context, this.mUri);
            this.mContentLocation = contentLocation;
            this.mId = contentLocation;
            attach(RetryScheduler.getInstance(context));
            return;
        }
        throw new IllegalArgumentException("Initializing from X-Mms-Content-Location is abandoned!");
    }

    private String getContentLocation(Context context, Uri uri) throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, PROJECTION, null, null, null);
        this.mLocked = false;
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    boolean z;
                    if (cursor.getInt(1) == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    this.mLocked = z;
                    String string = cursor.getString(0);
                    return string;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    public void process() {
        new Thread(this).start();
    }


    public void run() {
        String str = "Retrieval failed.";
        String str2 = TAG;
        try {
            DownloadManager.getInstance().markState(this.mUri, DownloadManager.STATE_DOWNLOADING);
            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(getPdu(this.mContentLocation)).parse();
            if (retrieveConf == null) {
                throw new MmsException("Invalid M-Retrieve.conf PDU.");
            }
            Uri msgUri = null;
            if (isDuplicateMessage(this.mContext, retrieveConf)) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mUri);
            } else {
                msgUri = PduPersister.getPduPersister(this.mContext).persist(retrieveConf, Inbox.CONTENT_URI);
                this.mTransactionState.setState(1);
                this.mTransactionState.setContentUri(msgUri);
                updateContentLocation(this.mContext, msgUri, this.mContentLocation, this.mLocked);
            }
            SqliteWrapper.delete(this.mContext, this.mContext.getContentResolver(), this.mUri, null, null);
            if (msgUri != null) {
                Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(this.mContext, msgUri);
            }
            sendAcknowledgeInd(retrieveConf);
        } catch (Throwable th) {
            Log.e(TAG, Log.getStackTraceString(th));
        } finally {
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mUri);
                String str3 = TAG;
                str3 = "Retrieval failed.";
                Log.e(str2, str);
            }
            notifyObservers();
        }
    }

    private static boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        if (rc.getMessageId() != null) {
            String[] selectionArgs = new String[]{new String(rc.getMessageId()), String.valueOf(132)};
            Context context2 = context;
            Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"_id"}, "(m_id = ? AND m_type = ?)", selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        return true;
                    }
                    cursor.close();
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private void sendAcknowledgeInd(RetrieveConf rc) throws MmsException, IOException {
        byte[] tranId = rc.getTransactionId();
        if (tranId != null) {
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(18, tranId);
            acknowledgeInd.setFrom(new EncodedStringValue(AppInfoUtils.getPhoneNumber(mContext)));
            if (MmsConfig.getNotifyWapMMSC()) {
                sendPdu(new PduComposer(this.mContext, acknowledgeInd).make(), this.mContentLocation);
            } else {
                sendPdu(new PduComposer(this.mContext, acknowledgeInd).make());
            }
        }
    }

    private static void updateContentLocation(Context context, Uri uri, String contentLocation, boolean locked) {
        ContentValues values = new ContentValues(2);
        values.put("ct_l", contentLocation);
        values.put("locked", Boolean.valueOf(locked));
        SqliteWrapper.update(context, context.getContentResolver(), uri, values, null, null);
    }

    public int getType() {
        return 1;
    }
}

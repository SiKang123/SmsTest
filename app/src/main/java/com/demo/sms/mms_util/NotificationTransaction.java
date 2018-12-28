package com.demo.sms.mms_util;

import android.content.Context;
import android.net.Uri;
import android.provider.Telephony.Mms.Inbox;
import android.util.Log;

import java.io.IOException;

import custom.google.android.mms.MmsException;
import custom.google.android.mms.pdu.NotificationInd;
import custom.google.android.mms.pdu.NotifyRespInd;
import custom.google.android.mms.pdu.PduComposer;
import custom.google.android.mms.pdu.PduPersister;

public class NotificationTransaction extends Transaction implements Runnable {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "NotificationTransaction";
    private String mContentLocation;
    private NotificationInd mNotificationInd;
    private Uri mUri;

    public NotificationTransaction(Context context, int serviceId, TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);
        this.mUri = Uri.parse(uriString);
        try {
            this.mNotificationInd = (NotificationInd) PduPersister.getPduPersister(context).load(this.mUri);
            this.mId = new String(this.mNotificationInd.getTransactionId());
            this.mContentLocation = new String(this.mNotificationInd.getContentLocation());
            attach(RetryScheduler.getInstance(context));
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load NotificationInd from: " + uriString, e);
            throw new IllegalArgumentException();
        }
    }

    public NotificationTransaction(Context context, int serviceId, TransactionSettings connectionSettings, NotificationInd ind) {
        super(context, serviceId, connectionSettings);
        try {
            this.mUri = PduPersister.getPduPersister(context).persist(ind, Inbox.CONTENT_URI);
            this.mNotificationInd = ind;
            this.mId = new String(ind.getTransactionId());
        } catch (MmsException e) {
            Log.e(TAG, "Failed to save NotificationInd in constructor.", e);
            throw new IllegalArgumentException();
        }
    }

    public void process() {
        new Thread(this).start();
    }

    public void run() {
        throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.transaction.NotificationTransaction.run():void");
    }

    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        NotifyRespInd notifyRespInd = new NotifyRespInd(18, this.mNotificationInd.getTransactionId(), status);
        if (MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(this.mContext, notifyRespInd).make(), this.mContentLocation);
        } else {
            sendPdu(new PduComposer(this.mContext, notifyRespInd).make());
        }
    }

    public int getType() {
        return 0;
    }
}

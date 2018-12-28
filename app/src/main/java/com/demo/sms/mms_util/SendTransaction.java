package com.demo.sms.mms_util;

import android.content.Context;
import android.net.Uri;

public class SendTransaction extends Transaction implements Runnable {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "SendTransaction";
    private final Uri mSendReqURI;
    private Thread mThread;

    public SendTransaction(Context context, int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        this.mSendReqURI = Uri.parse(uri);
        this.mId = uri;
        attach(RetryScheduler.getInstance(context));
    }

    public void process() {
        this.mThread = new Thread(this);
        this.mThread.start();
    }

    public void run() {
//        try {
//            RateController rateCtlr = RateController.getInstance();
//            if (!rateCtlr.isLimitSurpassed() || rateCtlr.isAllowedByUser()) {
//                PduPersister persister = PduPersister.getPduPersister(this.mContext);
//                GenericPdu sendReq = (SendReq) persister.load(this.mSendReqURI);
//                long date = System.currentTimeMillis() / 1000;
//                sendReq.setDate(date);
//                ContentValues values = new ContentValues(1);
//                values.put("date", Long.valueOf(date));
//                SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), this.mSendReqURI, values, null, null);
//                String lineNumber = MessageUtils.getLocalNumber();
//                if (!TextUtils.isEmpty(lineNumber)) {
//                    sendReq.setFrom(new EncodedStringValue(lineNumber));
//                }
//                long tokenKey = ContentUris.parseId(this.mSendReqURI);
//                byte[] response = sendPdu(SendingProgressTokenManager.get(Long.valueOf(tokenKey)), new PduComposer(this.mContext, sendReq).make());
//                SendingProgressTokenManager.remove(Long.valueOf(tokenKey));
//                if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
//                    Log.d(TAG, "[SendTransaction] run: send mms msg (" + this.mId + "), resp=" + new String(response));
//                }
//                SendConf conf = (SendConf) new PduParser(response).parse();
//                if (conf == null) {
//                    Log.e(TAG, "No M-Send.conf received.");
//                }
//                byte[] reqId = sendReq.getTransactionId();
//                byte[] confId = conf.getTransactionId();
//                if (Arrays.equals(reqId, confId)) {
//                    values = new ContentValues(2);
//                    int respStatus = conf.getResponseStatus();
//                    values.put("resp_st", Integer.valueOf(respStatus));
//                    if (respStatus != 128) {
//                        SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), this.mSendReqURI, values, null, null);
//                        Log.e(TAG, "Server returned an error code: " + respStatus);
//                        if (this.mTransactionState.getState() != 1) {
//                            this.mTransactionState.setState(2);
//                            this.mTransactionState.setContentUri(this.mSendReqURI);
//                            Log.e(TAG, "Delivery failed.");
//                        }
//                        notifyObservers();
//                        return;
//                    }
//                    values.put("m_id", PduPersister.toIsoString(conf.getMessageId()));
//                    SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), this.mSendReqURI, values, null, null);
//                    Uri uri = persister.move(this.mSendReqURI, Sent.CONTENT_URI);
//                    this.mTransactionState.setState(1);
//                    this.mTransactionState.setContentUri(uri);
//                    if (this.mTransactionState.getState() != 1) {
//                        this.mTransactionState.setState(2);
//                        this.mTransactionState.setContentUri(this.mSendReqURI);
//                        Log.e(TAG, "Delivery failed.");
//                    }
//                    notifyObservers();
//                    return;
//                }
//                Log.e(TAG, "Inconsistent Transaction-ID: req=" + new String(reqId) + ", conf=" + new String(confId));
//                if (this.mTransactionState.getState() != 1) {
//                    this.mTransactionState.setState(2);
//                    this.mTransactionState.setContentUri(this.mSendReqURI);
//                    Log.e(TAG, "Delivery failed.");
//                }
//                notifyObservers();
//                return;
//            }
//            Log.e(TAG, "Sending rate limit surpassed.");
//        } catch (Throwable th) {
//            Log.e(TAG, Log.getStackTraceString(th));
//        } finally {
//            if (this.mTransactionState.getState() != 1) {
//                this.mTransactionState.setState(2);
//                this.mTransactionState.setContentUri(this.mSendReqURI);
//                Log.e(TAG, "Delivery failed.");
//            }
//            notifyObservers();
//        }
    }

    public int getType() {
        return 2;
    }
}

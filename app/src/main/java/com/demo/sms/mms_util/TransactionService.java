package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Telephony.Mms;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import custom.android.common.NetworkConnectivityListener;
import custom.google.android.mms.pdu.PduPersister;

@SuppressLint({"LogTagMismatch", "WrongConstant"})
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class TransactionService extends Service implements Observer {
    public static final String ACTION_ONALARM = "android.intent.action.ACTION_ONALARM";
    private static final int APN_EXTENSION_WAIT = 30000;
    private static final int EVENT_CONTINUE_MMS_CONNECTIVITY = 3;
    private static final int EVENT_DATA_STATE_CHANGED = 2;
    private static final int EVENT_HANDLE_NEXT_PENDING_TRANSACTION = 4;
    private static final int EVENT_QUIT = 100;
    private static final int EVENT_TRANSACTION_REQUEST = 1;
    public static final String STATE = "state";
    public static final String STATE_URI = "uri";
    private static final String TAG = "TransactionService";
    private static final int TOAST_DOWNLOAD_LATER = 2;
    private static final int TOAST_MSG_QUEUED = 1;
    private static final int TOAST_NONE = -1;
    public static final String TRANSACTION_COMPLETED_ACTION = "android.intent.action.TRANSACTION_COMPLETED_ACTION";
    private ConnectivityManager mConnMgr;
    private NetworkConnectivityListener mConnectivityListener;
    private final ArrayList<Transaction> mPending = new ArrayList();
    private final ArrayList<Transaction> mProcessing = new ArrayList();
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    public Handler mToastHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private WakeLock mWakeLock;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(android.os.Message r28) {
            throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.transaction.TransactionService.ServiceHandler.handleMessage(android.os.Message):void");
        }


        private void processPendingTransaction(Transaction transaction, TransactionSettings settings) {
            int numProcessTransaction;
            String str = LogTag.TRANSACTION;
            String str2 = TransactionService.TAG;
            String str3 = LogTag.TRANSACTION;
            if (Log.isLoggable(str, 2)) {
                str3 = TransactionService.TAG;
                Log.v(str2, "processPendingTxn: transaction=" + transaction);
            }
            synchronized (TransactionService.this.mProcessing) {
                if (TransactionService.this.mPending.size() != 0) {
                    transaction = (Transaction) TransactionService.this.mPending.remove(0);
                }
                numProcessTransaction = TransactionService.this.mProcessing.size();
            }
            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }
                try {
                    int serviceId = transaction.getServiceId();
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TransactionService.TAG, "processPendingTxn: process " + serviceId);
                    }
                    if (!processTransaction(transaction)) {
                        TransactionService.this.stopSelf(serviceId);
                    } else if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TransactionService.TAG, "Started deferred processing of transaction  " + transaction);
                    }
                } catch (IOException e) {
                    IOException e2 = e;
                    str3 = TransactionService.TAG;
                    Log.w(str2, e2.getMessage(), e2);
                }
            } else if (numProcessTransaction == 0) {
                str3 = LogTag.TRANSACTION;
                if (Log.isLoggable(str, 2)) {
                    str3 = TransactionService.TAG;
                    Log.v(str2, "processPendingTxn: no more transaction, endMmsConnectivity");
                }
                TransactionService.this.endMmsConnectivity();
            }
        }
        private boolean processTransaction(Transaction r12) throws java.io.IOException {
            throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.transaction.TransactionService.ServiceHandler.processTransaction(com.joker.mmsfolder.transaction.Transaction):boolean");
        }
    }

    public void onCreate() {
        String str;
        String str2 = TAG;
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            str = TAG;
            Log.v(str2, "Creating TransactionService");
        }
        str = TAG;
        HandlerThread thread = new HandlerThread(str2);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mConnectivityListener = new NetworkConnectivityListener();
        this.mConnectivityListener.registerHandler(this.mServiceHandler, 2);
        this.mConnectivityListener.startListening(this);
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 2;
        }
        this.mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean noNetwork = !isNetworkAvailable();
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent=" + intent);
            Log.v(TAG, "    networkAvailable=" + (!noNetwork));
        }
        if (ACTION_ONALARM.equals(intent.getAction()) || intent.getExtras() == null) {
            Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(System.currentTimeMillis());
            if (cursor != null) {
                try {
                    int count = cursor.getCount();
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "onStart: cursor.count=" + count);
                    }
                    if (count == 0) {
                        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                            Log.v(TAG, "onStart: no pending messages. Stopping service.");
                        }
                        RetryScheduler.setRetryAlarm(this);
                        stopSelfIfIdle(startId);
                        return 2;
                    }
                    int columnIndexOfMsgId = cursor.getColumnIndexOrThrow("msg_id");
                    int columnIndexOfMsgType = cursor.getColumnIndexOrThrow("msg_type");
                    if (noNetwork) {
                        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                            Log.v(TAG, "onStart: registerForConnectionStateChanges");
                        }
                        MmsSystemEventReceiver.registerForConnectionStateChanges(getApplicationContext());
                    }
                    while (cursor.moveToNext()) {
                        int transactionType = getTransactionType(cursor.getInt(columnIndexOfMsgType));
                        if (noNetwork) {
                            onNetworkUnavailable(startId, transactionType);
                            cursor.close();
                            return 2;
                        }
                        switch (transactionType) {
                            case -1:
                                break;
                            case 1:
                                if (!isTransientFailure(cursor.getInt(cursor.getColumnIndexOrThrow("err_type")))) {
                                    continue;
                                }
                                break;
                        }
                        launchTransaction(startId, new TransactionBundle(transactionType, ContentUris.withAppendedId(Mms.CONTENT_URI, cursor.getLong(columnIndexOfMsgId)).toString()), false);
                    }
                    cursor.close();
                } finally {
                    cursor.close();
                }
            } else {
                if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                    Log.v(TAG, "onStart: no pending messages. Stopping service.");
                }
                RetryScheduler.setRetryAlarm(this);
                stopSelfIfIdle(startId);
            }
        } else {
            if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                Log.v(TAG, "onStart: launch transaction...");
            }
            launchTransaction(startId, new TransactionBundle(intent.getExtras()), noNetwork);
        }
        return 2;
    }

    private void stopSelfIfIdle(int startId) {
        String str = TAG;
        str = LogTag.TRANSACTION;
        synchronized (this.mProcessing) {
            if (this.mProcessing.isEmpty() && this.mPending.isEmpty()) {
                if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                    Log.v(TAG, "stopSelfIfIdle: STOP!");
                }
                if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                    Log.v(TAG, "stopSelfIfIdle: unRegisterForConnectionStateChanges");
                }
                MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());
                stopSelf(startId);
            }
        }
    }

    private static boolean isTransientFailure(int type) {
        return type < 10 && type > 0;
    }

    private boolean isNetworkAvailable() {
        return this.mConnMgr.getNetworkInfo(2).isAvailable();
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case DownloadManager.STATE_UNSTARTED /*128*/:
                return 2;
            case DownloadManager.STATE_TRANSIENT_FAILURE /*130*/:
                return 1;
            case DownloadManager.STATE_PERMANENT_FAILURE /*135*/:
                return 3;
            default:
                Log.w(TAG, "Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        String str = TAG;
        String str2;
        if (noNetwork) {
            str2 = TAG;
            Log.w(str, "launchTransaction: no network error!");
            onNetworkUnavailable(serviceId, txnBundle.getTransactionType());
            return;
        }
        Message msg = this.mServiceHandler.obtainMessage(1);
        msg.arg1 = serviceId;
        msg.obj = txnBundle;
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            str2 = TAG;
            Log.v(str, "launchTransaction: sending message " + msg);
        }
        this.mServiceHandler.sendMessage(msg);
    }

    private void onNetworkUnavailable(int serviceId, int transactionType) {
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            Log.v(TAG, "onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);
        }
        int toastType = -1;
        if (transactionType == 1) {
            toastType = 2;
        } else if (transactionType == 2) {
            toastType = 1;
        }
        if (toastType != -1) {
            this.mToastHandler.sendEmptyMessage(toastType);
        }
        stopSelf(serviceId);
    }

    public void onDestroy() {
        String str;
        String str2 = TAG;
        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
            str = TAG;
            Log.v(str2, "Destroying TransactionService");
        }
        if (!this.mPending.isEmpty()) {
            str = TAG;
            Log.w(str2, "TransactionService exiting with transaction still pending");
        }
        releaseWakeLock();
        this.mConnectivityListener.unregisterHandler(this.mServiceHandler);
        this.mConnectivityListener.stopListening();
        this.mConnectivityListener = null;
        this.mServiceHandler.sendEmptyMessage(100);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void update(Observable observable) {
        String str = TAG;
        String str2 = LogTag.TRANSACTION;
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();
        String str3 = LogTag.TRANSACTION;
        if (Log.isLoggable(str2, 2)) {
            str3 = TAG;
            Log.v(str, "update transaction " + serviceId);
        }
        try {
            synchronized (this.mProcessing) {
                this.mProcessing.remove(transaction);
                if (this.mPending.size() > 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "update: handle next pending transaction...");
                    }
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(4, transaction.getConnectionSettings()));
                } else {
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "update: endMmsConnectivity");
                    }
                    endMmsConnectivity();
                }
            }
            Intent intent = new Intent(TRANSACTION_COMPLETED_ACTION);
            TransactionState state = transaction.getState();
            int result = state.getState();
            intent.putExtra(STATE, result);
            switch (result) {
                case 1:
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "Transaction complete: " + serviceId);
                    }
                    intent.putExtra("uri", state.getContentUri());
                    switch (transaction.getType()) {
                        case 0:
                        case 1:
//                            MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
//                            MessagingNotification.updateDownloadFailedNotification(this);
                            break;
                        case 2:
                            RateController.getInstance().update();
                            break;
                    }
                    break;
                case 2:
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "Transaction failed: " + serviceId);
                        break;
                    }
                    break;
                default:
                    if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                        Log.v(TAG, "Transaction state unknown: " + serviceId + " " + result);
                        break;
                    }
                    break;
            }
            if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
                Log.v(TAG, "update: broadcast transaction result " + result);
            }
            sendBroadcast(intent);
        } finally {
            transaction.detach(this);
            MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());
            stopSelf(serviceId);
        }
    }

    private synchronized void createWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "MMS Connectivity");
            this.mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        this.mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

//    protected int beginMmsConnectivity() throws IOException {
//        createWakeLock();
//        int result = this.mConnMgr.startUsingNetworkFeature(0, "enableMMS");
//        if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
//            Log.v(TAG, "beginMmsConnectivity: result=" + result);
//        }
//        switch (result) {
//            case 0:
//            case 1:
//                acquireWakeLock();
//                return result;
//            default:
//                throw new IOException("Cannot establish MMS connectivity");
//        }
//    }

    protected void endMmsConnectivity() {
//        try {
//            if (Log.isLoggable(LogTag.TRANSACTION, 2)) {
//                Log.v(TAG, "endMmsConnectivity");
//            }
//            this.mServiceHandler.removeMessages(3);
//            if (this.mConnMgr != null) {
//                this.mConnMgr.stopUsingNetworkFeature(0, "enableMMS");
//            }
//            releaseWakeLock();
//        } catch (Throwable th) {
//            releaseWakeLock();
//        }
    }
}

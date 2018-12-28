package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class Transaction extends Observable {
    public static final int NOTIFICATION_TRANSACTION = 0;
    public static final int READREC_TRANSACTION = 3;
    public static final int RETRIEVE_TRANSACTION = 1;
    public static final int SEND_TRANSACTION = 2;
    protected Context mContext;
    protected String mId;
    private final int mServiceId;
    protected TransactionSettings mTransactionSettings;
    protected TransactionState mTransactionState = new TransactionState();

    public abstract int getType();

    public abstract void process();

    public Transaction(Context context, int serviceId, TransactionSettings settings) {
        this.mContext = context;
        this.mServiceId = serviceId;
        this.mTransactionSettings = settings;
    }

    public TransactionState getState() {
        return this.mTransactionState;
    }

    public boolean isEquivalent(Transaction transaction) {
        return getClass().equals(transaction.getClass()) && this.mId.equals(transaction.mId);
    }

    public int getServiceId() {
        return this.mServiceId;
    }

    public TransactionSettings getConnectionSettings() {
        return this.mTransactionSettings;
    }

    public void setConnectionSettings(TransactionSettings settings) {
        this.mTransactionSettings = settings;
    }

    protected byte[] sendPdu(byte[] pdu) throws IOException {
        return sendPdu(-1, pdu, this.mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException {
        return sendPdu(-1, pdu, mmscUrl);
    }

    protected byte[] sendPdu(long token, byte[] pdu) throws IOException {
        return sendPdu(token, pdu, this.mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(long token, byte[] pdu, String mmscUrl) throws IOException {
        ensureRouteToHost(mmscUrl, this.mTransactionSettings);
        return HttpUtils.httpConnection(this.mContext, token, mmscUrl, pdu, 1, this.mTransactionSettings.isProxySet(), this.mTransactionSettings.getProxyAddress(), this.mTransactionSettings.getProxyPort());
    }

    protected byte[] getPdu(String url) throws IOException {
        ensureRouteToHost(url, this.mTransactionSettings);
        return HttpUtils.httpConnection(this.mContext, -1, url, null, 2, this.mTransactionSettings.isProxySet(), this.mTransactionSettings.getProxyAddress(), this.mTransactionSettings.getProxyPort());
    }

    private void ensureRouteToHost(String url, TransactionSettings settings) throws IOException {
        String str = "Cannot establish route for ";
        String str2 = ": Unknown host";
        @SuppressLint("WrongConstant") ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        int inetAddr;
        String str3;
        if (settings.isProxySet()) {
            inetAddr = lookupHost(settings.getProxyAddress());
            if (inetAddr == -1) {
                str3 = "Cannot establish route for ";
                str3 = ": Unknown host";
                throw new IOException(str + url + str2);
            } else {
                return;
            }
        }
        inetAddr = lookupHost(Uri.parse(url).getHost());
        if (inetAddr == -1) {
            str3 = "Cannot establish route for ";
            str3 = ": Unknown host";
            throw new IOException(str + url + str2);
        }
//        else if (!connMgr.requestRouteToHost(2, inetAddr)) {
//            throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
//        }
    }

    public static int lookupHost(String hostname) {
        try {
            byte[] addrBytes = InetAddress.getByName(hostname).getAddress();
            return ((((addrBytes[3] & 255) << 24) | ((addrBytes[2] & 255) << 16)) | ((addrBytes[1] & 255) << 8)) | (addrBytes[0] & 255);
        } catch (UnknownHostException e) {
            return -1;
        }
    }

    public String toString() {
        return getClass().getName() + ": serviceId=" + this.mServiceId;
    }
}

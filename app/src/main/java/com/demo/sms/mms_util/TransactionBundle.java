package com.demo.sms.mms_util;

import android.os.Bundle;

public class TransactionBundle {
    private static final String MMSC_URL = "mmsc-url";
    private static final String PROXY_ADDRESS = "proxy-address";
    private static final String PROXY_PORT = "proxy-port";
    private static final String PUSH_DATA = "mms-push-data";
    public static final String TRANSACTION_TYPE = "type";
    public static final String URI = "uri";
    private final Bundle mBundle;

    private TransactionBundle(int transactionType) {
        this.mBundle = new Bundle();
        this.mBundle.putInt("type", transactionType);
    }

    public TransactionBundle(int transactionType, String uri) {
        this(transactionType);
        this.mBundle.putString("uri", uri);
    }

    public TransactionBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public void setConnectionSettings(String mmscUrl, String proxyAddress, int proxyPort) {
        this.mBundle.putString(MMSC_URL, mmscUrl);
        this.mBundle.putString(PROXY_ADDRESS, proxyAddress);
        this.mBundle.putInt(PROXY_PORT, proxyPort);
    }

    public void setConnectionSettings(TransactionSettings settings) {
        setConnectionSettings(settings.getMmscUrl(), settings.getProxyAddress(), settings.getProxyPort());
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public int getTransactionType() {
        return this.mBundle.getInt("type");
    }

    public String getUri() {
        return this.mBundle.getString("uri");
    }

    public byte[] getPushData() {
        return this.mBundle.getByteArray(PUSH_DATA);
    }

    public String getMmscUrl() {
        return this.mBundle.getString(MMSC_URL);
    }

    public String getProxyAddress() {
        return this.mBundle.getString(PROXY_ADDRESS);
    }

    public int getProxyPort() {
        return this.mBundle.getInt(PROXY_PORT);
    }
}

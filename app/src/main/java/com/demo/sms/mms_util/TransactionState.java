package com.demo.sms.mms_util;

import android.net.Uri;

public class TransactionState {
    public static final int FAILED = 2;
    public static final int INITIALIZED = 0;
    public static final int SUCCESS = 1;
    private Uri mContentUri = null;
    private int mState = 0;

    public synchronized int getState() {
        return this.mState;
    }

    synchronized void setState(int state) {
        if (state >= 0 || state <= 2) {
            this.mState = state;
        } else {
            throw new IllegalArgumentException("Bad state: " + state);
        }
    }

    public synchronized Uri getContentUri() {
        return this.mContentUri;
    }

    synchronized void setContentUri(Uri uri) {
        this.mContentUri = uri;
    }
}

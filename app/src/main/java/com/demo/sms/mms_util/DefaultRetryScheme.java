package com.demo.sms.mms_util;

import android.content.Context;

public class DefaultRetryScheme extends AbstractRetryScheme {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "DefaultRetryScheme";
    private static final int[] sDefaultRetryScheme = new int[]{0, 60000, 300000, 600000, 1800000};

    public DefaultRetryScheme(Context context, int retriedTimes) {
        super(retriedTimes);
        this.mRetriedTimes = this.mRetriedTimes < 0 ? 0 : this.mRetriedTimes;
        this.mRetriedTimes = this.mRetriedTimes >= sDefaultRetryScheme.length ? sDefaultRetryScheme.length - 1 : this.mRetriedTimes;
    }

    public int getRetryLimit() {
        return sDefaultRetryScheme.length;
    }

    public long getWaitingInterval() {
        return (long) sDefaultRetryScheme[this.mRetriedTimes];
    }
}

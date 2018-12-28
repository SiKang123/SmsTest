package com.demo.sms.mms_util;

public abstract class AbstractRetryScheme {
    public static final int INCOMING = 2;
    public static final int OUTGOING = 1;
    protected int mRetriedTimes;

    public abstract int getRetryLimit();

    public abstract long getWaitingInterval();

    public AbstractRetryScheme(int retriedTimes) {
        this.mRetriedTimes = retriedTimes;
    }
}

package com.demo.sms.mms_util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony.Mms.Sent;
import android.support.annotation.RequiresApi;

import com.demo.sms.utils.AppInfoUtils;

import java.io.IOException;

import custom.google.android.mms.MmsException;
import custom.google.android.mms.pdu.EncodedStringValue;
import custom.google.android.mms.pdu.PduComposer;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.pdu.ReadRecInd;

public class ReadRecTransaction extends Transaction {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "ReadRecTransaction";
    private final Uri mReadReportURI;

    public ReadRecTransaction(Context context, int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        this.mReadReportURI = Uri.parse(uri);
        this.mId = uri;
        attach(RetryScheduler.getInstance(context));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void process() {
        PduPersister persister = PduPersister.getPduPersister(this.mContext);
        try {
            ReadRecInd readRecInd = (ReadRecInd) persister.load(this.mReadReportURI);
            readRecInd.setFrom(new EncodedStringValue(AppInfoUtils.getPhoneNumber(mContext)));
            sendPdu(new PduComposer(this.mContext, readRecInd).make());
            Uri uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
            this.mTransactionState.setState(1);
            this.mTransactionState.setContentUri(uri);
        } catch (IOException e) {
        } catch (MmsException e2) {
        } catch (RuntimeException e3) {
        } finally {
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
            }
            notifyObservers();
        }
    }

    public int getType() {
        return 3;
    }
}

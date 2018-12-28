package com.demo.sms.mms_util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.provider.Telephony.Mms;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import custom.android.common.speech.LoggingEvents;
import custom.google.android.mms.pdu.EncodedStringValue;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.util.SqliteWrapper;

public class AddressUtils {
    private static final String TAG = "AddressUtils";

    private AddressUtils() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getFrom(Context context, Uri uri) {
        String msgId = uri.getLastPathSegment();
        Builder builder = Mms.CONTENT_URI.buildUpon();
        builder.appendPath(msgId).appendPath("addr");
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), builder.build(), new String[]{"address", "charset"}, "type=137", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String from = cursor.getString(0);
                    if (!TextUtils.isEmpty(from)) {
                        String string = new EncodedStringValue(cursor.getInt(1), PduPersister.getBytes(from)).getString();
                        return string;
                    }
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    public static String getTo(Context context, Uri uri) {
        String msgId = uri.getLastPathSegment();
        Builder builder = Mms.CONTENT_URI.buildUpon();
        builder.appendPath(msgId).appendPath("addr");
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), builder.build(), new String[]{"address", "charset"}, "type=151", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String to = cursor.getString(0);
                    if (!TextUtils.isEmpty(to)) {
                        String string = new EncodedStringValue(cursor.getInt(1), PduPersister.getBytes(to)).getString();
                        return string;
                    }
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return LoggingEvents.EXTRA_CALLING_APP_NAME;
    }
}

package com.demo.sms.mms_util;

import android.util.Log;

public class LogTag {
    public static final String APP = "Mms:app";
    public static final String TAG = "Mms";
    public static final String THREAD_CACHE = "Mms:threadcache";
    public static final String TRANSACTION = "Mms:transaction";

    private static String prettyArray(String[] array) {
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int len = array.length - 1;
        for (int i = 0; i < len; i++) {
            sb.append(array[i]);
            sb.append(", ");
        }
        sb.append(array[len]);
        sb.append("]");
        return sb.toString();
    }

    private static String logFormat(String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String[]) {
                args[i] = prettyArray((String[]) args[i]);
            }
        }
        return "[" + Thread.currentThread().getId() + "] " + String.format(format, args);
    }

    public static void debug(String format, Object... args) {
        Log.d("Mms", logFormat(format, args));
    }

    public static void warn(String format, Object... args) {
        Log.w("Mms", logFormat(format, args));
    }

    public static void error(String format, Object... args) {
        Log.e("Mms", logFormat(format, args));
    }
}

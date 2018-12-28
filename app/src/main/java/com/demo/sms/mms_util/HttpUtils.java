package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.util.Locale;

public class HttpUtils {
    private static final boolean DEBUG = false;
    private static final String HDR_KEY_ACCEPT = "Accept";
    private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HDR_VALUE_ACCEPT = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    private static final String HDR_VALUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();
    public static final int HTTP_GET_METHOD = 2;
    public static final int HTTP_POST_METHOD = 1;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "Mms:transaction";

    private HttpUtils() {
    }

    protected static byte[] httpConnection(Context r5, long r6, String r8, byte[] r9, int r10, boolean r11, String r12, int r13) throws IOException {
        throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.transaction.HttpUtils.httpConnection(android.content.Context, long, java.lang.String, byte[], int, boolean, java.lang.String, int):byte[]");
    }

    private static void handleHttpConnectionException(Exception exception, String url) throws IOException {
        Log.e("Mms:transaction", "Url: " + url + "\n" + exception.getMessage());
        IOException e = new IOException(exception.getMessage());
        e.initCause(exception);
        throw e;
    }

    @SuppressLint("WrongConstant")
    private static AndroidHttpClient createHttpClient(Context context) {
        String str = "Mms:transaction";
        String userAgent = MmsConfig.getUserAgent();
        AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, context);
        HttpParams params = client.getParams();
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        int soTimeout = MmsConfig.getHttpSocketTimeout();
        String str2 = "Mms:transaction";
        if (Log.isLoggable(str, 3)) {
            str2 = "Mms:transaction";
            Log.d(str, "[HttpUtils] createHttpClient w/ socket timeout " + soTimeout + " ms, " + ", UA=" + userAgent);
        }
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        return client;
    }

    private static String getHttpAcceptLanguage() {
        Locale locale = Locale.getDefault();
        StringBuilder builder = new StringBuilder();
        addLocaleToHttpAcceptLanguage(builder, locale);
        if (!locale.equals(Locale.US)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            addLocaleToHttpAcceptLanguage(builder, Locale.US);
        }
        return builder.toString();
    }

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        String language = locale.getLanguage();
        if (language != null) {
            builder.append(language);
            String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }
}

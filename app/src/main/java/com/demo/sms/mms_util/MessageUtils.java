package com.demo.sms.mms_util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import custom.android.common.speech.LoggingEvents;
import custom.google.android.mms.pdu.PduPart;


public class MessageUtils {
    public static final int IMAGE_COMPRESSION_QUALITY = 80;
    public static final int MESSAGE_OVERHEAD = 5000;
    public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;
    private static final char[] NUMERIC_CHARS_SUGAR = new char[]{'-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'};
    private static final String TAG = "Mms";
    private static HashMap numericSugarMap = new HashMap(NUMERIC_CHARS_SUGAR.length);
    private static String sLocalNumber;
    private static final Map<String, String> sRecipientAddress = new ConcurrentHashMap(20);

    interface ResizeImageResultCallback {
        void onResizeResult(PduPart pduPart, boolean z);
    }

    static {
        for (int i = 0; i < NUMERIC_CHARS_SUGAR.length; i++) {
            numericSugarMap.put(Character.valueOf(NUMERIC_CHARS_SUGAR[i]), Character.valueOf(NUMERIC_CHARS_SUGAR[i]));
        }
    }

    private MessageUtils() {
    }

    public static String getMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor == null) {
            return null;
        }
        if (!"mms".equals(cursor.getString(0))) {
            return getTextMessageDetails(context, cursor);
        }
        switch (cursor.getInt(15)) {
            case DownloadManager.STATE_UNSTARTED /*128*/:
            case 132:
                return getMultimediaMessageDetails(context, cursor, size);
            case DownloadManager.STATE_TRANSIENT_FAILURE /*130*/:
                return getNotificationIndDetails(context, cursor);
            default:
                Log.w("Mms", "No details could be retrieved.");
                return LoggingEvents.EXTRA_CALLING_APP_NAME;
        }
    }

    private static String getNotificationIndDetails(Context context, Cursor cursor) {
//        StringBuilder details = new StringBuilder();
//        Resources res = context.getResources();
//        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, cursor.getLong(1));
//        try {
//            NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(context).load(uri);
//            details.append(res.getString(2131230938));
//            details.append(res.getString(2131230941));
//            String from = extractEncStr(context, nInd.getFrom());
//            details.append(10);
//            details.append(res.getString(2131230942));
//            if (TextUtils.isEmpty(from)) {
//                from = res.getString(2131230883);
//            }
//            details.append(from);
//            details.append(10);
//            details.append(res.getString(2131230792, new Object[]{formatTimeStampString(context, nInd.getExpiry() * 1000, true)}));
//            details.append(10);
//            details.append(res.getString(2131230948));
//            EncodedStringValue subject = nInd.getSubject();
//            if (subject != null) {
//                details.append(subject.getString());
//            }
//            details.append(10);
//            details.append(res.getString(2131230954));
//            details.append(new String(nInd.getMessageClass()));
//            details.append(10);
//            details.append(res.getString(2131230949));
//            details.append(String.valueOf((nInd.getMessageSize() + 1023) / SlideshowModel.SLIDESHOW_SLOP));
//            details.append(context.getString(2131230793));
//            return details.toString();
//        } catch (MmsException e) {
//            Log.e("Mms", "Failed to load the message: " + uri, e);
//            return context.getResources().getString(2131230936);
//        }
        return "";
    }

    private static String getMultimediaMessageDetails(Context context, Cursor msgBox, int size) {
//        if (msgBox.getInt(15) == DownloadManager.STATE_TRANSIENT_FAILURE) {
//            return getNotificationIndDetails(context, msgBox);
//        }
//        StringBuilder details = new StringBuilder();
//        Resources res = context.getResources();
//        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgBox.getLong(1));
//        try {
//            MultimediaMessagePdu msg = (MultimediaMessagePdu) PduPersister.getPduPersister(context).load(uri);
//            details.append(res.getString(2131230938));
//            details.append(res.getString(2131230940));
//            if (msg instanceof RetrieveConf) {
//                String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
//                details.append(10);
//                details.append(res.getString(2131230942));
//                if (TextUtils.isEmpty(from)) {
//                    from = res.getString(2131230883);
//                }
//                details.append(from);
//            }
//            details.append(10);
//            details.append(res.getString(2131230943));
//            EncodedStringValue[] to = msg.getTo();
//            if (to != null) {
//                details.append(EncodedStringValue.concat(to));
//            } else {
//                Log.w("Mms", "recipient list is empty!");
//            }
//            if (msg instanceof SendReq) {
//                to = ((SendReq) msg).getBcc();
//                if (to != null && to.length > 0) {
//                    details.append(10);
//                    details.append(res.getString(2131230944));
//                    details.append(EncodedStringValue.concat(to));
//                }
//            }
//            details.append(10);
//            int msgBox2 = msgBox.getInt(16);
//            if (msgBox2 == 3) {
//                details.append(res.getString(2131230947));
//            } else if (msgBox2 == 1) {
//                details.append(res.getString(2131230946));
//            } else {
//                details.append(res.getString(2131230945));
//            }
//            details.append(formatTimeStampString(context, msg.getDate() * 1000, true));
//            details.append(10);
//            details.append(res.getString(2131230948));
//            String subStr = msg.getSubject();
//            if (subStr != null) {
//                subStr = subStr.getString();
//                size += subStr.length();
//                details.append(subStr);
//            }
//            details.append(10);
//            details.append(res.getString(2131230950));
//            details.append(getPriorityDescription(context, msg.getPriority()));
//            details.append(10);
//            details.append(res.getString(2131230949));
//            details.append(((size - 1) / 1000) + 1);
//            details.append(" KB");
//            return details.toString();
//        } catch (MmsException e) {
//            Log.e("Mms", "Failed to load the message: " + uri, e);
//            return context.getResources().getString(2131230936);
//        }
        return "";
    }

    private static String getTextMessageDetails(Context context, Cursor cursor) {
//        StringBuilder details = new StringBuilder();
//        Resources res = context.getResources();
//        details.append(res.getString(2131230938));
//        details.append(res.getString(2131230939));
//        details.append(10);
//        int smsType = cursor.getInt(7);
//        if (Sms.isOutgoingFolder(smsType)) {
//            details.append(res.getString(2131230943));
//        } else {
//            details.append(res.getString(2131230942));
//        }
//        details.append(cursor.getString(3));
//        details.append(10);
//        if (smsType == 3) {
//            details.append(res.getString(2131230947));
//        } else if (smsType == 1) {
//            details.append(res.getString(2131230946));
//        } else {
//            details.append(res.getString(2131230945));
//        }
//        details.append(formatTimeStampString(context, cursor.getLong(5), true));
//        int errorCode = cursor.getInt(10);
//        if (errorCode != 0) {
//            details.append(10).append(res.getString(2131230955)).append(errorCode);
//        }
//        return details.toString();
        return "";
    }

//    private static String getPriorityDescription(Context context, int PriorityValue) {
//        Resources res = context.getResources();
//        switch (PriorityValue) {
//            case DownloadManager.STATE_UNSTARTED /*128*/:
//                return res.getString(2131230953);
//            case DownloadManager.STATE_TRANSIENT_FAILURE /*130*/:
//                return res.getString(2131230951);
//            default:
//                return res.getString(2131230952);
//        }
//    }

//    public static int getAttachmentType(SlideshowModel model) {
//        if (model == null) {
//            return 0;
//        }
//        int numberOfSlides = model.size();
//        if (numberOfSlides > 1) {
//            return 4;
//        }
//        if (numberOfSlides == 1) {
//            SlideModel slide = model.get(0);
//            if (slide.hasVideo()) {
//                return 2;
//            }
//            if (slide.hasAudio() && slide.hasImage()) {
//                return 4;
//            }
//            if (slide.hasAudio()) {
//                return 3;
//            }
//            if (slide.hasImage()) {
//                return 1;
//            }
//            if (slide.hasText()) {
//                return 0;
//            }
//        }
//        return 0;
//    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        int format_flags;
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        if (then.year != now.year) {
            format_flags = 527104 | 20;
        } else if (then.yearDay != now.yearDay) {
            format_flags = 527104 | 16;
        } else {
            format_flags = 527104 | 1;
        }
        if (fullFormat) {
            format_flags |= 17;
        }
        return DateUtils.formatDateTime(context, when, format_flags);
    }

    public static String getRecipientsByIds(Context context, String recipientIds, boolean allowQuery) {
        String value = (String) sRecipientAddress.get(recipientIds);
        if (value != null) {
            return value;
        }
        if (TextUtils.isEmpty(recipientIds)) {
            value = LoggingEvents.EXTRA_CALLING_APP_NAME;
        } else {
            StringBuilder addressBuf = extractIdsToAddresses(context, recipientIds, allowQuery);
            if (addressBuf == null) {
                return LoggingEvents.EXTRA_CALLING_APP_NAME;
            }
            value = addressBuf.toString();
        }
        sRecipientAddress.put(recipientIds, value);
        return value;
    }

    private static StringBuilder extractIdsToAddresses(Context r13, String r14, boolean r15) {
        throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.ui.MessageUtils.extractIdsToAddresses(android.content.Context, java.lang.String, boolean):java.lang.StringBuilder");
    }

    public static void selectAudio(Context context, int requestCode) {
        if (context instanceof Activity) {
            Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
            intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
            intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", false);
            intent.putExtra("android.intent.extra.ringtone.INCLUDE_DRM", false);
            intent.putExtra("android.intent.extra.ringtone.TITLE", "");
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void recordSound(Context context, int requestCode) {
        if (context instanceof Activity) {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("audio/amr");
            intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder");
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void selectVideo(Context context, int requestCode) {
        selectMediaByType(context, requestCode, "video/*");
    }

    public static void selectImage(Context context, int requestCode) {
        selectMediaByType(context, requestCode, "image/*");
    }

    private static void selectMediaByType(Context context, int requestCode, String contentType) {
        if (context instanceof Activity) {
            Intent innerIntent = new Intent("android.intent.action.GET_CONTENT");
            innerIntent.setType(contentType);
            ((Activity) context).startActivityForResult(Intent.createChooser(innerIntent, null), requestCode);
        }
    }

    private static void log(String msg) {
        Log.d("Mms", "[MsgUtils] " + msg);
    }

    public static boolean isInstalled(Context context, String pkgName) {
        List<ApplicationInfo> mAppList = context.getPackageManager().getInstalledApplications(0);
        if (pkgName == null) {
            Log.w("Mms", "Null pkg name when checking if installed");
            return false;
        }
        for (ApplicationInfo info : mAppList) {
            if (info.packageName.equalsIgnoreCase(pkgName)) {
                return true;
            }
        }
        return false;
    }
}

package com.demo.sms.mms_util;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Sms;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;


import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import custom.android.common.speech.LoggingEvents;
import custom.google.android.mms.pdu.EncodedStringValue;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.util.SqliteWrapper;

@SuppressLint("WrongConstant")
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MessagingNotification {
    private static final int COLUMN_DATE = 1;
    private static final int COLUMN_MMS_ID = 2;
    private static final int COLUMN_SMS_ADDRESS = 2;
    private static final int COLUMN_SMS_BODY = 4;
    private static final int COLUMN_SUBJECT = 3;
    private static final int COLUMN_SUBJECT_CS = 4;
    private static final int COLUMN_THREAD_ID = 0;
    public static final int DOWNLOAD_FAILED_NOTIFICATION_ID = 531;
    private static final MmsSmsNotificationInfoComparator INFO_COMPARATOR = new MmsSmsNotificationInfoComparator();
    public static final int MESSAGE_FAILED_NOTIFICATION_ID = 789;
    private static final String[] MMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "_id", "sub", "sub_cs"};
    private static final String NEW_DELIVERY_SM_CONSTRAINT = "(type = 2 AND status = 0)";
    private static final String NEW_INCOMING_MM_CONSTRAINT = "(msg_box=1 AND seen=0 AND (m_type=130 OR m_type=132))";
    private static final String NEW_INCOMING_SM_CONSTRAINT = "(type = 1 AND seen = 0)";
    private static final String NOTIFICATION_DELETED_ACTION = "com.joker.mmsfolder.NOTIFICATION_DELETED_ACTION";
    private static final int NOTIFICATION_ID = 123;
    private static final String[] SMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "address", "subject", "body"};
    private static final String TAG = "Mms:app";
    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
    private static Handler mToastHandler = new Handler();
    private static OnDeletedReceiver sNotificationDeletedReceiver = new OnDeletedReceiver();
    private static Intent sNotificationOnDeleteIntent;

    private static final class MmsSmsDeliveryInfo {
        public CharSequence mTicker;
        public long mTimeMillis;

        public MmsSmsDeliveryInfo(CharSequence ticker, long timeMillis) {
            this.mTicker = ticker;
            this.mTimeMillis = timeMillis;
        }

        public void deliver(Context context, boolean isStatusMessage) {
            MessagingNotification.updateDeliveryNotification(context, isStatusMessage, this.mTicker, this.mTimeMillis);
        }
    }

    private static final class MmsSmsNotificationInfo {
        public Intent mClickIntent;
        public int mCount;
        public String mDescription;
        public int mIconResourceId;
        public CharSequence mTicker;
        public long mTimeMillis;
        public String mTitle;

        public MmsSmsNotificationInfo(Intent clickIntent, String description, int iconResourceId, CharSequence ticker, long timeMillis, String title, int count) {
            this.mClickIntent = clickIntent;
            this.mDescription = description;
            this.mIconResourceId = iconResourceId;
            this.mTicker = ticker;
            this.mTimeMillis = timeMillis;
            this.mTitle = title;
            this.mCount = count;
        }

        public void deliver(Context context, boolean isNew, int count, int uniqueThreads) {
            MessagingNotification.updateNotification(context, this.mClickIntent, this.mDescription, this.mIconResourceId, isNew, isNew ? this.mTicker : null, this.mTimeMillis, this.mTitle, count, uniqueThreads);
        }

        public long getTime() {
            return this.mTimeMillis;
        }
    }

    private static final class MmsSmsNotificationInfoComparator implements Comparator<MmsSmsNotificationInfo> {
        private MmsSmsNotificationInfoComparator() {
        }


        public int compare(MmsSmsNotificationInfo info1, MmsSmsNotificationInfo info2) {
            return Long.signum(info2.getTime() - info1.getTime());
        }
    }

    public static class OnDeletedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String str = "Mms:app";
            String str2 = "Mms:app";
            if (Log.isLoggable(str, 2)) {
                str2 = "Mms:app";
                Log.d(str, "[MessagingNotification] clear notification: mark all msgs seen");
            }
//            Conversation.markAllConversationsAsSeen(context);
        }
    }

    private MessagingNotification() {
    }

    public static void init(Context context) {
        String str = NOTIFICATION_DELETED_ACTION;
        IntentFilter intentFilter = new IntentFilter();
        String str2 = NOTIFICATION_DELETED_ACTION;
        intentFilter.addAction(str);
        context.registerReceiver(sNotificationDeletedReceiver, intentFilter);
        String str3 = NOTIFICATION_DELETED_ACTION;
        sNotificationOnDeleteIntent = new Intent(str);
    }

    public static void nonBlockingUpdateNewMessageIndicator(final Context context, final boolean isNew, final boolean isStatusMessage) {
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.blockingUpdateNewMessageIndicator(context, isNew, isStatusMessage);
            }
        }).start();
    }

    public static void blockingUpdateNewMessageIndicator(Context context, boolean isNew, boolean isStatusMessage) {
        String str = "Mms:app";
        SortedSet<MmsSmsNotificationInfo> accumulator = new TreeSet(INFO_COMPARATOR);
        Set<Long> threads = new HashSet(4);
        int count = (0 + accumulateNotificationInfo(accumulator, getMmsNewMessageNotificationInfo(context, threads))) + accumulateNotificationInfo(accumulator, getSmsNewMessageNotificationInfo(context, threads));
        cancelNotification(context, NOTIFICATION_ID);
        if (!accumulator.isEmpty()) {
            String str2 = "Mms:app";
            if (Log.isLoggable(str, 2)) {
                str2 = "Mms:app";
                Log.d(str, "blockingUpdateNewMessageIndicator: count=" + count + ", isNew=" + isNew);
            }
            ((MmsSmsNotificationInfo) accumulator.first()).deliver(context, isNew, count, threads.size());
        }
        MmsSmsDeliveryInfo delivery = getSmsNewDeliveryInfo(context);
        if (delivery != null) {
            delivery.deliver(context, isStatusMessage);
        }
    }

    public static void blockingUpdateAllNotifications(Context context) {
        nonBlockingUpdateNewMessageIndicator(context, false, false);
        updateSendFailedNotification(context);
        updateDownloadFailedNotification(context);
    }

    private static final int accumulateNotificationInfo(SortedSet set, MmsSmsNotificationInfo info) {
        if (info == null) {
            return 0;
        }
        set.add(info);
        return info.mCount;
    }



    private static final MmsSmsNotificationInfo getMmsNewMessageNotificationInfo(Context context, Set<Long> threads) {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, MMS_STATUS_PROJECTION, NEW_INCOMING_MM_CONSTRAINT, null, "date desc");
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            String address = AddressUtils.getFrom(context, Mms.CONTENT_URI.buildUpon().appendPath(Long.toString(cursor.getLong(2))).build());
            String subject = getMmsSubject(cursor.getString(3), cursor.getInt(4));
            long threadId = cursor.getLong(0);
            long timeMillis = 1000 * cursor.getLong(1);
            if (Log.isLoggable("Mms:app", 2)) {
                Log.d("Mms:app", "getMmsNewMessageNotificationInfo: count=" + cursor.getCount() + ", first addr = " + address + ", thread_id=" + threadId);
            }
            MmsSmsNotificationInfo notificationInfo = getNewMessageNotificationInfo(address, subject, context, 2130837604, null, threadId, timeMillis, cursor.getCount());
            threads.add(Long.valueOf(threadId));
            while (cursor.moveToNext()) {
                threads.add(Long.valueOf(cursor.getLong(0)));
            }
            cursor.close();
            return notificationInfo;
        } finally {
            cursor.close();
        }
    }

    private static final MmsSmsDeliveryInfo getSmsNewDeliveryInfo(Context context) {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Sms.CONTENT_URI, SMS_STATUS_PROJECTION, NEW_DELIVERY_SM_CONSTRAINT, null, "date desc");
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            String address = cursor.getString(2);
//            MmsSmsDeliveryInfo mmsSmsDeliveryInfo = new MmsSmsDeliveryInfo(String.format(context.getString(2131230987), new Object[]{address}), 3000);
            MmsSmsDeliveryInfo mmsSmsDeliveryInfo = new MmsSmsDeliveryInfo(String.format("", new Object[]{address}), 3000);
            cursor.close();
            return mmsSmsDeliveryInfo;
        } finally {
            cursor.close();
        }
    }

    private static final MmsSmsNotificationInfo getSmsNewMessageNotificationInfo(Context context, Set<Long> threads) {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Sms.CONTENT_URI, SMS_STATUS_PROJECTION, NEW_INCOMING_SM_CONSTRAINT, null, "date desc");
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            String address = cursor.getString(2);
            String body = cursor.getString(4);
            long threadId = cursor.getLong(0);
            long timeMillis = cursor.getLong(1);
            Log.d("Mms:app", "getSmsNewMessageNotificationInfo: count=" + cursor.getCount() + ", first addr=" + address + ", thread_id=" + threadId);
            MmsSmsNotificationInfo info = getNewMessageNotificationInfo(address, body, context, 2130837605, null, threadId, timeMillis, cursor.getCount());
            threads.add(Long.valueOf(threadId));
            while (cursor.moveToNext()) {
                threads.add(Long.valueOf(cursor.getLong(0)));
            }
            cursor.close();
            return info;
        } finally {
            cursor.close();
        }
    }

    private static final MmsSmsNotificationInfo getNewMessageNotificationInfo(String address, String body, Context context, int iconResourceId, String subject, long threadId, long timeMillis, int count) {
        String senderInfo = buildTickerMessage(context, address, null, null).toString();
        return new MmsSmsNotificationInfo(null, body, iconResourceId, buildTickerMessage(context, address, subject, body), timeMillis, senderInfo.substring(0, senderInfo.length() - 2), count);
    }

    public static void cancelNotification(Context context, int notificationId) {
        ((NotificationManager) context.getSystemService("notification")).cancel(notificationId);
    }

    private static void updateDeliveryNotification(final Context context, boolean isStatusMessage, final CharSequence message, final long timeMillis) {
//        if (isStatusMessage && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true)) {
//            mToastHandler.post(new Runnable() {
//                public void run() {
//                    Toast.makeText(context, message, (int) timeMillis).show();
//                }
//            });
//        }
    }

    private static void updateNotification(Context context, Intent clickIntent, String description, int iconRes, boolean isNew, CharSequence ticker, long timeMillis, String title, int messageCount, int uniqueThreadCount) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        if (sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true)) {
//            Notification notification = new Notification(iconRes, ticker, timeMillis);
//            if (uniqueThreadCount > 1) {
//                title = context.getString(2131230989);
//                clickIntent = new Intent("android.intent.action.MAIN");
//                clickIntent.setFlags(872415232);
//                clickIntent.setType("vnd.android-dir/mms-sms");
//            }
//            if (messageCount > 1) {
//                description = context.getString(2131230988, new Object[]{Integer.toString(messageCount)});
//            }
//            notification.setLatestEventInfo(context, title, description, PendingIntent.getActivity(context, null, clickIntent, 134217728));
//            if (isNew) {
//                String vibrateWhen = sp.contains(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN) ? sp.getString(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN, false) : sp.contains(MessagingPreferenceActivity.NOTIFICATION_VIBRATE) ? sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_VIBRATE, false) ? context.getString(2131230917) : context.getString(2131230918) : context.getString(2131230916);
//                isNew = vibrateWhen.equals("always");
//                boolean vibrateSilent = vibrateWhen.equals("silent");
//                boolean nowSilent = ((AudioManager) context.getSystemService(SmilHelper.ELEMENT_TAG_AUDIO)).getRingerMode() == 1;
//                if (isNew || (vibrateSilent && nowSilent)) {
//                    notification.defaults |= 2;
//                }
//                vibrateWhen = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, false);
//                notification.sound = TextUtils.isEmpty(vibrateWhen) ? null : Uri.parse(vibrateWhen);
//            }
//            notification.flags |= 1;
//            notification.defaults |= 4;
//            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, sNotificationOnDeleteIntent, 0);
//            ((NotificationManager) context.getSystemService("notification")).notify(NOTIFICATION_ID, notification);
//            context = ((PowerManager) context.getSystemService("power")).newWakeLock(268435466, "MessagingNotification");
//            context.setReferenceCounted(false);
//            context.acquire(10000);
//        }
    }

    protected static CharSequence buildTickerMessage(Context context, String address, String subject, String body) {
//        String displayAddress = Contact.get(address, true).getName();
//        StringBuilder buf = new StringBuilder(displayAddress == null ? LoggingEvents.EXTRA_CALLING_APP_NAME : displayAddress.replace(10, ' ').replace(13, ' '));
//        buf.append(':').append(' ');
//        int offset = buf.length();
//        if (!TextUtils.isEmpty(subject)) {
//            buf.append(subject.replace(10, ' ').replace(13, ' '));
//            buf.append(' ');
//        }
//        if (!TextUtils.isEmpty(body)) {
//            buf.append(body.replace(10, ' ').replace(13, ' '));
//        }
//        SpannableString spanText = new SpannableString(buf.toString());
//        spanText.setSpan(new StyleSpan(1), 0, offset, 33);
//        return spanText;
        return "";
    }

    private static String getMmsSubject(String sub, int charset) {
        return TextUtils.isEmpty(sub) ? LoggingEvents.EXTRA_CALLING_APP_NAME : new EncodedStringValue(charset, PduPersister.getBytes(sub)).getString();
    }

    public static void notifyDownloadFailed(Context context, long threadId) {
        notifyFailed(context, true, threadId, false);
    }

    public static void notifySendFailed(Context context) {
        notifyFailed(context, false, 0, false);
    }

    public static void notifySendFailed(Context context, boolean noisy) {
        notifyFailed(context, false, 0, noisy);
    }

    private static void notifyFailed(Context context, boolean isDownload, long threadId, boolean noisy) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        if (sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true)) {
//            PendingIntent pendingIntent;
//            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
//            long[] msgThreadId = new long[]{0};
//            int totalFailedCount = getUndeliveredMessageCount(context, msgThreadId);
//            Notification notification = new Notification();
//            if (totalFailedCount > 1) {
//                totalFailedCount = context.getString(2131230990, new Object[]{Integer.toString(totalFailedCount)});
//                msgThreadId = context.getString(2131230991);
//                pendingIntent = new Intent(context, ConversationList.class);
//            } else {
//                String title = isDownload ? context.getString(2131231010) : context.getString(2131231011);
//                totalFailedCount = context.getString(2131231012);
//                pendingIntent = new Intent(context, ComposeMessageActivity.class);
//                if (isDownload) {
//                    pendingIntent.putExtra("failed_download_flag", true);
//                } else {
//                    threadId = msgThreadId[0] != 0 ? msgThreadId[0] : 0;
//                    pendingIntent.putExtra("undelivered_flag", true);
//                }
//                pendingIntent.putExtra("thread_id", threadId);
//                msgThreadId = title;
//            }
//            pendingIntent.setFlags(335544320);
//            pendingIntent = PendingIntent.getActivity(context, 0, pendingIntent, 134217728);
//            notification.icon = 2130837606;
//            notification.tickerText = msgThreadId;
//            notification.setLatestEventInfo(context, msgThreadId, totalFailedCount, pendingIntent);
//            if (noisy) {
//                if (sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_VIBRATE, false)) {
//                    notification.defaults |= 2;
//                }
//                String ringtoneStr = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, null);
//                notification.sound = TextUtils.isEmpty(ringtoneStr) ? null : Uri.parse(ringtoneStr);
//            }
//            if (isDownload) {
//                nm.notify(DOWNLOAD_FAILED_NOTIFICATION_ID, notification);
//            } else {
//                nm.notify(MESSAGE_FAILED_NOTIFICATION_ID, notification);
//            }
//        }
    }

    private static int getUndeliveredMessageCount(Context context, long[] threadIdResult) {
        Context context2 = context;
        Cursor undeliveredCursor = SqliteWrapper.query(context2, context.getContentResolver(), UNDELIVERED_URI, new String[]{"thread_id"}, "read=0", null, null);
        if (undeliveredCursor == null) {
            return 0;
        }
        int count = undeliveredCursor.getCount();
        if (threadIdResult != null) {
            try {
                if (undeliveredCursor.moveToFirst()) {
                    threadIdResult[0] = undeliveredCursor.getLong(0);
                    if (threadIdResult.length >= 2) {
                        long firstId = threadIdResult[0];
                        while (undeliveredCursor.moveToNext()) {
                            if (undeliveredCursor.getLong(0) != firstId) {
                                firstId = 0;
                                break;
                            }
                        }
                        threadIdResult[1] = firstId;
                    }
                }
            } catch (Throwable th) {
                undeliveredCursor.close();
            }
        }
        undeliveredCursor.close();
        return count;
    }

    public static void updateSendFailedNotification(Context context) {
        if (getUndeliveredMessageCount(context, null) < 1) {
            cancelNotification(context, MESSAGE_FAILED_NOTIFICATION_ID);
        } else {
            notifySendFailed(context);
        }
    }

    public static void updateSendFailedNotificationForThread(Context context, long threadId) {
        long[] msgThreadId = new long[]{0, 0};
        if (getUndeliveredMessageCount(context, msgThreadId) > 0 && msgThreadId[0] == threadId && msgThreadId[1] != 0) {
            cancelNotification(context, MESSAGE_FAILED_NOTIFICATION_ID);
        }
    }

    private static int getDownloadFailedMessageCount(Context context) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), Inbox.CONTENT_URI, null, "m_type=" + String.valueOf(DownloadManager.STATE_TRANSIENT_FAILURE) + " AND " + "st" + "=" + String.valueOf(DownloadManager.STATE_PERMANENT_FAILURE), null, null);
        if (c == null) {
            return 0;
        }
        int count = c.getCount();
        c.close();
        return count;
    }

    public static void updateDownloadFailedNotification(Context context) {
        if (getDownloadFailedMessageCount(context) < 1) {
            cancelNotification(context, DOWNLOAD_FAILED_NOTIFICATION_ID);
        }
    }

    public static boolean isFailedToDeliver(Intent intent) {
        return intent != null && intent.getBooleanExtra("undelivered_flag", false);
    }

    public static boolean isFailedToDownload(Intent intent) {
        return intent != null && intent.getBooleanExtra("failed_download_flag", false);
    }
}

package com.demo.sms.mms_util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms.Conversations;
import android.support.annotation.RequiresApi;
import android.util.Log;

import custom.google.android.mms.util.SqliteWrapper;
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public abstract class Recycler {
    private static final boolean DEFAULT_AUTO_DELETE = false;
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "Recycler";
    private static MmsRecycler sMmsRecycler;
    private static SmsRecycler sSmsRecycler;

    public static class MmsRecycler extends Recycler {
        private static final String[] ALL_MMS_THREADS_PROJECTION;
        private static final int COLUMN_ID = 0;
        private static final int COLUMN_MMS_DATE = 2;
        private static final int COLUMN_MMS_READ = 3;
        private static final int COLUMN_THREAD_ID = 1;
        private static final int ID = 0;
        private static final int MESSAGE_COUNT = 1;
        private static final String[] MMS_MESSAGE_PROJECTION;
        private final String MAX_MMS_MESSAGES_PER_THREAD = "MaxMmsMessagesPerThread";
        private static String[] r0;

        static {
            String str = "thread_id";
            r0 = new String[2];
            String str2 = "thread_id";
            r0[0] = str;
            r0[1] = "count(*) as msg_count";
            ALL_MMS_THREADS_PROJECTION = r0;
            r0 = new String[3];
            str2 = "thread_id";
            r0[1] = str;
            r0[2] = "date";
            MMS_MESSAGE_PROJECTION = r0;
        }

        public int getMessageLimit(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt("MaxMmsMessagesPerThread", MmsConfig.getDefaultMMSMessagesPerThread());
        }

        public void setMessageLimit(Context context, int limit) {
            Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt("MaxMmsMessagesPerThread", limit);
            editPrefs.commit();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(0);
        }


        protected Cursor getAllThreads(Context context) {
            return SqliteWrapper.query(context, context.getContentResolver(), Uri.withAppendedPath(Mms.CONTENT_URI, "threads"), ALL_MMS_THREADS_PROJECTION, null, null, "date DESC");
        }

        public void deleteOldMessagesInSameThreadAsMessage(Context context, Uri uri) {
            Throwable th;
            if (Recycler.isAutoDeleteEnabled(context)) {
                Cursor cursor = null;
                long latestDate;
                try {
                    Context context2 = context;
                    cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, MMS_MESSAGE_PROJECTION, "thread_id in (select thread_id from pdu where _id=" + uri.getLastPathSegment() + ") AND locked=0", null, "date DESC");
                    if (cursor == null) {
                        Log.e(Recycler.TAG, "MMS: deleteOldMessagesInSameThreadAsMessage got back null cursor");
                        if (cursor != null) {
                            cursor.close();
                            return;
                        }
                        return;
                    }
                    int count = cursor.getCount();
                    int keep = getMessageLimit(context);
                    if (count - keep > 0) {
                        cursor.move(keep);
                        latestDate = cursor.getLong(2);
                        try {
                            long threadId = cursor.getLong(1);
                            if (cursor != null) {
                                cursor.close();
                            }
                            if (threadId != 0) {
                                deleteMessagesOlderThanDate(context, threadId, latestDate);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th3) {
                    th = th3;
                    latestDate = 0;
                    if (cursor != null) {
                        cursor.close();
                    }
//                    throw th;
                }
            }
        }

        protected void deleteMessagesForThread(Context r12, long r13, int r15) {
            throw new UnsupportedOperationException("Method not decompiled: com.joker.mmsfolder.util.Recycler.MmsRecycler.deleteMessagesForThread(android.content.Context, long, int):void");
        }

        private void deleteMessagesOlderThanDate(Context context, long threadId, long latestDate) {
            long cntDeleted = (long) SqliteWrapper.delete(context, context.getContentResolver(), Mms.CONTENT_URI, "thread_id=" + threadId + " AND locked=0 AND date<" + latestDate, null);
        }

        protected void dumpMessage(Cursor cursor, Context context) {
            long id = cursor.getLong(0);
        }

        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            int limit = getMessageLimit(context);
            while (cursor.moveToNext()) {
                try {
                    if (SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, MMS_MESSAGE_PROJECTION, "thread_id=" + getThreadId(cursor) + " AND locked=0", null, "date DESC").getCount() >= limit) {
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
            cursor.close();
            return false;
        }
    }

    public static class SmsRecycler extends Recycler {
        private static final String[] ALL_SMS_THREADS_PROJECTION;
        private static final int COLUMN_ID = 0;
        private static final int COLUMN_SMS_ADDRESS = 2;
        private static final int COLUMN_SMS_BODY = 3;
        private static final int COLUMN_SMS_DATE = 4;
        private static final int COLUMN_SMS_READ = 5;
        private static final int COLUMN_SMS_STATUS = 7;
        private static final int COLUMN_SMS_TYPE = 6;
        private static final int COLUMN_THREAD_ID = 1;
        private static final int ID = 0;
        private static final int MESSAGE_COUNT = 1;
        private static final String[] SMS_MESSAGE_PROJECTION;
        private final String MAX_SMS_MESSAGES_PER_THREAD = "MaxSmsMessagesPerThread";
        private static String[] r0;

        static {
            String str = "thread_id";
            r0 = new String[2];
            String str2 = "thread_id";
            r0[0] = str;
            r0[1] = "msg_count";
            ALL_SMS_THREADS_PROJECTION = r0;
            r0 = new String[8];
            str2 = "thread_id";
            r0[1] = str;
            r0[2] = "address";
            r0[3] = "body";
            r0[4] = "date";
            r0[5] = "read";
            r0[6] = "type";
            r0[7] = "status";
            SMS_MESSAGE_PROJECTION = r0;
        }

        public int getMessageLimit(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt("MaxSmsMessagesPerThread", MmsConfig.getDefaultSMSMessagesPerThread());
        }

        public void setMessageLimit(Context context, int limit) {
            Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt("MaxSmsMessagesPerThread", limit);
            editPrefs.commit();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(0);
        }

        protected Cursor getAllThreads(Context context) {
            return SqliteWrapper.query(context, context.getContentResolver(), Conversations.CONTENT_URI, ALL_SMS_THREADS_PROJECTION, null, null, "date DESC");
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, resolver, ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId), SMS_MESSAGE_PROJECTION, "locked=0", null, "date DESC");
                if (cursor == null) {
                    Log.e(Recycler.TAG, "SMS: deleteMessagesForThread got back null cursor");
                } else if (cursor.getCount() - keep > 0) {
                    cursor.move(keep);
                    long latestDate = cursor.getLong(4);
                    long delete = (long) SqliteWrapper.delete(context, resolver, ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId), "locked=0 AND date<" + latestDate, null);
                    if (cursor != null) {
                        cursor.close();
                    }
                } else if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        protected void dumpMessage(Cursor cursor, Context context) {
//            String dateStr = MessageUtils.formatTimeStampString(context, cursor.getLong(4), true);
        }

        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            int limit = getMessageLimit(context);
            while (cursor.moveToNext()) {
                try {
                    if (SqliteWrapper.query(context, context.getContentResolver(), ContentUris.withAppendedId(Conversations.CONTENT_URI, getThreadId(cursor)), SMS_MESSAGE_PROJECTION, "locked=0", null, "date DESC").getCount() >= limit) {
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
            cursor.close();
            return false;
        }
    }

    protected abstract boolean anyThreadOverLimit(Context context);

    protected abstract void deleteMessagesForThread(Context context, long j, int i);

    protected abstract void dumpMessage(Cursor cursor, Context context);

    protected abstract Cursor getAllThreads(Context context);

    public abstract int getMessageLimit(Context context);

    protected abstract long getThreadId(Cursor cursor);

    public abstract void setMessageLimit(Context context, int i);

    public static SmsRecycler getSmsRecycler() {
        if (sSmsRecycler == null) {
            sSmsRecycler = new SmsRecycler();
        }
        return sSmsRecycler;
    }

    public static MmsRecycler getMmsRecycler() {
        if (sMmsRecycler == null) {
            sMmsRecycler = new MmsRecycler();
        }
        return sMmsRecycler;
    }

    public static boolean checkForThreadsOverLimit(Context context) {
        return getSmsRecycler().anyThreadOverLimit(context) || getMmsRecycler().anyThreadOverLimit(context);
    }

    public void deleteOldMessages(Context context) {
        if (isAutoDeleteEnabled(context)) {
            Cursor cursor = getAllThreads(context);
            try {
                int limit = getMessageLimit(context);
                while (cursor.moveToNext()) {
                    deleteMessagesForThread(context, getThreadId(cursor), limit);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void deleteOldMessagesByThreadId(Context context, long threadId) {
        if (isAutoDeleteEnabled(context)) {
            deleteMessagesForThread(context, threadId, getMessageLimit(context));
        }
    }

    public static boolean isAutoDeleteEnabled(Context context) {
//        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MessagingPreferenceActivity.AUTO_DELETE, false);
        return false;
    }

    public int getMessageMinLimit() {
        return MmsConfig.getMinMessageCountPerThread();
    }

    public int getMessageMaxLimit() {
        return MmsConfig.getMaxMessageCountPerThread();
    }
}

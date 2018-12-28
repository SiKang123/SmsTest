package com.demo.sms.sms;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.demo.sms.mms_util.MmsConfig;
import com.demo.sms.mms_util.TransactionService;
import com.demo.sms.utils.AppInfoUtils;

import java.util.List;

import custom.google.android.mms.MmsException;
import custom.google.android.mms.pdu.DeliveryInd;
import custom.google.android.mms.pdu.GenericPdu;
import custom.google.android.mms.pdu.NotificationInd;
import custom.google.android.mms.pdu.PduParser;
import custom.google.android.mms.pdu.PduPersister;
import custom.google.android.mms.pdu.ReadOrigInd;
import custom.google.android.mms.util.SqliteWrapper;


/**
 * Created by SiKang on 2018/11/5.
 * 接收彩信，并将彩信保存到数据库
 */
public class MmsReceiver extends BroadcastReceiver {
    private static final int DEFERRED_MASK = 4;
    private static final boolean LOCAL_LOGV = false;
    public static final int STATE_DOWNLOADING = 129;
    public static final int STATE_PERMANENT_FAILURE = 135;
    public static final int STATE_TRANSIENT_FAILURE = 130;
    public static final int STATE_UNSTARTED = 128;

    private static final String TAG = "PushReceiver";

    @SuppressLint("WrongConstant")
    public void onReceive(Context context, Intent intent) {
        if (!AppInfoUtils.isDefaultSmsApp(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED") && "application/vnd.wap.mms-message".equals(intent.getType())) {
            new ReceivePushTask(context).execute(new Intent[]{intent});
        }
    }


    private class ReceivePushTask extends AsyncTask<Intent, Void, Void> {
        private Context mContext;

        public ReceivePushTask(Context context) {
            this.mContext = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        protected Void doInBackground(Intent... intents) {
            GenericPdu pdu = new PduParser(intents[0].getByteArrayExtra("data")).parse();
            if (pdu == null) {
                return null;
            }
            PduPersister pduPersister = PduPersister.getPduPersister(this.mContext);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            int type = pdu.getMessageType();
            Uri uri;
            try {
                switch (type) {
                    case STATE_TRANSIENT_FAILURE /*130*/:
                        NotificationInd nInd = (NotificationInd) pdu;
                        if (MmsConfig.getTransIdEnabled()) {
                            byte[] contentLocation = nInd.getContentLocation();
                            if ((byte) 61 == contentLocation[contentLocation.length - 1]) {
                                byte[] transactionId = nInd.getTransactionId();
                                byte[] contentLocationWithId = new byte[(contentLocation.length + transactionId.length)];
                                System.arraycopy(contentLocation, 0, contentLocationWithId, 0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId, contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }
                        if (!isDuplicateNotification(this.mContext, nInd)) {
                            uri = pduPersister.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI);
                            Intent intent = new Intent(this.mContext, TransactionService.class);
                            intent.putExtra("uri", uri.toString());
                            intent.putExtra("type", 0);
                            this.mContext.startService(intent);
                            break;
                        }
//                        uri = pduPersister.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI);
//                        ContentValues values = new ContentValues();
//
//                        SqliteWrapper.insert(this.mContext, contentResolver, uri, values);

                        break;
                    case 134:
                    case 136:
                        long threadId = findThreadId(this.mContext, pdu, type);
                        if (threadId != -1) {
                            uri = pduPersister.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI);
                            ContentValues values1 = new ContentValues(1);
                            values1.put("thread_id", Long.valueOf(threadId));
                            SqliteWrapper.update(this.mContext, contentResolver, uri, values1, null, null);
                            break;
                        }
                        break;
                    default:
                        try {
                            Log.e(TAG, "Received unrecognized PDU.");
                            break;
                        } catch (RuntimeException e2) {
                            Log.e(TAG, "Unexpected RuntimeException.", e2);
                            break;
                        }
                }
            } catch (MmsException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;
        if (type == 134) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }
        StringBuilder sb = new StringBuilder(40);
        sb.append("m_id");
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append("m_type");
        sb.append('=');
        sb.append(STATE_UNSTARTED);
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Telephony.Mms.CONTENT_URI, new String[]{"thread_id"}, sb.toString(), null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    long j = cursor.getLong(0);
                    return j;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        if (nInd.getContentLocation() != null) {
            String[] selectionArgs = new String[]{new String(nInd.getContentLocation())};
            Context context2 = context;
            Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Telephony.Mms.CONTENT_URI, new String[]{"_id"}, "ct_l = ?", selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        return true;
                    }
                    cursor.close();
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
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


//
//    private void insert(Context context,int msgBoxType, AttachmentType type, int idx) {
//        long threadId = Telephony.Threads.getOrCreateThreadId(context, FROM_NUM + idx);
//        Log.e("", "threadId = " + threadId);
//
//        String name_1 = null;
//        String name_2 = null;
//        String smil_text = null;
//        ContentValues cv_part_1 = null;
//        ContentValues cv_part_2 = null;
//
//        switch (type) {
//            case IMAGE:
//                name_1 = IMAGE_NAME_1;
//                name_2 = IMAGE_NAME_2;
//                smil_text = String.format(SMIL_TEXT_IMAGE, name_1, name_2);
//                cv_part_1 = createPartRecord(0, "image/jpeg", name_1, IMAGE_CID, name_1, null, null);
//                cv_part_2 = createPartRecord(0, "image/jpeg", name_2, IMAGE_CID.replace("cid", "cid_2"), name_2, null, null);
//                break;
//            case AUDIO:
//                name_1 = AUDIO_NAME;
//                smil_text = String.format(SMIL_TEXT_AUDIO, name_1);
//                cv_part_1 = createPartRecord(0, "audio/ogg", AUDIO_NAME, AUDIO_CID, AUDIO_NAME, null, null);
//                break;
//            case VIDEO:
//                name_1 = VIDEO_NAME;
//                smil_text = String.format(SMIL_TEXT_VIDEO, name_1);
//                cv_part_1 = createPartRecord(0, "video/3gpp", VIDEO_NAME, VIDEO_CID, VIDEO_NAME, null, null);
//                break;
//        }
//
//        // make MMS record
//        ContentValues cvMain = new ContentValues();
//        cvMain.put(Mms.THREAD_ID, threadId);
//
//        cvMain.put(Mms.MESSAGE_BOX, msgBoxType);
//        cvMain.put(Mms.READ, 1);
//        cvMain.put(Mms.DATE, System.currentTimeMillis() / 1000);
//        cvMain.put(Mms.SUBJECT, "my subject " + idx);
//
//        cvMain.put(Mms.CONTENT_TYPE, "application/vnd.wap.multipart.related");
//        cvMain.put(Mms.MESSAGE_CLASS, "personal");
//        cvMain.put(Mms.MESSAGE_TYPE, 132); // Retrive-Conf Mms
//        cvMain.put(Mms.MESSAGE_SIZE, getSize(name_1) + getSize(name_2) + 512);  // suppose have 512 bytes extra text size
//        cvMain.put(Mms.PRIORITY, String.valueOf(129));
//        cvMain.put(Mms.READ_REPORT, String.valueOf(129));
//        cvMain.put(Mms.DELIVERY_REPORT, String.valueOf(129));
//        Random random = new Random();
//        cvMain.put(Mms.MESSAGE_ID, String.valueOf(random.nextInt(100000)));
//        cvMain.put(Mms.TRANSACTION_ID, String.valueOf(random.nextInt(120000)));
//
//        long msgId = 0;
//        try {
//            msgId = ContentUris.parseId(getContentResolver().insert(Mms.CONTENT_URI, cvMain));
//        } catch (Exception e) {
//            Log.e("", "insert pdu record failed", e);
//            return;
//        }
//
//        // make parts
//        ContentValues cvSmil = createPartRecord(-1, "application/smil", "smil.xml", "<siml>", "smil.xml", null, smil_text);
//        cvSmil.put(Part.MSG_ID, msgId);
//
//        cv_part_1.put(Part.MSG_ID, msgId);
//        cv_part_2.put(Part.MSG_ID, msgId);
//
//        ContentValues cv_text_1 = createPartRecord(0, "text/plain", "text_1.txt", "<text_1>", "text_1.txt", null, null);
//        cv_text_1.put(Part.MSG_ID, msgId);
//        cv_text_1.remove(Part.TEXT);
//        cv_text_1.put(Part.TEXT, "slide 1 text");
//        cv_text_1.put(Part.CHARSET, "106");
//
//        ContentValues cv_text_2 = createPartRecord(0, "text/plain", "text_2.txt", "<text_2>", "text_2.txt", null, null);
//        cv_text_2.put(Part.MSG_ID, msgId);
//        cv_text_2.remove(Part.TEXT);
//        cv_text_2.put(Part.TEXT, "slide 2 text");
//        cv_text_2.put(Part.CHARSET, "106");
//
//        // insert parts
//        Uri partUri = Uri.parse("content://mms/" + msgId + "/part");
//        try {
//            getContentResolver().insert(partUri, cvSmil);
//
//            Uri dataUri_1 = getContentResolver().insert(partUri, cv_part_1);
//            if (!copyData(dataUri_1, name_1)) {
//                Log.e("", "write " + name_1 + " failed");
//                return;
//            }
//            getContentResolver().insert(partUri, cv_text_1);
//
//            Uri dataUri_2 = getContentResolver().insert(partUri, cv_part_2);
//            if (!copyData(dataUri_2, name_2)) {
//                Log.e("", "write " + name_2 + " failed");
//                return;
//            }
//            getContentResolver().insert(partUri, cv_text_2);
//        } catch (Exception e) {
//            Log.e("", "insert part failed", e);
//            return;
//        }
//
//        // to address
//        ContentValues cvAddr = new ContentValues();
//        cvAddr.put(Addr.MSG_ID, msgId);
//        cvAddr.put(Addr.ADDRESS, "703");
//        cvAddr.put(Addr.TYPE, "151");
//        cvAddr.put(Addr.CHARSET, 106);
//        getContentResolver().insert(Uri.parse("content://mms/" + msgId + "/addr"), cvAddr);
//
//        // from address
//        cvAddr.clear();
//        cvAddr.put(Addr.MSG_ID, msgId);
//        cvAddr.put(Addr.ADDRESS, FROM_NUM + idx);
//        cvAddr.put(Addr.TYPE, "137");
//        cvAddr.put(Addr.CHARSET, 106);
//        getContentResolver().insert(Uri.parse("content://mms/" + msgId + "/addr"), cvAddr);
//    }
//
//    private int getSize(final String name) {
//        InputStream is = null;
//        int size = 0;
//
//        try {
//            is = getAssets().open(name);
//            byte[] buffer = new byte[1024];
//            for (int len = 0; (len = is.read(buffer)) != -1;)
//                size += len;
//            return size;
//        } catch (FileNotFoundException e) {
//            Log.e("", "failed to found file?", e);
//            return 0;
//        } catch (IOException e) {
//            Log.e("", "write failed..", e);
//        } finally {
//            try {
//                if (is != null)
//                    is.close();
//            } catch (IOException e) {
//                Log.e("", "close failed...");
//            }
//        }
//        return 0;
//    }
//
//    private ContentValues createPartRecord(int seq, String ct, String name, String cid, String cl, String data,
//                                           String text) {
//        ContentValues cv = new ContentValues(8);
//        cv.put(Part.SEQ, seq);
//        cv.put(Part.CONTENT_TYPE, ct);
//        cv.put(Part.NAME, name);
//        cv.put(Part.CONTENT_ID, cid);
//        cv.put(Part.CONTENT_LOCATION, cl);
//        if (data != null)
//            cv.put(Part._DATA, data);
//        if (text != null)
//            cv.put(Part.TEXT, text);
//        return cv;
//    }
//
//    private boolean copyData(final Uri dataUri, final String name) {
//        InputStream input = null;
//        OutputStream output = null;
//
//        try {
//            input = getAssets().open(name);
//            output = getContentResolver().openOutputStream(dataUri);
//
//            byte[] buffer = new byte[1024];
//            for (int len = 0; (len = input.read(buffer)) != -1;)
//                output.write(buffer, 0, len);
//        } catch (FileNotFoundException e) {
//            Log.e("", "failed to found file?", e);
//            return false;
//        } catch (IOException e) {
//            Log.e("", "write failed..", e);
//            return false;
//        } finally {
//            try {
//                if (input != null)
//                    input.close();
//                if (output != null)
//                    output.close();
//            } catch (IOException e) {
//                Log.e("", "close failed...");
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//    enum AttachmentType {
//        IMAGE, AUDIO, VIDEO;
//    }


}

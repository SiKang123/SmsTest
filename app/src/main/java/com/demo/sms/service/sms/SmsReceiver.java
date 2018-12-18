package com.demo.sms.service.sms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.demo.sms.BuildConfig;
import com.demo.sms.R;

/**
 * Created by SiKang on 2018/11/5.
 * 短信接收器
 * 当用户同意将APP设置为默认短信应用后，手机收到短信第一时间会通知到这里
 * 由于系统短信应用已被取消默认权限，所有不会收到通知，保存短信的逻辑也不会执行，所以这里需要主动保存短信内容，否则用户打开短信列表将无法看见短信
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.getPackageManager().clearPackagePreferredActivities(BuildConfig.APPLICATION_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //如果是默认短信应用，收到短信主动保存到数据库
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction()) && Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdus.length; i++) {
                        //提取短信内容
                        SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String msgBody = msg.getMessageBody();
                        String msgAddress = msg.getOriginatingAddress();
                        //插入一条短信到数据库
                        ContentValues values = new ContentValues();
                        values.put("date", msg.getTimestampMillis());//发送时间
                        values.put("read", 0);//阅读状态 0：未读  1：已读
                        values.put("type", 1);//1为收 2为发
                        values.put("address", msgAddress);//送达号码
                        values.put("body", msgBody);//送达内容
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);//插入短信库
                        long msgDate = msg.getTimestampMillis();
                        Log.d("cky", "message from: " + msgAddress + ", message body: " + msgBody + ", message date: " + msgDate);
                        //通知栏提醒
                        notifySms(context, msgAddress, msgBody);
                    }
                }
            }
        }
    }


    /**
     * 添加一条短信通知
     */
    public void notifySms(Context context, String title, String body) {
        //点击通知栏进入系统短信列表
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("vnd.android-dir/mms-sms");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(title)
                .setContentIntent(pendingIntent);
        notifyNotification(context, builder);

    }

    /**
     * 显示通知
     */
    public void notifyNotification(Context context, NotificationCompat.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

        // 兼容android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(BuildConfig.APPLICATION_ID);
        }
        // 通知系统展示通知栏
        notificationManager.notify("test", 111, builder.build());
    }
}

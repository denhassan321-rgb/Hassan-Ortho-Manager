package com.orthoclinic.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String id=intent.getStringExtra("id");
        Intent open=new Intent(context,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,0,open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(context,"clinic_appointments")
            : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("message"))
            .setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_HIGH);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id == null ? (int)System.currentTimeMillis() : id.hashCode(),builder.build());
    }
}

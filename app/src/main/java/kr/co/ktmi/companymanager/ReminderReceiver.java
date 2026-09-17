package kr.co.ktmi.companymanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent intent){
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        String channel="ktm_schedule";
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(channel,"일정 알림",NotificationManager.IMPORTANCE_HIGH));
        Intent open=new Intent(c,MainActivity.class).putExtra("openTab","schedule");
        PendingIntent pi=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String title=intent.getStringExtra("title"); int days=intent.getIntExtra("days",1);
        android.app.Notification n=new android.app.Notification.Builder(c,channel)
            .setSmallIcon(R.drawable.ic_launcher).setColor(Color.rgb(8,8,79))
            .setContentTitle(days+"일 후 일정이 있습니다")
            .setContentText(title+" · "+intent.getStringExtra("date"))
            .setContentIntent(pi).setAutoCancel(true).build();
        nm.notify((int)System.currentTimeMillis(),n);
    }
}
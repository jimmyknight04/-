package kr.co.ktmi.companymanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ReminderScheduler {
    public static void schedule(Context c, JSONObject item){
        long id=item.optLong("id"); String date=item.optString("date"); String title=item.optString("title");
        try{
            Date d=new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(date);
            Calendar event=Calendar.getInstance(); event.setTime(d); event.set(Calendar.HOUR_OF_DAY,9); event.set(Calendar.MINUTE,0); event.set(Calendar.SECOND,0);
            for(int days:new int[]{2,1}){
                long trigger=event.getTimeInMillis()-days*24L*60L*60L*1000L;
                Intent i=new Intent(c,ReminderReceiver.class).putExtra("title",title).putExtra("date",date).putExtra("days",days);
                PendingIntent pi=PendingIntent.getBroadcast(c,(int)(id%200000000)*10+days,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
                if(trigger>System.currentTimeMillis()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pi);
            }
        }catch(Exception ignored){}
    }
    public static void cancel(Context c,long id){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        for(int days:new int[]{2,1}){Intent i=new Intent(c,ReminderReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(c,(int)(id%200000000)*10+days,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(pi!=null)am.cancel(pi);}
    }
}
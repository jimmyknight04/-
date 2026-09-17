package kr.co.ktmi.companymanager;
import android.app.*;import android.content.*;import org.json.*;import java.text.*;import java.util.*;
public final class ReminderScheduler{
 private static final int[] DAYS={14,7,2,1};private static final int[] BITS={8,4,2,1};
 public static void schedule(Context c,JSONObject o){if(!LocalStore.notificationsOn(c))return;long id=o.optLong("id");int mask=o.optInt("reminderMask",LocalStore.defaultReminderMask(c));try{Date d=new SimpleDateFormat("yyyy-MM-dd",Locale.KOREA).parse(o.optString("date"));Calendar e=Calendar.getInstance();e.setTime(d);e.set(Calendar.HOUR_OF_DAY,9);e.set(Calendar.MINUTE,0);e.set(Calendar.SECOND,0);for(int x=0;x<DAYS.length;x++){if((mask&BITS[x])==0)continue;int days=DAYS[x];long at=e.getTimeInMillis()-days*86400000L;Intent i=new Intent(c,ReminderReceiver.class).putExtra("title",o.optString("title")).putExtra("date",o.optString("date")).putExtra("days",days);PendingIntent pi=PendingIntent.getBroadcast(c,code(id,days),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);if(at>System.currentTimeMillis())((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);}}catch(Exception ignored){}}
 public static void cancel(Context c,long id){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);for(int d:DAYS){PendingIntent p=PendingIntent.getBroadcast(c,code(id,d),new Intent(c,ReminderReceiver.class),PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(p!=null){a.cancel(p);p.cancel();}}}
 public static void rescheduleAll(Context c){for(JSONObject o:LocalStore.schedules(c)){cancel(c,o.optLong("id"));if(!o.optBoolean("done"))schedule(c,o);}}
 private static int code(long id,int d){return (int)(Math.abs(id)%100000000)*20+d;}
}

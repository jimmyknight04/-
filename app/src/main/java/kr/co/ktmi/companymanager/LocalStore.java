package kr.co.ktmi.companymanager;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class LocalStore {
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("ktm_app_data", Context.MODE_PRIVATE); }
    private static JSONArray read(Context c, String key) {
        try { return new JSONArray(prefs(c).getString(key, "[]")); } catch (Exception e) { return new JSONArray(); }
    }
    private static void write(Context c, String key, JSONArray value) { prefs(c).edit().putString(key, value.toString()).apply(); }
    public static List<JSONObject> schedules(Context c) {
        List<JSONObject> out = new ArrayList<>(); JSONArray a=read(c,"schedules");
        for(int i=0;i<a.length();i++) out.add(a.optJSONObject(i));
        Collections.sort(out, Comparator.comparing(o -> o.optString("date","")));
        return out;
    }
    public static JSONObject saveSchedule(Context c, JSONObject item) {
        JSONArray a=read(c,"schedules"); long id=item.optLong("id",0);
        if(id==0){ id=System.currentTimeMillis()/1000; try{item.put("id",id);}catch(Exception ignored){} a.put(item); }
        else { for(int i=0;i<a.length();i++) if(a.optJSONObject(i).optLong("id")==id){ try{a.put(i,item);}catch(Exception ignored){} break; } }
        write(c,"schedules",a); return item;
    }
    public static void deleteSchedule(Context c,long id){ JSONArray a=read(c,"schedules"),b=new JSONArray(); for(int i=0;i<a.length();i++) if(a.optJSONObject(i).optLong("id")!=id)b.put(a.optJSONObject(i)); write(c,"schedules",b); }
    public static List<JSONObject> messages(Context c){ List<JSONObject> out=new ArrayList<>(); JSONArray a=read(c,"messages"); for(int i=a.length()-1;i>=0;i--)out.add(a.optJSONObject(i)); return out; }
    public static void addMessage(Context c,String text){ JSONArray a=read(c,"messages"); JSONObject o=new JSONObject(); try{o.put("id",System.currentTimeMillis());o.put("text",text);o.put("created",new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm",java.util.Locale.KOREA).format(new java.util.Date()));}catch(Exception ignored){} a.put(o);write(c,"messages",a); }
    public static void deleteMessage(Context c,long id){ JSONArray a=read(c,"messages"),b=new JSONArray(); for(int i=0;i<a.length();i++)if(a.optJSONObject(i).optLong("id")!=id)b.put(a.optJSONObject(i));write(c,"messages",b); }
}
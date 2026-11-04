package com.orthoclinic.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderScheduler {
    private static final String PREFS = "clinic_reminders";
    private static final String KEY = "items";

    static void schedule(Context context, String id, long at, String title, String message) {
        try {
            JSONObject item = new JSONObject();
            item.put("id", id); item.put("at", at);
            item.put("title", title); item.put("message", message);
            JSONArray arr = read(context);
            JSONArray next = new JSONArray();
            for (int i=0; i<arr.length(); i++) {
                JSONObject x = arr.getJSONObject(i);
                if (!id.equals(x.optString("id"))) next.put(x);
            }
            next.put(item);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, next.toString()).apply();
            setAlarm(context, item);
        } catch (Exception ignored) {}
    }

    static void cancel(Context context, String id) {
        JSONArray arr = read(context);
        JSONArray next = new JSONArray();
        for (int i=0; i<arr.length(); i++) {
            JSONObject x = arr.optJSONObject(i);
            if (x != null && !id.equals(x.optString("id"))) next.put(x);
            else if (x != null) {
                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra("id", id);
                PendingIntent pi = PendingIntent.getBroadcast(context, id.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pi);
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, next.toString()).apply();
    }

    static void restoreAll(Context context) {
        JSONArray arr = read(context);
        for (int i=0; i<arr.length(); i++) {
            JSONObject x = arr.optJSONObject(i);
            if (x != null && x.optLong("at") > System.currentTimeMillis()) setAlarm(context, x);
        }
    }

    private static void setAlarm(Context context, JSONObject x) {
        try {
            String id=x.getString("id");
            Intent intent=new Intent(context, ReminderReceiver.class);
            intent.putExtra("id",id);
            intent.putExtra("title",x.optString("title"));
            intent.putExtra("message",x.optString("message"));
            PendingIntent pi=PendingIntent.getBroadcast(context,id.hashCode(),intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            long at=x.getLong("at");
            if (Build.VERSION.SDK_INT >= 23) {
                if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);
                else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);
            } else am.setExact(AlarmManager.RTC_WAKEUP,at,pi);
        } catch (Exception ignored) {}
    }

    private static JSONArray read(Context context) {
        try { return new JSONArray(context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(KEY,"[]")); }
        catch (Exception e) { return new JSONArray(); }
    }
}

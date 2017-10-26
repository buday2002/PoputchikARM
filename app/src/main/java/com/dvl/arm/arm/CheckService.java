package com.dvl.arm.arm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Gravity;
import android.widget.Toast;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.PRIORITY_MAX;
import static com.dvl.arm.arm.GetMD5.md5;



import java.util.concurrent.ExecutionException;

import static android.R.attr.y;
import static java.lang.Integer.valueOf;
import static java.security.AccessController.getContext;

public class CheckService extends Service {

    JSONObject jsonObject = new JSONObject();
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    SharedPreferences sSettings;
    // это будет именем файла настроек
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";
    public static String timesettings="";

    public static Integer T1 = 10000;
    public static Integer T2 = 5;
    public static Integer T3 = 5;



    public String stServer="http://passline.ru";
    public String stIMEI="";
    public  String json="";

    public void GetSettings(){
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sSettings.contains(Server)) {
            stServer=sSettings.getString(Server, "http://passline.ru");
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI=telephonyManager.getDeviceId();

        try {
            timesettings = new POST().execute(stServer + "/api/settings", "imei", "sign", "","","", stIMEI, md5(md5(stIMEI)+ARM_SECRET_KEY),"","","").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            jsonObject = new JSONObject(timesettings);
            ArrayList arrayList = new ArrayList();
            arrayList.add(jsonObject.getString("T1"));
            arrayList.add(jsonObject.getString("T2"));
            arrayList.add(jsonObject.getString("T3"));

            String sign = jsonObject.getString("sign");

            Collections.sort(arrayList);
            String strformd5 = StringUtils.join(arrayList,";");
            String n = md5(md5(strformd5)+ARM_SECRET_KEY);
            if(sign.equals(n))
            {
                T1=valueOf(jsonObject.getString("T1"));
                T2=valueOf(jsonObject.getString("T2"));
                T3=valueOf(jsonObject.getString("T3"));
            }
            else
            {
              // return;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public CheckService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {

        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        GetSettings();
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mTimer.schedule(mMyTimerTask, 1000, T1*1000);
        return super.onStartCommand(intent, flags, startId);
    }


    public void onDestroy()
    {
        mTimer.cancel();
        super.onDestroy();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void CheckOrders() throws JSONException {
        try
        {
            json= new POST().execute(stServer+"/api/orders", "imei", "filter", "sign","","", stIMEI, "actual", md5(md5(stIMEI+";"+"actual")+ARM_SECRET_KEY),"","").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        boolean ok = false;
        json = StringEscapeUtils.unescapeJava(json);

        jsonObject = new JSONObject();

        if (!(json==""))
        {
            try {
                jsonObject = new JSONObject(json);//json.substring(0,json.length()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String checkstring = json;
            checkstring = checkstring.replace(" ","").replace("\"","");
            checkstring=checkstring.substring(checkstring.indexOf(",")+1,checkstring.length());
            checkstring = jsonObject.getString("calc")+";"+checkstring.replace("orders:","").replace(",sign:"+jsonObject.getString("sign")+"}","");
            checkstring = md5(md5(checkstring)+ARM_SECRET_KEY);
            String sign=jsonObject.getString("sign");

            ok = sign.equals(checkstring);

            if (ok)
            {
                Intent intent = new Intent("Orders");
                intent.putExtra("txt1", jsonObject.getString("calc"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }

        }

        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("orders");
        }  catch (JSONException e) {
            return;
        }

        int y = 0;
        for(y =0;y<jsonArray.length();y++)
        {
            try
            {
                JSONObject newjson = jsonArray.getJSONObject(y);
                String q =newjson.getString("id")+":orders";

                if (ok&&newjson.getString("type").equals("fpc")&&(MainActivity.NotifyCallsList.contains(q)))
                {
                    long currentTime = System.currentTimeMillis() / 1000L;
                    long CallsTime= valueOf(MainActivity.NotifyCallsTime.get(MainActivity.NotifyCallsList.indexOf(q)).toString());
                    if((currentTime-CallsTime)>T2)
                    {
                        MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
                        MainActivity.NotifyCallsList.remove(q);

                        ArrayList arrayList = new ArrayList();
                        arrayList.add(stIMEI);
                        arrayList.add(newjson.getString("id"));
                        arrayList.add("order");

                        Collections.sort(arrayList);
                        String strformd5 = StringUtils.join(arrayList, ";");
                        String md5 = md5(strformd5);

                        String sign = md5(md5 + ARM_SECRET_KEY);

                        try {
                            String s = new POST().execute(stServer + "/api/notseen", "order_id", "order_type", "imei", "sign","", newjson.getString("id"),"order",stIMEI,sign,"").get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }


                if (ok&&newjson.getString("type").equals("fpc")&&(!MainActivity.NotifyCallsList.contains(q) )){
                     NotifyOrders(newjson.getString("id"),newjson.getString("call_uts"),newjson.getString("place"),newjson.getString("content"),newjson.getString("wagon"));}
            } catch (JSONException e) {
                e.printStackTrace();
            }

        };
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void CheckFilms() throws JSONException {
        try
        {
            json= new POST().execute(stServer+"/api/films", "imei", "filter", "sign","","", stIMEI, "actual", md5(md5(stIMEI+";"+"actual")+ARM_SECRET_KEY),"","").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean ok = false;
        json = StringEscapeUtils.unescapeJava(json);

        jsonObject = new JSONObject();

        if (!(json==""))
        {
            try {
                jsonObject = new JSONObject(json);//json.substring(0,json.length()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String checkstring = json;
            checkstring = checkstring.replace(" ","").replace("\"","");
            checkstring=checkstring.substring(checkstring.indexOf(",")+1,checkstring.length());
            checkstring = jsonObject.getString("calc")+";"+checkstring.replace("films:","").replace(",sign:"+jsonObject.getString("sign")+"}","");
            checkstring = md5(md5(checkstring)+ARM_SECRET_KEY);
            String sign=jsonObject.getString("sign");

            ok = sign.equals(checkstring);

         //   if (ok)
         //   {
         //       Intent intent = new Intent("Orders");
         //       intent.putExtra("txt1", jsonObject.getString("calc"));
         //       LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         //   }

        }

        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("films");
        }  catch (JSONException e) {
            return;
        }

        int y = 0;
        for(y =0;y<jsonArray.length();y++)
        {
            try
            {
                JSONObject newjson = jsonArray.getJSONObject(y);
                String q =newjson.getString("id")+":films";

                if (ok&&(MainActivity.NotifyCallsList.contains(q))){
                    long currentTime = System.currentTimeMillis()/ 1000L;
                    long CallsTime= valueOf(MainActivity.NotifyCallsTime.get(MainActivity.NotifyCallsList.indexOf(q)).toString());
                    if((currentTime-CallsTime)>T2)
                    {
                        MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
                        MainActivity.NotifyCallsList.remove(q);

                        ArrayList arrayList = new ArrayList();
                        arrayList.add(stIMEI);
                        arrayList.add(newjson.getString("id"));
                        arrayList.add("films");

                        Collections.sort(arrayList);
                        String strformd5 = StringUtils.join(arrayList, ";");
                        String md5 = md5(strformd5);

                        String sign = md5(md5 + ARM_SECRET_KEY);

                        try {
                            String s = new POST().execute(stServer + "/api/notseen", "order_id", "order_type", "imei", "sign","", newjson.getString("id"),"films",stIMEI,sign,"").get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }


                if (ok&&(!MainActivity.NotifyCallsList.contains(q))){
                    NotifyFilms(newjson.getString("id"),newjson.getString("call_uts"),newjson.getString("place"),newjson.getString("film"),newjson.getString("price"),newjson.getString("code"),newjson.getString("wagon"));}
            } catch (JSONException e) {
                e.printStackTrace();
            }

        };
    }


    void CheckCalls() throws JSONException {
        try
        {
            json= new POST().execute(stServer+"/api/calls", "imei", "filter", "sign","","", stIMEI, "actual", md5(md5(stIMEI+";"+"actual")+ARM_SECRET_KEY),"","").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean ok = false;
        json = StringEscapeUtils.unescapeJava(json);

        jsonObject = new JSONObject();

        if (!(json==""))
        {
            try {
                jsonObject = new JSONObject(json);//json.substring(0,json.length()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        String checkstring = json;
            checkstring = checkstring.replace(" ","").replace("\"","");
            checkstring=checkstring.substring(checkstring.indexOf(",")+1,checkstring.length());
            checkstring = jsonObject.getString("calc")+";"+checkstring.replace("calls:","").replace(",sign:"+jsonObject.getString("sign")+"}","");
            checkstring = md5(md5(checkstring)+ARM_SECRET_KEY);
            String sign=jsonObject.getString("sign");

         ok = sign.equals(checkstring);

            if (ok)
            {
                Intent intent = new Intent("Calls");
                intent.putExtra("txt1", jsonObject.getString("calc"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }


        }
        JSONArray jsonArray = jsonObject.getJSONArray("calls");

        int y = 0;
        for(y =0;y<jsonArray.length();y++)
        {
            try
            {
                JSONObject newjson = jsonArray.getJSONObject(y);
                String q =newjson.getString("id")+":calls";

                if (ok&&(MainActivity.NotifyCallsList.contains(q))){
                    long currentTime = System.currentTimeMillis()/ 1000L;
                    long CallsTime= valueOf(MainActivity.NotifyCallsTime.get(MainActivity.NotifyCallsList.indexOf(q)).toString());
                    if((currentTime-CallsTime)>T2)
                    {
                        MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
                        MainActivity.NotifyCallsList.remove(q);

                        ArrayList arrayList = new ArrayList();
                        arrayList.add(stIMEI);
                        arrayList.add(newjson.getString("id"));
                        arrayList.add("call");

                        Collections.sort(arrayList);
                        String strformd5 = StringUtils.join(arrayList, ";");
                        String md5 = md5(strformd5);

                        String sign = md5(md5 + ARM_SECRET_KEY);

                        try {
                            String s = new POST().execute(stServer + "/api/notseen", "order_id", "order_type", "imei", "sign","", newjson.getString("id"),"call",stIMEI,sign,"").get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }


                if (ok&&(!MainActivity.NotifyCallsList.contains(q))){
                    NotifyCalls(newjson.getString("id"),newjson.getString("call_uts"),newjson.getString("place"),newjson.getString("message"),newjson.getString("wagon"),newjson.getString("passanger"));}
            } catch (JSONException e) {
                e.printStackTrace();
            }

        };






    }




    private static final int NOTIFY_ID_ORDERS = 30000001;
    private static final int NOTIFY_ID_CALLS = 40000001;
    private static final int NOTIFY_ID_FILMS = 20000001;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    public void NotifyOrders(String ID, String Time, String place, String content, String wagon ) {
        String Head = "";
        String Content = "";
        Context context = getApplicationContext();

        if (!MainActivity.NotifyCallsList.contains(ID+":orders"))
        {
            long currentTime = System.currentTimeMillis()/ 1000L;
            MainActivity.NotifyCallsList.add(ID+":orders");
            MainActivity.NotifyCallsTime.add(String.valueOf(currentTime));
        }

        content = content.replace("\"","").replace("{","").replace("}","");

        Head = "Вагон: "+wagon+"Место №" + place;
        if (Content.length()>20) {
            Content = content.substring(0, 20)+"...";
        }else
        {
            Content=content;
        }

        String timezoneID = TimeZone.getDefault().getID();
        Calendar cal = Calendar.getInstance();
        Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone(timezoneID));
        String formattedDate = sdf.format(date);

        Intent notificationIntent = new Intent(context, MessagesActivity.class);
        int requestID = (int) System.currentTimeMillis();

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("ID", ID);
        notificationIntent.putExtra("time", formattedDate);
        notificationIntent.putExtra("content", content);
        notificationIntent.putExtra("place", place);
        notificationIntent.putExtra("wagon", wagon);
        notificationIntent.putExtra( "status","Новый");
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestID, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.images)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ico))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(Head)
                .setContentText(Content) // Текст уведомления
                .setPriority(PRIORITY_MAX);


        Notification notification = builder.getNotification(); // до API 16


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }

        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        notification.flags = notification.flags | Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID_ORDERS + Integer.parseInt(ID), notification);

       // new POST().execute(stServer+"/arm/status/set/", "id", "status", "comment", ID, "5", "");

    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void NotifyFilms(String ID, String Time, String place, String film, String price,String code, String wagon ) {
        String Head = "";
        String Content = "";
        Context context = getApplicationContext();

        Head = "Вагон: "+wagon+"Место №" + place;
        if (Content.length()>20) {
            Content = film.substring(0, 20)+"...";
        }else
        {
            Content=film;
        }

        String timezoneID = TimeZone.getDefault().getID();
        Calendar cal = Calendar.getInstance();
        Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone(timezoneID));
        String formattedDate = sdf.format(date);

        Intent notificationIntent = new Intent(context, FilmsActivity.class);
        int requestID = (int) System.currentTimeMillis();

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("ID", ID);
        notificationIntent.putExtra("time", formattedDate);
        notificationIntent.putExtra("Film", film);
        notificationIntent.putExtra("Code", code);
        notificationIntent.putExtra("Price", price);
        notificationIntent.putExtra("place", place);
        notificationIntent.putExtra("wagon", wagon);
        notificationIntent.putExtra( "status","Новый");
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestID, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.film)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.film1))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(Head)
                .setContentText(Content) // Текст уведомления
                .setPriority(PRIORITY_MAX);


        Notification notification = builder.getNotification(); // до API 16


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }

        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        notification.flags = notification.flags | Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID_FILMS + Integer.parseInt(ID), notification);

        // new POST().execute(stServer+"/arm/status/set/", "id", "status", "comment", ID, "5", "");

    }




    public void NotifyCalls(String ID, String Time, String Mesto, String Subj, String wagon, String name ) {
        String Head = "";
        String Content = "";
        Context context = getApplicationContext();

        Head = "вагон № " + wagon + " место №" + Mesto;
        if (Content.length()>20) {
            Content = Subj.substring(0, 20)+"...";
        }else
        {
            Content=Subj;
        }

        String timezoneID = TimeZone.getDefault().getID();

        Calendar cal = Calendar.getInstance();
        Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone(timezoneID));
        String formattedDate = sdf.format(date);


        Intent notificationIntent = new Intent(context, ChatMsg.class);
        int requestID = (int) System.currentTimeMillis();

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("ID", ID);
        notificationIntent.putExtra("time", formattedDate);
        notificationIntent.putExtra("subj", Subj);
        notificationIntent.putExtra("mesto", Mesto);
        notificationIntent.putExtra("wagon", wagon);
        notificationIntent.putExtra("name", name);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestID, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

       if (!MainActivity.NotifyCallsList.contains(ID+":calls"))
       {
           long currentTime = System.currentTimeMillis()/ 1000L;
           MainActivity.NotifyCallsTime.add(String.valueOf(currentTime));
           MainActivity.NotifyCallsList.add(ID+":calls");
       }
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.chat)
                    // большая картинка
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.msg))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(Head)
                    .setContentText(Content) // Текст уведомления
                    .setPriority(PRIORITY_MAX);
        }


        Notification notification = builder.getNotification(); // до API 16

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }

        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        notification.flags = notification.flags | Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID_CALLS + Integer.parseInt(ID), notification);

        //new POST().execute(stServer+"/arm/im/statusset/", "id", "status_msg", "", ID, "1", "");

    }

    class MyTimerTask extends TimerTask {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run()
        {
            try {
                CheckOrders();
                CheckCalls();
                CheckFilms();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}

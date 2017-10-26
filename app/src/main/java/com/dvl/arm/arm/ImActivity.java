package com.dvl.arm.arm;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static com.dvl.arm.arm.GetMD5.md5;
import static java.lang.Integer.valueOf;


public class ImActivity extends AppCompatActivity {

    JSONObject jsonObject = new JSONObject();
    JSONArray jsonArray = null;
    public  String json="";
    public String head[];
    public String subj[];
    public String foother[];

    public String mesto="";
    public String order_id="";

    SharedPreferences sSettings;
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String Vagon = "Vagon";

    public String stServer="http://passline.ru";
    public String stVagon="";

    public String stIMEI="";
    public String ID="";
    public static final String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";




    public void GetSettings(){
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sSettings.contains(Server)) {
            stServer=sSettings.getString(Server, "http://passline.ru");
        }
        if (sSettings.contains(Vagon)) {
            stVagon=sSettings.getString(Vagon, "");
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI=telephonyManager.getDeviceId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_zakaz);
        Intent intent = getIntent();
        GetSettings();


        GetAllZakaz(stVagon);

    }


    private void GetAllZakaz(String vagon) {

        ListView  lv = (ListView)findViewById(R.id.lvZakaz);

        try {
            json = new POST().execute(stServer+"/api/calls", "imei", "filter", "sign","","", stIMEI, "all", md5(md5(stIMEI+";"+"all")+ARM_SECRET_KEY),"","").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        boolean ok = false;
        json = StringEscapeUtils.unescapeJava(json);

        jsonObject = new JSONObject();
        try {
        if (!(json==""))
          {

              jsonObject = new JSONObject(json);//json.substring(0,json.length()));

              String checkstring = json;
              checkstring = checkstring.replace(" ","").replace("\"","");
              checkstring=checkstring.substring(checkstring.indexOf(",")+1,checkstring.length());
              checkstring = jsonObject.getString("calc")+";"+checkstring.replace("calls:","").replace(",sign:"+jsonObject.getString("sign")+"}","");
              checkstring = md5(md5(checkstring)+ARM_SECRET_KEY);
              String sign=jsonObject.getString("sign");
              ok = sign.equals(checkstring);

          }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonArray = jsonObject.getJSONArray("calls");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(jsonArray==null){
            return;
        }

        String[] items = new String[jsonArray.length()];
        int y = 0;
        for (y = 0; y < jsonArray.length(); y++) {
            try {
                JSONObject newjson = jsonArray.getJSONObject(y);;
                String status="";

                switch (newjson.getString("status")) {
                    case "0":
                        status = "Новый";
                        break;

                    case "1":
                        status = "Принят";
                        break;

                    case "2":
                        status = "Обработан";
                        break;
                }

               String Time= newjson.getString("call_uts").toString();

                String timezoneID = TimeZone.getDefault().getID();

                Calendar cal = Calendar.getInstance();
                Date date = new Date(valueOf(Time)*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone(timezoneID));
                String formattedDate = sdf.format(date);

                if (ok)
                    {
                        items[jsonArray.length() - y-1] = "Сообщение вагон №" + newjson.getString("wagon") + " место№" + newjson.getString("place") + " " + formattedDate + " Статус: " + status;
                    }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        };

        ArrayAdapter<String> adapter = new ArrayAdapter1(this,
                android.R.layout.simple_list_item_1, items);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String txt="";
                String status="";
                String msg="";
                String vagon="";




                try {
                        JSONObject newjson = jsonArray.getJSONObject(jsonArray.length()-position-1);
                        switch (newjson.getString("status")){
                            case "0":
                                status="Новый";
                                break;

                            case "1":
                                status="Принят";
                                break;

                            case "2":
                                status="Обработан";
                                break;

                        }

                    String Time= newjson.getString("call_uts").toString();

                    ID=newjson.getString("id").toString();

                    String timezoneID = TimeZone.getDefault().getID();
                    Calendar cal = Calendar.getInstance();
                    Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
                    sdf.setTimeZone(TimeZone.getTimeZone(timezoneID)); // give a timezone reference for formating (see comment at the bottom
                    String formattedDate = sdf.format(date);



                        order_id = newjson.getString("id");
                        vagon = newjson.getString("wagon");
                        mesto = newjson.getString("place");
                        txt = "Сообщение вагон №" + newjson.getString("wagon") + " место№" + newjson.getString("place");
                        msg= "Текст сообщения:\n"+newjson.getString("message")+"\nВремя поступления:\n"+formattedDate+"\nИмя пассажира:\n"+newjson.getString("passanger");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                final SpannableString s = new SpannableString(msg);
                Linkify.addLinks(s, Linkify.PHONE_NUMBERS);


                int NOTIFY_ID_CALLS = 40000001;
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(NOTIFY_ID_CALLS + Integer.parseInt(ID));

                if (status.equals("Новый"))

                {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(stIMEI);
                    arrayList.add(ID);
                    arrayList.add("1");

                    Collections.sort(arrayList);
                    String strformd5 = StringUtils.join(arrayList,";");
                    String md5=md5(strformd5);
                    String sign=md5(md5+ARM_SECRET_KEY);
                    String as="";
                    try {
                        as = new POST().execute(stServer + "/api/setcallstatus","imei", "id", "status", "sign","",stIMEI, ID, "1", sign,"").get();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }


                String q =ID+":calls";
                if (MainActivity.NotifyCallsList.contains(order_id+":calls")) {
                    MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
                    MainActivity.NotifyCallsList.remove(q);
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(ImActivity.this);
                builder.setTitle(Html.fromHtml("<font color='#000000'>"+txt+"</font>"))
                        .setMessage(s)
                        .setIcon(R.drawable.arm)
                        .setCancelable(false)
                        .setNegativeButton(" Закрыть",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                     finish();
                                                    }

                                                })
                        .setPositiveButton(" Завершить вызов",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ArrayList arrayList = new ArrayList();
                                        arrayList.add(stIMEI);
                                        arrayList.add(ID);
                                        arrayList.add("2");

                                        Collections.sort(arrayList);
                                        String strformd5 = StringUtils.join(arrayList,";");
                                        String md5=md5(strformd5);

                                        String sign=md5(md5+ARM_SECRET_KEY);
                                        String s="";
                                        try {
                                            s = new POST().execute(stServer + "/api/setcallstatus","imei", "id", "status", "sign","",stIMEI, ID, "2", sign,"").get();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                        Intent intent = new Intent(ImActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);
                                        finish();
                                    }

                                });
                AlertDialog alert = builder.create();



                alert.getWindow().setBackgroundDrawableResource(R.drawable.round);
                alert.show();
                ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                ((TextView)alert.findViewById(android.R.id.message)).setTextColor(Color.BLACK);
                ((TextView)alert.findViewById(android.R.id.message)).setLinkTextColor(Color.BLUE);
                alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
             }
        });

        lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

}
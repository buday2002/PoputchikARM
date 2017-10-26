package com.dvl.arm.arm;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static com.dvl.arm.arm.GetMD5.md5;
import static java.lang.Integer.valueOf;

public class ZakazActivity extends AppCompatActivity {

    JSONObject jsonObject = new JSONObject();
    JSONArray jsonArray = null;
    public  String json="";
    public String wagon="";
    public String place="";
    public String content="";
    public String head[];
    public String subj[];
    public String foother[];
    Context context = this;
    public String[] pos;

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

        GetAllZakaz();

    }


    private void GetAllZakaz() {

        ListView  lv = (ListView)findViewById(R.id.lvZakaz);

        try {
            json = new POST().execute(stServer+"/api/orders", "imei", "filter", "sign","","", stIMEI, "all", md5(md5(stIMEI+";"+"all")+ARM_SECRET_KEY),"","").get();
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
                checkstring = jsonObject.getString("calc")+";"+checkstring.replace("orders:","").replace(",sign:"+jsonObject.getString("sign")+"}","");
                checkstring = md5(md5(checkstring)+ARM_SECRET_KEY);
                String sign=jsonObject.getString("sign");
                ok = sign.equals(checkstring);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonArray = jsonObject.getJSONArray("orders");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(jsonArray==null){
            return;
        }

        //Узнаем сколько заказов пришло именно проводнику
        pos=new String[jsonArray.length()];
        int k=0;
        for (int j = 0; j < jsonArray.length(); j++) {

            try {
                JSONObject newjson = jsonArray.getJSONObject(jsonArray.length() - j - 1);

                if (newjson.getString("type").equals("fpc")){
                    pos[k]= String.valueOf(jsonArray.length() - j - 1);
                    k++;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        String[] items = new String[k];
        int y = 0;
        int p=0;
        for (y = 0; y < jsonArray.length(); y++) {
            try {
                JSONObject newjson = jsonArray.getJSONObject(jsonArray.length()-y-1);;
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
                    case "3":
                        status = "Отклонен";
                        break;
                    case "4":
                        status = "Оплачен";
                        break;
                }

                String Time= newjson.getString("call_uts").toString();

                String timezoneID = TimeZone.getDefault().getID();

                Calendar cal = Calendar.getInstance();
                Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
                sdf.setTimeZone(TimeZone.getTimeZone(timezoneID)); // give a timezone reference for formating (see comment at the bottom
                String formattedDate = sdf.format(date);


                    if (ok&&newjson.getString("type").equals("fpc")){
                        items[p] = "Заказ вагон №" + newjson.getString("wagon") + " место№" + newjson.getString("place") + " " + formattedDate + " Статус: " + status;
                        p++;
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
                String formattedDate="";


                try {
                    JSONObject newjson = jsonArray.getJSONObject(valueOf(pos[position]));
                    switch (newjson.getString("status")){
                        case "0":
                            status= "Новый";
                            break;

                        case "1":
                            status= "Принят";
                            break;

                        case "2":
                            status= "Обработан";
                            break;
                        case "3":
                            status = "Отклонен";
                            break;
                        case "4":
                            status = "Оплачен";
                            break;

                    }

                    String Time= newjson.getString("call_uts").toString();

                    ID=newjson.getString("id").toString();

                    String timezoneID = TimeZone.getDefault().getID();

                    Calendar cal = Calendar.getInstance();
                    Date date = new Date(valueOf(Time)*1000L); // *1000 is to convert seconds to milliseconds
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
                    sdf.setTimeZone(TimeZone.getTimeZone(timezoneID)); // give a timezone reference for formating (see comment at the bottom
                    formattedDate = sdf.format(date);



                    order_id = newjson.getString("id");
                    wagon = newjson.getString("wagon");
                    place = newjson.getString("place");
                    content = newjson.getString("content");
                    newjson.getString("status");
                    content = content.replace("\"","").replace("{","").replace("}","");

                    txt = "Сообщение вагон №" + newjson.getString("wagon") + " место№" + newjson.getString("place");
                    msg= "Текст сообщения:\n"+newjson.getString("message")+"\nВремя поступления:\n"+formattedDate+"\nИмя пассажира:\n"+newjson.getString("passanger");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final SpannableString s = new SpannableString(msg);
                Intent intent = new Intent(context, MessagesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("ID", order_id);
                intent.putExtra("time", formattedDate);
                intent.putExtra("content", content);
                intent.putExtra("place", place);
                intent.putExtra("wagon", wagon);
                intent.putExtra( "status",status);
                startActivity(intent);

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
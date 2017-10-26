package com.dvl.arm.arm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static com.dvl.arm.arm.GetMD5.md5;

public class ChatMsg extends AppCompatActivity {
    TextView header;
    TextView time;
    TextView subj;
    String ID = "";
    final Context context = this;
    public boolean close = false;
    String stIMEI = "";
    String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";
    JSONObject jsonObject = new JSONObject();

    SharedPreferences sSettings;
    // это будет именем файла настроек
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String Vagon = "Vagon";

    public String stServer = "http://passline.ru";
    public String stVagon = "";
    public String json = "";

    public void GetSettings() {
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sSettings.contains(Server)) {
            stServer = sSettings.getString(Server, "http://passline.ru");
        }
        if (sSettings.contains(Vagon)) {
            stVagon = sSettings.getString(Vagon, "");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_msg);
        Bundle extras = getIntent().getExtras();

        GetSettings();





        header = (TextView) findViewById(R.id.tvHeaderChat);
        time = (TextView) findViewById(R.id.tvTimeChat);
        subj = (TextView) findViewById(R.id.tvSubjChat);

        Intent intent = getIntent();
        String id = extras.getString("ID");//intent.getStringExtra("ID");
        getIntent().removeExtra("ID");
        String Time = extras.getString("time");
        getIntent().removeExtra("time");
        String Subj = extras.getString("subj");
        getIntent().removeExtra("subj");
        String Mesto = extras.getString("mesto");
        getIntent().removeExtra("mesto");
        String wagon = extras.getString("wagon");
        getIntent().removeExtra("wagon");
        String name = extras.getString("name");
        getIntent().removeExtra("name");




        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI=telephonyManager.getDeviceId();


        ArrayList arrayList = new ArrayList();
        arrayList.add(stIMEI);
        arrayList.add(id);
        arrayList.add("1");

        Collections.sort(arrayList);
        String strformd5 = StringUtils.join(arrayList,";");
        String md5=md5(strformd5);

        String sign=md5(md5+ARM_SECRET_KEY);
        String s="";
        try {
            s = new POST().execute(stServer + "/api/setcallstatus","imei", "id", "status", "sign","",stIMEI, id, "1", sign,"").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // собираем строку для проверки хеша
        ArrayList JsonList = new ArrayList();
        try {
            jsonObject = new JSONObject(s);
            for (int i = 0;i < jsonObject.length();i++) {
                if(!jsonObject.names().get(i).equals("sign")) {
                    String m=jsonObject.getString(jsonObject.names().get(i).toString());
                    if(m=="true"){m="True";} // - питоновские заморочки
                    if(m=="false"){m="False";}// - питоновские заморочки
                    JsonList.add(m);
                }
                else{sign = jsonObject.getString(jsonObject.names().get(i).toString());}
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(JsonList);
        String Names = StringUtils.join(JsonList,";");
        boolean ok=false;

        // солим и проверяем
        String check = md5(md5(Names)+ARM_SECRET_KEY);
        try {
            ok = (check.equals(sign)&&jsonObject.getString("status")=="true"); //если подпись проверена и сервер принял наши настройки, то работаем дальше
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String q =id+":calls";
        if (MainActivity.NotifyCallsList.contains(id+":calls")) {
            MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
            MainActivity.NotifyCallsList.remove(q);
        }

        header.setText("Новое сообщение с места №" + Mesto + " вагон №"+ wagon + "\nИмя пассажира: "+name);
        time.setText(Time);
        ID = id;
        subj.setText(Subj);

        Button btn = (Button)findViewById(R.id.btnOKChat);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ChatMsg.super.finish();
            }
        });

    }



}
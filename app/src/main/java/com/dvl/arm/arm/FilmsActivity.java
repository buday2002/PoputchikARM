package com.dvl.arm.arm;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.view.KeyEvent.KEYCODE_HOME;
import static com.dvl.arm.arm.GetMD5.md5;
import static java.lang.String.valueOf;

public class FilmsActivity extends AppCompatActivity {
    TextView header;
    TextView time;
    TextView subj;
    TextView code;
    String ID = "";
    final Context context = this;
    public boolean close = false;
    public boolean paid = false;
    public static String msg="";


    SharedPreferences sSettings;
    // это будет именем файла настроек
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String Vagon = "Vagon";

    public String stServer = "http://passline.ru";
    public String stVagon = "";
    public String json = "";

    public String Time = "";
    public String Film = "";
    public String Price = "";
    public String Code = "";
    public String place = "";
    public String wagon = "";
    public String Status = "";

    public String stIMEI = "";
    public static final String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";

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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_films);
        Bundle extras = getIntent().getExtras();

        GetSettings();


        header = (TextView) findViewById(R.id.tvHeaderF);
        time = (TextView) findViewById(R.id.tvTimeF);
        subj = (TextView) findViewById(R.id.tvSubjF);
        code = (TextView) findViewById(R.id.tvCodeF);

        Intent intent = getIntent();
        ID = extras.getString("ID");
        getIntent().removeExtra("ID");
        Time = extras.getString("time");
        getIntent().removeExtra("time");
        Film = extras.getString("Film");
        getIntent().removeExtra("Film");
        Code = extras.getString("Code");
        getIntent().removeExtra("Code");
        Price = extras.getString("Price");
        getIntent().removeExtra("Price");
        place = extras.getString("place");
        getIntent().removeExtra("place");
        wagon = extras.getString("wagon");
        getIntent().removeExtra("wagon");
        Status = extras.getString("status");
        getIntent().removeExtra("status");
        header.setText("Вагон№" + wagon + " место№" + place);
        time.setText(Time);
        subj.setText("Заявка:\n" + "Фильм: "+Film+",\nЦена: "+ valueOf(Float.parseFloat(Price)/100)+ "\nВремя поступления:\n" + Time + "\nСтатус заказа: " + Status);


        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI = telephonyManager.getDeviceId();


        Button pay = (Button) findViewById(R.id.btnPayF);
        Button cancel = (Button) findViewById(R.id.btnCancelF);

        if (Status.equals("Обработан")||Status.equals("Отклонен"))
        {
            pay.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
        }


        ArrayList arrayList = new ArrayList();
        arrayList.add(stIMEI);
        arrayList.add(ID);
        arrayList.add("1");

        Collections.sort(arrayList);
        String strformd5 = StringUtils.join(arrayList, ";");
        String md5 = md5(strformd5);

        String sign = md5(md5 + ARM_SECRET_KEY);
        String s = "";

        String q =ID+":films";
        if (MainActivity.NotifyCallsList.contains(ID+":films")) {
            MainActivity.NotifyCallsTime.remove(MainActivity.NotifyCallsList.indexOf(q));
            MainActivity.NotifyCallsList.remove(q);
        }

        int NOTIFY_ID_ORDERS = 20000001;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFY_ID_ORDERS + Integer.parseInt(ID));

        if (Status.equals("Новый"))

        {
            try {
                s = new POST().execute(stServer + "/api/setfilmstatus", "imei", "id", "status", "sign","", stIMEI, ID, "1", sign,"").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        code.setText(Code);
        //new POST().execute(stServer+"/arm/status/set/", "id", "status", "comment", id, "1", "");
    }


    public void PayClick(View v) {
        if (!paid) {

            ArrayList arrayList = new ArrayList();
            arrayList.add(stIMEI);
            arrayList.add(ID);
            arrayList.add("4");

            Collections.sort(arrayList);
            String strformd5 = StringUtils.join(arrayList, ";");
            String md5 = md5(strformd5);

            String sign = md5(md5 + ARM_SECRET_KEY);
            String s = "";
            try {
                s = new POST().execute(stServer + "/api/setfilmstatus", "imei", "id", "status", "sign", "", stIMEI, ID, "4", sign, "").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(s);

                code.setText(jsonObject.getString("code"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MainActivity.NotifyCallsList.remove(ID + ":films");

            Toast toast = Toast.makeText(this,
                    "Получен код: " + code.getText(),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 60);
            toast.show();
            Button btn = (Button)findViewById(R.id.btnCancelF);
            Button btnPay = (Button)findViewById(R.id.btnPayF);
            btn.setVisibility(View.GONE);
            btnPay.setText("Закрыть");
            paid=true;
        }else
        {
            ArrayList arrayList = new ArrayList();
            arrayList.add(stIMEI);
            arrayList.add(ID);
            arrayList.add("2");

            Collections.sort(arrayList);
            String strformd5 = StringUtils.join(arrayList, ";");
            String md5 = md5(strformd5);

            String sign = md5(md5 + ARM_SECRET_KEY);
            String s = "";
            try {
                s = new POST().execute(stServer + "/api/setfilmstatus", "imei", "id", "status", "sign","message", stIMEI, ID, "2", sign,msg).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(FilmsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            MainActivity.NotifyCallsList.remove(ID+":films");
            finish();
        }


    }

    public void CancelClick(View v) {

        Button btn = (Button)findViewById(R.id.btnCancelF);
        Button btnPay = (Button)findViewById(R.id.btnPayF);
        if (!close)
        {
            Otkaz otkaz = new Otkaz(context,"Отправить","Укажите причину отказа:",ID);
            otkaz.show();
            btnPay.setVisibility(View.GONE);
            btn.setText("Закрыть");
            close = true;
        }else
        {
            ArrayList arrayList = new ArrayList();
            arrayList.add(stIMEI);
            arrayList.add(ID);
            arrayList.add("3");

            Collections.sort(arrayList);
            String strformd5 = StringUtils.join(arrayList, ";");
            String md5 = md5(strformd5);

            String sign = md5(md5 + ARM_SECRET_KEY);
            String s = "";
            try {
                s = new POST().execute(stServer + "/api/setfilmstatus", "imei", "id", "status", "sign","message", stIMEI, ID, "3", sign,msg).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(FilmsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            MainActivity.NotifyCallsList.remove(ID+":films");
            finish();
        }



    }
}
package com.dvl.arm.arm;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static com.dvl.arm.arm.GetMD5.md5;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private Button button;
    private EditText editTextMainScreen;
    public static TextView number;
    public static TextView name;
    public static String fio="";
    public static String vagon="";
    public static String server="";
    public static boolean StartService = false;
    public ImageButton imgSettings;
    public ImageButton imgStart;
    public ImageButton imgExit;
    public TextView start;

    AlertDialog.Builder ad;

    private Timer sTimer;
    private MainActivity.sMyTimerTask sMyTimerTask;

    public static SharedPreferences sSettings;
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String Vagon = "Vagon";
    public static final String Fio = "Fio";
    public static final ArrayList NotifyCallsList = new ArrayList();
    public static final ArrayList NotifyCallsTime = new ArrayList();

    public String stServer="http://passline.ru";
    public String stVagon="";
    public String stFio="";

    public String stIMEI="";

    public static final String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";



    public void GetSettings(){
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sSettings.contains(Server)) {
            stServer=sSettings.getString(Server, "http://passline.ru");
        }
        if (sSettings.contains(Vagon)) {
            stVagon=sSettings.getString(Vagon, "");
        }
        if (sSettings.contains(Fio)) {
            stFio=sSettings.getString(Fio, "");
        }

        number.setText(stVagon);
        name.setText("Проводник:\n"+stFio);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI=telephonyManager.getDeviceId();
    }

    public static void SetName() {
        name.setText("Проводник:\n"+fio);
        number.setText(vagon);
   }

    public void SetSettings() {
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sSettings.edit();
            ed.putString(Server, server);
            ed.putString(Vagon, vagon);
            ed.putString(Fio, fio);
            ed.commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverOrders,
                new IntentFilter("Orders"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverCalls,
                new IntentFilter("Calls"));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        Button my = (Button) findViewById(R.id.btnZakaz);
        imgSettings = (ImageButton) findViewById(R.id.imageButton);
        imgStart = (ImageButton) findViewById(R.id.StartButton);
        imgExit = (ImageButton) findViewById(R.id.imageExit);
        start = (TextView)findViewById(R.id.tvStart);
        number = (TextView) findViewById(R.id.tvNumber);
        name = (TextView) findViewById(R.id.txtName);

        // my.setVisibility(View.INVISIBLE);
        sTimer = new Timer();
        sMyTimerTask = new MainActivity.sMyTimerTask();

        Button calls = (Button) findViewById(R.id.btnCalls);
        Button btnFilms = (Button) findViewById(R.id.btnFilms);
        sTimer.schedule(sMyTimerTask, 1000, 1000);


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    1);
        }



        if (isMyServiceRunning(CheckService.class)) {
            imgStart.setImageResource(R.drawable.stop);
            start.setText(R.string.monitoring_start);

        } else {
            imgStart.setImageResource(R.drawable.start);
            start.setText(R.string.monitoring_stop);
            LoginDialog lgDlg = new LoginDialog(context, "Войти в систему", "Авторизация");
            lgDlg.getWindow().setBackgroundDrawableResource(R.drawable.round);
            lgDlg.show();
            lgDlg.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            lgDlg.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        }

        imgSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SetSettings();
                Intent intent = new Intent(MainActivity.this, OptionActivity.class);
                startActivity(intent);
            }
        });


        btnFilms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SetSettings();
                Intent intent = new Intent(MainActivity.this, FilmActivity.class);
                startActivity(intent);
            }
        });

        imgExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String title = "Завершение приложения";
                String message = "Вы действительно хотите выйти?";
                String positive = "Да";
                String negatine = "Нет";

                ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle(title);
                ad.setMessage(message);
                ad.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {


                        StartService=false;
                        new Stop().execute();

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancelAll();
                        try {
                            String result = new POST().execute(stServer + "/api/exit", "imei", "sign", "","","","", stIMEI, md5(md5(stIMEI)+ARM_SECRET_KEY),"","","","").get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
                ad.setNegativeButton(negatine, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        return;
                    }
                });
                ad.setCancelable(false);
                ad.show();
            }
        });



        imgStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isMyServiceRunning(CheckService.class))
                {
                    SetSettings();
                    imgStart.setImageResource(R.drawable.stop);
                    start.setText(R.string.monitoring_start);
                    StartService=true;
                    new Start().execute();
                }else
                {
                    SetSettings();
                    imgStart.setImageResource(R.drawable.start);
                    start.setText(R.string.monitoring_stop);
                    StartService=false;
                    new Stop().execute();
                }
            }
        });

        calls.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetSettings();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, OptionActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeClick(View v)
    {
        SetSettings();
        Intent intent = new Intent(MainActivity.this, OptionActivity.class);
        startActivity(intent);
    }



    public void zakazClick(View v)
    {
        SetSettings();
        Intent intent = new Intent(this, ZakazActivity.class);
        startActivity(intent);


    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public class Start extends AsyncTask<String,String, String> {

        @Override
        protected String doInBackground(String... strings) {
            return "";



        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            imgStart.setImageResource(R.drawable.stop);
            start.setText(R.string.monitoring_start);
            SetSettings();
            startService(new Intent(MainActivity.this, CheckService.class));
        }
    }


    public class Stop extends AsyncTask<String,String, String> {

        @Override
        protected String doInBackground(String... strings) {
            return "";



        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            imgStart.setImageResource(R.drawable.start);
            start.setText(R.string.monitoring_stop);
            stopService(new Intent(MainActivity.this, CheckService.class));
        }
    }

    class sMyTimerTask extends TimerTask {

        @Override
        public void run()
        {
            if(StartService&&!isMyServiceRunning(CheckService.class))
            {
                new Start().execute();
            }


            if(!StartService&&isMyServiceRunning(CheckService.class))
            {
                new Stop().execute();
            }

        }
    };
/*------------------------------------------------------------------------------*/


    private BroadcastReceiver mMessageReceiverOrders = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String txt1 = intent.getStringExtra("txt1");
            String txt2="МОИ ЗАКАЗЫ\n";
            String txt3 = "Незавершенных: "+txt1;
            final SpannableStringBuilder text = new SpannableStringBuilder(txt2+txt3);
            final ForegroundColorSpan style = new ForegroundColorSpan(Color.RED);
            text.setSpan(style, txt2.length(), text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            Button ord = (Button) findViewById(R.id.btnZakaz);
            ord.setText(text);
        }
    };
    private BroadcastReceiver mMessageReceiverCalls = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String txt1 = intent.getStringExtra("txt1");
            String txt2="МОИ УВЕДОМЛЕНИЯ\n";
            String txt3 = "Незавершенных: "+txt1;
            final SpannableStringBuilder text = new SpannableStringBuilder(txt2+txt3);
            final ForegroundColorSpan style = new ForegroundColorSpan(Color.RED);
            text.setSpan(style, txt2.length(), text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            Button call = (Button) findViewById(R.id.btnCalls);
            call.setText(text);

        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverOrders);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverCalls);
        super.onDestroy();
    }

}

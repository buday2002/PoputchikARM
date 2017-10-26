package com.dvl.arm.arm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import com.dvl.arm.arm.GetMD5;

import static android.provider.Settings.Global.getString;
import static android.support.v4.content.ContextCompat.startActivity;
import static com.dvl.arm.arm.GetMD5.md5;
import static java.lang.System.in;

/**
 * Created by admin on 28.01.2017.
 */

public class LoginDialog extends AlertDialog {

    String s ="";
    JSONObject jsonObject = new JSONObject();

    String stServer="";
    String stVagon="";
    String stFio="";
    String stIMEI = "";
    String ARM_SECRET_KEY = "e456cae1ea22363c83fdcf4fc6cc0f0a";


    public LoginDialog(Context context, String btn, String msg) {
        super(context);
        setMessage(msg);

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View promptView = layoutInflater.inflate(R.layout.loginfrm, null);
        Builder LoginDialogBuilder = new Builder(context);
        setView(promptView);
        setCancelable(false);

        SharedPreferences sSettings;
        // это будет именем файла настроек
        final String APP_PREFERENCES = "ARMSettings";
        final String Server = "Server";
        final String Fio = "Fio";
        final String Vagon = "Vagon";

            sSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            if (sSettings.contains(Server)) {
                stServer=sSettings.getString(Server, "http://passline.ru");
            }
            if (sSettings.contains(Fio)) {
                stFio=sSettings.getString(Fio, "");
            }
             if (sSettings.contains(Fio)) {
                 stVagon=sSettings.getString(Vagon, "");
            }
        if (stServer==""){
            stServer="http://passline.ru";
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        stIMEI=telephonyManager.getDeviceId();

       EditText server = (EditText) promptView.findViewById(R.id.etServer);
       EditText vagons = (EditText) promptView.findViewById(R.id.etVagons);
       EditText fio = (EditText) promptView.findViewById(R.id.etFIO);


      //Разрешаем в вагоны вводить только цифры, "-" и ","
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))&&source.charAt(i)!='-'&&source.charAt(i)!=',') {

                        Toast toast = Toast.makeText(getContext(),
                                "Допускается вводить только номер вагона, список номеров через запятую или диапазон через тире (Пример: 1,2,4-6)",
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 60);
                        toast.show();

                        return "";
                    }
                }
                return null;
            }
        };
        vagons.setFilters(new InputFilter[] { filter });


       server.setText(stServer);
       vagons.setText(stVagon);
       fio.setText(stFio);


       setButton(AlertDialog.BUTTON_POSITIVE, btn, (new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }));

        setButton(DialogInterface.BUTTON_NEGATIVE, "Выход",
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something
                    }
                });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText server = (EditText)findViewById(R.id.etServer);
                EditText vagons = (EditText)findViewById(R.id.etVagons);
                EditText fio = (EditText)findViewById(R.id.etFIO);
                stServer = server.getText().toString();

                boolean ok=false;

                String strformd5 = "";


                String vag = vagons.getText().toString();
                if (vag.isEmpty())
                {
                    Toast toast = Toast.makeText(getContext(),
                            "Укажите вагоны, которые будете обслуживать!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 60);
                    toast.show();
                    return;
                }

                String del = ";";
                String md5 ="";
                String sign="";

                ArrayList arrayList = new ArrayList();
                arrayList.add(stIMEI);
                arrayList.add(vagons.getText().toString());
                if(fio.getText()  != null)
                {
                    arrayList.add(fio.getText().toString());
                }
                Collections.sort(arrayList);
                strformd5 = StringUtils.join(arrayList,";");

                md5=md5(strformd5);

                sign=md5(md5+ARM_SECRET_KEY);

                stVagon=vagons.getText().toString();
                try {
                    s = new POST().execute(stServer + "/api/setwagons", "imei", "wagons", "person","sign","", stIMEI, vagons.getText().toString(), fio.getText().toString(), sign,"").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                try {
                    s= URLEncoder.encode(s, "UTF-8").replace("%EF%BB%BF%EF%BB%BF","").replace("%EF%BB%BF","");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                s = StringEscapeUtils.unescapeJava(s);
                try {
                    s= URLDecoder.decode(s, "UTF-8");
                } catch (UnsupportedEncodingException e) {
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

                // солим и проверяем
                String check = md5(md5(Names)+ARM_SECRET_KEY);
                try {
                    ok = (check.equals(sign)&&jsonObject.getString("status")=="true"); //если подпись проверена и сервер принял наши настройки, то работаем дальше
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if ((!s.isEmpty())||ok) {
                    MainActivity.server=server.getText().toString();
                    MainActivity.fio=fio.getText().toString();
                    MainActivity.vagon=vagons.getText().toString();
                    MainActivity.SetName();
                    MainActivity.StartService = true;
                    dismiss();
                } else {

                    Toast toast = Toast.makeText(getContext(),
                            "Ошибка авторизации",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 60);
                    toast.show();
                }
            }
        });


        getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });


    }


}

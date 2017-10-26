package com.dvl.arm.arm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OptionActivity extends AppCompatActivity {
    public EditText server;
    public EditText fio;

    SharedPreferences sSettings;
    // это будет именем файла настроек
    public static final String APP_PREFERENCES = "ARMSettings";
    public static final String Server = "Server";
    public static final String Fio = "Fio";
    public static final String Vagon = "Vagon";

    public String stServer="http://passline.ru";
    public String stFio="";
    public String stVagon="";

    public void GetSettings(){
        sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sSettings.contains(Server)) {
            stServer=sSettings.getString(Server, "http://passline.ru");
        }
        if (sSettings.contains(Fio)) {
            stFio=sSettings.getString(Fio, "");
        }
        if (sSettings.contains(Vagon)) {
            stVagon=sSettings.getString(Vagon, "");
        }

        if (stServer==""){
            stServer="http://passline.ru";
        }

        server.setText(stServer);
        fio.setText(stFio);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        server = (EditText)findViewById(R.id.etServer);
        fio = (EditText)findViewById(R.id.etFio);

        setTitle("Настройки");
        GetSettings();


        final Button save = (Button) findViewById(R.id.btnsave);
        save.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sSettings.edit();
                ed.putString(Server, server.getText().toString());
                ed.putString(Fio, fio.getText().toString());
                ed.commit();

                MainActivity.fio = fio.getText().toString();

                new POST().execute(stServer+"/arm/login/setfio/", "login", "fio","","","",stVagon, fio.getText().toString(),"","","");
                //создаем и отображаем текстовое уведомление
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Настройки успешно сохранены",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 60);
                toast.show();

                finish();


            }

        });

    }
}

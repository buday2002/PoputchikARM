package com.dvl.arm.arm;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import static android.app.PendingIntent.getActivity;
import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by admin on 28.01.2017.
 */

public class Otkaz extends AlertDialog {



     public Otkaz(Context context, String btn, String msg, String id) {
        super(context);
        setMessage(msg);

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View promptView = layoutInflater.inflate(R.layout.otkaz, null);
        Builder OtkazBuilder = new Builder(context);
        // set prompts.xml to be the layout file of the alertdialog builder
        setView(promptView);
        setCancelable(false);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button close = (Button) findViewById(R.id.btnCanClose);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText otkaz = (EditText) findViewById(R.id.etOtkaz);
                MessagesActivity.msg = otkaz.getText().toString();
                FilmsActivity.msg = otkaz.getText().toString();
                dismiss();
           }

        });


    }


}

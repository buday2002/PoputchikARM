package com.dvl.arm.arm;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by admin on 06.02.2017.
 */

public class ArrayAdapter1 extends ArrayAdapter<String> {


    public ArrayAdapter1(Context context, int resID, String[] items) {
        super(context, resID, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ((TextView) v).setTextColor(Color.BLACK);
        String st=((TextView) v).getText().toString().substring(((TextView) v).getText().toString().lastIndexOf(" ")+1,((TextView) v).getText().toString().length());
        if (st.equals("Новый")) {
            ((TextView) v).setBackgroundColor(Color.RED);
        }
        if (st.equals("Оплачен")) {
            ((TextView) v).setBackgroundColor(Color.GREEN);
        }
        if (st.equals("Принят")) {
            ((TextView) v).setBackgroundColor(Color.BLUE);
        }
        if (st.equals("Отклонен")) {
            ((TextView) v).setBackgroundColor(Color.GRAY);
        }
        if (st.equals("Обработан")) {
            ((TextView) v).setBackgroundColor(Color.rgb(237,237,237));
        }
        return v;
    }

}
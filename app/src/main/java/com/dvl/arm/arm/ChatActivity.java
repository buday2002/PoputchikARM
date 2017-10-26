package com.dvl.arm.arm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    public String order_id;
    public String mesto;
    public String vagon;
    public int who;

    private DiscussArrayAdapter adapter;
    private ListView lv;
    private EditText editText1;
    private static Random random;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        order_id = intent.getStringExtra("order_id");
        mesto = intent.getStringExtra("mesto");
        vagon = intent.getStringExtra("vagon");

        setContentView(R.layout.activity_chat);
        random = new Random();

        lv = (ListView) findViewById(R.id.listView1);

        adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.listitem_discuss);

        lv.setAdapter(adapter);

        adapter.add(new OneComment(true, "Вагон: "+ vagon));
        adapter.add(new OneComment(true, "Место: "+ mesto));

        editText1 = (EditText) findViewById(R.id.editText1);
        editText1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    adapter.add(new OneComment(false, editText1.getText().toString()));
                    editText1.setText("");
                    return true;
                }
                return false;
            }
        });

    }
}

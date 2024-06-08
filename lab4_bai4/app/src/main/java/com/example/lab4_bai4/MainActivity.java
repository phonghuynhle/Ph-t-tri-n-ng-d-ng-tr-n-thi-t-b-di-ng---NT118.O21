package com.example.lab4_bai4;

import android.Manifest;
import android.content.*;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Switch swAutoResponse;
    private LinearLayout llButtons;
    private Button btnSafe, btnMayday;
    private ArrayList<String> requesters;
    private ArrayAdapter<String> adapter;
    private ListView lvMessages;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private final String AUTO_RESPONSE = "auto_response";
    public static boolean isRunning;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request SMS permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, 1);

        findViewsByIds();
        initVariables();
        handleOnClickListeners();
        initBroadcastReceiver();
    }

    private void findViewsByIds() {
        swAutoResponse = findViewById(R.id.sw_auto_response);
        llButtons = findViewById(R.id.ll_buttons);
        lvMessages = findViewById(R.id.lv_messages);
        btnSafe = findViewById(R.id.btn_safe);
        btnMayday = findViewById(R.id.btn_mayday);
    }

    private void initVariables() {
        requesters = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requesters);
        lvMessages.setAdapter(adapter);
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        swAutoResponse.setChecked(sharedPreferences.getBoolean(AUTO_RESPONSE, false));
        if (swAutoResponse.isChecked()) {
            llButtons.setVisibility(View.GONE);
        } else {
            llButtons.setVisibility(View.VISIBLE);
        }
    }

    private void handleOnClickListeners() {
        btnSafe.setOnClickListener(view -> respond(true));
        btnMayday.setOnClickListener(view -> respond(false));
        swAutoResponse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                llButtons.setVisibility(View.GONE);
            } else {
                llButtons.setVisibility(View.VISIBLE);
            }
            editor.putBoolean(AUTO_RESPONSE, isChecked);
            editor.commit();
        });
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> newMessages = intent.getStringArrayListExtra("messages");
                if (newMessages != null) {
                    requesters.addAll(newMessages);
                    adapter.notifyDataSetChanged();
                    if (swAutoResponse.isChecked()) {
                        respond(true);
                    }
                }
            }
        };
    }

    private void respond(boolean isSafe) {
        String responseText = isSafe ? getString(R.string.i_am_safe_and_well_worry_not) : getString(R.string.tell_my_mother_i_love_her);
        for (String requester : requesters) {
            // send SMS response (requires SEND_SMS permission)
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(requester, null, responseText, null, null);
        }
        requesters.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        registerReceiver(broadcastReceiver, new IntentFilter("com.example.lab4_cau3.SMS_RECEIVED"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        unregisterReceiver(broadcastReceiver);
    }
}

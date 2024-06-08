package com.example.lab4_bai4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                ArrayList<String> messages = new ArrayList<>();
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();
                    if (messageBody.contains("are you ok?")) {
                        messages.add(sender);
                    }
                }
                if (!messages.isEmpty()) {
                    if (MainActivity.isRunning) {
                        Intent broadcastIntent = new Intent("com.example.lab4_cau3.SMS_RECEIVED");
                        broadcastIntent.putStringArrayListExtra("messages", messages);
                        context.sendBroadcast(broadcastIntent);
                    } else {
                        Intent startIntent = new Intent(context, MainActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.putStringArrayListExtra("messages", messages);
                        context.startActivity(startIntent);
                    }
                }
            }
        }
    }
}

package com.mihirkandoi.smartdoorlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startApp = new Intent(context, Dashboard.class);
        startApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String action=intent.getStringExtra("action");
        if(action.equals("unlock door")) {
            startApp.putExtra("action", "unlock door");
            context.startActivity(startApp);
        }
        else if(action.equals("start stream")) {
            startApp.putExtra("action", "start stream");
            context.startActivity(startApp);
        }
        //This is used to close the notification tray
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }
}

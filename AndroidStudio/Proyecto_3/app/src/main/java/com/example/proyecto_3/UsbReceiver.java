package com.example.proyecto_3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.example.proyecto_3.USB_PERMISSION".equals(action)) {
            boolean permissionGranted = intent.getBooleanExtra("permission", false);
            if (permissionGranted) {
                Log.d("UsbReceiver", "USB Permission granted");
            } else {
                Log.d("UsbReceiver", "USB Permission denied");
            }
        }
    }
}

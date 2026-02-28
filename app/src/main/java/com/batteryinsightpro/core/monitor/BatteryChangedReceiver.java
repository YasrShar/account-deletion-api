package com.batteryinsightpro.core.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BatteryMonitorCore.getInstance(context).refreshFromStickyIntent(intent);
    }
}

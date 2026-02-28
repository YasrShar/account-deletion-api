package com.batteryinsightpro.core.monitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;

import androidx.lifecycle.MutableLiveData;

import com.batteryinsightpro.core.algo.BatteryMath;
import com.batteryinsightpro.data.model.BatterySnapshot;

public class BatteryMonitorCore {
    private static volatile BatteryMonitorCore INSTANCE;
    private final Context appContext;
    private final BatteryManager batteryManager;
    private final PowerManager powerManager;
    private final MutableLiveData<BatterySnapshot> snapshotLiveData = new MutableLiveData<>();
    private final ForegroundAppDetector foregroundAppDetector;

    private BatteryMonitorCore(Context context) {
        appContext = context.getApplicationContext();
        batteryManager = (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);
        powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        foregroundAppDetector = new ForegroundAppDetector(appContext);
    }

    public static BatteryMonitorCore getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BatteryMonitorCore.class) {
                if (INSTANCE == null) INSTANCE = new BatteryMonitorCore(context);
            }
        }
        return INSTANCE;
    }

    public MutableLiveData<BatterySnapshot> getSnapshotLiveData() {
        return snapshotLiveData;
    }

    public void start() {
        Intent sticky = appContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (sticky != null) refreshFromStickyIntent(sticky);
    }

    public void refreshFromStickyIntent(Intent intent) {
        BatterySnapshot s = new BatterySnapshot();
        s.timestamp = System.currentTimeMillis();
        s.levelPercent = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        s.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        s.plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        s.temperatureC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
        s.voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        long currentUa = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        s.currentMa = BatteryMath.normalizeCurrentMa(currentUa);
        s.powerW = BatteryMath.computePowerW(s.voltageMv, s.currentMa);
        s.screenOn = powerManager.isInteractive();
        s.foregroundPackage = foregroundAppDetector.getCurrentForegroundPackage();
        boolean hasCurrent = currentUa != Integer.MIN_VALUE && currentUa != 0;
        boolean hasCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0;
        s.confidence = BatteryMath.confidenceLabel(hasCurrent, hasCounter);
        snapshotLiveData.postValue(s);
    }
}

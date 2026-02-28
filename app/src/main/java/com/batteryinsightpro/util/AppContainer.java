package com.batteryinsightpro.util;

import android.content.Context;

import com.batteryinsightpro.data.repo.BatteryRepository;

public class AppContainer {
    private static volatile AppContainer instance;
    public final BatteryRepository batteryRepository;

    private AppContainer(Context context) {
        batteryRepository = new BatteryRepository(context);
    }

    public static AppContainer get(Context context) {
        if (instance == null) {
            synchronized (AppContainer.class) {
                if (instance == null) instance = new AppContainer(context.getApplicationContext());
            }
        }
        return instance;
    }
}

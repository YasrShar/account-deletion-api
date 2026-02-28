package com.batteryinsightpro.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.batteryinsightpro.data.db.AppDatabase;
import com.batteryinsightpro.data.db.BatteryDao;
import com.batteryinsightpro.data.db.BatterySampleEntity;
import com.batteryinsightpro.data.model.BatterySnapshot;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatteryRepository {
    private final BatteryDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public BatteryRepository(Context context) {
        dao = AppDatabase.getInstance(context).batteryDao();
    }

    public void saveSnapshot(BatterySnapshot snapshot) {
        io.execute(() -> {
            BatterySampleEntity e = new BatterySampleEntity();
            e.timestamp = snapshot.timestamp;
            e.levelPercent = snapshot.levelPercent;
            e.status = snapshot.status;
            e.plugType = snapshot.plugType;
            e.temperatureC = snapshot.temperatureC;
            e.voltageMv = snapshot.voltageMv;
            e.currentMa = snapshot.currentMa;
            e.powerW = snapshot.powerW;
            e.isScreenOn = snapshot.screenOn;
            e.foregroundPackage = snapshot.foregroundPackage;
            dao.insertSample(e);
        });
    }

    public LiveData<List<BatterySampleEntity>> getLast24h() {
        long start = System.currentTimeMillis() - (24L * 60 * 60 * 1000);
        return dao.getSamplesSince(start);
    }
}

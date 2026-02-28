package com.batteryinsightpro.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.batteryinsightpro.core.monitor.BatteryMonitorCore;
import com.batteryinsightpro.data.model.BatterySnapshot;
import com.batteryinsightpro.data.repo.BatteryRepository;

public class DashboardViewModel extends AndroidViewModel {
    private final BatteryMonitorCore monitorCore;
    private final BatteryRepository repository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        monitorCore = BatteryMonitorCore.getInstance(application);
        repository = new BatteryRepository(application);
        monitorCore.start();
    }

    public LiveData<BatterySnapshot> getLiveSnapshot() {
        return monitorCore.getSnapshotLiveData();
    }

    public void persist(BatterySnapshot snapshot) {
        repository.saveSnapshot(snapshot);
    }
}

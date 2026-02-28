package com.batteryinsightpro.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.batteryinsightpro.data.db.AppDatabase;

public class SampleCleanupWorker extends Worker {
    public SampleCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long retentionDays = 7;
        long cutoff = System.currentTimeMillis() - retentionDays * 24L * 60 * 60 * 1000;
        AppDatabase.getInstance(getApplicationContext()).batteryDao().deleteOldSamples(cutoff);
        return Result.success();
    }
}

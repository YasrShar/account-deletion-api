package com.batteryinsightpro.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "charge_sessions")
public class ChargeSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long startTs;
    public long endTs;
    public int startLevel;
    public int endLevel;
    public float avgCurrentMa;
    public float peakCurrentMa;
    public float avgTempC;
    public float maxTempC;
    public float estimatedCapacityMahDelta;
    public float estimatedWearScore;
}

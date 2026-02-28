package com.batteryinsightpro.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "battery_samples")
public class BatterySampleEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long timestamp;
    public int levelPercent;
    public int status;
    public int plugType;
    public float temperatureC;
    public int voltageMv;
    public float currentMa;
    public float powerW;
    public boolean isScreenOn;
    public String foregroundPackage;
}

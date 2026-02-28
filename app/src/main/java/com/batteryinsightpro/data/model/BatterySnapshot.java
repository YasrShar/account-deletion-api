package com.batteryinsightpro.data.model;

public class BatterySnapshot {
    public long timestamp;
    public int levelPercent;
    public int status;
    public int plugType;
    public float temperatureC;
    public int voltageMv;
    public float currentMa;
    public float powerW;
    public boolean screenOn;
    public String foregroundPackage;
    public String confidence;
}

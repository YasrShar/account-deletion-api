package com.batteryinsightpro.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "charger_tests")
public class ChargerTestEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String cableName;
    public float avgMaScreenOff;
    public float avgMaScreenOn;
    public float avgW;
    public float tempRise;
    public float stabilityScore;
    public long createdAt;
}

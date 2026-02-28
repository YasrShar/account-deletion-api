package com.batteryinsightpro.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BatteryDao {
    @Insert
    void insertSample(BatterySampleEntity sample);

    @Insert
    void insertSession(ChargeSessionEntity session);

    @Insert
    void insertChargerTest(ChargerTestEntity test);

    @Query("SELECT * FROM battery_samples WHERE timestamp > :fromTs ORDER BY timestamp DESC")
    LiveData<List<BatterySampleEntity>> getSamplesSince(long fromTs);

    @Query("SELECT * FROM charge_sessions ORDER BY startTs DESC LIMIT 100")
    LiveData<List<ChargeSessionEntity>> getRecentSessions();

    @Query("DELETE FROM battery_samples WHERE timestamp < :beforeTs")
    void deleteOldSamples(long beforeTs);
}

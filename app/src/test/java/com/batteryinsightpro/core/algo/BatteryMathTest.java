package com.batteryinsightpro.core.algo;

import org.junit.Assert;
import org.junit.Test;

public class BatteryMathTest {
    @Test
    public void ema_smoothesValue() {
        float result = BatteryMath.ema(1000f, 2000f, 0.3f);
        Assert.assertEquals(1300f, result, 0.01f);
    }

    @Test
    public void wearScore_increasesWithHeatAndEndLevel() {
        float low = BatteryMath.wearScore(80, 30f, false, 0f);
        float high = BatteryMath.wearScore(100, 40f, true, 20f);
        Assert.assertTrue(high > low);
    }

    @Test
    public void capacityEstimator_chargeCounter() {
        float cap = CapacityEstimator.estimateFromChargeCounter(1000000, 2200000, 20, 80);
        Assert.assertTrue(cap > 1500f);
    }
}

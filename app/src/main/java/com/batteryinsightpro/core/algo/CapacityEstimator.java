package com.batteryinsightpro.core.algo;

public class CapacityEstimator {
    private CapacityEstimator() {}

    public static float estimateFromChargeCounter(long startUaH, long endUaH, int startLevel, int endLevel) {
        if (endLevel <= startLevel) return -1f;
        float deltaMah = (endUaH - startUaH) / 1000f;
        float deltaPct = endLevel - startLevel;
        return (deltaMah / deltaPct) * 100f;
    }

    public static float estimateByCurrentIntegration(float[] currentMa, long[] timestampsMs) {
        if (currentMa.length != timestampsMs.length || currentMa.length < 2) return -1f;
        float mah = 0f;
        for (int i = 1; i < currentMa.length; i++) {
            float hours = (timestampsMs[i] - timestampsMs[i - 1]) / 3600000f;
            mah += (currentMa[i] * hours);
        }
        return Math.abs(mah);
    }
}

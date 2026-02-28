package com.batteryinsightpro.core.algo;

public class UsageEstimator {
    private UsageEstimator() {}

    public static float estimateAppConsumptionMah(float batteryDropPercent, float estimatedCapacityMah, float foregroundRatio) {
        float totalDropMah = (batteryDropPercent / 100f) * estimatedCapacityMah;
        return Math.max(0f, totalDropMah * foregroundRatio);
    }

    public static float estimateDeepSleepPercent(float screenOffMinutes, float noForegroundMinutes, float totalMinutes) {
        if (totalMinutes <= 0f) return 0f;
        return Math.min(100f, ((screenOffMinutes + noForegroundMinutes) / 2f) / totalMinutes * 100f);
    }
}

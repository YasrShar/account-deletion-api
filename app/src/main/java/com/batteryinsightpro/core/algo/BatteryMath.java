package com.batteryinsightpro.core.algo;

public class BatteryMath {
    private BatteryMath() {}

    public static float normalizeCurrentMa(long rawCurrentUa) {
        return rawCurrentUa / 1000f;
    }

    public static float computePowerW(int voltageMv, float currentMa) {
        return (voltageMv / 1000f) * (currentMa / 1000f);
    }

    public static float ema(float previous, float current, float alpha) {
        return (alpha * current) + ((1f - alpha) * previous);
    }

    public static float estimateTimeHours(float deltaPercent, float percentPerHour) {
        if (Math.abs(percentPerHour) < 0.01f) return -1f;
        return deltaPercent / percentPerHour;
    }

    public static float wearScore(int endLevel, float avgTemp, boolean fastCharge, float minutesNearHundred) {
        float score = 0f;
        score += Math.max(0, endLevel - 80) * 0.8f;
        score += Math.max(0, avgTemp - 32f) * 1.2f;
        score += fastCharge ? 12f : 0f;
        score += minutesNearHundred * 0.2f;
        return Math.min(100f, Math.max(0f, score));
    }

    public static String confidenceLabel(boolean hasCurrent, boolean hasCounter) {
        if (hasCurrent && hasCounter) return "High";
        if (hasCurrent || hasCounter) return "Medium";
        return "Low";
    }
}

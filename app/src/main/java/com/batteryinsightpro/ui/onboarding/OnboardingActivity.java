package com.batteryinsightpro.ui.onboarding;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.batteryinsightpro.core.permissions.PermissionNavigator;
import com.batteryinsightpro.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.usageAccessButton.setOnClickListener(v -> PermissionNavigator.openUsageAccess(this));
        binding.overlayButton.setOnClickListener(v -> PermissionNavigator.openOverlaySettings(this));
        binding.optimizationButton.setOnClickListener(v -> PermissionNavigator.openBatteryOptimization(this));
        binding.doneButton.setOnClickListener(v -> finish());
    }
}

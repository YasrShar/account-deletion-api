package com.batteryinsightpro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.batteryinsightpro.databinding.ActivityMainBinding;
import com.batteryinsightpro.ui.chargespeed.ChargeSpeedFragment;
import com.batteryinsightpro.ui.dashboard.DashboardFragment;
import com.batteryinsightpro.ui.health.HealthFragment;
import com.batteryinsightpro.ui.history.HistoryFragment;
import com.batteryinsightpro.ui.settings.SettingsFragment;
import com.batteryinsightpro.ui.usage.UsageFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                show(new DashboardFragment());
            } else if (id == R.id.nav_health) {
                show(new HealthFragment());
            } else if (id == R.id.nav_usage) {
                show(new UsageFragment());
            } else if (id == R.id.nav_charge_speed) {
                show(new ChargeSpeedFragment());
            } else if (id == R.id.nav_history) {
                show(new HistoryFragment());
            } else if (id == R.id.nav_settings) {
                show(new SettingsFragment());
            }
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

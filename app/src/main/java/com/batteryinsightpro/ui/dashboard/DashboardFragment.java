package com.batteryinsightpro.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.batteryinsightpro.databinding.FragmentDashboardBinding;

import java.util.Locale;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
        vm.getLiveSnapshot().observe(getViewLifecycleOwner(), snapshot -> {
            binding.levelText.setText(String.format(Locale.getDefault(), "%d%%", snapshot.levelPercent));
            binding.statusText.setText(getStatusText(snapshot.status));
            binding.currentText.setText(String.format(Locale.getDefault(), "%.0f mA", snapshot.currentMa));
            binding.voltageText.setText(String.format(Locale.getDefault(), "%.2f V", snapshot.voltageMv / 1000f));
            binding.powerText.setText(String.format(Locale.getDefault(), "%.2f W", snapshot.powerW));
            binding.tempText.setText(String.format(Locale.getDefault(), "%.1f °C", snapshot.temperatureC));
            binding.confidenceText.setText(snapshot.confidence);
            vm.persist(snapshot);
        });
        return binding.getRoot();
    }

    private String getStatusText(int status) {
        switch (status) {
            case 2: return "Charging";
            case 3: return "Discharging";
            case 5: return "Full";
            default: return "Unknown";
        }
    }
}

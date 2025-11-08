package com.example.monetizationdemo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monetizationdemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MonetizationManager.Listener {

    private ActivityMainBinding binding;
    private MonetizationManager monetizationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        monetizationManager = new MonetizationManager(this);
        monetizationManager.setListener(this);
        monetizationManager.bind(this, binding.admobBanner, binding.fanBannerContainer);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!monetizationManager.showExitConfirmationDialog()) {
                    setEnabled(false);
                    finish();
                }
            }
        });

        binding.buttonRemoveAds.setOnClickListener(v -> monetizationManager.launchRemoveAdsPurchase());
        binding.buttonRestore.setOnClickListener(v -> {
            Toast.makeText(this, R.string.restoring_purchases, Toast.LENGTH_SHORT).show();
            monetizationManager.restorePurchases();
        });
        binding.buttonShowInterstitial.setOnClickListener(v -> monetizationManager.showInterstitialAd());
    }

    @Override
    protected void onResume() {
        super.onResume();
        monetizationManager.refreshAdsIfNeeded(binding.fanBannerContainer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        monetizationManager.onDestroy();
        binding = null;
    }

    @Override
    public void onAdsStatusChanged(boolean adsRemoved) {
        if (binding == null) {
            return;
        }
        if (adsRemoved) {
            binding.status.setText(R.string.ads_disabled);
            binding.buttonRemoveAds.setEnabled(false);
            binding.buttonShowInterstitial.setEnabled(false);
        } else {
            binding.status.setText(R.string.ads_enabled);
            binding.buttonRemoveAds.setEnabled(true);
            binding.buttonShowInterstitial.setEnabled(true);
        }
    }
}

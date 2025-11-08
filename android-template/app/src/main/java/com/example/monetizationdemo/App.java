package com.example.monetizationdemo;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;

/**
 * Application entry point to initialize ad SDKs early in the app lifecycle.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> {
            // AdMob initialized
        });

        AudienceNetworkAds.initialize(this);
    }
}

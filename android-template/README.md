# Android Monetization Template

This template project provides a minimal Java-based Android application that integrates AdMob, Meta Audience Network, and Google Play Billing (v8.1.0) to offer an easily reusable monetization foundation. All monetization logic lives inside a single `MonetizationManager` class so it can be dropped into other projects with minimal changes.

## Features
- **Single entry point** for ads and in-app purchases via `MonetizationManager`.
- AdMob banner and interstitial examples.
- Meta Audience Network banner and interstitial examples.
- Google Play Billing integration (Billing Library 8.1.0) for removing ads with a $1 in-app product.
- Lightweight UI with a toggle button showing how to remove/restore ads.

## Project Structure
```
android-template/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/monetizationdemo/
│       │   ├── App.java
│       │   ├── MainActivity.java
│       │   └── MonetizationManager.java
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/strings.xml
├── build.gradle (project)
├── gradle.properties
└── settings.gradle
```

## Getting Started
1. Open `android-template` in Android Studio (Giraffe or newer recommended).
2. Replace the placeholder ad unit IDs and Facebook placement IDs with your own production values.
3. In the Google Play Console, configure a non-consumable in-app product with the ID defined in `MonetizationManager#PRODUCT_ID_REMOVE_ADS` and set the price to $1.00.
4. Update the application ID in `app/build.gradle` and the manifest if you want to publish under a different namespace.
5. Sync Gradle and run on a test device. Test purchases and ads using the official sandbox/testing tools.

## Notes
- Meta Audience Network requires testing with devices registered in the Meta developer portal. Replace the sample IDs in `MonetizationManager` when releasing.
- Remember to update your Privacy Policy and comply with both Google Play and ad network requirements before publishing.
- This project intentionally keeps UI logic simple so you can reuse the monetization class across your other apps.

## Key Class: MonetizationManager
The [`MonetizationManager`](app/src/main/java/com/example/monetizationdemo/MonetizationManager.java) class exposes:
- `setListener(Listener)` to react to ad state changes.
- `bind(Activity, AdView, ViewGroup)` to initialize SDKs with your UI components.
- `launchRemoveAdsPurchase()` and `restorePurchases()` for billing.
- `showInterstitialAd()` to display whichever network is ready.
- `onDestroy()` to release SDK resources.

````java
MonetizationManager manager = new MonetizationManager(context);
manager.setListener(isRemoved -> updateUi(isRemoved));
manager.bind(activity, binding.admobBanner, binding.fanBannerContainer);
````

Adjust the `PRODUCT_ID_REMOVE_ADS` constant and ad unit IDs before publishing.

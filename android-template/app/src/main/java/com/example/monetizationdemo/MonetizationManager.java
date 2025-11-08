package com.example.monetizationdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.monetizationdemo.databinding.DialogExitConfirmationBinding;
import com.example.monetizationdemo.databinding.NativeExitAdmobBinding;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Centralizes all monetization responsibilities: loading ads, handling the billing flow,
 * and remembering when the user has removed advertisements.
 */
public class MonetizationManager implements PurchasesUpdatedListener {

    public interface Listener {
        void onAdsStatusChanged(boolean adsRemoved);
    }

    public static final String PRODUCT_ID_REMOVE_ADS = "remove_ads";

    private static final String TAG = "MonetizationManager";
    private static final String PREFS_NAME = "monetization_prefs";
    private static final String KEY_ADS_REMOVED = "ads_removed";

    private final Context appContext;
    private Activity activity;

    private BillingClient billingClient;
    private ProductDetails removeAdsProductDetails;

    private AdView adMobBannerView;
    private com.facebook.ads.AdView fanBannerView;
    private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitial;
    private InterstitialAd fanInterstitial;
    private NativeAd adMobNativeAd;
    private com.facebook.ads.AdView fanRectangleView;
    private AlertDialog exitDialog;

    private boolean adsRemoved;
    private Listener listener;

    public MonetizationManager(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.adsRemoved = readAdsRemovedPreference();
        initializeBilling();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onAdsStatusChanged(adsRemoved);
        }
    }

    /**
     * Bind the activity and ad views once the UI is ready.
     */
    public void bind(@NonNull Activity activity,
                     AdView adMobBanner,
                     ViewGroup fanBannerContainer) {
        this.activity = activity;
        this.adMobBannerView = adMobBanner;

        if (!adsRemoved) {
            loadAdMobBanner();
            loadAdMobInterstitial();
            loadFanBanner(fanBannerContainer);
            loadFanInterstitial();
            loadAdMobNativeAd();
            loadFanRectangle();
        } else {
            hideAdViews();
        }
        notifyAdsStatusChanged();
    }

    public boolean areAdsRemoved() {
        return adsRemoved;
    }

    public void refreshAdsIfNeeded(ViewGroup fanBannerContainer) {
        if (!adsRemoved) {
            loadAdMobBanner();
            loadAdMobInterstitial();
            loadFanBanner(fanBannerContainer);
            loadFanInterstitial();
            loadAdMobNativeAd();
            loadFanRectangle();
        }
    }

    private void loadAdMobBanner() {
        if (adMobBannerView == null || adsRemoved) {
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        adMobBannerView.loadAd(request);
    }

    private void loadAdMobInterstitial() {
        if (activity == null || adsRemoved) {
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
                activity,
                "ca-app-pub-3940256099942544/1033173712",
                request,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                        adMobInterstitial = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.w(TAG, "AdMob interstitial failed: " + loadAdError.getMessage());
                        adMobInterstitial = null;
                    }
                }
        );
    }

    private void loadFanBanner(ViewGroup container) {
        if (container == null || adsRemoved) {
            return;
        }
        destroyFanBanner();
        fanBannerView = new com.facebook.ads.AdView(activity, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
        container.removeAllViews();
        container.addView(fanBannerView);
        fanBannerView.loadAd(fanBannerView.buildLoadAdConfig().withAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.w(TAG, "FAN banner failed: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // banner loaded
            }

            @Override
            public void onAdClicked(Ad ad) { }

            @Override
            public void onLoggingImpression(Ad ad) { }
        }).build());
    }

    private void loadFanInterstitial() {
        if (activity == null || adsRemoved) {
            return;
        }
        destroyFanInterstitial();
        fanInterstitial = new InterstitialAd(activity, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID");
        fanInterstitial.loadAd(
                fanInterstitial.buildLoadAdConfig()
                        .withAdListener(new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) { }

                            @Override
                            public void onInterstitialDismissed(Ad ad) { }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Log.w(TAG, "FAN interstitial failed: " + adError.getErrorMessage());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) { }

                            @Override
                            public void onAdClicked(Ad ad) { }

                            @Override
                            public void onLoggingImpression(Ad ad) { }
                        })
                        .build());
    }

    private void loadAdMobNativeAd() {
        if (activity == null || adsRemoved) {
            return;
        }

        AdLoader adLoader = new AdLoader.Builder(activity, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(nativeAd -> {
                    if (adMobNativeAd != null) {
                        adMobNativeAd.destroy();
                    }
                    adMobNativeAd = nativeAd;
                })
                .withAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.w(TAG, "AdMob native ad failed: " + loadAdError.getMessage());
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadFanRectangle() {
        if (activity == null || adsRemoved) {
            return;
        }
        destroyFanRectangle();
        fanRectangleView = new com.facebook.ads.AdView(activity, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.RECTANGLE_HEIGHT_250);
        fanRectangleView.loadAd(fanRectangleView.buildLoadAdConfig().withAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.w(TAG, "FAN rectangle failed: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) { }

            @Override
            public void onAdClicked(Ad ad) { }

            @Override
            public void onLoggingImpression(Ad ad) { }
        }).build());
    }

    public void showInterstitialAd() {
        if (adsRemoved) {
            return;
        }
        if (adMobInterstitial != null) {
            adMobInterstitial.show(activity);
            adMobInterstitial = null;
            loadAdMobInterstitial();
        } else if (fanInterstitial != null && fanInterstitial.isAdLoaded()) {
            fanInterstitial.show();
        }
    }

    /**
     * Starts the billing flow for removing advertisements.
     */
    public void launchRemoveAdsPurchase() {
        if (activity == null || removeAdsProductDetails == null) {
            Log.w(TAG, "Billing not ready yet");
            return;
        }

        BillingFlowParams.ProductDetailsParams productDetailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(removeAdsProductDetails)
                        .build();

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productDetailsParams))
                .build();

        billingClient.launchBillingFlow(activity, flowParams);
    }

    /**
     * Restore purchases (e.g., when the user presses a restore button).
     */
    public void restorePurchases() {
        if (billingClient == null || billingClient.getConnectionState() != BillingClient.ConnectionState.CONNECTED) {
            initializeBilling();
            return;
        }
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchasesList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        handlePurchases(purchasesList);
                    }
                }
        );
    }

    private void initializeBilling() {
        PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build();

        billingClient = BillingClient.newBuilder(appContext)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Will retry on next request.");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                    queryExistingPurchases();
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    private void queryProductDetails() {
        QueryProductDetailsParams.Product product =
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(product))
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                (BillingResult billingResult, QueryProductDetailsResult productDetailsResult) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && productDetailsResult != null) {
                        List<ProductDetails> productDetailsList = productDetailsResult.getProductDetailsList();
                        if (productDetailsList == null) {
                            return;
                        }
                        for (ProductDetails details : productDetailsList) {
                            if (PRODUCT_ID_REMOVE_ADS.equals(details.getProductId())) {
                                removeAdsProductDetails = details;
                                break;
                            }
                        }
                    } else {
                        Log.e(TAG, "Product detail query failed: " + billingResult.getDebugMessage());
                    }
                });
    }

    private void queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        handlePurchases(purchases);
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled purchase");
        } else {
            Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
        }
    }

    private void handlePurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            List<String> productIds = purchase.getProducts();
            if (productIds == null || !productIds.contains(PRODUCT_ID_REMOVE_ADS)) {
                continue;
            }
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgeParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgeParams, billingResult -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            markAdsRemoved();
                        }
                    });
                } else {
                    markAdsRemoved();
                }
            }
        }
    }

    private void markAdsRemoved() {
        adsRemoved = true;
        writeAdsRemovedPreference(true);
        hideAdViews();
        notifyAdsStatusChanged();
    }

    public boolean showExitConfirmationDialog() {
        if (activity == null) {
            return false;
        }
        if (exitDialog != null && exitDialog.isShowing()) {
            return true;
        }

        DialogExitConfirmationBinding binding = DialogExitConfirmationBinding.inflate(LayoutInflater.from(activity));
        binding.exitAdContainer.removeAllViews();
        binding.exitAdContainer.setVisibility(View.GONE);

        if (!adsRemoved) {
            boolean adShown = populateAdMobExitAd(binding.exitAdContainer);
            if (!adShown) {
                adShown = populateFanExitAd(binding.exitAdContainer);
            }
            if (!adShown) {
                binding.exitAdContainer.setVisibility(View.GONE);
                if (adMobNativeAd == null) {
                    loadAdMobNativeAd();
                }
                if (fanRectangleView == null) {
                    loadFanRectangle();
                }
            }
        } else {
            binding.exitAdContainer.setVisibility(View.GONE);
        }

        binding.buttonExitCancel.setOnClickListener(v -> dismissExitDialog());
        binding.buttonExitConfirm.setOnClickListener(v -> {
            dismissExitDialog();
            activity.finish();
        });

        exitDialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setCancelable(true)
                .create();
        exitDialog.setOnDismissListener(dialog -> binding.exitAdContainer.removeAllViews());
        exitDialog.show();

        return true;
    }

    private boolean populateAdMobExitAd(ViewGroup container) {
        if (activity == null || adMobNativeAd == null) {
            return false;
        }
        NativeExitAdmobBinding nativeBinding = NativeExitAdmobBinding.inflate(LayoutInflater.from(activity), container, false);
        NativeAd nativeAd = adMobNativeAd;

        nativeBinding.nativeAdView.setHeadlineView(nativeBinding.nativeAdHeadline);
        nativeBinding.nativeAdHeadline.setText(nativeAd.getHeadline());

        nativeBinding.nativeAdView.setMediaView(nativeBinding.nativeAdMedia);
        if (nativeAd.getMediaContent() != null) {
            nativeBinding.nativeAdMedia.setMediaContent(nativeAd.getMediaContent());
            nativeBinding.nativeAdMedia.setVisibility(View.VISIBLE);
        } else {
            nativeBinding.nativeAdMedia.setVisibility(View.GONE);
        }

        nativeBinding.nativeAdView.setIconView(nativeBinding.nativeAdIcon);
        NativeAd.Image icon = nativeAd.getIcon();
        if (icon != null) {
            Drawable iconDrawable = icon.getDrawable();
            if (iconDrawable != null) {
                nativeBinding.nativeAdIcon.setImageDrawable(iconDrawable);
                nativeBinding.nativeAdIcon.setVisibility(View.VISIBLE);
            } else {
                nativeBinding.nativeAdIcon.setVisibility(View.GONE);
            }
        } else {
            nativeBinding.nativeAdIcon.setVisibility(View.GONE);
        }

        nativeBinding.nativeAdView.setBodyView(nativeBinding.nativeAdBody);
        if (nativeAd.getBody() != null) {
            nativeBinding.nativeAdBody.setText(nativeAd.getBody());
            nativeBinding.nativeAdBody.setVisibility(View.VISIBLE);
        } else {
            nativeBinding.nativeAdBody.setVisibility(View.GONE);
        }

        nativeBinding.nativeAdView.setCallToActionView(nativeBinding.nativeAdCallToAction);
        if (nativeAd.getCallToAction() != null) {
            nativeBinding.nativeAdCallToAction.setText(nativeAd.getCallToAction());
            nativeBinding.nativeAdCallToAction.setVisibility(View.VISIBLE);
        } else {
            nativeBinding.nativeAdCallToAction.setVisibility(View.GONE);
        }

        nativeBinding.nativeAdView.setAdvertiserView(nativeBinding.nativeAdAdvertiser);
        if (nativeAd.getAdvertiser() != null) {
            nativeBinding.nativeAdAdvertiser.setText(nativeAd.getAdvertiser());
            nativeBinding.nativeAdAdvertiser.setVisibility(View.VISIBLE);
        } else {
            nativeBinding.nativeAdAdvertiser.setVisibility(View.GONE);
        }

        nativeBinding.nativeAdView.setNativeAd(nativeAd);

        container.removeAllViews();
        container.addView(nativeBinding.getRoot());
        container.setVisibility(View.VISIBLE);
        return true;
    }

    private boolean populateFanExitAd(ViewGroup container) {
        if (activity == null || fanRectangleView == null) {
            return false;
        }
        ViewParent parent = fanRectangleView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(fanRectangleView);
        }
        container.removeAllViews();
        container.addView(fanRectangleView);
        container.setVisibility(View.VISIBLE);
        return true;
    }

    private void hideAdViews() {
        if (adMobBannerView != null) {
            adMobBannerView.setVisibility(View.GONE);
            adMobBannerView.destroy();
            adMobBannerView = null;
        }
        destroyFanBanner();
        destroyFanInterstitial();
        destroyAdMobNativeAd();
        destroyFanRectangle();
        dismissExitDialog();
    }

    private void destroyFanBanner() {
        if (fanBannerView != null) {
            fanBannerView.destroy();
            fanBannerView = null;
        }
    }

    private void destroyFanRectangle() {
        if (fanRectangleView != null) {
            fanRectangleView.destroy();
            fanRectangleView = null;
        }
    }

    private void destroyFanInterstitial() {
        if (fanInterstitial != null) {
            fanInterstitial.destroy();
            fanInterstitial = null;
        }
    }

    private void destroyAdMobNativeAd() {
        if (adMobNativeAd != null) {
            adMobNativeAd.destroy();
            adMobNativeAd = null;
        }
    }

    private boolean readAdsRemovedPreference() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ADS_REMOVED, false);
    }

    private void writeAdsRemovedPreference(boolean removed) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply();
    }

    private void notifyAdsStatusChanged() {
        if (listener != null) {
            listener.onAdsStatusChanged(adsRemoved);
        }
    }

    private void dismissExitDialog() {
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
        exitDialog = null;
    }

    public void onDestroy() {
        if (adMobBannerView != null) {
            adMobBannerView.destroy();
        }
        destroyFanBanner();
        destroyFanInterstitial();
        destroyAdMobNativeAd();
        destroyFanRectangle();
        dismissExitDialog();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}

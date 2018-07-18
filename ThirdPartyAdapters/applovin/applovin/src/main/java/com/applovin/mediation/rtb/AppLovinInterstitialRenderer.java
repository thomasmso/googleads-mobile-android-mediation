package com.applovin.mediation.rtb;

import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.AppLovinUtils;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.mediation.rtb.AdRenderingCallback;
import com.google.android.gms.ads.mediation.rtb.InterstitialAd;
import com.google.android.gms.ads.mediation.rtb.InterstitialEventListener;
import com.google.android.gms.ads.mediation.rtb.RtbAdConfiguration;

/**
 * Created by Thomas So on July 17 2018
 */
public final class AppLovinInterstitialRenderer
        implements InterstitialAd, AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener
{
    private static final String TAG = "AppLovinInterRenderer";

    /**
     * Data used to render an RTB interstitial ad.
     */
    private final RtbAdConfiguration adConfiguration;

    /**
     * Callback object to notify the Google Mobile Ads SDK if ad rendering succeeded or failed.
     */
    private final AdRenderingCallback<InterstitialAd, InterstitialEventListener> callback;

    /**
     * Listener object to notify the Google Mobile Ads SDK of interstitial presentation events.
     */
    private InterstitialEventListener listener;


    private final AppLovinSdk sdk;
    private       AppLovinAd  ad;

    public AppLovinInterstitialRenderer(RtbAdConfiguration adConfiguration, AdRenderingCallback<InterstitialAd, InterstitialEventListener> callback)
    {
        this.adConfiguration = adConfiguration;
        this.callback = callback;

        this.sdk = AppLovinUtils.retrieveSdk( adConfiguration.serverParameters, adConfiguration.context );
    }

    public void loadAd()
    {
        sdk.getAdService().loadNextAdForAdToken( adConfiguration.bidResponse, this );
    }

    @Override
    public void showAd()
    {
        // Update mute state
        boolean muted = AppLovinUtils.shouldMuteAudio( adConfiguration.mediationExtras );
        sdk.getSettings().setMuted( muted );

        final AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create( sdk, adConfiguration.context );
        interstitialAd.setAdDisplayListener( this );
        interstitialAd.setAdClickListener( this );
        interstitialAd.setAdVideoPlaybackListener( this );
        interstitialAd.showAndRender( ad );
    }

    //region AppLovin Listeners
    @Override
    public void adReceived(AppLovinAd ad)
    {
        Log.d( TAG, "Interstitial did load ad: " + ad.getAdIdNumber() );

        this.ad = ad;

        listener = callback.onSuccess( AppLovinInterstitialRenderer.this );
    }

    @Override
    public void failedToReceiveAd(int code)
    {
        Log.e( TAG, "Failed to load interstitial ad with error: " + code );

        int admobErrorCode = AppLovinUtils.toAdMobErrorCode( code );
        callback.onFailure( Integer.toString( admobErrorCode ) );
    }

    @Override
    public void adDisplayed(AppLovinAd ad)
    {
        Log.d( TAG, "Interstitial dismissed" );
        listener.reportAdImpression();
    }

    @Override
    public void adHidden(AppLovinAd ad)
    {
        Log.d( TAG, "Interstitial hidden" );
        listener.onAdClosed();
    }

    @Override
    public void adClicked(AppLovinAd ad)
    {
        Log.d( TAG, "Interstitial clicked" );
        listener.reportAdClicked();
        listener.onAdLeftApplication();
    }

    @Override
    public void videoPlaybackBegan(AppLovinAd ad)
    {
        Log.d( TAG, "Interstitial video playback began" );
    }

    @Override
    public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched)
    {
        Log.d( TAG, "Interstitial video playback ended at playback percent: " + percentViewed + "%" );
    }
    //endregion
}

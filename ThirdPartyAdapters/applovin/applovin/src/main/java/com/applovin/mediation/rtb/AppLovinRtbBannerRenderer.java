package com.applovin.mediation.rtb;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.applovin.adview.AppLovinAdView;
import com.applovin.adview.AppLovinAdViewDisplayErrorCode;
import com.applovin.adview.AppLovinAdViewEventListener;
import com.applovin.mediation.AppLovinUtils;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.rtb.AdRenderingCallback;
import com.google.android.gms.ads.mediation.rtb.BannerAd;
import com.google.android.gms.ads.mediation.rtb.BannerEventListener;
import com.google.android.gms.ads.mediation.rtb.RtbAdConfiguration;

/**
 * Created by Thomas So on July 17 2018
 */
public final class AppLovinRtbBannerRenderer
        implements BannerAd, AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdViewEventListener
{
    private static final String TAG = "AppLovinRtbBannerRenderer";

    /**
     * Data used to render an RTB banner ad.
     */
    private final RtbAdConfiguration adConfiguration;

    /**
     * Callback object to notify the Google Mobile Ads SDK if ad rendering succeeded or failed.
     */
    private final AdRenderingCallback<BannerAd, BannerEventListener> callback;

    /**
     * Listener object to notify the Google Mobile Ads SDK of banner presentation events.
     */
    private BannerEventListener listener;

    private final AppLovinSdk    sdk;
    private final AppLovinAdSize adSize;
    private       AppLovinAdView adView;

    public AppLovinRtbBannerRenderer(RtbAdConfiguration adConfiguration,
                                     AdSize adSize,
                                     AdRenderingCallback<BannerAd, BannerEventListener> callback)
    {
        this.adConfiguration = adConfiguration;
        this.callback = callback;

        // Convert requested size to AppLovin Ad Size.
        this.adSize = AppLovinUtils.appLovinAdSizeFromAdMobAdSize( adSize );
        this.sdk = AppLovinUtils.retrieveSdk( adConfiguration.serverParameters, adConfiguration.context );
    }

    public void loadAd()
    {
        if ( adSize != null )
        {
            // Create adview object
            adView = new AppLovinAdView( sdk, adSize, adConfiguration.context );
            adView.setAdDisplayListener( this );
            adView.setAdClickListener( this );
            adView.setAdViewEventListener( this );

            // Load ad!
            sdk.getAdService().loadNextAdForAdToken( adConfiguration.bidResponse, this );
        }
        else
        {
            callback.onFailure( "Failed to request banner with unsupported size" );
        }
    }

    @NonNull
    @Override
    public View getView()
    {
        return adView;
    }

    //region AppLovin Listeners
    @Override
    public void adReceived(AppLovinAd ad)
    {
        Log.d( TAG, "Banner did load ad: " + ad.getAdIdNumber() );

        listener = callback.onSuccess( AppLovinRtbBannerRenderer.this );

        adView.renderAd( ad );
    }

    @Override
    public void failedToReceiveAd(int code)
    {
        Log.e( TAG, "Failed to load banner ad with error: " + code );

        int admobErrorCode = AppLovinUtils.toAdMobErrorCode( code );
        callback.onFailure( Integer.toString( admobErrorCode ) );
    }

    @Override
    public void adDisplayed(AppLovinAd ad)
    {
        Log.d( TAG, "Banner displayed" );
        listener.reportAdImpression();
    }

    @Override
    public void adHidden(AppLovinAd ad)
    {
        Log.d( TAG, "Banner hidden" );
    }

    @Override
    public void adClicked(AppLovinAd ad)
    {
        Log.d( TAG, "Banner clicked" );
        listener.reportAdClicked();
    }

    @Override
    public void adOpenedFullscreen(AppLovinAd ad, AppLovinAdView adView)
    {
        Log.d( TAG, "Banner opened fullscreen" );
        listener.onAdOpened();
    }

    @Override
    public void adClosedFullscreen(AppLovinAd ad, AppLovinAdView adView)
    {
        Log.d( TAG, "Banner closed fullscreen" );
        listener.onAdClosed();
    }

    @Override
    public void adLeftApplication(AppLovinAd ad, AppLovinAdView adView)
    {
        Log.d( TAG, "Banner left application" );
        listener.onAdLeftApplication();
    }

    @Override
    public void adFailedToDisplay(AppLovinAd ad, AppLovinAdView adView, AppLovinAdViewDisplayErrorCode code)
    {
        Log.e( TAG, "Banner failed to display: " + code );
    }
    //endregion
}

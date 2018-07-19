package com.applovin.mediation.rtb;

import android.util.Log;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.mediation.AppLovinRewardItem;
import com.applovin.mediation.AppLovinUtils;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.mediation.rtb.AdRenderingCallback;
import com.google.android.gms.ads.mediation.rtb.RewardedAd;
import com.google.android.gms.ads.mediation.rtb.RewardedEventListener;
import com.google.android.gms.ads.mediation.rtb.RtbAdConfiguration;

import java.util.Map;

/**
 * Created by Thomas So on July 17 2018
 */
public final class AppLovinRtbRewardedRenderer
        implements RewardedAd, AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener, AppLovinAdRewardListener
{
    private static final String TAG = "AppLovinRtbRewardedRenderer";

    /**
     * Data used to render an RTB rewarded ad.
     */
    private RtbAdConfiguration adConfiguration;

    /**
     * Callback object to notify the Google Mobile Ads SDK if ad rendering succeeded or failed.
     */
    private final AdRenderingCallback<RewardedAd, RewardedEventListener> callback;

    /**
     * Listener object to notify the Google Mobile Ads SDK of rewarded presentation events.
     */
    private RewardedEventListener listener;

    private final AppLovinSdk                      sdk;
    private       AppLovinIncentivizedInterstitial incentivizedInterstitial;
    private       AppLovinAd                       ad;
    private       boolean                          fullyWatched;
    private       AppLovinRewardItem               rewardItem;

    public AppLovinRtbRewardedRenderer(RtbAdConfiguration adConfiguration, AdRenderingCallback<RewardedAd, RewardedEventListener> callback)
    {
        this.adConfiguration = adConfiguration;
        this.callback = callback;

        this.sdk = AppLovinUtils.retrieveSdk( adConfiguration.serverParameters, adConfiguration.context );
    }

    public void loadAd()
    {
        // Create rewarded video object
        incentivizedInterstitial = AppLovinIncentivizedInterstitial.create( sdk );

        // Load ad!
        sdk.getAdService().loadNextAdForAdToken( adConfiguration.bidResponse, this );
    }

    @Override
    public void showAd()
    {
        // Update mute state
        boolean muted = AppLovinUtils.shouldMuteAudio( adConfiguration.mediationExtras );
        sdk.getSettings().setMuted( muted );

        incentivizedInterstitial.show( ad, adConfiguration.context, this, this, this, this );
    }

    //region AppLovin Listeners
    @Override
    public void adReceived(AppLovinAd ad)
    {
        Log.d( TAG, "Rewarded video did load ad: " + ad.getAdIdNumber() );

        this.ad = ad;

        listener = callback.onSuccess( AppLovinRtbRewardedRenderer.this );
    }

    @Override
    public void failedToReceiveAd(int code)
    {
        Log.e( TAG, "Failed to load rewarded video with error: " + code );

        int admobErrorCode = AppLovinUtils.toAdMobErrorCode( code );
        callback.onFailure( Integer.toString( admobErrorCode ) );
    }

    @Override
    public void adDisplayed(AppLovinAd ad)
    {
        Log.d( TAG, "Rewarded video displayed" );
        listener.reportAdImpression();
    }

    @Override
    public void adHidden(AppLovinAd ad)
    {
        Log.d( TAG, "Rewarded video hidden" );

        if ( fullyWatched && rewardItem != null )
        {
            listener.onRewarded( rewardItem );
        }

        listener.onAdClosed();

        // Clear states in the case this listener gets re-used in the future.
        fullyWatched = false;
        rewardItem = null;
    }

    @Override
    public void adClicked(AppLovinAd ad)
    {
        Log.d( TAG, "Rewarded video clicked" );
        listener.reportAdClicked();
        listener.onAdLeftApplication();
    }

    @Override
    public void videoPlaybackBegan(AppLovinAd ad)
    {
        Log.d( TAG, "Rewarded video playback began" );
        listener.onVideoStarted();
    }

    @Override
    public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched)
    {
        Log.d( TAG, "Rewarded video playback ended at playback percent: " + percentViewed + "%" );
        listener.onVideoCompleted();
    }
    //endregion

    //region AppLovin Reward Listener
    @Override
    public void userOverQuota(AppLovinAd ad, Map<String, String> response)
    {
        Log.e( TAG, "Rewarded video validation request for ad did exceed quota with response: " + response );
    }

    @Override
    public void validationRequestFailed(AppLovinAd ad, int code)
    {
        Log.e( TAG, "Rewarded video validation request for ad failed with error code: " + code );
    }

    @Override
    public void userRewardRejected(AppLovinAd ad, Map<String, String> response)
    {
        Log.e( TAG, "Rewarded video validation request was rejected with response: " + response );
    }

    @Override
    public void userDeclinedToViewAd(AppLovinAd ad)
    {
        Log.e( TAG, "User declined to view rewarded video" );
    }

    @Override
    public void userRewardVerified(AppLovinAd ad, Map<String, String> response)
    {
        final String currency = response.get( "currency" );
        final String amountStr = response.get( "amount" );

        // AppLovin returns amount as double.
        final int amount = (int) Double.parseDouble( amountStr );

        Log.d( TAG, "Rewarded " + amount + " " + currency );
        rewardItem = new AppLovinRewardItem( amount, currency );
    }
    //endregion
}

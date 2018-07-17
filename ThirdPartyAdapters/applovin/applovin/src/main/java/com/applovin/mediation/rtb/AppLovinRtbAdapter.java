package com.applovin.mediation.rtb;

import android.app.Application;
import android.util.Log;

import com.applovin.mediation.AppLovinUtils;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.ads.mediation.sample.adapter.BuildConfig;
import com.google.ads.mediation.sample.adapter.rtb.SampleRtbBannerRenderer;
import com.google.ads.mediation.sample.adapter.rtb.SampleRtbInterstitialRenderer;
import com.google.ads.mediation.sample.adapter.rtb.SampleRtbRewardedRenderer;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.rtb.AdRenderingCallback;
import com.google.android.gms.ads.mediation.rtb.BannerAd;
import com.google.android.gms.ads.mediation.rtb.BannerEventListener;
import com.google.android.gms.ads.mediation.rtb.InterstitialAd;
import com.google.android.gms.ads.mediation.rtb.InterstitialEventListener;
import com.google.android.gms.ads.mediation.rtb.RewardedAd;
import com.google.android.gms.ads.mediation.rtb.RewardedEventListener;
import com.google.android.gms.ads.mediation.rtb.RtbAdConfiguration;
import com.google.android.gms.ads.mediation.rtb.RtbAdapter;
import com.google.android.gms.ads.mediation.rtb.RtbConfiguration;
import com.google.android.gms.ads.mediation.rtb.RtbSignalData;
import com.google.android.gms.ads.mediation.rtb.SignalCallbacks;
import com.google.android.gms.ads.mediation.rtb.VersionInfo;

import java.util.List;

/**
 * Created by Thomas So on July 17 2018
 */
public class AppLovinRtbAdapter
        extends RtbAdapter
{
    private static final String TAG = AppLovinRtbAdapter.class.getSimpleName();

    // @Override
    // Why is this not in the base adapter
    public void setUp()
    {
        AppLovinSdk.getInstance( new Application() ).initializeSdk();
    }

    @Override
    public void initialize()
    {
        AppLovinSdk.getInstance( new Application() ).initializeSdk();
    }

    @Override
    public void updateConfiguration(final List<RtbConfiguration> list)
    {
        super.updateConfiguration( list );
    }

    @Override
    public VersionInfo getSdkVersion()
    {
        String versionString = AppLovinSdk.VERSION;
        String splits[] = versionString.split( "\\." );
        int major = Integer.parseInt( splits[0] );
        int minor = Integer.parseInt( splits[1] );
        int patch = Integer.parseInt( splits[2] );

        return new VersionInfo( major, minor, patch );
    }

    @Override
    public VersionInfo getAdapterVersion()
    {
        String versionString = BuildConfig.VERSION_NAME;
        String splits[] = versionString.split( "\\." );
        int major = Integer.parseInt( splits[0] );
        int minor = Integer.parseInt( splits[1] );
        // Adapter versions have 2 patch versions. Multiply the first patch by 100.
        int micro = Integer.parseInt( splits[2] ) * 100 + Integer.parseInt( splits[3] );

        return new VersionInfo( major, minor, micro );
    }

    @Override
    public void collectSignals(final RtbSignalData rtbSignalData, final SignalCallbacks signalCallbacks)
    {
        // TODO: I hope that we do not use the SDK Key and Context from here to initialize SDK / get bid token with...
        // Check if the publisher provided extra parameters
        if ( rtbSignalData.extras != null )
        {
            Log.i( TAG, "Extras that adapter gets: " + rtbSignalData.extras );
        }


        AppLovinSdk sdk = AppLovinUtils.retrieveSdk( rtbSignalData.extras, rtbSignalData.context );
        String bidToken = sdk.getAdService().getBidToken();

        Log.i( TAG, "Generated bid token: " + bidToken );

        signalCallbacks.onSuccess( bidToken );
    }

    @Override
    public void renderBannerAd(RtbAdConfiguration adConfiguration, AdSize adSize, AdRenderingCallback<BannerAd, BannerEventListener> callback)
    {
        AppLovinBannerRenderer bannerRenderer = new AppLovinBannerRenderer( adConfiguration, adSize, callback );
        bannerRenderer.render();
    }

    @Override
    public void renderInterstitialAd(
            RtbAdConfiguration adConfiguration,
            AdRenderingCallback<InterstitialAd, InterstitialEventListener> callback)
    {
        Log.e( "Test", "renderInterstitial" );
        Log.e( "Test", "Bid Response: " + adConfiguration.bidResponse );
        if ( adConfiguration.mediationExtras != null )
        {
            Log.e( "Test", "Extras that renderInterstitial gets: " + adConfiguration.mediationExtras.toString() );
        }
        SampleRtbInterstitialRenderer interstitialRenderer =
                new SampleRtbInterstitialRenderer( adConfiguration, callback );
        interstitialRenderer.render();
    }

    @Override
    public void renderRewardedAd(
            RtbAdConfiguration adConfiguration,
            AdRenderingCallback<RewardedAd, RewardedEventListener> callback)
    {
        Log.e( "Test", "renderRewarded" );
        Log.e( "Test", "Bid Response: " + adConfiguration.bidResponse );
        if ( adConfiguration.mediationExtras != null )
        {
            Log.e( "Test", "Extras that renderRewarded gets: " + adConfiguration.mediationExtras.toString() );
        }
        SampleRtbRewardedRenderer rewardedRenderer =
                new SampleRtbRewardedRenderer( adConfiguration, callback );
        rewardedRenderer.render();
    }
}

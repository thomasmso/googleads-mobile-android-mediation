package com.applovin.mediation;

import com.google.android.gms.ads.reward.RewardItem;

/**
 * Created by Thomas So on July 17 2018
 */
public final class AppLovinRewardItem
        implements RewardItem
{
    private final int    mAmount;
    private final String mType;

    public AppLovinRewardItem(int amount, String type)
    {
        mAmount = amount;
        mType = type;
    }

    @Override
    public String getType()
    {
        return mType;
    }

    @Override
    public int getAmount()
    {
        return mAmount;
    }
}

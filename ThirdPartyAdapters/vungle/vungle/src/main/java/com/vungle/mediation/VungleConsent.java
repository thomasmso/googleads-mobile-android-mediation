package com.vungle.mediation;

import com.vungle.warren.Vungle;

/**
 * A public static class used to set Vungle Consent Status.
 */

public class VungleConsent {
    private static Vungle.Consent sCurrentVungleConsent = null;

    public static void updateConsentStatus(Vungle.Consent consentStatus) {
        sCurrentVungleConsent = consentStatus;
        if (Vungle.isInitialized() && sCurrentVungleConsent != null) {
            Vungle.updateConsentStatus(consentStatus);
        }
    }

    public static Vungle.Consent getCurrentVungleConsent() {
        return sCurrentVungleConsent;
    }
}

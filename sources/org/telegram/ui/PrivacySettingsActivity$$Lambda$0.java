package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class PrivacySettingsActivity$$Lambda$0 implements RequestDelegate {
    static final RequestDelegate $instance = new PrivacySettingsActivity$$Lambda$0();

    private PrivacySettingsActivity$$Lambda$0() {
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        PrivacySettingsActivity.lambda$onFragmentDestroy$0$PrivacySettingsActivity(tLObject, tLRPC$TL_error);
    }
}

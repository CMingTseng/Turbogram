package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class TwoStepVerificationActivity$$Lambda$8 implements RequestDelegate {
    static final RequestDelegate $instance = new TwoStepVerificationActivity$$Lambda$8();

    private TwoStepVerificationActivity$$Lambda$8() {
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        TwoStepVerificationActivity.lambda$checkSecretValues$21$TwoStepVerificationActivity(tLObject, tLRPC$TL_error);
    }
}

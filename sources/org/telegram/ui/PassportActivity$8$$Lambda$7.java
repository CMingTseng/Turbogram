package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.PassportActivity.C19378;

final /* synthetic */ class PassportActivity$8$$Lambda$7 implements RequestDelegate {
    private final C19378 arg$1;

    PassportActivity$8$$Lambda$7(C19378 c19378) {
        this.arg$1 = c19378;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$null$13$PassportActivity$8(tLObject, tLRPC$TL_error);
    }
}

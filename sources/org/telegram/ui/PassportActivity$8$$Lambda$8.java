package org.telegram.ui;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.PassportActivity.C19378;

final /* synthetic */ class PassportActivity$8$$Lambda$8 implements Runnable {
    private final C19378 arg$1;
    private final TLObject arg$2;
    private final TLRPC$TL_error arg$3;

    PassportActivity$8$$Lambda$8(C19378 c19378, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1 = c19378;
        this.arg$2 = tLObject;
        this.arg$3 = tLRPC$TL_error;
    }

    public void run() {
        this.arg$1.lambda$null$12$PassportActivity$8(this.arg$2, this.arg$3);
    }
}

package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_secureRequiredType;
import org.telegram.ui.PassportActivity.19.C19271;

final /* synthetic */ class PassportActivity$19$1$$Lambda$1 implements RequestDelegate {
    private final C19271 arg$1;
    private final String arg$2;
    private final TLRPC$TL_secureRequiredType arg$3;
    private final PassportActivityDelegate arg$4;
    private final ErrorRunnable arg$5;

    PassportActivity$19$1$$Lambda$1(C19271 c19271, String str, TLRPC$TL_secureRequiredType tLRPC$TL_secureRequiredType, PassportActivityDelegate passportActivityDelegate, ErrorRunnable errorRunnable) {
        this.arg$1 = c19271;
        this.arg$2 = str;
        this.arg$3 = tLRPC$TL_secureRequiredType;
        this.arg$4 = passportActivityDelegate;
        this.arg$5 = errorRunnable;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$run$2$PassportActivity$19$1(this.arg$2, this.arg$3, this.arg$4, this.arg$5, tLObject, tLRPC$TL_error);
    }
}

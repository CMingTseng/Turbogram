package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class LocaleController$$Lambda$5 implements RequestDelegate {
    private final LocaleController arg$1;
    private final int arg$2;

    LocaleController$$Lambda$5(LocaleController localeController, int i) {
        this.arg$1 = localeController;
        this.arg$2 = i;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$applyRemoteLanguage$7$LocaleController(this.arg$2, tLObject, tLRPC$TL_error);
    }
}

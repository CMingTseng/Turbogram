package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_account_verifyEmail;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.PassportActivity.C19323;

final /* synthetic */ class PassportActivity$3$$Lambda$5 implements RequestDelegate {
    private final C19323 arg$1;
    private final Runnable arg$2;
    private final ErrorRunnable arg$3;
    private final TLRPC$TL_account_verifyEmail arg$4;

    PassportActivity$3$$Lambda$5(C19323 c19323, Runnable runnable, ErrorRunnable errorRunnable, TLRPC$TL_account_verifyEmail tLRPC$TL_account_verifyEmail) {
        this.arg$1 = c19323;
        this.arg$2 = runnable;
        this.arg$3 = errorRunnable;
        this.arg$4 = tLRPC$TL_account_verifyEmail;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$onItemClick$6$PassportActivity$3(this.arg$2, this.arg$3, this.arg$4, tLObject, tLRPC$TL_error);
    }
}

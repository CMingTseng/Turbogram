package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.LoginActivity.LoginActivityPasswordView;

final /* synthetic */ class LoginActivity$LoginActivityPasswordView$$Lambda$12 implements RequestDelegate {
    private final LoginActivityPasswordView arg$1;

    LoginActivity$LoginActivityPasswordView$$Lambda$12(LoginActivityPasswordView loginActivityPasswordView) {
        this.arg$1 = loginActivityPasswordView;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$null$3$LoginActivity$LoginActivityPasswordView(tLObject, tLRPC$TL_error);
    }
}

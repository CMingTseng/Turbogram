package org.telegram.ui;

import android.os.Bundle;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_auth_sendCode;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.LoginActivity.PhoneView;

final /* synthetic */ class LoginActivity$PhoneView$$Lambda$5 implements RequestDelegate {
    private final PhoneView arg$1;
    private final Bundle arg$2;
    private final TLRPC$TL_auth_sendCode arg$3;

    LoginActivity$PhoneView$$Lambda$5(PhoneView phoneView, Bundle bundle, TLRPC$TL_auth_sendCode tLRPC$TL_auth_sendCode) {
        this.arg$1 = phoneView;
        this.arg$2 = bundle;
        this.arg$3 = tLRPC$TL_auth_sendCode;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$onNextPressed$7$LoginActivity$PhoneView(this.arg$2, this.arg$3, tLObject, tLRPC$TL_error);
    }
}

package org.telegram.ui;

import android.os.Bundle;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_auth_sendCode;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.LoginActivity.PhoneView;

final /* synthetic */ class LoginActivity$PhoneView$$Lambda$7 implements Runnable {
    private final PhoneView arg$1;
    private final TLRPC$TL_error arg$2;
    private final Bundle arg$3;
    private final TLObject arg$4;
    private final TLRPC$TL_auth_sendCode arg$5;

    LoginActivity$PhoneView$$Lambda$7(PhoneView phoneView, TLRPC$TL_error tLRPC$TL_error, Bundle bundle, TLObject tLObject, TLRPC$TL_auth_sendCode tLRPC$TL_auth_sendCode) {
        this.arg$1 = phoneView;
        this.arg$2 = tLRPC$TL_error;
        this.arg$3 = bundle;
        this.arg$4 = tLObject;
        this.arg$5 = tLRPC$TL_auth_sendCode;
    }

    public void run() {
        this.arg$1.lambda$null$6$LoginActivity$PhoneView(this.arg$2, this.arg$3, this.arg$4, this.arg$5);
    }
}

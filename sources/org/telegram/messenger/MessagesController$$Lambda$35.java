package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$35 implements RequestDelegate {
    static final RequestDelegate $instance = new MessagesController$$Lambda$35();

    private MessagesController$$Lambda$35() {
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        MessagesController.lambda$deleteUserPhoto$49$MessagesController(tLObject, tLRPC$TL_error);
    }
}

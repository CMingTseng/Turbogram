package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$65 implements RequestDelegate {
    private final MessagesController arg$1;

    MessagesController$$Lambda$65(MessagesController messagesController) {
        this.arg$1 = messagesController;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$loadHintDialogs$94$MessagesController(tLObject, tLRPC$TL_error);
    }
}

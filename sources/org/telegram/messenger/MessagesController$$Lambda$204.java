package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$204 implements RequestDelegate {
    private final MessagesController arg$1;
    private final long arg$2;

    MessagesController$$Lambda$204(MessagesController messagesController, long j) {
        this.arg$1 = messagesController;
        this.arg$2 = j;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$null$110$MessagesController(this.arg$2, tLObject, tLRPC$TL_error);
    }
}

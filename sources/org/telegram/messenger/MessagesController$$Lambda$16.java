package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$16 implements RequestDelegate {
    static final RequestDelegate $instance = new MessagesController$$Lambda$16();

    private MessagesController$$Lambda$16() {
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        MessagesController.lambda$hideReportSpam$21$MessagesController(tLObject, tLRPC$TL_error);
    }
}

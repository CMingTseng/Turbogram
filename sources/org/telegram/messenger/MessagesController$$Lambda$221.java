package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_help_proxyDataPromo;

final /* synthetic */ class MessagesController$$Lambda$221 implements RequestDelegate {
    private final MessagesController arg$1;
    private final TLRPC$TL_help_proxyDataPromo arg$2;
    private final long arg$3;

    MessagesController$$Lambda$221(MessagesController messagesController, TLRPC$TL_help_proxyDataPromo tLRPC$TL_help_proxyDataPromo, long j) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_help_proxyDataPromo;
        this.arg$3 = j;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$null$76$MessagesController(this.arg$2, this.arg$3, tLObject, tLRPC$TL_error);
    }
}

package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$updates_Difference;

final /* synthetic */ class MessagesController$$Lambda$166 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$updates_Difference arg$2;
    private final int arg$3;
    private final int arg$4;

    MessagesController$$Lambda$166(MessagesController messagesController, TLRPC$updates_Difference tLRPC$updates_Difference, int i, int i2) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$updates_Difference;
        this.arg$3 = i;
        this.arg$4 = i2;
    }

    public void run() {
        this.arg$1.lambda$null$188$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}

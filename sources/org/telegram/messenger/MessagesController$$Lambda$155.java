package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$TL_updateUserBlocked;

final /* synthetic */ class MessagesController$$Lambda$155 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$TL_updateUserBlocked arg$2;

    MessagesController$$Lambda$155(MessagesController messagesController, TLRPC$TL_updateUserBlocked tLRPC$TL_updateUserBlocked) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_updateUserBlocked;
    }

    public void run() {
        this.arg$1.lambda$null$221$MessagesController(this.arg$2);
    }
}

package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$TL_messages_chatFull;

final /* synthetic */ class MessagesController$$Lambda$240 implements Runnable {
    private final MessagesController arg$1;
    private final int arg$2;
    private final TLRPC$TL_messages_chatFull arg$3;
    private final int arg$4;

    MessagesController$$Lambda$240(MessagesController messagesController, int i, TLRPC$TL_messages_chatFull tLRPC$TL_messages_chatFull, int i2) {
        this.arg$1 = messagesController;
        this.arg$2 = i;
        this.arg$3 = tLRPC$TL_messages_chatFull;
        this.arg$4 = i2;
    }

    public void run() {
        this.arg$1.lambda$null$13$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}

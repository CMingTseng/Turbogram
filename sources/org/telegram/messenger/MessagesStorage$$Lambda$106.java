package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$ChatFull;

final /* synthetic */ class MessagesStorage$$Lambda$106 implements Runnable {
    private final MessagesStorage arg$1;
    private final TLRPC$ChatFull arg$2;

    MessagesStorage$$Lambda$106(MessagesStorage messagesStorage, TLRPC$ChatFull tLRPC$ChatFull) {
        this.arg$1 = messagesStorage;
        this.arg$2 = tLRPC$ChatFull;
    }

    public void run() {
        this.arg$1.lambda$null$63$MessagesStorage(this.arg$2);
    }
}

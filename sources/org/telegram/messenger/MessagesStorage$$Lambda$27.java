package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$photos_Photos;

final /* synthetic */ class MessagesStorage$$Lambda$27 implements Runnable {
    private final MessagesStorage arg$1;
    private final int arg$2;
    private final TLRPC$photos_Photos arg$3;

    MessagesStorage$$Lambda$27(MessagesStorage messagesStorage, int i, TLRPC$photos_Photos tLRPC$photos_Photos) {
        this.arg$1 = messagesStorage;
        this.arg$2 = i;
        this.arg$3 = tLRPC$photos_Photos;
    }

    public void run() {
        this.arg$1.lambda$putDialogPhotos$43$MessagesStorage(this.arg$2, this.arg$3);
    }
}

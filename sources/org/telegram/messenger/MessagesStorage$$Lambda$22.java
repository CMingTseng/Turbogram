package org.telegram.messenger;

final /* synthetic */ class MessagesStorage$$Lambda$22 implements Runnable {
    private final MessagesStorage arg$1;
    private final int arg$2;
    private final long arg$3;

    MessagesStorage$$Lambda$22(MessagesStorage messagesStorage, int i, long j) {
        this.arg$1 = messagesStorage;
        this.arg$2 = i;
        this.arg$3 = j;
    }

    public void run() {
        this.arg$1.lambda$deleteDialog$36$MessagesStorage(this.arg$2, this.arg$3);
    }
}

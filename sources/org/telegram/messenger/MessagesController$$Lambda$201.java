package org.telegram.messenger;

import android.util.LongSparseArray;
import org.telegram.tgnet.TLRPC$messages_Dialogs;

final /* synthetic */ class MessagesController$$Lambda$201 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$messages_Dialogs arg$2;
    private final LongSparseArray arg$3;
    private final LongSparseArray arg$4;
    private final LongSparseArray arg$5;

    MessagesController$$Lambda$201(MessagesController messagesController, TLRPC$messages_Dialogs tLRPC$messages_Dialogs, LongSparseArray longSparseArray, LongSparseArray longSparseArray2, LongSparseArray longSparseArray3) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$messages_Dialogs;
        this.arg$3 = longSparseArray;
        this.arg$4 = longSparseArray2;
        this.arg$5 = longSparseArray3;
    }

    public void run() {
        this.arg$1.lambda$null$116$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5);
    }
}

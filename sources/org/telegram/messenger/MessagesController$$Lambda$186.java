package org.telegram.messenger;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.ActionBar.BaseFragment;

final /* synthetic */ class MessagesController$$Lambda$186 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$TL_error arg$2;
    private final BaseFragment arg$3;
    private final TLObject arg$4;
    private final boolean arg$5;
    private final boolean arg$6;

    MessagesController$$Lambda$186(MessagesController messagesController, TLRPC$TL_error tLRPC$TL_error, BaseFragment baseFragment, TLObject tLObject, boolean z, boolean z2) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_error;
        this.arg$3 = baseFragment;
        this.arg$4 = tLObject;
        this.arg$5 = z;
        this.arg$6 = z2;
    }

    public void run() {
        this.arg$1.lambda$null$159$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6);
    }
}

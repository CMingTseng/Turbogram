package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$TL_channels_editBanned;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.ActionBar.BaseFragment;

final /* synthetic */ class MessagesController$$Lambda$232 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$TL_error arg$2;
    private final BaseFragment arg$3;
    private final TLRPC$TL_channels_editBanned arg$4;
    private final boolean arg$5;

    MessagesController$$Lambda$232(MessagesController messagesController, TLRPC$TL_error tLRPC$TL_error, BaseFragment baseFragment, TLRPC$TL_channels_editBanned tLRPC$TL_channels_editBanned, boolean z) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_error;
        this.arg$3 = baseFragment;
        this.arg$4 = tLRPC$TL_channels_editBanned;
        this.arg$5 = z;
    }

    public void run() {
        this.arg$1.lambda$null$39$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5);
    }
}

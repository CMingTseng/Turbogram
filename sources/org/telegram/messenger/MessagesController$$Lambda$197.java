package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_createChat;
import org.telegram.ui.ActionBar.BaseFragment;

final /* synthetic */ class MessagesController$$Lambda$197 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$TL_error arg$2;
    private final BaseFragment arg$3;
    private final TLRPC$TL_messages_createChat arg$4;

    MessagesController$$Lambda$197(MessagesController messagesController, TLRPC$TL_error tLRPC$TL_error, BaseFragment baseFragment, TLRPC$TL_messages_createChat tLRPC$TL_messages_createChat) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_error;
        this.arg$3 = baseFragment;
        this.arg$4 = tLRPC$TL_messages_createChat;
    }

    public void run() {
        this.arg$1.lambda$null$134$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}

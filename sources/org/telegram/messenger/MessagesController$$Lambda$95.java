package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.ActionBar.BaseFragment;

final /* synthetic */ class MessagesController$$Lambda$95 implements RequestDelegate {
    private final MessagesController arg$1;
    private final BaseFragment arg$2;
    private final TLRPC$TL_channels_inviteToChannel arg$3;

    MessagesController$$Lambda$95(MessagesController messagesController, BaseFragment baseFragment, TLRPC$TL_channels_inviteToChannel tLRPC$TL_channels_inviteToChannel) {
        this.arg$1 = messagesController;
        this.arg$2 = baseFragment;
        this.arg$3 = tLRPC$TL_channels_inviteToChannel;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$addUsersToChannel$145$MessagesController(this.arg$2, this.arg$3, tLObject, tLRPC$TL_error);
    }
}

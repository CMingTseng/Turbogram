package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class ChannelEditInfoActivity$$Lambda$13 implements RequestDelegate {
    private final ChannelEditInfoActivity arg$1;

    ChannelEditInfoActivity$$Lambda$13(ChannelEditInfoActivity channelEditInfoActivity) {
        this.arg$1 = channelEditInfoActivity;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$loadAdminedChannels$21$ChannelEditInfoActivity(tLObject, tLRPC$TL_error);
    }
}

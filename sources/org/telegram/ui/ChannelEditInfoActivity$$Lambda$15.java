package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class ChannelEditInfoActivity$$Lambda$15 implements RequestDelegate {
    private final ChannelEditInfoActivity arg$1;

    ChannelEditInfoActivity$$Lambda$15(ChannelEditInfoActivity channelEditInfoActivity) {
        this.arg$1 = channelEditInfoActivity;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$generateLink$26$ChannelEditInfoActivity(tLObject, tLRPC$TL_error);
    }
}

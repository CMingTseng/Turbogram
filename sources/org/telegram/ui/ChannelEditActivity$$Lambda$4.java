package org.telegram.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import java.util.ArrayList;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$TL_chatChannelParticipant;

final /* synthetic */ class ChannelEditActivity$$Lambda$4 implements OnClickListener {
    private final ChannelEditActivity arg$1;
    private final ArrayList arg$2;
    private final TLRPC$ChannelParticipant arg$3;
    private final int arg$4;
    private final TLRPC$TL_chatChannelParticipant arg$5;

    ChannelEditActivity$$Lambda$4(ChannelEditActivity channelEditActivity, ArrayList arrayList, TLRPC$ChannelParticipant tLRPC$ChannelParticipant, int i, TLRPC$TL_chatChannelParticipant tLRPC$TL_chatChannelParticipant) {
        this.arg$1 = channelEditActivity;
        this.arg$2 = arrayList;
        this.arg$3 = tLRPC$ChannelParticipant;
        this.arg$4 = i;
        this.arg$5 = tLRPC$TL_chatChannelParticipant;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.arg$1.lambda$createMenuForParticipant$6$ChannelEditActivity(this.arg$2, this.arg$3, this.arg$4, this.arg$5, dialogInterface, i);
    }
}

package org.telegram.messenger;

import java.util.ArrayList;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_sendMultiMedia;

final /* synthetic */ class SendMessagesHelper$$Lambda$10 implements RequestDelegate {
    private final SendMessagesHelper arg$1;
    private final ArrayList arg$2;
    private final ArrayList arg$3;
    private final TLRPC$TL_messages_sendMultiMedia arg$4;

    SendMessagesHelper$$Lambda$10(SendMessagesHelper sendMessagesHelper, ArrayList arrayList, ArrayList arrayList2, TLRPC$TL_messages_sendMultiMedia tLRPC$TL_messages_sendMultiMedia) {
        this.arg$1 = sendMessagesHelper;
        this.arg$2 = arrayList;
        this.arg$3 = arrayList2;
        this.arg$4 = tLRPC$TL_messages_sendMultiMedia;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$performSendMessageRequestMulti$28$SendMessagesHelper(this.arg$2, this.arg$3, this.arg$4, tLObject, tLRPC$TL_error);
    }
}

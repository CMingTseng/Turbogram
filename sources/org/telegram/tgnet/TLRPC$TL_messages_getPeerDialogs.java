package org.telegram.tgnet;

import java.util.ArrayList;

public class TLRPC$TL_messages_getPeerDialogs extends TLObject {
    public static int constructor = -462373635;
    public ArrayList<TLRPC$InputDialogPeer> peers = new ArrayList();

    public TLObject deserializeResponse(AbstractSerializedData stream, int constructor, boolean exception) {
        return TLRPC$TL_messages_peerDialogs.TLdeserialize(stream, constructor, exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        stream.writeInt32(481674261);
        int count = this.peers.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            ((TLRPC$InputDialogPeer) this.peers.get(a)).serializeToStream(stream);
        }
    }
}

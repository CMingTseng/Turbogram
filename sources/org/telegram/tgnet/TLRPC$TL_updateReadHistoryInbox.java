package org.telegram.tgnet;

public class TLRPC$TL_updateReadHistoryInbox extends TLRPC$Update {
    public static int constructor = -1721631396;
    public int max_id;
    public TLRPC$Peer peer;
    public int pts;
    public int pts_count;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.peer = TLRPC$Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
        this.max_id = stream.readInt32(exception);
        this.pts = stream.readInt32(exception);
        this.pts_count = stream.readInt32(exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        this.peer.serializeToStream(stream);
        stream.writeInt32(this.max_id);
        stream.writeInt32(this.pts);
        stream.writeInt32(this.pts_count);
    }
}

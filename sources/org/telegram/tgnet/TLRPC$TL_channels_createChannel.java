package org.telegram.tgnet;

public class TLRPC$TL_channels_createChannel extends TLObject {
    public static int constructor = -192332417;
    public String about;
    public boolean broadcast;
    public int flags;
    public boolean megagroup;
    public String title;

    public TLObject deserializeResponse(AbstractSerializedData stream, int constructor, boolean exception) {
        return TLRPC$Updates.TLdeserialize(stream, constructor, exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        this.flags = this.broadcast ? this.flags | 1 : this.flags & -2;
        this.flags = this.megagroup ? this.flags | 2 : this.flags & -3;
        stream.writeInt32(this.flags);
        stream.writeString(this.title);
        stream.writeString(this.about);
    }
}

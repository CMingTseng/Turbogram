package org.telegram.tgnet;

public class TLRPC$TL_textEmail extends TLRPC$RichText {
    public static int constructor = -564523562;
    public TLRPC$RichText text;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.text = TLRPC$RichText.TLdeserialize(stream, stream.readInt32(exception), exception);
        this.email = stream.readString(exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        this.text.serializeToStream(stream);
        stream.writeString(this.email);
    }
}

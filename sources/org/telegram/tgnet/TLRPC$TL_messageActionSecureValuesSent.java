package org.telegram.tgnet;

import java.util.ArrayList;

public class TLRPC$TL_messageActionSecureValuesSent extends TLRPC$MessageAction {
    public static int constructor = -648257196;
    public ArrayList<TLRPC$SecureValueType> types = new ArrayList();

    public void readParams(AbstractSerializedData stream, boolean exception) {
        if (stream.readInt32(exception) == 481674261) {
            int count = stream.readInt32(exception);
            int a = 0;
            while (a < count) {
                TLRPC$SecureValueType object = TLRPC$SecureValueType.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object != null) {
                    this.types.add(object);
                    a++;
                } else {
                    return;
                }
            }
        } else if (exception) {
            throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        stream.writeInt32(481674261);
        int count = this.types.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            ((TLRPC$SecureValueType) this.types.get(a)).serializeToStream(stream);
        }
    }
}

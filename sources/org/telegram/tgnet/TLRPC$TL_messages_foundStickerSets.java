package org.telegram.tgnet;

import java.util.ArrayList;

public class TLRPC$TL_messages_foundStickerSets extends TLRPC$messages_FoundStickerSets {
    public static int constructor = 1359533640;
    public int hash;
    public ArrayList<TLRPC$StickerSetCovered> sets = new ArrayList();

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.hash = stream.readInt32(exception);
        if (stream.readInt32(exception) == 481674261) {
            int count = stream.readInt32(exception);
            int a = 0;
            while (a < count) {
                TLRPC$StickerSetCovered object = TLRPC$StickerSetCovered.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object != null) {
                    this.sets.add(object);
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
        stream.writeInt32(this.hash);
        stream.writeInt32(481674261);
        int count = this.sets.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            ((TLRPC$StickerSetCovered) this.sets.get(a)).serializeToStream(stream);
        }
    }
}

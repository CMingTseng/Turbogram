package org.telegram.tgnet;

import java.util.ArrayList;
import org.telegram.tgnet.TLRPC.User;

public class TLRPC$TL_help_proxyDataPromo extends TLRPC$help_ProxyData {
    public static int constructor = 737668643;
    public ArrayList<TLRPC$Chat> chats = new ArrayList();
    public int expires;
    public TLRPC$Peer peer;
    public ArrayList<User> users = new ArrayList();

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.expires = stream.readInt32(exception);
        this.peer = TLRPC$Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
        if (stream.readInt32(exception) == 481674261) {
            int count = stream.readInt32(exception);
            int a = 0;
            while (a < count) {
                TLRPC$Chat object = TLRPC$Chat.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object != null) {
                    this.chats.add(object);
                    a++;
                } else {
                    return;
                }
            }
            if (stream.readInt32(exception) == 481674261) {
                count = stream.readInt32(exception);
                a = 0;
                while (a < count) {
                    User object2 = User.TLdeserialize(stream, stream.readInt32(exception), exception);
                    if (object2 != null) {
                        this.users.add(object2);
                        a++;
                    } else {
                        return;
                    }
                }
            } else if (exception) {
                throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
            }
        } else if (exception) {
            throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        int a;
        stream.writeInt32(constructor);
        stream.writeInt32(this.expires);
        this.peer.serializeToStream(stream);
        stream.writeInt32(481674261);
        int count = this.chats.size();
        stream.writeInt32(count);
        for (a = 0; a < count; a++) {
            ((TLRPC$Chat) this.chats.get(a)).serializeToStream(stream);
        }
        stream.writeInt32(481674261);
        count = this.users.size();
        stream.writeInt32(count);
        for (a = 0; a < count; a++) {
            ((User) this.users.get(a)).serializeToStream(stream);
        }
    }
}

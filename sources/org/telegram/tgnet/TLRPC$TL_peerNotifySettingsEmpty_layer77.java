package org.telegram.tgnet;

public class TLRPC$TL_peerNotifySettingsEmpty_layer77 extends TLRPC$PeerNotifySettings {
    public static int constructor = 1889961234;

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
    }
}

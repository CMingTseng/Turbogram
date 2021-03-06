package com.google.android.gms.internal.firebase_abt;

import java.util.Arrays;

final class zzl {
    final int tag;
    final byte[] zzac;

    zzl(int i, byte[] bArr) {
        this.tag = i;
        this.zzac = bArr;
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzl)) {
            return false;
        }
        zzl zzl = (zzl) obj;
        return this.tag == zzl.tag && Arrays.equals(this.zzac, zzl.zzac);
    }

    public final int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.zzac);
    }
}

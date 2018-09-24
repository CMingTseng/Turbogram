package com.google.android.gms.internal.ads;

import com.google.android.gms.ads.internal.gmsg.zzv;
import java.util.Map;

final class zzafu implements zzv<Object> {
    private final /* synthetic */ zzaft zzchv;

    zzafu(zzaft zzaft) {
        this.zzchv = zzaft;
    }

    public final void zza(Object obj, Map<String, String> map) {
        synchronized (this.zzchv.mLock) {
            if (this.zzchv.zzchr.isDone()) {
            } else if (this.zzchv.zzchp.equals(map.get("request_id"))) {
                zzafz zzafz = new zzafz(1, map);
                String type = zzafz.getType();
                String valueOf = String.valueOf(zzafz.zzoh());
                zzane.zzdk(new StringBuilder((String.valueOf(type).length() + 24) + String.valueOf(valueOf).length()).append("Invalid ").append(type).append(" request error: ").append(valueOf).toString());
                this.zzchv.zzchr.set(zzafz);
            }
        }
    }
}

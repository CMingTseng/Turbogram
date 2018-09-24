package com.google.android.gms.internal.ads;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import java.util.concurrent.TimeUnit;

@zzadh
@TargetApi(14)
public final class zzapp {
    private final long zzcyj = TimeUnit.MILLISECONDS.toNanos(((Long) zzkb.zzik().zzd(zznk.zzavh)).longValue());
    private long zzcyk;
    private boolean zzcyl = true;

    zzapp() {
    }

    public final void zza(SurfaceTexture surfaceTexture, zzapf zzapf) {
        if (zzapf != null) {
            long timestamp = surfaceTexture.getTimestamp();
            if (this.zzcyl || Math.abs(timestamp - this.zzcyk) >= this.zzcyj) {
                this.zzcyl = false;
                this.zzcyk = timestamp;
                zzakk.zzcrm.post(new zzapq(this, zzapf));
            }
        }
    }

    public final void zzsw() {
        this.zzcyl = true;
    }
}

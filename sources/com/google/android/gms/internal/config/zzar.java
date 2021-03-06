package com.google.android.gms.internal.config;

import com.google.android.gms.common.util.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

public final class zzar {
    private boolean zzap;
    private int zzaz;
    private long zzbd;
    private Map<String, zzal> zzbe;

    public zzar() {
        this(-1);
    }

    private zzar(int i, long j, Map<String, zzal> map, boolean z) {
        this.zzaz = 0;
        this.zzbd = -1;
        this.zzbe = new HashMap();
        this.zzap = false;
    }

    @VisibleForTesting
    private zzar(long j) {
        this(0, -1, null, false);
    }

    public final int getLastFetchStatus() {
        return this.zzaz;
    }

    public final boolean isDeveloperModeEnabled() {
        return this.zzap;
    }

    public final void zza(String str, zzal zzal) {
        this.zzbe.put(str, zzal);
    }

    public final void zza(Map<String, zzal> map) {
        this.zzbe = map;
    }

    public final void zza(boolean z) {
        this.zzap = z;
    }

    public final void zzc(long j) {
        this.zzbd = j;
    }

    public final void zzc(String str) {
        if (this.zzbe.get(str) != null) {
            this.zzbe.remove(str);
        }
    }

    public final void zzf(int i) {
        this.zzaz = i;
    }

    public final Map<String, zzal> zzr() {
        return this.zzbe;
    }

    public final long zzs() {
        return this.zzbd;
    }
}

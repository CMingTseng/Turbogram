package com.google.android.gms.ads.internal;

import com.google.android.gms.internal.ads.zzajh;
import com.google.android.gms.internal.ads.zzane;
import com.google.android.gms.internal.ads.zzos;
import com.google.android.gms.internal.ads.zzrf;

final class zzbj implements Runnable {
    private final /* synthetic */ zzbc zzaaf;
    private final /* synthetic */ String zzaal;
    private final /* synthetic */ zzajh zzaam;

    zzbj(zzbc zzbc, String str, zzajh zzajh) {
        this.zzaaf = zzbc;
        this.zzaal = str;
        this.zzaam = zzajh;
    }

    public final void run() {
        try {
            ((zzrf) this.zzaaf.zzvw.zzadi.get(this.zzaal)).zzb((zzos) this.zzaam.zzcoj);
        } catch (Throwable e) {
            zzane.zzd("#007 Could not call remote method.", e);
        }
    }
}

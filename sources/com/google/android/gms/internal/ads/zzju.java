package com.google.android.gms.internal.ads;

import android.content.Context;
import android.os.RemoteException;
import com.google.android.gms.dynamic.ObjectWrapper;

final class zzju extends zza<zzks> {
    private final /* synthetic */ Context val$context;
    private final /* synthetic */ zzjn zzarq;
    private final /* synthetic */ String zzarr;
    private final /* synthetic */ zzxn zzars;
    private final /* synthetic */ zzjr zzart;

    zzju(zzjr zzjr, Context context, zzjn zzjn, String str, zzxn zzxn) {
        this.zzart = zzjr;
        this.val$context = context;
        this.zzarq = zzjn;
        this.zzarr = str;
        this.zzars = zzxn;
        super(zzjr);
    }

    public final /* synthetic */ Object zza(zzld zzld) throws RemoteException {
        return zzld.createInterstitialAdManager(ObjectWrapper.wrap(this.val$context), this.zzarq, this.zzarr, this.zzars, 12451000);
    }

    public final /* synthetic */ Object zzib() throws RemoteException {
        zzks zza = this.zzart.zzarj.zza(this.val$context, this.zzarq, this.zzarr, this.zzars, 2);
        if (zza != null) {
            return zza;
        }
        zzjr.zza(this.val$context, "interstitial");
        return new zzmj();
    }
}

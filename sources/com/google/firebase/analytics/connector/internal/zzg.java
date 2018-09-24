package com.google.firebase.analytics.connector.internal;

import android.os.Bundle;
import com.google.android.gms.measurement.AppMeasurement;
import com.google.android.gms.measurement.AppMeasurement.OnEventListener;

final class zzg implements OnEventListener {
    private final /* synthetic */ zzf zzbsx;

    public zzg(zzf zzf) {
        this.zzbsx = zzf;
    }

    public final void onEvent(String str, String str2, Bundle bundle, long j) {
        if (str != null && !str.equals(AppMeasurement.CRASH_ORIGIN) && zzc.zzfp(str2)) {
            Bundle bundle2 = new Bundle();
            bundle2.putString("name", str2);
            bundle2.putLong("timestampInMillis", j);
            bundle2.putBundle("params", bundle);
            this.zzbsx.zzbst.onMessageTriggered(3, bundle2);
        }
    }
}

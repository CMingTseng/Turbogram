package com.google.android.gms.internal.ads;

import com.google.android.gms.common.internal.Constants;
import com.mohamadamin.persianmaterialdatetimepicker.date.MonthView;
import org.json.JSONObject;

@zzadh
public class zzaal {
    private final zzaqw zzbnd;
    private final String zzbxf;

    public zzaal(zzaqw zzaqw) {
        this(zzaqw, "");
    }

    public zzaal(zzaqw zzaqw, String str) {
        this.zzbnd = zzaqw;
        this.zzbxf = str;
    }

    public final void zza(int i, int i2, int i3, int i4, float f, int i5) {
        try {
            this.zzbnd.zza("onScreenInfoChanged", new JSONObject().put("width", i).put(MonthView.VIEW_PARAMS_HEIGHT, i2).put("maxSizeWidth", i3).put("maxSizeHeight", i4).put(Constants.PARAM_DENSITY, (double) f).put("rotation", i5));
        } catch (Throwable e) {
            zzane.zzb("Error occured while obtaining screen information.", e);
        }
    }

    public final void zzb(int i, int i2, int i3, int i4) {
        try {
            this.zzbnd.zza("onSizeChanged", new JSONObject().put("x", i).put("y", i2).put("width", i3).put(MonthView.VIEW_PARAMS_HEIGHT, i4));
        } catch (Throwable e) {
            zzane.zzb("Error occured while dispatching size change.", e);
        }
    }

    public final void zzbw(String str) {
        try {
            this.zzbnd.zza("onError", new JSONObject().put("message", str).put("action", this.zzbxf));
        } catch (Throwable e) {
            zzane.zzb("Error occurred while dispatching error event.", e);
        }
    }

    public final void zzbx(String str) {
        try {
            this.zzbnd.zza("onReadyEventReceived", new JSONObject().put("js", str));
        } catch (Throwable e) {
            zzane.zzb("Error occured while dispatching ready Event.", e);
        }
    }

    public final void zzby(String str) {
        try {
            this.zzbnd.zza("onStateChanged", new JSONObject().put("state", str));
        } catch (Throwable e) {
            zzane.zzb("Error occured while dispatching state change.", e);
        }
    }

    public final void zzc(int i, int i2, int i3, int i4) {
        try {
            this.zzbnd.zza("onDefaultPositionReceived", new JSONObject().put("x", i).put("y", i2).put("width", i3).put(MonthView.VIEW_PARAMS_HEIGHT, i4));
        } catch (Throwable e) {
            zzane.zzb("Error occured while dispatching default position.", e);
        }
    }
}

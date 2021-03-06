package com.google.android.gms.internal.ads;

import com.google.android.gms.ads.internal.gmsg.zzv;
import java.util.Map;
import org.json.JSONObject;

final class zzpk implements zzv<Object> {
    private final /* synthetic */ zzacm zzbji;
    private final /* synthetic */ zzpf zzbjj;

    zzpk(zzpf zzpf, zzacm zzacm) {
        this.zzbjj = zzpf;
        this.zzbji = zzacm;
    }

    public final void zza(Object obj, Map<String, String> map) {
        JSONObject jSONObject = new JSONObject();
        try {
            for (String str : map.keySet()) {
                jSONObject.put(str, map.get(str));
            }
            jSONObject.put(TtmlNode.ATTR_ID, this.zzbjj.zzbjh);
            this.zzbji.zza("sendMessageToNativeJs", jSONObject);
        } catch (Throwable e) {
            zzane.zzb("Unable to dispatch sendMessageToNativeJs event", e);
        }
    }
}

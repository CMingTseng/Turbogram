package com.google.android.gms.internal.ads;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

final class zzail implements Runnable {
    private final /* synthetic */ Bitmap val$bitmap;
    private final /* synthetic */ zzaii zzcna;

    zzail(zzaii zzaii, Bitmap bitmap) {
        this.zzcna = zzaii;
        this.val$bitmap = bitmap;
    }

    public final void run() {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.val$bitmap.compress(CompressFormat.PNG, 0, byteArrayOutputStream);
        synchronized (this.zzcna.mLock) {
            this.zzcna.zzcmn.zzecm = new zzbft();
            this.zzcna.zzcmn.zzecm.zzedl = byteArrayOutputStream.toByteArray();
            this.zzcna.zzcmn.zzecm.mimeType = "image/png";
            this.zzcna.zzcmn.zzecm.zzamf = Integer.valueOf(1);
        }
    }
}

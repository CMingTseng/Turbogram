package com.google.android.gms.internal.ads;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.ObjectWrapper;

@zzadh
public final class zzon extends zzpx {
    private final Uri mUri;
    private final Drawable zzbhu;
    private final double zzbhv;

    public zzon(Drawable drawable, Uri uri, double d) {
        this.zzbhu = drawable;
        this.mUri = uri;
        this.zzbhv = d;
    }

    public final double getScale() {
        return this.zzbhv;
    }

    public final Uri getUri() throws RemoteException {
        return this.mUri;
    }

    public final IObjectWrapper zzjy() throws RemoteException {
        return ObjectWrapper.wrap(this.zzbhu);
    }
}

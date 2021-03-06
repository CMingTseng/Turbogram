package com.google.android.gms.vision.face.internal.client;

import android.content.Context;
import android.graphics.PointF;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.android.gms.dynamite.DynamiteModule;
import com.google.android.gms.dynamite.DynamiteModule.LoadingException;
import com.google.android.gms.internal.vision.zzl;
import com.google.android.gms.internal.vision.zzm;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import java.nio.ByteBuffer;

public final class zza extends zzl<zze> {
    private final zzc zzce;

    public zza(Context context, zzc zzc) {
        super(context, "FaceNativeHandle", "face");
        this.zzce = zzc;
        zzp();
    }

    protected final /* synthetic */ Object zza(DynamiteModule dynamiteModule, Context context) throws RemoteException, LoadingException {
        zzg zzg;
        IBinder instantiate = dynamiteModule.instantiate("com.google.android.gms.vision.face.ChimeraNativeFaceDetectorCreator");
        if (instantiate == null) {
            zzg = null;
        } else {
            IInterface queryLocalInterface = instantiate.queryLocalInterface("com.google.android.gms.vision.face.internal.client.INativeFaceDetectorCreator");
            zzg = queryLocalInterface instanceof zzg ? (zzg) queryLocalInterface : new zzh(instantiate);
        }
        return zzg == null ? null : zzg.zza(ObjectWrapper.wrap(context), this.zzce);
    }

    public final Face[] zzb(ByteBuffer byteBuffer, zzm zzm) {
        if (!isOperational()) {
            return new Face[0];
        }
        try {
            FaceParcel[] zzc = ((zze) zzp()).zzc(ObjectWrapper.wrap(byteBuffer), zzm);
            Face[] faceArr = new Face[zzc.length];
            for (int i = 0; i < zzc.length; i++) {
                Landmark[] landmarkArr;
                FaceParcel faceParcel = zzc[i];
                int i2 = faceParcel.id;
                PointF pointF = new PointF(faceParcel.centerX, faceParcel.centerY);
                float f = faceParcel.width;
                float f2 = faceParcel.height;
                float f3 = faceParcel.zzcf;
                float f4 = faceParcel.zzcg;
                LandmarkParcel[] landmarkParcelArr = faceParcel.zzch;
                if (landmarkParcelArr == null) {
                    landmarkArr = new Landmark[0];
                } else {
                    Landmark[] landmarkArr2 = new Landmark[landmarkParcelArr.length];
                    for (int i3 = 0; i3 < landmarkParcelArr.length; i3++) {
                        LandmarkParcel landmarkParcel = landmarkParcelArr[i3];
                        landmarkArr2[i3] = new Landmark(new PointF(landmarkParcel.f378x, landmarkParcel.f379y), landmarkParcel.type);
                    }
                    landmarkArr = landmarkArr2;
                }
                faceArr[i] = new Face(i2, pointF, f, f2, f3, f4, landmarkArr, faceParcel.zzbs, faceParcel.zzbt, faceParcel.zzbu);
            }
            return faceArr;
        } catch (Throwable e) {
            Log.e("FaceNativeHandle", "Could not call native face detector", e);
            return new Face[0];
        }
    }

    public final boolean zzd(int i) {
        if (!isOperational()) {
            return false;
        }
        try {
            return ((zze) zzp()).zzd(i);
        } catch (Throwable e) {
            Log.e("FaceNativeHandle", "Could not call native face detector", e);
            return false;
        }
    }

    protected final void zzm() throws RemoteException {
        ((zze) zzp()).zzn();
    }
}

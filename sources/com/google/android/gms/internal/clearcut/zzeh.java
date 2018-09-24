package com.google.android.gms.internal.clearcut;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

final class zzeh {
    private static final Class<?> zzoh = zzdp();
    private static final zzex<?, ?> zzoi = zzd(false);
    private static final zzex<?, ?> zzoj = zzd(true);
    private static final zzex<?, ?> zzok = new zzez();

    static int zza(List<Long> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzdc) {
            zzdc zzdc = (zzdc) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zze(zzdc.getLong(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zze(((Long) list.get(i3)).longValue());
        }
        return i;
    }

    private static <UT, UB> UB zza(int i, int i2, UB ub, zzex<UT, UB> zzex) {
        Object zzdz;
        if (ub == null) {
            zzdz = zzex.zzdz();
        }
        zzex.zza(zzdz, i, (long) i2);
        return zzdz;
    }

    static <UT, UB> UB zza(int i, List<Integer> list, zzck<?> zzck, UB ub, zzex<UT, UB> zzex) {
        if (zzck == null) {
            return ub;
        }
        UB ub2;
        int intValue;
        if (list instanceof RandomAccess) {
            int size = list.size();
            int i2 = 0;
            int i3 = 0;
            ub2 = ub;
            while (i2 < size) {
                intValue = ((Integer) list.get(i2)).intValue();
                if (zzck.zzb(intValue) != null) {
                    if (i2 != i3) {
                        list.set(i3, Integer.valueOf(intValue));
                    }
                    intValue = i3 + 1;
                } else {
                    ub2 = zza(i, intValue, (Object) ub2, (zzex) zzex);
                    intValue = i3;
                }
                i2++;
                i3 = intValue;
            }
            if (i3 != size) {
                list.subList(i3, size).clear();
            }
        } else {
            Object zza;
            Iterator it = list.iterator();
            while (it.hasNext()) {
                intValue = ((Integer) it.next()).intValue();
                if (zzck.zzb(intValue) == null) {
                    zza = zza(i, intValue, zza, (zzex) zzex);
                    it.remove();
                }
            }
            ub2 = zza;
        }
        return ub2;
    }

    public static void zza(int i, List<String> list, zzfr zzfr) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zza(i, (List) list);
        }
    }

    public static void zza(int i, List<?> list, zzfr zzfr, zzef zzef) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zza(i, (List) list, zzef);
        }
    }

    public static void zza(int i, List<Double> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzg(i, list, z);
        }
    }

    static <T, FT extends zzca<FT>> void zza(zzbu<FT> zzbu, T t, T t2) {
        zzby zza = zzbu.zza((Object) t2);
        if (!zza.isEmpty()) {
            zzbu.zzb(t).zza(zza);
        }
    }

    static <T> void zza(zzdj zzdj, T t, T t2, long j) {
        zzfd.zza((Object) t, j, zzdj.zzb(zzfd.zzo(t, j), zzfd.zzo(t2, j)));
    }

    static <T, UT, UB> void zza(zzex<UT, UB> zzex, T t, T t2) {
        zzex.zze(t, zzex.zzg(zzex.zzq(t), zzex.zzq(t2)));
    }

    static int zzb(List<Long> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzdc) {
            zzdc zzdc = (zzdc) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzf(zzdc.getLong(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzf(((Long) list.get(i3)).longValue());
        }
        return i;
    }

    public static void zzb(int i, List<zzbb> list, zzfr zzfr) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzb(i, (List) list);
        }
    }

    public static void zzb(int i, List<?> list, zzfr zzfr, zzef zzef) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzb(i, (List) list, zzef);
        }
    }

    public static void zzb(int i, List<Float> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzf(i, list, z);
        }
    }

    static int zzc(int i, Object obj, zzef zzef) {
        return obj instanceof zzcv ? zzbn.zza(i, (zzcv) obj) : zzbn.zzb(i, (zzdo) obj, zzef);
    }

    static int zzc(int i, List<?> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int zzr = zzbn.zzr(i) * size;
        int i2;
        if (list instanceof zzcx) {
            zzcx zzcx = (zzcx) list;
            i2 = 0;
            while (i2 < size) {
                Object raw = zzcx.getRaw(i2);
                i2++;
                zzr = raw instanceof zzbb ? zzbn.zzb((zzbb) raw) + zzr : zzbn.zzh((String) raw) + zzr;
            }
            return zzr;
        }
        i2 = 0;
        while (i2 < size) {
            raw = list.get(i2);
            i2++;
            zzr = raw instanceof zzbb ? zzbn.zzb((zzbb) raw) + zzr : zzbn.zzh((String) raw) + zzr;
        }
        return zzr;
    }

    static int zzc(int i, List<?> list, zzef zzef) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int zzr = zzbn.zzr(i) * size;
        int i2 = 0;
        while (i2 < size) {
            Object obj = list.get(i2);
            i2++;
            zzr = obj instanceof zzcv ? zzbn.zza((zzcv) obj) + zzr : zzbn.zzb((zzdo) obj, zzef) + zzr;
        }
        return zzr;
    }

    static int zzc(List<Long> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzdc) {
            zzdc zzdc = (zzdc) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzg(zzdc.getLong(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzg(((Long) list.get(i3)).longValue());
        }
        return i;
    }

    public static void zzc(int i, List<Long> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzc(i, list, z);
        }
    }

    public static boolean zzc(int i, int i2, int i3) {
        return i2 < 40 || ((((long) i2) - ((long) i)) + 1) + 9 <= ((2 * ((long) i3)) + 3) + ((((long) i3) + 3) * 3);
    }

    static int zzd(int i, List<zzbb> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int zzr = zzbn.zzr(i) * size;
        for (size = 0; size < list.size(); size++) {
            zzr += zzbn.zzb((zzbb) list.get(size));
        }
        return zzr;
    }

    static int zzd(int i, List<zzdo> list, zzef zzef) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i2 += zzbn.zzc(i, (zzdo) list.get(i3), zzef);
        }
        return i2;
    }

    static int zzd(List<Integer> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzch) {
            zzch zzch = (zzch) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzx(zzch.getInt(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzx(((Integer) list.get(i3)).intValue());
        }
        return i;
    }

    private static zzex<?, ?> zzd(boolean z) {
        try {
            Class zzdq = zzdq();
            if (zzdq == null) {
                return null;
            }
            return (zzex) zzdq.getConstructor(new Class[]{Boolean.TYPE}).newInstance(new Object[]{Boolean.valueOf(z)});
        } catch (Throwable th) {
            return null;
        }
    }

    public static void zzd(int i, List<Long> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzd(i, list, z);
        }
    }

    static boolean zzd(Object obj, Object obj2) {
        return obj == obj2 || (obj != null && obj.equals(obj2));
    }

    public static zzex<?, ?> zzdm() {
        return zzoi;
    }

    public static zzex<?, ?> zzdn() {
        return zzoj;
    }

    public static zzex<?, ?> zzdo() {
        return zzok;
    }

    private static Class<?> zzdp() {
        try {
            return Class.forName("com.google.protobuf.GeneratedMessage");
        } catch (Throwable th) {
            return null;
        }
    }

    private static Class<?> zzdq() {
        try {
            return Class.forName("com.google.protobuf.UnknownFieldSetSchema");
        } catch (Throwable th) {
            return null;
        }
    }

    static int zze(List<Integer> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzch) {
            zzch zzch = (zzch) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzs(zzch.getInt(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzs(((Integer) list.get(i3)).intValue());
        }
        return i;
    }

    public static void zze(int i, List<Long> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzn(i, list, z);
        }
    }

    static int zzf(List<Integer> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzch) {
            zzch zzch = (zzch) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzt(zzch.getInt(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzt(((Integer) list.get(i3)).intValue());
        }
        return i;
    }

    public static void zzf(int i, List<Long> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zze(i, list, z);
        }
    }

    public static void zzf(Class<?> cls) {
        if (!zzcg.class.isAssignableFrom(cls) && zzoh != null && !zzoh.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Message classes must extend GeneratedMessage or GeneratedMessageLite");
        }
    }

    static int zzg(List<Integer> list) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        int i;
        if (list instanceof zzch) {
            zzch zzch = (zzch) list;
            int i2 = 0;
            for (i = 0; i < size; i++) {
                i2 += zzbn.zzu(zzch.getInt(i));
            }
            return i2;
        }
        i = 0;
        for (int i3 = 0; i3 < size; i3++) {
            i += zzbn.zzu(((Integer) list.get(i3)).intValue());
        }
        return i;
    }

    public static void zzg(int i, List<Long> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzl(i, list, z);
        }
    }

    static int zzh(List<?> list) {
        return list.size() << 2;
    }

    public static void zzh(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zza(i, (List) list, z);
        }
    }

    static int zzi(List<?> list) {
        return list.size() << 3;
    }

    public static void zzi(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzj(i, list, z);
        }
    }

    static int zzj(List<?> list) {
        return list.size();
    }

    public static void zzj(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzm(i, list, z);
        }
    }

    public static void zzk(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzb(i, (List) list, z);
        }
    }

    public static void zzl(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzk(i, list, z);
        }
    }

    public static void zzm(int i, List<Integer> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzh(i, list, z);
        }
    }

    public static void zzn(int i, List<Boolean> list, zzfr zzfr, boolean z) throws IOException {
        if (list != null && !list.isEmpty()) {
            zzfr.zzi(i, list, z);
        }
    }

    static int zzo(int i, List<Long> list, boolean z) {
        return list.size() == 0 ? 0 : zza(list) + (list.size() * zzbn.zzr(i));
    }

    static int zzp(int i, List<Long> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zzb(list);
    }

    static int zzq(int i, List<Long> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zzc(list);
    }

    static int zzr(int i, List<Integer> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zzd((List) list);
    }

    static int zzs(int i, List<Integer> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zze(list);
    }

    static int zzt(int i, List<Integer> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zzf((List) list);
    }

    static int zzu(int i, List<Integer> list, boolean z) {
        int size = list.size();
        if (size == 0) {
            return 0;
        }
        return (size * zzbn.zzr(i)) + zzg(list);
    }

    static int zzv(int i, List<?> list, boolean z) {
        int size = list.size();
        return size == 0 ? 0 : zzbn.zzj(i, 0) * size;
    }

    static int zzw(int i, List<?> list, boolean z) {
        int size = list.size();
        return size == 0 ? 0 : size * zzbn.zzg(i, 0);
    }

    static int zzx(int i, List<?> list, boolean z) {
        int size = list.size();
        return size == 0 ? 0 : size * zzbn.zzc(i, true);
    }
}

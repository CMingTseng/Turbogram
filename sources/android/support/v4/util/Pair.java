package android.support.v4.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Pair<F, S> {
    @Nullable
    public final F first;
    @Nullable
    public final S second;

    public Pair(@Nullable F first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair) o;
        if (ObjectsCompat.equals(p.first, this.first) && ObjectsCompat.equals(p.second, this.second)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = this.first == null ? 0 : this.first.hashCode();
        if (this.second != null) {
            i = this.second.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "Pair{" + String.valueOf(this.first) + " " + String.valueOf(this.second) + "}";
    }

    @NonNull
    public static <A, B> Pair<A, B> create(@Nullable A a, @Nullable B b) {
        return new Pair(a, b);
    }
}

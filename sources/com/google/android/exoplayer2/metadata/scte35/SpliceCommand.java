package com.google.android.exoplayer2.metadata.scte35;

import com.google.android.exoplayer2.metadata.Metadata.Entry;

public abstract class SpliceCommand implements Entry {
    public String toString() {
        return "SCTE-35 splice command: type=" + getClass().getSimpleName();
    }

    public int describeContents() {
        return 0;
    }
}

package com.google.android.exoplayer2.source.hls;

import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;

final class HlsSampleStream implements SampleStream {
    private int sampleQueueIndex = -1;
    private final HlsSampleStreamWrapper sampleStreamWrapper;
    private final int trackGroupIndex;

    public HlsSampleStream(HlsSampleStreamWrapper sampleStreamWrapper, int trackGroupIndex) {
        this.sampleStreamWrapper = sampleStreamWrapper;
        this.trackGroupIndex = trackGroupIndex;
    }

    public void bindSampleQueue() {
        Assertions.checkArgument(this.sampleQueueIndex == -1);
        this.sampleQueueIndex = this.sampleStreamWrapper.bindSampleQueueToSampleStream(this.trackGroupIndex);
    }

    public void unbindSampleQueue() {
        if (this.sampleQueueIndex != -1) {
            this.sampleStreamWrapper.unbindSampleQueue(this.trackGroupIndex);
            this.sampleQueueIndex = -1;
        }
    }

    public boolean isReady() {
        return this.sampleQueueIndex == -3 || (hasValidSampleQueueIndex() && this.sampleStreamWrapper.isReady(this.sampleQueueIndex));
    }

    public void maybeThrowError() throws IOException {
        if (this.sampleQueueIndex == -2) {
            throw new SampleQueueMappingException(this.sampleStreamWrapper.getTrackGroups().get(this.trackGroupIndex).getFormat(0).sampleMimeType);
        }
        this.sampleStreamWrapper.maybeThrowError();
    }

    public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean requireFormat) {
        return hasValidSampleQueueIndex() ? this.sampleStreamWrapper.readData(this.sampleQueueIndex, formatHolder, buffer, requireFormat) : -3;
    }

    public int skipData(long positionUs) {
        return hasValidSampleQueueIndex() ? this.sampleStreamWrapper.skipData(this.sampleQueueIndex, positionUs) : 0;
    }

    private boolean hasValidSampleQueueIndex() {
        return (this.sampleQueueIndex == -1 || this.sampleQueueIndex == -3 || this.sampleQueueIndex == -2) ? false : true;
    }
}

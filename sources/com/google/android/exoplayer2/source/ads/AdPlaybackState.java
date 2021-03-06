package com.google.android.exoplayer2.source.ads;

import android.net.Uri;
import android.support.annotation.CheckResult;
import com.google.android.exoplayer2.C0246C;
import com.google.android.exoplayer2.util.Assertions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public final class AdPlaybackState {
    public static final int AD_STATE_AVAILABLE = 1;
    public static final int AD_STATE_ERROR = 4;
    public static final int AD_STATE_PLAYED = 3;
    public static final int AD_STATE_SKIPPED = 2;
    public static final int AD_STATE_UNAVAILABLE = 0;
    public static final AdPlaybackState NONE = new AdPlaybackState(new long[0]);
    public final int adGroupCount;
    public final long[] adGroupTimesUs;
    public final AdGroup[] adGroups;
    public final long adResumePositionUs;
    public final long contentDurationUs;

    public static final class AdGroup {
        public final int count;
        public final long[] durationsUs;
        public final int[] states;
        public final Uri[] uris;

        public AdGroup() {
            this(-1, new int[0], new Uri[0], new long[0]);
        }

        private AdGroup(int count, int[] states, Uri[] uris, long[] durationsUs) {
            Assertions.checkArgument(states.length == uris.length);
            this.count = count;
            this.states = states;
            this.uris = uris;
            this.durationsUs = durationsUs;
        }

        public int getFirstAdIndexToPlay() {
            return getNextAdIndexToPlay(-1);
        }

        public int getNextAdIndexToPlay(int lastPlayedAdIndex) {
            int nextAdIndexToPlay = lastPlayedAdIndex + 1;
            while (nextAdIndexToPlay < this.states.length && this.states[nextAdIndexToPlay] != 0 && this.states[nextAdIndexToPlay] != 1) {
                nextAdIndexToPlay++;
            }
            return nextAdIndexToPlay;
        }

        public boolean hasUnplayedAds() {
            return this.count == -1 || getFirstAdIndexToPlay() < this.count;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AdGroup adGroup = (AdGroup) o;
            if (this.count == adGroup.count && Arrays.equals(this.uris, adGroup.uris) && Arrays.equals(this.states, adGroup.states) && Arrays.equals(this.durationsUs, adGroup.durationsUs)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((((this.count * 31) + Arrays.hashCode(this.uris)) * 31) + Arrays.hashCode(this.states)) * 31) + Arrays.hashCode(this.durationsUs);
        }

        @CheckResult
        public AdGroup withAdCount(int count) {
            boolean z = this.count == -1 && this.states.length <= count;
            Assertions.checkArgument(z);
            return new AdGroup(count, copyStatesWithSpaceForAdCount(this.states, count), (Uri[]) Arrays.copyOf(this.uris, count), copyDurationsUsWithSpaceForAdCount(this.durationsUs, count));
        }

        @CheckResult
        public AdGroup withAdUri(Uri uri, int index) {
            boolean z;
            long[] durationsUs;
            boolean z2 = false;
            if (this.count == -1 || index < this.count) {
                z = true;
            } else {
                z = false;
            }
            Assertions.checkArgument(z);
            int[] states = copyStatesWithSpaceForAdCount(this.states, index + 1);
            if (states[index] == 0) {
                z2 = true;
            }
            Assertions.checkArgument(z2);
            if (this.durationsUs.length == states.length) {
                durationsUs = this.durationsUs;
            } else {
                durationsUs = copyDurationsUsWithSpaceForAdCount(this.durationsUs, states.length);
            }
            Uri[] uris = (Uri[]) Arrays.copyOf(this.uris, states.length);
            uris[index] = uri;
            states[index] = 1;
            return new AdGroup(this.count, states, uris, durationsUs);
        }

        @CheckResult
        public AdGroup withAdState(int state, int index) {
            boolean z;
            long[] durationsUs;
            Uri[] uris;
            boolean z2 = false;
            if (this.count == -1 || index < this.count) {
                z = true;
            } else {
                z = false;
            }
            Assertions.checkArgument(z);
            int[] states = copyStatesWithSpaceForAdCount(this.states, index + 1);
            if (states[index] == 0 || states[index] == 1 || states[index] == state) {
                z2 = true;
            }
            Assertions.checkArgument(z2);
            if (this.durationsUs.length == states.length) {
                durationsUs = this.durationsUs;
            } else {
                durationsUs = copyDurationsUsWithSpaceForAdCount(this.durationsUs, states.length);
            }
            if (this.uris.length == states.length) {
                uris = this.uris;
            } else {
                uris = (Uri[]) Arrays.copyOf(this.uris, states.length);
            }
            states[index] = state;
            return new AdGroup(this.count, states, uris, durationsUs);
        }

        @CheckResult
        public AdGroup withAdDurationsUs(long[] durationsUs) {
            boolean z = this.count == -1 || durationsUs.length <= this.uris.length;
            Assertions.checkArgument(z);
            if (durationsUs.length < this.uris.length) {
                durationsUs = copyDurationsUsWithSpaceForAdCount(durationsUs, this.uris.length);
            }
            return new AdGroup(this.count, this.states, this.uris, durationsUs);
        }

        @CheckResult
        public AdGroup withAllAdsSkipped() {
            if (this.count == -1) {
                return new AdGroup(0, new int[0], new Uri[0], new long[0]);
            }
            int count = this.states.length;
            int[] states = Arrays.copyOf(this.states, count);
            int i = 0;
            while (i < count) {
                if (states[i] == 1 || states[i] == 0) {
                    states[i] = 2;
                }
                i++;
            }
            return new AdGroup(count, states, this.uris, this.durationsUs);
        }

        @CheckResult
        private static int[] copyStatesWithSpaceForAdCount(int[] states, int count) {
            int oldStateCount = states.length;
            int newStateCount = Math.max(count, oldStateCount);
            states = Arrays.copyOf(states, newStateCount);
            Arrays.fill(states, oldStateCount, newStateCount, 0);
            return states;
        }

        @CheckResult
        private static long[] copyDurationsUsWithSpaceForAdCount(long[] durationsUs, int count) {
            int oldDurationsUsCount = durationsUs.length;
            int newDurationsUsCount = Math.max(count, oldDurationsUsCount);
            durationsUs = Arrays.copyOf(durationsUs, newDurationsUsCount);
            Arrays.fill(durationsUs, oldDurationsUsCount, newDurationsUsCount, C0246C.TIME_UNSET);
            return durationsUs;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AdState {
    }

    public AdPlaybackState(long... adGroupTimesUs) {
        int count = adGroupTimesUs.length;
        this.adGroupCount = count;
        this.adGroupTimesUs = Arrays.copyOf(adGroupTimesUs, count);
        this.adGroups = new AdGroup[count];
        for (int i = 0; i < count; i++) {
            this.adGroups[i] = new AdGroup();
        }
        this.adResumePositionUs = 0;
        this.contentDurationUs = C0246C.TIME_UNSET;
    }

    private AdPlaybackState(long[] adGroupTimesUs, AdGroup[] adGroups, long adResumePositionUs, long contentDurationUs) {
        this.adGroupCount = adGroups.length;
        this.adGroupTimesUs = adGroupTimesUs;
        this.adGroups = adGroups;
        this.adResumePositionUs = adResumePositionUs;
        this.contentDurationUs = contentDurationUs;
    }

    public int getAdGroupIndexForPositionUs(long positionUs) {
        int index = this.adGroupTimesUs.length - 1;
        while (index >= 0 && (this.adGroupTimesUs[index] == Long.MIN_VALUE || this.adGroupTimesUs[index] > positionUs)) {
            index--;
        }
        return (index < 0 || !this.adGroups[index].hasUnplayedAds()) ? -1 : index;
    }

    public int getAdGroupIndexAfterPositionUs(long positionUs) {
        int index = 0;
        while (index < this.adGroupTimesUs.length && this.adGroupTimesUs[index] != Long.MIN_VALUE && (positionUs >= this.adGroupTimesUs[index] || !this.adGroups[index].hasUnplayedAds())) {
            index++;
        }
        return index < this.adGroupTimesUs.length ? index : -1;
    }

    @CheckResult
    public AdPlaybackState withAdCount(int adGroupIndex, int adCount) {
        Assertions.checkArgument(adCount > 0);
        if (this.adGroups[adGroupIndex].count == adCount) {
            return this;
        }
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = this.adGroups[adGroupIndex].withAdCount(adCount);
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withAdUri(int adGroupIndex, int adIndexInAdGroup, Uri uri) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = adGroups[adGroupIndex].withAdUri(uri, adIndexInAdGroup);
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withPlayedAd(int adGroupIndex, int adIndexInAdGroup) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = adGroups[adGroupIndex].withAdState(3, adIndexInAdGroup);
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withSkippedAd(int adGroupIndex, int adIndexInAdGroup) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = adGroups[adGroupIndex].withAdState(2, adIndexInAdGroup);
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withAdLoadError(int adGroupIndex, int adIndexInAdGroup) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = adGroups[adGroupIndex].withAdState(4, adIndexInAdGroup);
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withSkippedAdGroup(int adGroupIndex) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        adGroups[adGroupIndex] = adGroups[adGroupIndex].withAllAdsSkipped();
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withAdDurationsUs(long[][] adDurationUs) {
        AdGroup[] adGroups = (AdGroup[]) Arrays.copyOf(this.adGroups, this.adGroups.length);
        for (int adGroupIndex = 0; adGroupIndex < this.adGroupCount; adGroupIndex++) {
            adGroups[adGroupIndex] = adGroups[adGroupIndex].withAdDurationsUs(adDurationUs[adGroupIndex]);
        }
        return new AdPlaybackState(this.adGroupTimesUs, adGroups, this.adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withAdResumePositionUs(long adResumePositionUs) {
        if (this.adResumePositionUs == adResumePositionUs) {
            return this;
        }
        return new AdPlaybackState(this.adGroupTimesUs, this.adGroups, adResumePositionUs, this.contentDurationUs);
    }

    @CheckResult
    public AdPlaybackState withContentDurationUs(long contentDurationUs) {
        return this.contentDurationUs == contentDurationUs ? this : new AdPlaybackState(this.adGroupTimesUs, this.adGroups, this.adResumePositionUs, contentDurationUs);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdPlaybackState that = (AdPlaybackState) o;
        if (this.adGroupCount == that.adGroupCount && this.adResumePositionUs == that.adResumePositionUs && this.contentDurationUs == that.contentDurationUs && Arrays.equals(this.adGroupTimesUs, that.adGroupTimesUs) && Arrays.equals(this.adGroups, that.adGroups)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((((this.adGroupCount * 31) + ((int) this.adResumePositionUs)) * 31) + ((int) this.contentDurationUs)) * 31) + Arrays.hashCode(this.adGroupTimesUs)) * 31) + Arrays.hashCode(this.adGroups);
    }
}

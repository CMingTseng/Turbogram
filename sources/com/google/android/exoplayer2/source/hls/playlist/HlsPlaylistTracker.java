package com.google.android.exoplayer2.source.hls.playlist;

import android.net.Uri;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.source.MediaSourceEventListener.EventDispatcher;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist.HlsUrl;
import java.io.IOException;

public interface HlsPlaylistTracker {

    public interface PlaylistEventListener {
        void onPlaylistChanged();

        boolean onPlaylistError(HlsUrl hlsUrl, boolean z);
    }

    public interface PrimaryPlaylistListener {
        void onPrimaryPlaylistRefreshed(HlsMediaPlaylist hlsMediaPlaylist);
    }

    public static final class PlaylistResetException extends IOException {
        public final String url;

        public PlaylistResetException(String url) {
            this.url = url;
        }
    }

    public static final class PlaylistStuckException extends IOException {
        public final String url;

        public PlaylistStuckException(String url) {
            this.url = url;
        }
    }

    void addListener(PlaylistEventListener playlistEventListener);

    long getInitialStartTimeUs();

    @Nullable
    HlsMasterPlaylist getMasterPlaylist();

    @Nullable
    HlsMediaPlaylist getPlaylistSnapshot(HlsUrl hlsUrl);

    boolean isLive();

    boolean isSnapshotValid(HlsUrl hlsUrl);

    void maybeThrowPlaylistRefreshError(HlsUrl hlsUrl) throws IOException;

    void maybeThrowPrimaryPlaylistRefreshError() throws IOException;

    void refreshPlaylist(HlsUrl hlsUrl);

    void removeListener(PlaylistEventListener playlistEventListener);

    void start(Uri uri, EventDispatcher eventDispatcher, PrimaryPlaylistListener primaryPlaylistListener);

    void stop();
}

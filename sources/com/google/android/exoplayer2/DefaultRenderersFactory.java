package com.google.android.exoplayer2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class DefaultRenderersFactory implements RenderersFactory {
    public static final long DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS = 5000;
    public static final int EXTENSION_RENDERER_MODE_OFF = 0;
    public static final int EXTENSION_RENDERER_MODE_ON = 1;
    public static final int EXTENSION_RENDERER_MODE_PREFER = 2;
    protected static final int MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY = 50;
    private static final String TAG = "DefaultRenderersFactory";
    private final long allowedVideoJoiningTimeMs;
    private final Context context;
    @Nullable
    private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private final int extensionRendererMode;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtensionRendererMode {
    }

    public DefaultRenderersFactory(Context context) {
        this(context, 0);
    }

    @Deprecated
    public DefaultRenderersFactory(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this(context, (DrmSessionManager) drmSessionManager, 0);
    }

    public DefaultRenderersFactory(Context context, int extensionRendererMode) {
        this(context, null, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    @Deprecated
    public DefaultRenderersFactory(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode) {
        this(context, drmSessionManager, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    public DefaultRenderersFactory(Context context, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        this(context, null, extensionRendererMode, allowedVideoJoiningTimeMs);
    }

    @Deprecated
    public DefaultRenderersFactory(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        this.context = context;
        this.extensionRendererMode = extensionRendererMode;
        this.allowedVideoJoiningTimeMs = allowedVideoJoiningTimeMs;
        this.drmSessionManager = drmSessionManager;
    }

    public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener, AudioRendererEventListener audioRendererEventListener, TextOutput textRendererOutput, MetadataOutput metadataRendererOutput, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        if (drmSessionManager == null) {
            drmSessionManager = this.drmSessionManager;
        }
        ArrayList<Renderer> renderersList = new ArrayList();
        buildVideoRenderers(this.context, drmSessionManager, this.allowedVideoJoiningTimeMs, eventHandler, videoRendererEventListener, this.extensionRendererMode, renderersList);
        buildAudioRenderers(this.context, drmSessionManager, buildAudioProcessors(), eventHandler, audioRendererEventListener, this.extensionRendererMode, renderersList);
        buildTextRenderers(this.context, textRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        buildMetadataRenderers(this.context, metadataRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        buildMiscellaneousRenderers(this.context, eventHandler, this.extensionRendererMode, renderersList);
        return (Renderer[]) renderersList.toArray(new Renderer[renderersList.size()]);
    }

    protected void buildVideoRenderers(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        Throwable e;
        ArrayList<Renderer> arrayList = out;
        arrayList.add(new MediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT, allowedVideoJoiningTimeMs, drmSessionManager, false, eventHandler, eventListener, 50));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex;
            int extensionRendererIndex2 = out.size();
            if (extensionRendererMode == 2) {
                extensionRendererIndex = extensionRendererIndex2 - 1;
            } else {
                extensionRendererIndex = extensionRendererIndex2;
            }
            try {
                extensionRendererIndex2 = extensionRendererIndex + 1;
                try {
                    out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer").getConstructor(new Class[]{Boolean.TYPE, Long.TYPE, Handler.class, VideoRendererEventListener.class, Integer.TYPE}).newInstance(new Object[]{Boolean.valueOf(true), Long.valueOf(allowedVideoJoiningTimeMs), eventHandler, eventListener, Integer.valueOf(50)}));
                    Log.i(TAG, "Loaded LibvpxVideoRenderer.");
                } catch (ClassNotFoundException e2) {
                } catch (Exception e3) {
                    e = e3;
                    throw new RuntimeException("Error instantiating VP9 extension", e);
                }
            } catch (ClassNotFoundException e4) {
                extensionRendererIndex2 = extensionRendererIndex;
            } catch (Exception e5) {
                e = e5;
                extensionRendererIndex2 = extensionRendererIndex;
                throw new RuntimeException("Error instantiating VP9 extension", e);
            }
        }
    }

    protected void buildAudioRenderers(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AudioProcessor[] audioProcessors, Handler eventHandler, AudioRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        int extensionRendererIndex;
        Exception e;
        ArrayList<Renderer> arrayList = out;
        arrayList.add(new MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, drmSessionManager, false, eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex2 = out.size();
            if (extensionRendererMode == 2) {
                extensionRendererIndex = extensionRendererIndex2 - 1;
            } else {
                extensionRendererIndex = extensionRendererIndex2;
            }
            try {
                extensionRendererIndex2 = extensionRendererIndex + 1;
                try {
                    out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                    Log.i(TAG, "Loaded LibopusAudioRenderer.");
                    extensionRendererIndex = extensionRendererIndex2;
                } catch (ClassNotFoundException e2) {
                    extensionRendererIndex = extensionRendererIndex2;
                    extensionRendererIndex2 = extensionRendererIndex + 1;
                    try {
                        out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                        Log.i(TAG, "Loaded LibflacAudioRenderer.");
                        extensionRendererIndex = extensionRendererIndex2;
                    } catch (ClassNotFoundException e3) {
                        extensionRendererIndex = extensionRendererIndex2;
                        extensionRendererIndex2 = extensionRendererIndex + 1;
                        out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                        Log.i(TAG, "Loaded FfmpegAudioRenderer.");
                    } catch (Exception e4) {
                        e = e4;
                        throw new RuntimeException("Error instantiating FLAC extension", e);
                    }
                    extensionRendererIndex2 = extensionRendererIndex + 1;
                    try {
                        out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                        Log.i(TAG, "Loaded FfmpegAudioRenderer.");
                    } catch (ClassNotFoundException e5) {
                        return;
                    } catch (Exception e6) {
                        e = e6;
                        throw new RuntimeException("Error instantiating FFmpeg extension", e);
                    }
                } catch (Exception e7) {
                    e = e7;
                    throw new RuntimeException("Error instantiating Opus extension", e);
                }
            } catch (ClassNotFoundException e8) {
                extensionRendererIndex2 = extensionRendererIndex;
                extensionRendererIndex = extensionRendererIndex2;
                extensionRendererIndex2 = extensionRendererIndex + 1;
                out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                Log.i(TAG, "Loaded LibflacAudioRenderer.");
                extensionRendererIndex = extensionRendererIndex2;
                extensionRendererIndex2 = extensionRendererIndex + 1;
                out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                Log.i(TAG, "Loaded FfmpegAudioRenderer.");
            } catch (Exception e9) {
                e = e9;
                extensionRendererIndex2 = extensionRendererIndex;
                throw new RuntimeException("Error instantiating Opus extension", e);
            }
            try {
                extensionRendererIndex2 = extensionRendererIndex + 1;
                out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                Log.i(TAG, "Loaded LibflacAudioRenderer.");
                extensionRendererIndex = extensionRendererIndex2;
            } catch (ClassNotFoundException e10) {
                extensionRendererIndex2 = extensionRendererIndex;
                extensionRendererIndex = extensionRendererIndex2;
                extensionRendererIndex2 = extensionRendererIndex + 1;
                out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                Log.i(TAG, "Loaded FfmpegAudioRenderer.");
            } catch (Exception e11) {
                e = e11;
                extensionRendererIndex2 = extensionRendererIndex;
                throw new RuntimeException("Error instantiating FLAC extension", e);
            }
            try {
                extensionRendererIndex2 = extensionRendererIndex + 1;
                out.add(extensionRendererIndex, (Renderer) Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer").getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class}).newInstance(new Object[]{eventHandler, eventListener, audioProcessors}));
                Log.i(TAG, "Loaded FfmpegAudioRenderer.");
            } catch (ClassNotFoundException e12) {
                extensionRendererIndex2 = extensionRendererIndex;
            } catch (Exception e13) {
                e = e13;
                extensionRendererIndex2 = extensionRendererIndex;
                throw new RuntimeException("Error instantiating FFmpeg extension", e);
            }
        }
    }

    protected void buildTextRenderers(Context context, TextOutput output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new TextRenderer(output, outputLooper));
    }

    protected void buildMetadataRenderers(Context context, MetadataOutput output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new MetadataRenderer(output, outputLooper));
    }

    protected void buildMiscellaneousRenderers(Context context, Handler eventHandler, int extensionRendererMode, ArrayList<Renderer> arrayList) {
    }

    protected AudioProcessor[] buildAudioProcessors() {
        return new AudioProcessor[0];
    }
}

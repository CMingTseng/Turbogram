package com.google.android.exoplayer2.drm;

import android.support.annotation.Nullable;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import java.util.UUID;

public interface MediaDrmCallback {
    byte[] executeKeyRequest(UUID uuid, KeyRequest keyRequest, @Nullable String str) throws Exception;

    byte[] executeProvisionRequest(UUID uuid, ProvisionRequest provisionRequest) throws Exception;
}

package org.telegram.ui;

import java.util.HashMap;
import org.telegram.messenger.LocaleController$LocaleInfo;

final /* synthetic */ class LaunchActivity$$Lambda$34 implements Runnable {
    private final LaunchActivity arg$1;
    private final HashMap arg$2;
    private final LocaleController$LocaleInfo[] arg$3;
    private final String arg$4;

    LaunchActivity$$Lambda$34(LaunchActivity launchActivity, HashMap hashMap, LocaleController$LocaleInfo[] localeController$LocaleInfoArr, String str) {
        this.arg$1 = launchActivity;
        this.arg$2 = hashMap;
        this.arg$3 = localeController$LocaleInfoArr;
        this.arg$4 = str;
    }

    public void run() {
        this.arg$1.lambda$null$47$LaunchActivity(this.arg$2, this.arg$3, this.arg$4);
    }
}

package org.telegram.ui;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.ActionBar.AlertDialog;

final /* synthetic */ class LaunchActivity$$Lambda$46 implements Runnable {
    private final LaunchActivity arg$1;
    private final AlertDialog arg$2;
    private final TLObject arg$3;
    private final TLRPC$TL_error arg$4;
    private final String arg$5;
    private final int arg$6;
    private final String arg$7;
    private final String arg$8;
    private final Integer arg$9;

    LaunchActivity$$Lambda$46(LaunchActivity launchActivity, AlertDialog alertDialog, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error, String str, int i, String str2, String str3, Integer num) {
        this.arg$1 = launchActivity;
        this.arg$2 = alertDialog;
        this.arg$3 = tLObject;
        this.arg$4 = tLRPC$TL_error;
        this.arg$5 = str;
        this.arg$6 = i;
        this.arg$7 = str2;
        this.arg$8 = str3;
        this.arg$9 = num;
    }

    public void run() {
        this.arg$1.lambda$null$12$LaunchActivity(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6, this.arg$7, this.arg$8, this.arg$9);
    }
}

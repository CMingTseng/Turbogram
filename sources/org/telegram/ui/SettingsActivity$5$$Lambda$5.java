package org.telegram.ui;

import android.view.View;
import android.view.View.OnClickListener;
import org.telegram.ui.SettingsActivity.C21335;

final /* synthetic */ class SettingsActivity$5$$Lambda$5 implements OnClickListener {
    private final C21335 arg$1;
    private final boolean[] arg$2;
    private final int arg$3;

    SettingsActivity$5$$Lambda$5(C21335 c21335, boolean[] zArr, int i) {
        this.arg$1 = c21335;
        this.arg$2 = zArr;
        this.arg$3 = i;
    }

    public void onClick(View view) {
        this.arg$1.lambda$onItemClick$5$SettingsActivity$5(this.arg$2, this.arg$3, view);
    }
}

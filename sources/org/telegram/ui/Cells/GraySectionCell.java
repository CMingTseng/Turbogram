package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import turbogram.Utilities.TurboUtils;

public class GraySectionCell extends FrameLayout {
    private TextView textView = new TextView(getContext());

    public GraySectionCell(Context context) {
        int i = 5;
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_graySection));
        this.textView.setTextSize(1, 13.0f);
        this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.textView.setTextColor(Theme.getColor(Theme.key_graySectionText));
        this.textView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        View view = this.textView;
        if (!LocaleController.isRTL) {
            i = 3;
        }
        addView(view, LayoutHelper.createFrame(-1, -1.0f, i | 48, 16.0f, 0.0f, 16.0f, 0.0f));
        this.textView.setTypeface(TurboUtils.getTurboTypeFace());
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32.0f), 1073741824));
    }

    public void setText(String text) {
        this.textView.setText(text);
    }
}

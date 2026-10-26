package com.ansub.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

// One HDMI input, full screen, for TVs where no other app will show it: on Fire TV only Amazon's own
// apps may open its HDMI screen, and the one player that takes HDMI links plays live TV instead.
// Back leaves. Leaving any other way (Home, an app) lets go of the input too.
public class InputActivity extends Activity {
    static final String EXTRA_INPUT = "input";

    private TvView tv;
    private TextView noSignal;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        tv = new TvView(this);
        root.addView(tv, new FrameLayout.LayoutParams(-1, -1));
        noSignal = new TextView(this);
        noSignal.setTextColor(Color.WHITE);
        noSignal.setTextSize(28);
        noSignal.setText(Strings.t("No signal"));
        noSignal.setVisibility(View.GONE);
        root.addView(noSignal, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        setContentView(root);
        tv.setCallback(new TvView.TvInputCallback() {
            @Override
            public void onVideoAvailable(String inputId) {
                noSignal.setVisibility(View.GONE);
            }

            @Override
            public void onVideoUnavailable(String inputId, int reason) {
                noSignal.setVisibility(reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING
                        ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onConnectionFailed(String inputId) {
                finish();
            }

            @Override
            public void onDisconnected(String inputId) {
                finish();
            }
        });
        tune(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tune(intent);
    }

    private void tune(Intent intent) {
        String id = intent.getStringExtra(EXTRA_INPUT);
        if (id == null) {
            finish();
            return;
        }
        tv.tune(id, TvContract.buildChannelUriForPassthroughInput(id));
        tv.setStreamVolume(1f); // full, so the TV's own volume is what counts

    }

    @Override
    protected void onStop() {
        super.onStop();
        tv.reset();
        finish();
    }
}

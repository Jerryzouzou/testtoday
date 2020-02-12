package com.hard.function.BezierLines;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hard.function.R;

/**
 * @author Jerry Lai on 2019/06/05
 */
public class BezierRun2CircleActivity extends AppCompatActivity {

    private Context mContext;
    private BezierRun2CircleView circleView;
    private TextView tvRatio;   //比例显示
    private SeekBar ratioSeekBar;   //比例调节

    private static final int MAX = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_circle);

        findViewAndInit();

    }

    private void findViewAndInit() {
        mContext = this;
        circleView = findViewById(R.id.circle_bezier_view);
        tvRatio = findViewById(R.id.tv_ratio);
        ratioSeekBar = findViewById(R.id.ratio_seek_bar);

        ratioSeekBar.setMax(MAX);
        circleView.setRatio(0.55f);
        tvRatio.setText(String.format(getString(R.string.ratio), "0.55"));
        ratioSeekBar.setProgress(55);
        ratioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float r = progress / (float) MAX;
                tvRatio.setText(String.format(getString(R.string.ratio), ""+r));
                circleView.setRatio(r);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}

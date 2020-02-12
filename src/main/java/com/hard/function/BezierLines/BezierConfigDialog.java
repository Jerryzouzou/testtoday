package com.hard.function.BezierLines;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.hard.function.R;

/**
 * @author Jerry Lai on 2019/05/27
 */
public class BezierConfigDialog extends AppCompatDialogFragment {

    // 阶级最大值
    private static final int ORDER_MAX = 7;
    // 速率最大值
    private static final int RATE_MAX = 5;
    // 速率的间隔
    private static final int RATE_INTERVAL = 5;
    // 是否显示降阶线
    private boolean isShowReduceOrderLine;
    // 是否循环播放
    private boolean isLoop;
    // 阶数
    private int order;
    // 速率
    private int rate;
    private boolean isDismissing = false;

    private static BezierConfigDialog dialog;
    private Animation disAnimation;
    private RelativeLayout rlBg;
    private LinearLayout llMenu;
    private Switch stShowReduce;
    private Switch stLoop;
    private SeekBar sbOrder;
    private SeekBar sbRate;
    private TextView tvOrder;
    private TextView tvRate;

    public static BezierConfigDialog getInstance(){
        if(dialog == null){
            Bundle bundle = new Bundle();
            dialog = new BezierConfigDialog();
            dialog.setArguments(bundle);
        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TranslucentNoTitle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bezier_setting, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlBg = view.findViewById(R.id.rl_background);
        llMenu = view.findViewById(R.id.ll_menu);
        stShowReduce = view.findViewById(R.id.reduce_switch);
        stLoop = view.findViewById(R.id.loop_switch);
        sbOrder = view.findViewById(R.id.order_seekbar);
        sbRate = view.findViewById(R.id.rate_seekbar);
        tvOrder = view.findViewById(R.id.tv_order);
        tvRate = view.findViewById(R.id.tv_rate);

        stShowReduce.setChecked(isShowReduceOrderLine);
        stLoop.setChecked(isLoop);
        sbOrder.setMax(ORDER_MAX - 1);
        sbOrder.setProgress(order);
        tvOrder.setText(getString(R.string.order, order+1));
        sbRate.setMax(RATE_MAX);
        sbRate.setProgress(rate);
        tvRate.setText(getString(R.string.rate, (rate+1)*RATE_INTERVAL));

        this.getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        llMenu.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dialog_show_anim));
        disAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_dismiss_anim);
        disAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BezierConfigDialog.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        sbOrder.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (getActivity()==null || !(getActivity() instanceof BezierBeginDerivationActivity)) {
                    return;
                }
                BezierBeginDerivationActivity activity = (BezierBeginDerivationActivity) getActivity();
                tvOrder.setText(getString(R.string.order, progress+1));
                activity.setOrder(progress+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (getActivity()==null || !(getActivity() instanceof BezierBeginDerivationActivity)) {
                    return;
                }
                BezierBeginDerivationActivity activity = (BezierBeginDerivationActivity) getActivity();
                int realRate = (progress+1)*RATE_INTERVAL;
                tvRate.setText(getString(R.string.rate, realRate));
                activity.setRate(realRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        stLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getActivity()==null || !(getActivity() instanceof BezierBeginDerivationActivity)) {
                    return;
                }
                BezierBeginDerivationActivity activity = (BezierBeginDerivationActivity) getActivity();
                activity.setLoopPlay(isChecked);
            }
        });
        stShowReduce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getActivity()==null || !(getActivity() instanceof BezierBeginDerivationActivity)) {
                    return;
                }
                BezierBeginDerivationActivity activity = (BezierBeginDerivationActivity) getActivity();
                activity.setShowReduceOrderLine(isShowReduceOrderLine);
            }
        });
        rlBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity()==null || !(getActivity() instanceof BezierBeginDerivationActivity)) {
                    return;
                }
                BezierBeginDerivationActivity activity = (BezierBeginDerivationActivity) getActivity();
                activity.restart();
                dismiss();
            }
        });
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                dismiss();
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        isDismissing = false;
    }

    @Override
    public void dismiss() {
        if(isDismissing) return;
        isDismissing = true;
        llMenu.startAnimation(disAnimation);
    }

    public void setShowReduceOrderLine(boolean showReduceOrderLine) {
        isShowReduceOrderLine = showReduceOrderLine;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public void setOrder(int order) {
        if (order > ORDER_MAX){
            order = ORDER_MAX;
        }else if (order < 1){
            order = 1;
        }
        this.order = order - 1;
    }

    public void setRate(int rate) {
        rate = rate / RATE_INTERVAL;
        if(rate > RATE_MAX){
            rate = RATE_MAX;
        }else if(rate < 1){
            rate = 1;
        }
        this.rate = rate - 1;
    }
}

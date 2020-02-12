package com.hard.function.BezierLines;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.hard.function.R;
import com.hard.function.tool.PrintOut;

import androidx.annotation.Nullable;

/**
 * @author Jerry Lai on 2019/05/09
 * 演示贝塞尔曲线动画
 */
public class BezierBeginDerivationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivSetting, ivPlay;
    private BezierRunView bezierView;
    private BezierConfigDialog bezierConfigDialog;

    // 是否显示降阶线
    private boolean isShowReduceOrderLine = true;
    // 是否循环播放
    private boolean isLoopPlay = false;
    // 阶数（默认五阶）
    private int order = 5;
    // 速率（默认10个点的跳过播放）
    private int rate = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_begin_derivation);

        findView();
        init();
    }

    private void init() {
        bezierView.setShowReduceOrderLine(isShowReduceOrderLine);
        bezierView.setLoop(isLoopPlay);
        bezierView.setOrder(order);
        bezierView.setRate(rate);
    }

    private void findView() {
        bezierView = findViewById(R.id.bezier_view);
        ivPlay = findViewById(R.id.iv_play);
        ivSetting = findViewById(R.id.iv_setting);
        ivSetting.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        bezierConfigDialog = BezierConfigDialog.getInstance();
    }

    @Override
    public void onClick(View v) {
        int state = bezierView.getState();
        switch (v.getId()){
            case R.id.iv_setting:
                if(state == BezierRunView.RUNNING){
                    PrintOut.printToast(this, "动画播放中，请稍等...");
                    return;
                }
                bezierConfigDialog.setLoop(isLoopPlay);
                bezierConfigDialog.setOrder(order);
                bezierConfigDialog.setRate(rate);
                bezierConfigDialog.setShowReduceOrderLine(isShowReduceOrderLine);
                bezierConfigDialog.show(getSupportFragmentManager(), "BezierBeginDerivationActivity");
                break;
            case R.id.iv_play:
                if(state == BezierRunView.RUNNING){     //run-->stop
                    bezierView.pause();
                    ivPlay.setImageResource(R.mipmap.icons_play);
                }else if (state == BezierRunView.STOP){     //stop-->run
                    bezierView.pause();
                    ivPlay.setImageResource(R.mipmap.icons_pause);
                }else {
                    bezierView.start();
                    ivPlay.setImageResource(R.mipmap.icons_pause);
                }
                break;
        }
    }

    public void resetPlayBtn(){
        ivPlay.setImageResource(R.mipmap.icons_play);
    }

    public void setShowReduceOrderLine(boolean showReduceOrderLine) {
        isShowReduceOrderLine = showReduceOrderLine;
        bezierView.setShowReduceOrderLine(isShowReduceOrderLine);
    }

    public void setLoopPlay(boolean loopPlay) {
        isLoopPlay = loopPlay;
        bezierView.setLoop(isLoopPlay);
    }

    public void setOrder(int order) {
        this.order = order;
        bezierView.setOrder(order);
        bezierView.invalidate();
    }

    public void setRate(int rate) {
        this.rate = rate;
        bezierView.setRate(rate);
        bezierView.invalidate();
    }

    public void restart(){
        bezierView.clean();
        bezierView.start();
    }
}

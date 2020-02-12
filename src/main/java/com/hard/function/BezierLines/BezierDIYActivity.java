package com.hard.function.BezierLines;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.hard.function.R;
import com.hard.function.tool.UIUtils;

import java.util.List;

/**
 * @author Jerry Lai on 2019/06/18
 */
public class BezierDIYActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "BezierDIY";
    private Context mContext;
    private DIYBezierView diyBezierView;
    private RadioGroup rgStatus;
    private Switch showLineSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_bezier);
        mContext = this;

        findView();
    }

    private void findView() {
        findViewById(R.id.btn_print).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);
        diyBezierView = findViewById(R.id.diy_bezier_view);
        rgStatus = findViewById(R.id.rg_status);
        showLineSwitch = findViewById(R.id.show_line_switch);

        diyBezierView.setIsShowHelpLine(true);
        showLineSwitch.setChecked(true);
        //lambda表达式
        rgStatus.setOnCheckedChangeListener((RadioGroup group, @IdRes int checkedId)->{
            switch (checkedId){
                case R.id.status_free:  // 自由变换
                    diyBezierView.setStatus(DIYBezierView.Status.FREE);
                    break;
                case R.id.status_three: // 三点拽动
                    diyBezierView.setStatus(DIYBezierView.Status.THREE);
                    break;
                case R.id.status_mirror_diff:
                    diyBezierView.setStatus(DIYBezierView.Status.MIRROR_DIFF);  // 镜像异向
                    break;
                case R.id.status_mirror_same:       // 镜像同向
                    diyBezierView.setStatus(DIYBezierView.Status.MIRROR_SAME);
                    break;
            }
        });

        showLineSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked)->{
            diyBezierView.setIsShowHelpLine(isChecked);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_print:
                printControlPointList();
                break;
            case R.id.btn_reset:
                diyBezierView.reset();
                break;
        }
    }

    private void printControlPointList() {
        StringBuilder sb = new StringBuilder();
        List<PointF> controlPointList = diyBezierView.getControlPointList();
        for (int i = 0; i < controlPointList.size(); i++) {
            PointF point = controlPointList.get(i);
            sb.append("第"+i+"个点坐标(单位dp)：[ ")
                    .append(UIUtils.px2dip(mContext, point.x)+",")
                    .append(UIUtils.px2dip(mContext, point.y)+" ]\n");
        }
        Log.i(TAG, "ControlPointList: "+sb.toString());
    }
}

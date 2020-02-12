package com.hard.function.BezierLines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.hard.function.R;
import com.hard.function.common.GridCoordinateCustomBaseView;
import com.hard.function.tool.UIUtils;
import com.hard.function.tool.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Jerry Lai on 2019/05/28
 * 用贝塞尔曲线绘制圆的过程
 * 正方形区域四边，四个控制点固定为每边中间点，每脚有两个按比例mRatio移动控制点，然后每脚
 * 4个控制点绘制三阶贝塞尔曲线
 */
public class BezierRun2CircleView extends GridCoordinateCustomBaseView {

    private int LINE_WIDTH;
    private float mRatio;   // 控制点占半径的比例
    private float mRadius;  //圆半径
    private int screenWidth, screenHeight;
    private int[] lineColor;    //控制线的颜色
    private List<PointF> controlPointList;  //控制点列表，顺序为：右上、右下、左下、左上
    private PointF mCenterPoint;    // 圆的中心点
    private Path bezierLinePath;// 贝塞尔曲线模拟圆过程的路径
    private Paint circlePaint;  //绘制圆的画笔
    private Paint bezierLinePaint;  // 绘制贝塞尔曲线的画笔
    private Paint controlLinePaint;     //绘制控制线的画笔
    private Context mContext;

    public BezierRun2CircleView(Context context) {
        super(context);
    }

    public BezierRun2CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BezierRun2CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void init(Context context) {
        mContext = context;
        screenWidth = UIUtils.getScreenWidth(context);
        screenHeight = UIUtils.getScreenHeight(context);
        mRadius = screenWidth / 3;
        LINE_WIDTH = UIUtils.dip2px(2);
        mRatio = 0.55f;
        lineColor = new int[4];
        lineColor[0] = context.getColor(R.color.colorYellow);
        lineColor[1] = context.getColor(R.color.colorGreenLight);
        lineColor[2] = context.getColor(R.color.colorOrange);
        lineColor[3] = context.getColor(R.color.colorPinkDark);
        mCenterPoint = new PointF(0, 0);
        controlPointList = new ArrayList<>();
        bezierLinePath = new Path();

        bezierLinePaint = new Paint();
        bezierLinePaint.setAntiAlias(true);
        bezierLinePaint.setStyle(Paint.Style.FILL);
        bezierLinePaint.setColor(R.color.colorLowBlue);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(LINE_WIDTH);
        circlePaint.setColor(R.color.colorRed);

        controlLinePaint = new Paint();
        controlLinePaint.setAntiAlias(true);
        controlLinePaint.setStyle(Paint.Style.STROKE);
        controlLinePaint.setStrokeWidth(LINE_WIDTH);

        calculateControlPoint();
    }

    /**
     * 计算圆的控制点
     */
    private void calculateControlPoint() {
        // 计算 中间控制点到端点的距离
        float controlWidth = mRatio * mRadius;
        controlPointList.clear();

        // 右上
        controlPointList.add(new PointF(0, -mRadius));
        controlPointList.add(new PointF(controlWidth, -mRadius));
        controlPointList.add(new PointF(mRadius, -controlWidth));

        // 右下
        controlPointList.add(new PointF(mRadius, 0));
        controlPointList.add(new PointF(mRadius, controlWidth));
        controlPointList.add(new PointF(controlWidth, mRadius));

        // 左下
        controlPointList.add(new PointF(0, mRadius));
        controlPointList.add(new PointF(-controlWidth, mRadius));
        controlPointList.add(new PointF(-mRadius, controlWidth));
        // 左上
        controlPointList.add(new PointF(-mRadius, 0));
        controlPointList.add(new PointF(-mRadius, -controlWidth));
        controlPointList.add(new PointF(-controlWidth, -mRadius));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCoordinateGrid(canvas);
        canvas.translate(mWidth/2, mHeight/2);
        bezierLinePath.reset();
        for (int i = 0; i < 4; i++) {
            if (i == 0){
                bezierLinePath.moveTo(controlPointList.get(i*3).x, controlPointList.get(i*3).y);
            }else {
                bezierLinePath.lineTo(controlPointList.get(i*3).x, controlPointList.get(i*3).y);
            }
            int endPointIndex = i==3 ? 0 : (i*3 + 3);
            //画4个控制点的三阶贝塞尔曲线，API
            bezierLinePath.cubicTo(controlPointList.get(i*3+1).x, controlPointList.get(i*3+1).y,
                    controlPointList.get(i*3+2).x, controlPointList.get(i*3+2).y,
                    controlPointList.get(endPointIndex).x, controlPointList.get(endPointIndex).y);
        }

        //绘制贝塞尔曲线
        canvas.drawPath(bezierLinePath, bezierLinePaint);

        //绘制目标圆
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, circlePaint);

        //绘制控制线
        for (int i = 0; i < controlPointList.size(); i++) {
            controlLinePaint.setColor(lineColor[i / 3]);
            int endPointIndex = (i == controlPointList.size()-1) ? 0 : i+1;
            canvas.drawLine(controlPointList.get(i).x, controlPointList.get(i).y,
                    controlPointList.get(endPointIndex).x, controlPointList.get(endPointIndex).y,
                    controlLinePaint);
        }
    }

    /**
     * 设置比例-----（0~1）
     */
    public void setRatio(float ratio){
        this.mRatio = ratio;
        calculateControlPoint();
        invalidate();
    }
}

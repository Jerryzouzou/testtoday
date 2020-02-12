package com.hard.function.BezierLines;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hard.function.R;
import com.hard.function.common.GridCoordinateCustomBaseView;
import com.hard.function.tool.UIUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Jerry Lai on 2019/06/12
 */
public class DIYBezierView extends GridCoordinateCustomBaseView {

    private PointF centerPointF;    //圆心
    //控制点列表，顺序为：右上、右下、左下、左上
    private List<PointF> controlPointList;
    //选中的点集合，受 status 影响
    private List<PointF> curSelectPointList;
    private PointF curSelectPoint;

    private Path controlPath;
    private Path diyBezierPath;
    private Paint diyBezierPaint;
    private Paint controlLinePaint;
    private Paint controlPointPaint;
    private Paint circlePaint;
    private Status status;  //拖曳时的状态
    private Resources res;
    private Context mContext;

    // 线的宽度
    private int LINE_WIDTH;
    // 控制点的半径
    private int POINT_RADIO_WIDTH;
    // 选中控制点的半径
    private int SEL_POINT_RADIO_WIDTH;
    private float mRadius;      //圆半径
    private float mRatio;       //控制点占半径的比例
    // 是否显示辅助线
    private boolean mIsShowHelpLine;
    // 触碰的x轴
    private float mLastX = -1;
    // 触碰的y轴
    private float mLastY = -1;

    // 有效触碰的范围
    private int mTouchRegionWidth;

    public DIYBezierView(Context context) {
        super(context);
    }

    public DIYBezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DIYBezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        mContext = context;
        res = mContext.getResources();
        int width = UIUtils.getScreenWidth(context);
        mRadius = width / 4;
        LINE_WIDTH = UIUtils.dip2px(2);
        POINT_RADIO_WIDTH = UIUtils.dip2px(4);
        SEL_POINT_RADIO_WIDTH = UIUtils.dip2px(6);
        mTouchRegionWidth = UIUtils.dip2px(20);

        centerPointF = new PointF(0, 0);
        controlPointList = new ArrayList<>();
        curSelectPointList = new ArrayList<>();
        controlPath = new Path();
        diyBezierPath = new Path();
        mIsShowHelpLine = true;
        mRatio = 0.55f;
        status = Status.FREE;

        diyBezierPaint = UIUtils.getFillPaint(Color.GREEN);
        circlePaint = UIUtils.getStrokePaint(context, LINE_WIDTH, res.getColor(R.color.colorOrange, null));
        controlLinePaint =UIUtils.getStrokePaint(context, LINE_WIDTH, Color.RED);
        controlPointPaint =UIUtils.getFillPaint(Color.RED);

        calculateControlPoint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCoordinateGrid(canvas);
        canvas.translate(mWidth/2, mHeight/2);

        diyBezierPath.reset();
        //计算每边控制基线的贝塞尔曲线路径
        for (int i = 0; i < 4; i++) {
            if(i == 0){
                diyBezierPath.moveTo(controlPointList.get(i*3).x, controlPointList.get(i*3).y);
            }else {
                diyBezierPath.lineTo(controlPointList.get(i*3).x, controlPointList.get(i*3).y);
            }
            int endPonitIndex = (i==3) ? 0 : (i*3+3);
            diyBezierPath.cubicTo(controlPointList.get(i*3+1).x, controlPointList.get(i*3+1).y,
                    controlPointList.get(i*3+2).x, controlPointList.get(i*3+2).y,
                    controlPointList.get(endPonitIndex).x, controlPointList.get(endPonitIndex).y);
        }
        canvas.drawPath(diyBezierPath, diyBezierPaint);

        if(!mIsShowHelpLine) return;
        canvas.drawCircle(centerPointF.x, centerPointF.y, mRadius, circlePaint);

        //绘制控制基线
        controlPath.reset();
        for (int i = 0; i < 4; i++) {
            int startIndex = i * 3;
            if(i == 0){
                controlPath.moveTo(controlPointList.get(controlPointList.size()-1).x,
                        controlPointList.get(controlPointList.size() - 1).y);
            }else {
                controlPath.moveTo(controlPointList.get(startIndex - 1).x,
                        controlPointList.get(startIndex - 1).y);
            }
            controlPath.lineTo(controlPointList.get(startIndex).x, controlPointList.get(startIndex).y);
            controlPath.lineTo(controlPointList.get(startIndex+1).x, controlPointList.get(startIndex+1).y);
        }
        canvas.drawPath(controlPath, controlLinePaint);

        //绘制控制点
        for (int i = 0; i < controlPointList.size(); i++) {
            PointF point = controlPointList.get(i);
            float w;
            if(curSelectPointList.contains(point)){
                controlPointPaint.setColor(Color.BLUE);
                w = SEL_POINT_RADIO_WIDTH;
            }else {
                controlPointPaint.setColor(Color.RED);
                w = POINT_RADIO_WIDTH;
            }
            canvas.drawCircle(point.x, point.y, w, controlPointPaint);
        }

        // 如果为三点拽动，将三点连接
        if(status == Status.THREE){
            if(curSelectPointList.size() == 1) return;
            for (int i = 0; i < curSelectPointList.size() - 1; i++) {
                controlPointPaint.setColor(Color.BLUE);
                canvas.drawLine(curSelectPointList.get(i).x, curSelectPointList.get(i).y,
                        curSelectPointList.get(i+1).x, curSelectPointList.get(i+1).y, controlPointPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(selectControlPoint(x, y)){
                    mLastX = x;
                    mLastY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mLastY==-1 || mLastX==-1){
                    return true;
                }

                float offsetX = x - mLastX;
                float offsetY = y - mLastY;
                if((status==Status.MIRROR_DIFF || status==Status.MIRROR_SAME) && curSelectPoint!=null){
                    curSelectPoint.x += offsetX;
                    curSelectPoint.y += offsetY;

                    if(status == Status.MIRROR_DIFF){
                        offsetX = -offsetX;
                        offsetY = -offsetY;
                    }
                    PointF otherPoint = null;
                    for (PointF point : curSelectPointList){
                        if(point != curSelectPoint){
                            otherPoint = point;
                            break;
                        }
                    }
                    if(otherPoint != null){
                        otherPoint.x += offsetX;
                        otherPoint.y += offsetY;
                    }
                }else{
                    for (PointF point : curSelectPointList){
                        point.x += offsetX;
                        point.y += offsetY;
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                curSelectPointList.clear();
                curSelectPoint = null;
                mLastY = mLastX = -1;
                break;
        }

        invalidate();
        return true;
    }

    /**
     * 是否在有效的触碰范围
     */
    private boolean selectControlPoint(float x, float y) {
        int selectIndex = -1;
        for (int i = 0; i < controlPointList.size(); i++) {
            PointF point = controlPointList.get(i);
            float realX = point.x + mWidth/2;
            float realY = point.y + mHeight/2;
            RectF rectRange = new RectF(realX-mTouchRegionWidth, realY-mTouchRegionWidth,
                    realX+mTouchRegionWidth, realY+mTouchRegionWidth);
            if(rectRange.contains(x, y)){
                selectIndex = i;
                break;
            }
        }
        if(selectIndex == -1){
            return false;
        }

        curSelectPointList.clear();
        curSelectPoint = controlPointList.get(selectIndex);
        switch (status){
            case FREE:      // 任意点拽动
                curSelectPointList.add(curSelectPoint);
                break;
            case THREE:
                int offsetSeleIndex = (selectIndex + 1) % 12;
                int offsetRangeIndex = offsetSeleIndex / 3;
                if(offsetSeleIndex == 0){
                    curSelectPointList.add(controlPointList.get(11));
                }else {
                    curSelectPointList.add(controlPointList.get(offsetRangeIndex*3 - 1));
                }
                curSelectPointList.add(controlPointList.get(offsetRangeIndex*3));
                curSelectPointList.add(controlPointList.get(offsetRangeIndex*3 + 1));
                break;
            case MIRROR_DIFF:
            case MIRROR_SAME:
                if(selectIndex==0 || selectIndex==6){
                    curSelectPointList.add(controlPointList.get(0));
                    curSelectPointList.add(controlPointList.get(6));
                }else {
                    curSelectPointList.add(controlPointList.get(selectIndex));
                    curSelectPointList.add(controlPointList.get(12 - selectIndex));
                }
                break;
        }

        return true;
    }

    public void reset(){
        calculateControlPoint();
        invalidate();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 设置比例--印象计算控制点因子
     */
    public void setRatio(float ratio) {
        this.mRatio = ratio;
        calculateControlPoint();
        invalidate();
    }

    public void setIsShowHelpLine(boolean isShowHelpLine) {
        this.mIsShowHelpLine = isShowHelpLine;
        invalidate();
    }

    public List<PointF> getControlPointList() {
        return controlPointList;
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

    public enum Status {
        FREE,          // 自由拽动
        THREE,         // 三点拽动
        MIRROR_DIFF,   // 镜像异向
        MIRROR_SAME,   // 镜像同向
    }
}

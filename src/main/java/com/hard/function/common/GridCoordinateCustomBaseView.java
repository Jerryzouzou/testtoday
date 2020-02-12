package com.hard.function.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.hard.function.tool.PrintOut;
import com.hard.function.tool.UIUtils;

import androidx.annotation.Nullable;

/**
 * @author Jerry Lai
 * 带坐标和网格背景基础view
 */
public abstract class GridCoordinateCustomBaseView extends View {

    protected String TAG = this.getClass().getSimpleName();
    private Context mContext;

    // 坐标画笔
    private Paint mCoordinatePaint;
    // 网格画笔
    private Paint mGridPaint;
    // 写字画笔
    private Paint mTextPaint;

    private int mCoordinateColor;
    private int mGridColor;

    // 网格宽度 50px
    private int mGridWidth = 50;
    // 坐标线宽度
    private final float mCoordinateLineWidth = 5f;
    // 标柱的高度
    private final float mCoordinateFlagHeight = 15f;
    // 网格宽度
    private final float mGridLineWidth = 2f;
    // 字体大小
    private float mTextSize;

    public float mWidth;
    public float mHeight;

    public GridCoordinateCustomBaseView(Context context) {
        super(context);
        mContext = context;
        initPaint(context);
        init(context);
    }

    public GridCoordinateCustomBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint(context);
        init(context);
    }

    public GridCoordinateCustomBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initPaint(context);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        PrintOut.printJerryDebugLog("onSizeChanged init mWidth="+mWidth+"--mHeight="+mHeight);
    }

    //给子类初始化实现
    protected abstract void init(Context context);

    private void initPaint(Context context) {
        mCoordinateColor = Color.BLACK;
        mGridColor = Color.LTGRAY;
        mTextSize = UIUtils.sp2px(context, 10);

        mCoordinatePaint = new Paint();
        mCoordinatePaint.setAntiAlias(true);    //抗锯齿
        mCoordinatePaint.setColor(mCoordinateColor);
        mCoordinatePaint.setStrokeWidth(mCoordinateLineWidth);

        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(mGridColor);
        mGridPaint.setStrokeWidth(mGridLineWidth);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);      //文本对齐方式
        mTextPaint.setColor(mCoordinateColor);
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 画网格和坐标，以图形中心为原点
     */
    protected  void drawCoordinateGrid(Canvas canvas){
        float halfWidth = mWidth / 2;
        float halfHeight = mHeight / 2;

        //画网格线
        canvas.save();
        canvas.translate(halfWidth, halfHeight);
        //画X-Y轴
        canvas.drawLine(0, -halfHeight, 0, halfHeight, mCoordinatePaint);
        canvas.drawLine(-halfWidth, 0, halfWidth, 0, mCoordinatePaint);

        int curWidth = mGridWidth;
        // 画竖线
        while (curWidth < halfWidth + mGridWidth){
            // 向右画
            canvas.drawLine(curWidth, -halfHeight, curWidth, halfHeight, mGridPaint);
            // 向左画
            canvas.drawLine(-curWidth, -halfHeight, -curWidth, halfHeight, mGridPaint);

            //画标注
            canvas.drawLine(curWidth, 0, curWidth, -mCoordinateFlagHeight, mCoordinatePaint);
            canvas.drawLine(-curWidth, 0, -curWidth, -mCoordinateFlagHeight, mCoordinatePaint);

            //画刻度
            if(curWidth % (mGridWidth << 1) == 0){
                canvas.drawText(""+curWidth, curWidth, mTextSize*1.5f, mTextPaint);
                canvas.drawText(-curWidth+"", -curWidth, mTextSize*1.5f, mTextPaint);
            }

            curWidth += mGridWidth;
        }

        //画横线
        int curHeight = mGridWidth;
        while (curHeight < (halfHeight + mGridWidth)){
            //向下画
            canvas.drawLine(-halfWidth, curHeight, halfWidth, curHeight, mGridPaint);
            //向上画
            canvas.drawLine(-halfWidth, -curHeight, halfWidth, -curHeight, mGridPaint);
            //画标注
            canvas.drawLine(0, curHeight, mCoordinateFlagHeight, curHeight, mCoordinatePaint);
            canvas.drawLine(0, -curHeight, mCoordinateFlagHeight, -curHeight, mCoordinatePaint);

            //画刻度
            if(curHeight % (mGridWidth<<1) == 0){
                canvas.drawText(curHeight+"", -(mTextSize*2f), curHeight, mTextPaint);
                canvas.drawText(-curHeight+"", -(mTextSize*2f), -curHeight, mTextPaint);
            }

            curHeight += mGridWidth;
        }

        canvas.restore();
    }
}

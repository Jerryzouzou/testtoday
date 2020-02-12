package com.hard.function.BezierLines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hard.function.R;
import com.hard.function.common.GridCoordinateCustomBaseView;
import com.hard.function.tool.UIUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Jerry Lai
 * 贝塞尔曲线，
 */
public class BezierRunView extends GridCoordinateCustomBaseView {

    private Context mContext;
    private Handler mHandler;

    // 准备状态
    public static final int PREPARE = 0x0001;
    // 运行状态
    public static final int RUNNING = 0x0002;
    // 停止状态
    public static final int STOP = 0x0004;
    // 帧数：1000，即1000个点来绘制一条线
    private static final int FRAME = 1000;
    // handler 事件
    private static final int HANDLE_EVENT = 0x101;
    private static final int HANDLE_CLEAN = 0x102;

    //贝塞尔曲线的路径
    private Path mBezierPath;
    //绘制贝塞尔曲线的画笔
    private Paint mBezierPaint;
    // 控制点的画笔
    private Paint mControlPaint;
    // 绘制端点的画笔
    private Paint mPointPaint;
    // 中间阶层的线画笔
    private Paint mIntermediaPaint;
    // 绘字笔
    private Paint mTextPaint;

    // 初始默认的控制点
    private List<PointF> DEFAULT_POINT;
    //控制点坐标
    private List<PointF> mControlPointList;
    //贝塞尔曲线计算出路径的坐标
    private List<PointF> mBezierPointList;
    //不同阶的贝塞尔曲线的颜色
    private List<Integer> mLineColor;
    // 绘制时，贝塞尔曲线的点
    private PointF mCurBezierPoint;
    // 当前选中的点
    private PointF mCurSelectPoint;
    /**
     * 层级说明：
     *  最里级 pointList保存的是当前阶数（order-i）控制点的边上u%*length的点的集合,一个pointList一条边
     *  中间级 orderPointList 保存的是当前阶数（order-i）所有边此次保存好的pointList
     *  最外级 intermediateList 保存的是每一阶所需要的辅助信息，例如intermediateList[0]是order阶所需的
     *  (order-1)阶的贝塞尔曲线所有辅助信息,intermediateList最后一组orderPointList的最后一条边pointList就是最终的贝塞尔曲线
     */
    private List<List<List<PointF>>> mIntermediateList = new ArrayList<>();

    /**
     * 层级说明：
     * 第1层：边的数据
     * 第2层：边中的点数据
     */
    private final List<List<PointF>> mIntermediateDrawList = new ArrayList<>();

    // 有效触碰的范围
    private int mTouchRegionWidth;
    // 当前状态
    private int mState = PREPARE;
    // 速率，每次绘制跳过的帧数，等于10，即表示每次绘制跳过10帧
    private int mRate = 10;
    // 当前的比例
    private float mCurRatio;
    // 最高阶的控制点个数
    private int mPointCount;
    // 是否绘制降阶线,就是辅助显示线
    private boolean isShowReduceOrderLine = true;
    // 是否循环播放
    private boolean isLoop;
    // 普通线的宽度
    private int LINE_WIDTH;
    // 贝塞尔曲线的宽度
    private int BEZIER_LINE_WIDTH;
    // 控制点的半径
    private int POINT_RADIO_WIDTH;


    public BezierRunView(Context context) {
        super(context);
    }

    public BezierRunView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BezierRunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        mContext = context;
        mHandler = new MyHandler(this);

        LINE_WIDTH = UIUtils.dip2px(2);
        BEZIER_LINE_WIDTH = UIUtils.dip2px(3);
        POINT_RADIO_WIDTH = UIUtils.dip2px(4);
        mTouchRegionWidth = UIUtils.dip2px(20); // 触碰范围

        mState = PREPARE;

        mLineColor = new ArrayList<>();
        mLineColor.add(context.getColor(R.color.colorYellow));
        mLineColor.add(context.getColor(R.color.colorGreenLight));
        mLineColor.add(context.getColor(R.color.colorBlueDark));
        mLineColor.add(context.getColor(R.color.colorGrayDark));
        mLineColor.add(context.getColor(R.color.colorPinkDark));
        mLineColor.add(context.getColor(R.color.colorOrange));

        int width = UIUtils.getScreenWidth(context);
        DEFAULT_POINT = new ArrayList<>();
        DEFAULT_POINT.add(new PointF(width / 10, width));
        DEFAULT_POINT.add(new PointF(width / 5, width / 5));
        DEFAULT_POINT.add(new PointF(width / 3, width / 2));
        DEFAULT_POINT.add(new PointF(width / 2, width / 8));
        DEFAULT_POINT.add(new PointF(width / 5 * 4, width / 4));
        DEFAULT_POINT.add(new PointF(width / 7 * 6, width /3 * 2));
        DEFAULT_POINT.add(new PointF(width / 7 * 6, width /5 * 4));
        DEFAULT_POINT.add(new PointF(width / 2, width /2 *3));

        /*DEFAULT_POINT.add(new PointF(width / 5, width / 5));
        DEFAULT_POINT.add(new PointF(width / 3, width / 2));
        DEFAULT_POINT.add(new PointF(width / 3 * 2, width / 4));
        DEFAULT_POINT.add(new PointF(width / 2, width / 3));
        DEFAULT_POINT.add(new PointF(width / 4 * 2, width / 8));
        DEFAULT_POINT.add(new PointF(width / 5 * 4, width / 12));
        DEFAULT_POINT.add(new PointF(width / 5 * 4, width));
        DEFAULT_POINT.add(new PointF(width / 2, width));*/

        //初始化控制点
        mControlPointList = new ArrayList<>();
        mPointCount = 8;
        for (int i = 0; i < mPointCount; i++) {
            if(i > DEFAULT_POINT.size()){
                break;
            }
            mControlPointList.add(DEFAULT_POINT.get(i));
        }

        mBezierPaint = new Paint();
        mBezierPaint.setAntiAlias(true);
        mBezierPaint.setStrokeWidth(BEZIER_LINE_WIDTH);
        mBezierPaint.setStyle(Paint.Style.STROKE);  //描边
        mBezierPaint.setColor(getBezierLineColor());
        mBezierPaint.setStrokeCap(Paint.Cap.ROUND);     //起始圆头

        mControlPaint = new Paint();
        mControlPaint.setAntiAlias(true);
        mControlPaint.setColor(getControlLineColor());
        mControlPaint.setStrokeWidth(LINE_WIDTH);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(UIUtils.sp2px(context, 15));

        mIntermediaPaint = new Paint();
        mIntermediaPaint.setAntiAlias(true);
        mIntermediaPaint.setStrokeWidth(LINE_WIDTH);

        //初始化存放贝塞尔曲线最终结果的路径
        mBezierPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画坐标和网格
        drawCoordinateGrid(canvas);
        //绘制控制基础线和点
        drawControlLine(canvas);
        //绘制贝塞尔曲线
        canvas.drawPath(mBezierPath, mBezierPaint);
        if(mState != PREPARE){
            mPointPaint.setStyle(Paint.Style.FILL);
            if(isShowReduceOrderLine){
                for (int i = 0; i < mIntermediateDrawList.size(); i++) {
                    List<PointF> lineList = mIntermediateDrawList.get(i);
                    mIntermediaPaint.setColor(getColor(i));
                    mPointPaint.setColor(getColor(i));
                    for (int j = 0; j < lineList.size() - 1; j++) {
                        //画线
                        canvas.drawLine(lineList.get(j).x, lineList.get(j).y, lineList.get(j+1).x,
                                lineList.get(j+1).y, mIntermediaPaint);
                        //画线的第一点，第二点是下一条线的第一点
                        canvas.drawCircle(lineList.get(j).x, lineList.get(j).y,POINT_RADIO_WIDTH,
                                mPointPaint);
                    }
                    //补齐最后一条线的第二点
                    canvas.drawCircle(lineList.get(lineList.size()-1).x, lineList.get(lineList.size()-1).y,
                            POINT_RADIO_WIDTH, mPointPaint);
                }
            }

            //播放完并且不循环播放的时候重置按键信息
            if(mCurRatio==1 && !isLoop && getContext() instanceof BezierBeginDerivationActivity){
                ((BezierBeginDerivationActivity) getContext()).resetPlayBtn();
            }

            mPointPaint.setColor(getBezierLineColor());
            canvas.drawCircle(mCurBezierPoint.x, mCurBezierPoint.y, POINT_RADIO_WIDTH, mPointPaint);
            mHandler.sendEmptyMessage(HANDLE_EVENT);
        }

        canvas.drawText("u = " + mCurRatio, UIUtils.getScreenWidth(mContext) / 4,
                UIUtils.getScreenHeight(mContext) * 11 / 12, mTextPaint);
    }

    /**
     * 绘制控制基础线和点
     */
    private void drawControlLine(Canvas canvas) {
        mPointPaint.setColor(getControlLineColor());

        //绘制控制点
        for (PointF point : mControlPointList){
            mPointPaint.setStyle(Paint.Style.FILL);     //实心点
            mPointPaint.setStrokeWidth(0);
            canvas.drawCircle(point.x, point.y, POINT_RADIO_WIDTH, mPointPaint);

            mPointPaint.setStyle(Paint.Style.STROKE);   //点外再画一个环
            mPointPaint.setStrokeWidth(1);
            canvas.drawCircle(point.x, point.y, POINT_RADIO_WIDTH+2, mPointPaint);
        }

        //绘制控制基线
        for (int i = 0; i < mControlPointList.size() - 1; i++) {
            canvas.drawLine(mControlPointList.get(i).x, mControlPointList.get(i).y,
                    mControlPointList.get(i+1).x, mControlPointList.get(i+1).y, mControlPaint);
        }
    }


    public void start(){
        mBezierPath.reset();    //启动前先重置
        mState = RUNNING;
        if(mControlPointList.size() > 5){
            mBezierPointList = BezierUtils.calculateBezierPointByDp(mControlPointList, FRAME);  //动态规划计算贝塞尔曲线的点
        }else {
            mBezierPointList = BezierUtils.buildBezierPoint(mControlPointList, FRAME);  //计算贝塞尔曲线的点
        }
        prepareBezierPath();    //将贝塞尔曲线的点串成path
        if(isShowReduceOrderLine){  //是否计算中间阶级辅助线的点
            mIntermediateList.clear();
            mIntermediateDrawList.clear();
            mIntermediateList = BezierUtils.calculateIntermediateLine(mControlPointList, FRAME);
        }
        mCurRatio = 0;
        setCurBezierPoint(mBezierPointList.get(0));
        invalidate();
    }

    public void clean(){
       mHandler.sendEmptyMessage(HANDLE_CLEAN);
    }

    /**
     * 暂停或者继续
     */
    public void pause(){
        if (mState == RUNNING){
            mState = STOP;
        }else if(mState == STOP){
            mState = RUNNING;
            mHandler.sendEmptyMessage(HANDLE_EVENT);
        }
    }

    /**
     * 将计算好的 贝塞尔曲线的点 组装成路径
     * 至于这路径中有多少个点，取决于{@link #FRAME}属性的值
     */
    private void prepareBezierPath(){
        for (int i = 0; i < mBezierPointList.size(); i++) {
            PointF point = mBezierPointList.get(i);
            if(i == 0){
                mBezierPath.moveTo(point.x, point.y);
            }else {
                mBezierPath.lineTo(point.x, point.y);
            }
        }
    }

    /**
     *  控制线控制点颜色
     */
    private int getControlLineColor() {
        return mContext!=null ? mContext.getColor(R.color.colorLowBlue) : null;
    }

    /**
     *  贝塞尔曲线颜色
     */
    private int getBezierLineColor() {
        return mContext!=null ? mContext.getColor(R.color.colorRed) : null;
    }

    /**
     * 设置贝塞尔曲线阶数，就是控制点数+1
     * @param order 范围2~7
     */
    public void setOrder(int order){
        this.mPointCount = order + 1;
        mControlPointList.clear();
        for (int i = 0; i < mPointCount; i++) {
            if(mPointCount > DEFAULT_POINT.size()){
                break;
            }
            mControlPointList.add(DEFAULT_POINT.get(i));
        }
    }

    /**
     * 获取对应颜色，并防止越界
     */
    private int getColor(int index){
        return mLineColor.get(index % mLineColor.size());
    }

    public int getTouchRegionWidth() {
        return mTouchRegionWidth;
    }

    public void setTouchRegionWidth(int mTouchRegionWidth) {
        this.mTouchRegionWidth = mTouchRegionWidth;
    }

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public int getRate() {
        return mRate;
    }

    public void setRate(int mRate) {
        this.mRate = mRate;
    }

    public float getCurRatio() {
        return mCurRatio;
    }

    public void setCurRatio(float mCurRatio) {
        this.mCurRatio = mCurRatio;
    }

    public int getPointCount() {
        return mPointCount;
    }

    public void setPointCount(int mPointCount) {
        this.mPointCount = mPointCount;
    }

    public boolean isShowReduceOrderLine() {
        return isShowReduceOrderLine;
    }

    public void setShowReduceOrderLine(boolean showReduceOrderLine) {
        isShowReduceOrderLine = showReduceOrderLine;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    private int getBezierPointListSize() {
        return mBezierPointList.size();
    }

    private void setCurBezierPoint(PointF curBezierPoint) {
        this.mCurBezierPoint = curBezierPoint;
    }

    private List<PointF> getBezierPointList() {
        return mBezierPointList;
    }

    /**
     * 设置绘制值 intermediateDrawList
     */
    private void setIntermediateDrawList(List<List<PointF>> intermediateDrawList) {
        mIntermediateDrawList.clear();
        mIntermediateDrawList.addAll(intermediateDrawList);
    }

    private List<List<List<PointF>>> getIntermediateList() {
        return mIntermediateList;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mState != PREPARE){ // 没有在准备状态不能进行操作
            return true;
        }

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isLegalControlPoint(x, y);

                break;
            case MotionEvent.ACTION_MOVE:
                if(mCurBezierPoint == null) return true;
                mCurBezierPoint.x = x;
                mCurBezierPoint.y = y;
                mIntermediateList.clear();
                mIntermediateDrawList.clear();
                if(mBezierPointList != null){
                    mBezierPointList.clear();
                }
                mBezierPath.reset();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mCurBezierPoint = null;
                break;
        }

        return false;
    }

    /**
     * 判断触摸是否在控制点mControlPointList中的有效范围之内，如果是则将该点赋值为触摸点
     */
    private void isLegalControlPoint(float x, float y) {
        if(mCurBezierPoint == null){
            for (PointF point : mControlPointList){
                RectF pointRange = new RectF(point.x-mTouchRegionWidth, point.y-mTouchRegionWidth,
                        point.x+mTouchRegionWidth, point.y+mTouchRegionWidth);
                if(pointRange.contains(x, y)){
                    mCurBezierPoint = point;
                    break;
                }
            }
        }
    }

    private static final class MyHandler extends Handler{

        private final BezierRunView bezierRunView;
        private int mCurFrame;  // 当前帧数

        public MyHandler(BezierRunView bezierRunView) {
            this.bezierRunView = bezierRunView;
            this.mCurFrame = 0;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLE_EVENT){
                if(bezierRunView.getState() == STOP) return;

                mCurFrame += bezierRunView.getRate();   //+=速率
                if(mCurFrame >= bezierRunView.getBezierPointListSize()){
                    mCurFrame = 0;
                    if(!bezierRunView.isLoop()){
                        bezierRunView.setState(PREPARE);
                        bezierRunView.setCurRatio(1);
                        bezierRunView.setIntermediateDrawList(new ArrayList<List<PointF>>());
                        bezierRunView.invalidate();
                        return;
                    }
                }

                List<PointF> bezierPoint = bezierRunView.getBezierPointList();
                bezierRunView.setCurBezierPoint(bezierPoint.get(mCurFrame));

                if(bezierRunView.isShowReduceOrderLine()){
                    List<List<List<PointF>>> intermediateList = bezierRunView.getIntermediateList();
                    //实时变化辅助线，需要从intermediateList重新组装
                    List<List<PointF>> intermediateDrawList = new ArrayList<>();

                    for (int i = 0; i < intermediateList.size(); i++) {
                        List<List<PointF>> lineList = intermediateList.get(i);
                        List<PointF> intermediatePoint = new ArrayList<>();
                        for (int j = 0; j < lineList.size(); j++) {
                            intermediatePoint.add(lineList.get(j).get(mCurFrame));
                        }
                        intermediateDrawList.add(intermediatePoint);
                    }
                    bezierRunView.setIntermediateDrawList(intermediateDrawList);
                }

                int ratio = (int) ((((float)mCurFrame/bezierPoint.size())*100)*100f);
                bezierRunView.setCurRatio(ratio>1 ? 1 : ratio);
                bezierRunView.invalidate();
            }else if(msg.what == HANDLE_CLEAN){
                mCurFrame = 0;
            }
        }
    }
}

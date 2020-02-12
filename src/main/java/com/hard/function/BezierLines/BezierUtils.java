package com.hard.function.BezierLines;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jerry Lai on 2019/05/20
 * 贝塞尔曲线计算工具类，二阶AD/AB = BE/BC = DF/DE = u(比例值)，多阶进行降阶
 * 贝塞尔曲线计算公式：Pik = (1-u) * Pi(k-1) + u * P(i+1)(k-1)
 * 1、k 表示阶数，当 k=n 时，即相当于前面demo所讲的一阶控制点；当 k=0 时，表示最高阶的控制点，即我们程序猿最初给定的那几个控制点；
 * 2、i 表示点的下标，这个只是为了便于区分；
 * 3、u 表示比例值
 */
public class BezierUtils {

    // x轴坐标
    public static final int X_TYPE = 1;
    // y轴坐标
    public static final int Y_TYPE = 2;

    /**
     * @param controlPointList 控制基点list
     * @param frame 帧数
     * @return  计算好有frame帧的贝塞尔曲线的点list
     */
    public static List<PointF> buildBezierPoint(List<PointF> controlPointList, int frame){
        List<PointF> bezierPointList = new ArrayList<>();
        float dalta = 1f / frame;
        int order = controlPointList.size() - 1;    //贝塞尔曲线阶数
        for (float u = 0; u <= 1; u += dalta) {
            bezierPointList.add(new PointF(calculateBezierPoint(X_TYPE, u, order, 0, controlPointList),
                    calculateBezierPoint(Y_TYPE, u, order, 0, controlPointList)));
        }
        return bezierPointList;
    }

    /**
     * 递归方式计算上面方法贝塞尔曲线的点坐标
     * @param type             {@link #X_TYPE} 表示x轴的坐标， {@link #Y_TYPE} 表示y轴的坐标
     * @param u                当前的比例
     * @param k                阶数
     * @param pi               当前坐标（具体为 x轴 或 y轴）
     * @param controlPointList 控制点的坐标
     *
     * 公式解说：（p表示坐标点，后面的数字只是区分）
     *  场景：有一条线p1到p2，p0在中间，求p0的坐标
     *       p1◉--------○----------------◉p2
     *             u    p0
     * 公式：p0 = p1+u*(p2-p1) 整理得出 p0 = (1-u)*p1+u*p2
     */
    private static float calculateBezierPoint(int type, float u, int k, int pi, List<PointF> controlPointList) {
        if(k == 1){     //base case = 1阶贝塞尔曲线
            float p1, p2;
            if(type == X_TYPE){
                p1 = controlPointList.get(pi).x;
                p2 = controlPointList.get(pi+1).x;
            }else {
                p1 = controlPointList.get(pi).y;
                p2 = controlPointList.get(pi+1).y;
            }
            return ((1 - u)*p1+u*p2);
        }else {
            return ((1 - u)*calculateBezierPoint(type, u, k-1, pi, controlPointList)+
                    u*calculateBezierPoint(type,u, k-1, pi+1,controlPointList));
        }
    }

    /**
     * 动态规划方式计算上面方法贝塞尔曲线的点坐标（参考下面方法calculateIntermediateLine，比计算中间辅助
     * 信息多计算一阶，最后那阶就是贝塞尔曲线）
     * 从高阶到低阶不断进行降阶计算，(order-i)是计算(order-i-1)阶所有的u%的点，然后intermediateList集合，
     * 计算到最低阶就是对应的贝塞尔曲线的点集合，即intermediateList最后一组orderPointList的最后一条边
     * pointList就是最终的贝塞尔曲线，即intermediateList.get(i_size-1).get(o_size-1)
     */
    public static List<PointF> calculateBezierPointByDp(List<PointF> controlPointList, int frame) {
        List<List<List<PointF>>> intermediateList = new ArrayList<>();
        int order = controlPointList.size() - 1;    //阶数
        float delta = 1f / frame;   //增长偏量

        /**
         * 一阶时没有辅助线，二阶时是一条辅助线，只需计算(order-1)次，当前计算阶数是（order-i）的中间
         * 阶辅助信息
         */
        for (int i = 0; i < order; i++) {
            List<List<PointF>> orderPointList = new ArrayList<>();
            /**
             * 终止条件为每一阶的边的条数，阶数与边数相等
             * 随着i的增大，即阶数的降低，相应的需要计算的边数对应减少
             */
            for (int j = 0; j < order - i; j++) {
                List<PointF> pointList = new ArrayList<>();
                for (float u = 0; u <= 1; u+=delta) {
                    float p1x;
                    float p1y;
                    float p2x;
                    float p2y;
                    int beforeOrderCurPointIndex = (int) (u * frame);   // 上一阶中，对应的当前帧的下标

                    if (intermediateList.size() == 0){
                        p1x = controlPointList.get(j).x;
                        p1y = controlPointList.get(j).y;
                        p2x = controlPointList.get(j+1).x;
                        p2y = controlPointList.get(j+1).y;
                    }else {
                        //获取上一阶辅助线1 u位置的点
                        p1x = intermediateList.get(i - 1).get(j).get(beforeOrderCurPointIndex).x;
                        p1y = intermediateList.get(i - 1).get(j).get(beforeOrderCurPointIndex).y;
                        //获取上一阶辅助线2 u位置的点
                        p2x = intermediateList.get(i - 1).get(j+1).get(beforeOrderCurPointIndex).x;
                        p2y = intermediateList.get(i - 1).get(j+1).get(beforeOrderCurPointIndex).y;
                    }
                    float p0x = (1 - u)*p1x + u*p2x;
                    float p0y = (1 - u)*p1y + u*p2y;
                    pointList.add(new PointF(p0x, p0y));
                }
                orderPointList.add(pointList);
            }
            intermediateList.add(orderPointList);
        }
        int i_size = intermediateList.size();
        int o_size = intermediateList.get(i_size - 1).size();
        return intermediateList.get(i_size-1).get(o_size-1);
    }

    /**
     * 计算中间阶级辅助信息，三级保存。
     *  最里级 pointList保存的是当前阶数（order-i）控制点的边上u%*length的点的集合,一个pointList一条边
     *  中间级 orderPointList 保存的是当前阶数（order-i）所有边此次保存好的pointList
     *  最外级 intermediateList 保存的是每一阶所需要的辅助信息，例如intermediateList[0]是order阶所需的
     *  (order-1)阶的贝塞尔曲线所有辅助信息
     * @param controlPointList 控制点集合
     * @param frame 帧数，即一条边上有多少个点
     * @return intermediateList 返回中间阶级具体信息
     */
    public static List<List<List<PointF>>> calculateIntermediateLine(List<PointF> controlPointList, int frame){
        List<List<List<PointF>>> intermediateList = new ArrayList<>();
        int order = controlPointList.size() - 1;    //阶数
        float delta = 1f / frame;   //增长偏量

        /**
         * 一阶时没有辅助线，二阶时是一条辅助线，只需计算(order-1)次，当前计算阶数是（order-i）的中间
         * 阶辅助信息
         */
        for (int i = 0; i < order - 1; i++) {
            List<List<PointF>> orderPointList = new ArrayList<>();
            /**
             * 终止条件为每一阶的边的条数，阶数与边数相等
             * 随着i的增大，即阶数的降低，相应的需要计算的边数对应减少
             */
            for (int j = 0; j < order - i; j++) {
                List<PointF> pointList = new ArrayList<>();
                for (float u = 0; u <= 1; u+=delta) {
                    float p1x;
                    float p1y;
                    float p2x;
                    float p2y;
                    int beforeOrderCurPointIndex = (int) (u * frame);   // 上一阶中，对应的当前帧的下标

                    if (intermediateList.size() == 0){
                        p1x = controlPointList.get(j).x;
                        p1y = controlPointList.get(j).y;
                        p2x = controlPointList.get(j+1).x;
                        p2y = controlPointList.get(j+1).y;
                    }else {
                        //获取上一阶辅助线1 u位置的点
                        p1x = intermediateList.get(i - 1).get(j).get(beforeOrderCurPointIndex).x;
                        p1y = intermediateList.get(i - 1).get(j).get(beforeOrderCurPointIndex).y;
                        //获取上一阶辅助线2 u位置的点
                        p2x = intermediateList.get(i - 1).get(j+1).get(beforeOrderCurPointIndex).x;
                        p2y = intermediateList.get(i - 1).get(j+1).get(beforeOrderCurPointIndex).y;
                    }
                    float p0x = (1 - u)*p1x + u*p2x;
                    float p0y = (1 - u)*p1y + u*p2y;
                    pointList.add(new PointF(p0x, p0y));
                }
                orderPointList.add(pointList);
            }
            intermediateList.add(orderPointList);
        }
        return intermediateList;
    }
}

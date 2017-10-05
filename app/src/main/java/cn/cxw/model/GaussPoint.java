package cn.cxw.model;

import cn.cxw.util.NumberUtil;

/**
 * Created by CXW-IBM on 2017/8/28.
 * 高斯坐标点模型
 */

public class GaussPoint {
    public double x;
    public double y;

    public GaussPoint(){}

    public GaussPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return String.format("X：%s  Y：%s", NumberUtil.formatNumber(x, "##,#####"), NumberUtil.formatNumber(y, "##,#####"));
    }
}

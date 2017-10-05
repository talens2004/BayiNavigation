package cn.cxw.model;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;

import cn.cxw.util.CoordinateUtil;

/**
 * Created by CXW-IBM on 2017/8/28.
 * 一个点
 */

public class Point {
    //高斯直角坐标
    public GaussPoint gauss;
    //gps原始经纬度
    public LatLng gps;
    //百度经纬度
    public LatLng bd;

    public Point(){}
    //从百度位置得到点坐标
    public Point(BDLocation bdLocation){
        bd = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        gps = CoordinateUtil.BDToGPS(bdLocation);
        gauss = CoordinateUtil.GPSToGauss(gps.latitude, gps.longitude);
    }
    //从高斯坐标得到点
    public Point(GaussPoint gaussPoint){
        gauss = gaussPoint;
        gps = CoordinateUtil.GaussToGPS(gauss.x, gauss.y);
        bd = CoordinateUtil.GPSToBD(gps.latitude, gps.longitude);
    }
    //从百度坐标得到点
    public Point(LatLng bdll){
        this.bd = bdll;
        gps = CoordinateUtil.BDToGPS(bdll.latitude, bdll.longitude);
        gauss = CoordinateUtil.GPSToGauss(gps.latitude, gps.longitude);
    }
    //从横纵坐标得到点
    public Point(double bdLatitude, double bdLongitude){
        bd = new LatLng(bdLatitude, bdLongitude);
        gps = CoordinateUtil.BDToGPS(bd.latitude, bd.longitude);
        gauss = CoordinateUtil.GPSToGauss(gps.latitude, gps.longitude);
    }

}

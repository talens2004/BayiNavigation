package cn.cxw.core;

import android.util.Log;

import com.baidu.mapapi.model.CoordUtil;
import com.baidu.mapapi.model.LatLng;

import cn.cxw.model.GaussPoint;
import cn.cxw.model.Point;
import cn.cxw.util.CoordinateUtil;
import cn.cxw.util.SDCardHelper;

import static cn.cxw.util.CoordinateUtil.GPSToBD;

public class Constants {

    //分页加载数据每页条数
    public final static int pageSize = 20;
    //使用区域常量
    public final static LatLng northeast = new LatLng(43, 113);
    public final static LatLng southwest = new LatLng(42, 112);
    public final static LatLng beijing = new LatLng(39, 116);
    public final static LatLng chengde = new LatLng(41, 118);

    //地图缩放级别范围
    public static final float MAX_LEVEL = 16f;
    public static final float DEFAULT_LEVEL = 15f;
    public static final float MIN_LEVEL = 10f;

    //List刷新消息常量
    public static final int hasData = 1;
    public static final int noLeftData = 2;

    //偏差纠正
    public final static GaussPoint dp = new GaussPoint(-80, -2);

    public static boolean isPointEffect(LatLng pt) {
        if (pt.latitude < beijing.latitude || pt.latitude > northeast.latitude || pt.longitude < southwest.longitude || pt.longitude > chengde.longitude) {
            return false;
        }
        return true;
    }

    public static final String sdCardRootDirName = SDCardHelper.getInnerSDCardPath() + "/ArmyMap";
    public static final String sdCardTileDirName = sdCardRootDirName + "/LocalTileImage";
    public static final String sdCardDbBackupDirName = sdCardRootDirName + "/DbBackup";

    public static void testInfo() {

        String info = "";
        LatLng gps = GPSToBD(42.105658, 112.136444);
        info += "\n左下角百度坐标-纬度：" + String.format("%.6f", gps.latitude) + " 经度：" + String.format("%.6f", gps.longitude);
        gps = GPSToBD(42.501480, 112.849185);
        info += "\n右上角百度坐标-纬度：" + String.format("%.6f", gps.latitude) + " 经度：" + String.format("%.6f", gps.longitude);

        com.baidu.mapapi.model.inner.Point p = CoordUtil.ll2point(GPSToBD(42.105658, 112.136444));
        info += "\n左下角像素坐标-----x:" + p.x + "  y:" + p.y;
        com.baidu.mapapi.model.inner.Point p1 = CoordUtil.ll2point(GPSToBD(42.501480, 112.849185));
        info += "\n右上角像素坐标-----x:" + p1.x + "  y:" + p1.y;

        GaussPoint gauss = CoordinateUtil.GPSToGauss(42.105658, 112.136444);
        info += "\n左下角高斯坐标-x：" + String.format("%.0f", gauss.x) + " y:" + String.format("%.0f", gauss.y);
        gauss = CoordinateUtil.GPSToGauss(42.501480, 112.849185);
        info += "\n右上角高斯坐标-x：" + String.format("%.0f", gauss.x) + " y:" + String.format("%.0f", gauss.y);

    }

    public static void getRect() {
        Point sw = new Point(new GaussPoint(4661000 + dp.x, 19598000 + dp.y));
        Point ne = new Point(new GaussPoint(4717000 + dp.x, 19657000 + dp.y));
        String info = "";


        com.baidu.mapapi.model.inner.Point p = CoordUtil.ll2point(sw.bd);
        info += "\n左下角像素坐标-----x:" + p.x + "  y:" + p.y;
        com.baidu.mapapi.model.inner.Point p1 = CoordUtil.ll2point(ne.bd);
        info += "\n右上角像素坐标-----x:" + p1.x + "  y:" + p1.y;


        GaussPoint gauss = CoordinateUtil.GPSToGauss(sw.gps);
        info += "\n左下角高斯坐标-x：" + String.format("%.0f", gauss.x) + " y:" + String.format("%.0f", gauss.y);
        gauss = CoordinateUtil.GPSToGauss(ne.gps);
        info += "\n右上角高斯坐标-x：" + String.format("%.0f", gauss.x) + " y:" + String.format("%.0f", gauss.y);

        Log.e("MainActivity", info);
    }
}

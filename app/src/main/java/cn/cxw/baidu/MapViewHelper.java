package cn.cxw.baidu;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;

import cn.cxw.core.Constants;
import cn.cxw.model.GaussPoint;
import cn.cxw.util.CoordinateUtil;

/**
 * Created by CXW-IBM on 2017/9/3.
 */

public class MapViewHelper {

    /**
     * 设置中心点
     */
    public static void setUserMapCenter(BaiduMap mBaiduMap, GaussPoint p) {
        //将高斯坐标转换为经纬度
        LatLng cenpt = CoordinateUtil.GaussToGPS(p.x, p.y);
        float zoom = Constants.DEFAULT_LEVEL;
        cenpt = CoordinateUtil.GPSToBD(cenpt.latitude, cenpt.longitude);
        setUserMapCenter(mBaiduMap, cenpt, zoom);
    }

    /**
     * 设置中心点
     */
    public static void setUserMapCenter(BaiduMap mBaiduMap, LatLng pt) {
        //将高斯坐标转换为经纬度
        float zoom = Constants.DEFAULT_LEVEL;
        setUserMapCenter(mBaiduMap, pt, zoom);
    }

    public static void setUserMapCenter(BaiduMap mBaiduMap, LatLng cenpt, float zoom) {
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(zoom)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //动画切换改变地图状态
        //mBaiduMap.setMapStatus(mMapStatusUpdate);
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
    }
}

package cn.cxw.baidu;

import android.app.Activity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import cn.cxw.armymap.R;
import cn.cxw.armymap.dialogs.Dialogs;
import cn.cxw.dao.TrailsDao;
import cn.cxw.model.GaussPoint;
import cn.cxw.model.Point;
import cn.cxw.model.TrailItems;
import cn.cxw.model.Trails;
import cn.cxw.util.CoordinateUtil;

/**
 * Created by CXW-IBM on 2017/8/28.
 */

public class Markers {

    public static final int TRAIL_COLOR = 0xAAFF0000;
    public static final int ROAD_COLOR = 0xAAFFBB00;
    public static final int HIGHELIGHT_ROAD_COLOR = 0x99FF0000;

    public static class Anchor {
        public float anchorX;
        public float anchorY;

        public Anchor(double anchorX, double anchorY) {
            this.anchorX = (float) anchorX;
            this.anchorY = (float) anchorY;
        }

        public Anchor(float anchorX, float anchorY) {
            this.anchorX = anchorX;
            this.anchorY = anchorY;
        }
    }

    public static void clickMarker(Activity activity, Marker marker) {
        LatLng pt = marker.getPosition();
        GaussPoint gp = CoordinateUtil.BDToGauss(pt);
        Dialogs.showPointInfoDialog(activity, pt, gp);
    }

    public static void showCoordLines(BaiduMap mBaiduMap, MapStatus mapStatus) {
        LatLng target = mapStatus.target;
        Point p = new Point(target);

        //mBaiduMap.getProjection().fromScreenLocation(point);

        //mBaiduMap.getProjection().toScreenLocation(latlng);

    }

    //清除所有标记
    public static void clearTrail(BaiduMap mBaiduMap) {
        mBaiduMap.clear();
    }

    //更新轨迹
    public static void updateTrail(BaiduMap mBaiduMap, LatLng lastPoint, LatLng newPoint) {
        //构造纹理资源
        BitmapDescriptor custom1 = BitmapDescriptorFactory.fromResource(R.mipmap.icon_road_red_arrow);
        //构造纹理队列
        List<BitmapDescriptor> customList = new ArrayList<BitmapDescriptor>();
        customList.add(custom1);

        List<LatLng> points = new ArrayList<LatLng>();
        List<Integer> index = new ArrayList<Integer>();
        points.add(lastPoint);//点元素
        index.add(0);//设置该点的纹理索引
        points.add(newPoint);//点元素

        //构造对象
        OverlayOptions ooPolyline = new PolylineOptions()
                .width(8)
                .color(TRAIL_COLOR)
                .points(points)
                .dottedLine(false)
                .customTextureList(customList)
                .textureIndex(index);
        //添加到地图
        mBaiduMap.addOverlay(ooPolyline);
    }

    public static void showTrail(BaiduMap mBaiduMap, Trails trail) {
        int color16 = TRAIL_COLOR;
        showTrail(mBaiduMap, trail, color16);
    }

    public static void showTrail(BaiduMap mBaiduMap, Trails trail, int color16) {
        int width = 8;
        showTrail(mBaiduMap, trail, color16, width);
    }

    public static void showTrail(BaiduMap mBaiduMap, Trails trail, int color16, int w) {
        List<TrailItems> list = TrailsDao.getTrailItems(trail);
        if (list.size() < 2) {
            return;
        }

        // 构造折线点坐标
        List<LatLng> points = new ArrayList<LatLng>();
        for (TrailItems item : list) {
            points.add(new LatLng(item.latitude, item.longitude));
        }

        OverlayOptions ooPolyline = new PolylineOptions().width(w).color(color16).points(points);
        mBaiduMap.addOverlay(ooPolyline);
    }

    // 绘制所有道路
    public static void showAllRoads(BaiduMap mBaiduMap) {
        List<Trails> trails = TrailsDao.getAllRoads();
        for (Trails t : trails) {
            showTrail(mBaiduMap, t, ROAD_COLOR);
        }
    }

    public static Marker addMarker(BaiduMap mBaiduMap, LatLng point) {
        int iconId = R.mipmap.icon_gcoding;
        return addMarker(mBaiduMap, point, iconId);
    }

    public static Marker addMarker(BaiduMap mBaiduMap, LatLng point, int iconId) {
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(iconId);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .draggable(true);  //设置手势拖拽
        //将marker添加到地图上
        Marker marker = (Marker) (mBaiduMap.addOverlay(option));
        return marker;
    }

    public static Marker addMarker(BaiduMap mBaiduMap, LatLng point, BitmapDescriptor bitmap, Anchor anchor) {
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .anchor(anchor.anchorX, anchor.anchorY)
                .draggable(true);  //设置手势拖拽
        //将marker添加到地图上
        Marker marker = (Marker) (mBaiduMap.addOverlay(option));
        return marker;
    }

    public static Overlay addTextLabel(BaiduMap mBaiduMap, LatLng point, String text) {
        OverlayOptions ooText = new TextOptions()
                .bgColor(0xAAFFFF00)
                .fontSize(28)
                .fontColor(0xFF000000)
                .text(text)
                .position(point);
        return mBaiduMap.addOverlay(ooText);
    }
}

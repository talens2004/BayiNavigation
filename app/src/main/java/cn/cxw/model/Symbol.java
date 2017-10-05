package cn.cxw.model;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;

import cn.msqlite.Annotations;

/**
 * Created by CXW-IBM on 2017/9/17.
 */

@Annotations.TableName("Symbol")
public class Symbol {
    @Annotations.PrimaryKey
    @Annotations.Default("1")
    public long id;
    //方案Id
    public long planId;
    //纬度
    public double bdLatitude;
    //经度
    public double bdLongitude;
    //名称
    public String name;
    //图标资源Id
    public int iconResId;
    //图标标签
    public Marker iconMarker;
    //文本标签
    public Overlay textLabel;

    public LatLng getPosition(){
        return new LatLng(bdLatitude, bdLongitude);
    }

}

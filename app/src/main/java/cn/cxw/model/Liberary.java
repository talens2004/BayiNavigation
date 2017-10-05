package cn.cxw.model;

import com.baidu.mapapi.model.LatLng;

import cn.cxw.util.DateHelper;
import cn.msqlite.Annotations;

/**
 * Created by CXW-IBM on 2017/9/3.
 */

@Annotations.TableName("Liberary")
public class Liberary {

    @Annotations.PrimaryKey
    @Annotations.Default("1")
    public long id;
    public String name;
    public double x;
    public double y;
    public double latitude;
    public double longitude;
    public long createTime;

    public Liberary(){ }
    public Liberary(String name, LatLng pt, GaussPoint point){
        this.name = name;
        this.x = point.x;
        this.y = point.y;
        this.latitude = pt.latitude;
        this.longitude = pt.longitude;
        createTime = System.currentTimeMillis();
    }

    public String getCreateTime(){
        return DateHelper.convertTime2Date(createTime);
    }

    public String getGaussRemark(){
        return new GaussPoint(x, y).toString();
    }
}

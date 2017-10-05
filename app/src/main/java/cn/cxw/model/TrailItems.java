package cn.cxw.model;

import cn.msqlite.Annotations;

/**
 * Created by CXW-IBM on 2017/9/2.
 */

@Annotations.TableName("TrailItems")
public class TrailItems {

    @Annotations.PrimaryKey
    @Annotations.Default("1")
    public long id;

    public long trailId;
    public double latitude;
    public double longitude;
    public long time;


    public TrailItems(){}
    public TrailItems(long trailId, double latitude, double longitude, long time) {
        this.trailId = trailId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String pointToString(){
        Point p = new Point(latitude, longitude);
        return p.gauss.toString();
    }
}

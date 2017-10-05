package cn.cxw.dao;

import java.util.List;

import cn.cxw.model.TrailItems;
import cn.cxw.model.Trails;

import static cn.cxw.core.Constants.pageSize;

/**
 * Created by CXW-IBM on 2017/9/3.
 */

public class TrailsDao extends BaseDao {


    public static long saveTrail(Trails trail){
        return db.insert(trail);
    }

    public static int updateTrail(Trails trail){
        return db.update(trail);
    }

    public static void deleteTrail(Trails trail){
        //删除所有点
        db.delete(TrailItems.class, "trailId = " + trail.id);
        //删除记录
        db.delete(trail);
    }

    public static long saveTrailItem(TrailItems item){
        return db.insert(item);
    }


    public static List<Trails> getAllRoads(){
        return db.select(Trails.class,  "isRoad = 1", null, null, null);
    }

    public static List<Trails> getTrails(int pages){
        return db.select(Trails.class,  null, null, "id desc", (pages - 1) * pageSize + "," + pageSize);
    }

    public static List<TrailItems> getTrailItems(Trails trail){
        return db.select(TrailItems.class, "trailId = " + trail.id , null, null, null);
    }

    public static TrailItems getTrailItem(long id){
        return db.select(TrailItems.class, "id = " + id , null, null, null).get(0);
    }
}
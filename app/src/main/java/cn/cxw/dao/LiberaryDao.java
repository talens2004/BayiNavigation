package cn.cxw.dao;

import java.util.List;

import cn.cxw.model.Liberary;

import static cn.cxw.core.Constants.pageSize;

/**
 * Created by CXW-IBM on 2017/9/3.
 */

public class LiberaryDao extends BaseDao {

    //保存单个点
    public static long savePoint(Liberary lib) {
        return db.insert(lib);
    }

    //更新点信息
    public static int updatePoint(Liberary lib){
        return db.update(lib);
    }

    //更新点信息
    public static int deletePoint(Liberary lib){
        return db.delete(lib);
    }

    //获取最近20条记录，从第1页开始
    public static List<Liberary> getLibPoints(int pages) {
        return db.select(Liberary.class, null, null, "createTime desc", (pages - 1) * pageSize + "," + pageSize);
    }
}

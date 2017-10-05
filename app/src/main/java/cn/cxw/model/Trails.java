package cn.cxw.model;

import cn.msqlite.Annotations;

/**
 * Created by CXW-IBM on 2017/9/2.
 * 轨迹数据库
 */

@Annotations.TableName("Trails")
public class Trails {

    @Annotations.PrimaryKey
    @Annotations.Default("1")
    public long id;
    public String title;
    public boolean isRoad = false;
    public long startItemId;
    public long endItemId;
}

package cn.cxw.model;

import com.baidu.mapapi.map.Marker;

import java.util.HashMap;
import java.util.Map;

import cn.msqlite.Annotations;

/**
 * Created by CXW-IBM on 2017/9/17.
 * 标图方案
 */

@Annotations.TableName("Plan")
public class Plan {

    @Annotations.PrimaryKey
    @Annotations.Default("1")
    public long id;
    //方案名称
    public String title;
    //时间
    public long updateTimestamp;
    //图标
    public Map<Marker, Symbol> symbolMap = new HashMap<Marker, Symbol>();

}

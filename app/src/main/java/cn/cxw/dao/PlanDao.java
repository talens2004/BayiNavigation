package cn.cxw.dao;

import java.util.List;

import cn.cxw.model.Plan;
import cn.cxw.model.Symbol;

/**
 * Created by CXW-IBM on 2017/9/17.
 */

public class PlanDao extends BaseDao {
    //保存标图
    public static long savePlan(Plan plan) {
        return db.insert(plan);
    }

    //更新标图
    public static int updatePlan(Plan plan) {
        return db.update(plan);
    }

    //获取最后的标图方案
    public static Plan getLatestPlan() {
        List<Plan> list = db.select(Plan.class, null, null, "updateTimestamp desc", "1");
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static long saveSymbol(Symbol symbol) {
        return db.insert(symbol);
    }

    public static long updateSymbol(Symbol symbol) {
        return db.update(symbol);
    }

    public static int deleteSymbol(Symbol symbol) {
        return db.delete(symbol);
    }
}

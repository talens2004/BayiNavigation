package cn.cxw.dao;


import cn.cxw.armymap.MainApplication;
import cn.cxw.core.DatabaseHelper;

public class BaseDao {
	public static DatabaseHelper db = MainApplication.dbHelper;
	public static MainApplication mApp = MainApplication.INSTANCE;
}

package cn.cxw.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import cn.cxw.model.Liberary;
import cn.cxw.model.Plan;
import cn.cxw.model.Symbol;
import cn.cxw.model.TrailItems;
import cn.cxw.model.Trails;
import cn.msqlite.MSQLiteOpenHelper;
import cn.msqlite.models.Table;


/**
 * 数据处理类
 * @author cxw
 *
 */
public class DatabaseHelper extends MSQLiteOpenHelper
{
    private static final String TAG = "DatabaseHelper";
    private static final int DB_VERSION = 3;
    public static final String DB_NAME = "cn.cxw.armymap.db";
	
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        TABLE_CACHES.put(Trails.class, new Table(Trails.class));
        TABLE_CACHES.put(TrailItems.class, new Table(TrailItems.class));
        TABLE_CACHES.put(Liberary.class, new Table(Liberary.class));
        TABLE_CACHES.put(Plan.class, new Table(Plan.class));
        TABLE_CACHES.put(Symbol.class, new Table(Symbol.class));
    }

    /**
     * 数据库第1次创建时调用
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d(TAG, "create db");
        createTable(db, Trails.class, true);
        createTable(db, TrailItems.class, true);
        createTable(db, Liberary.class, true);
        createTable(db, Plan.class, true);
        createTable(db, Symbol.class, true);
    }

    /**
     * 数据库版本发生变化时调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.e(TAG, "drop db");
//        dropTable(db, Trails.class, true);
//        dropTable(db, TrailItems.class, true);
//        dropTable(db, Liberary.class, true);
//        dropTable(db, Plan.class, true);
//        dropTable(db, Symbol.class, true);
        onCreate(db);
    }
    
    /**
     * 重置数据库某表
     * @param clz
     */
    public void resetTable(Class<?> clz){
    	SQLiteDatabase db = openDatabase();
        dropTable(db, clz, true);
        createTable(db, clz, true);
        closeDatabase();
    }
    
    /**
     * 保存对象
     * @param list
     */
    public <T> void saveObjects(List<T> list){
    	if(list.isEmpty()) 
    		return;
    			
    	SQLiteDatabase db = openDatabase();
    	Table table = TABLE_CACHES.get(list.get(0).getClass());
    	for(T t : list){
    		insert(db, table, t);
    	}
        closeDatabase();
    }
    
    // 根据条件语句删除数据库
    public void delete(Class<?> clz, String whereClause){
    	SQLiteDatabase db = openDatabase();
    	deleteFrom(db, clz, whereClause, null);
        closeDatabase();
    }
    
    public <T> void resetAndSave(List<T> list){
    	if(list.isEmpty()) 
    		return;

    	SQLiteDatabase db = openDatabase();
    	Class<?> clz = list.get(0).getClass();
    	Table table = TABLE_CACHES.get(clz);
        dropTable(db, clz, true);
        createTable(db, clz, true);
    	for(T t : list)
    		insert(db, table, t);

        closeDatabase();
    }
}
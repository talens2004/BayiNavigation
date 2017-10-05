package cn.msqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cn.msqlite.models.Column;
import cn.msqlite.models.Table;
import cn.msqlite.queries.CreateTable;
import cn.msqlite.queries.Drop;

public abstract class MSQLiteOpenHelper extends SQLiteOpenHelper
{
	// 同步器
//	private static AtomicInteger mOpenCounter = new AtomicInteger(); 
	private static int mOpenCounter = 0;
    private SQLiteDatabase mDatabase;  
    
    public synchronized SQLiteDatabase openDatabase() {  
    	if(mOpenCounter < 0)
    		mOpenCounter = 0;
    	    	
        if(++mOpenCounter == 1) {  
        	System.out.println("创建数据库" + mOpenCounter + "次");
            // Opening new database  
            mDatabase = getWritableDatabase();  
        }  
        return mDatabase;  
    }  
  
    public synchronized void closeDatabase() { 
        if(--mOpenCounter == 0) {  
        	System.out.println("正准备关闭数据库" + mOpenCounter + "次"); 
            // Closing database  
            mDatabase.close();  
        }  
    }  
     
	/**
	 * 当选择某行时执行接口动作
	 * @author cxw
	 *
	 * @param <T>
	 */
	public static interface OnRowSelectedListener<T>
	{
		void onRowSelected(Cursor cursor, T Object);
	}
	/**
	 * collection of classes that will be automatically converted to database TABLE_CACHES in on create
	 * and upgraded in opUpgrade
	 */
	protected final Collection<Class<?>> classes  = new HashSet<Class<?>>();

    /**
     * cache the table meta information instead of compute it at each time.
     */
    public final static Map<Class<?>, Table> TABLE_CACHES = new HashMap<Class<?>, Table>();

//	// Constructors
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	public MSQLiteOpenHelper(Context context, String name, CursorFactory factory,
//			int version, DatabaseErrorHandler errorHandler) {
//		super(context, name, factory, version, errorHandler);
//	}
	
	public MSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public MSQLiteOpenHelper(Context context, String name, CursorFactory factory,
			int version, Collection<Class<?>> trackedClasses)
	{
		this(context, name, factory, version);
		trackClasses(trackedClasses);
	}
	
	public MSQLiteOpenHelper(Context context, String name, CursorFactory factory,
			int version, Class<?>[] trackedClasses)
	{
		this(context, name, factory, version);
		trackClasses(trackedClasses);
	}

	/**
	 * Static methods,创建表
	 * @param database
	 * @param type
	 * @param ifNotExist
	 */
	public static void createTable(SQLiteDatabase database, Class<?> type, boolean ifNotExist)
	{
        createTable(database, TABLE_CACHES.get(type), ifNotExist);
	}
	
	public static void createTable(SQLiteDatabase database, Table table, boolean ifNotExist)
	{
		database.execSQL(new CreateTable(table).setIF_NOT_EXIST(ifNotExist).build());
	}
	
	private static void upgradeTable(SQLiteDatabase database, Table table)
	{
		Cursor cursor = database.rawQuery("PRAGMA table_info("+table.getName()+");", null);
		if (cursor.getCount() == 0)
		{
			createTable(database, table, false);
			Log.i("DatabaseUpgrade", "table created: "+table.getName());
		}
		else
		{
			Table currentDatabaseTable = Table.fromCursor(table.getName(), cursor);
			for (String sql : Table.upgradeTable(currentDatabaseTable, table))
			{
				database.execSQL(sql);
				Log.i("DatabaseUpgrade", "table altered. Query: "+sql);
			}
		}
	}
	
	public void upgradeDatabase()
	{
		SQLiteDatabase database = openDatabase();;
		upgradeDatabase(database);
		closeDatabase();
	}
	
	public void upgradeDatabase(SQLiteDatabase database)
	{
		for (Class<?> c : this.classes)
		{
			upgradeTable(database, TABLE_CACHES.get(c));
		}
	}
	
	/**
	 * Add classes to the collection of tracked classes.
	 * You should call this before onCreate to ensure that all TABLE_CACHES are automatically created for your classes.
	 */
	public void trackClasses(Collection<Class<?>> classes)
	{
		this.classes.addAll(classes);
	}
	
	/**
	 * Add single class to be automatically added to database in onCreate.
	 * @param trackedClass
	 */
	public void trackClass(Class<?> trackedClass)
	{
		this.classes.add(trackedClass);
	}
	
	/**
	 * Add classes to the collection of tracked classes.
	 * You should call this before onCreate to ensure that all TABLE_CACHES are automatically created for your classes.
	 */
	public void trackClasses(Class<?>[] classes)
	{
		for (Class<?> c : classes)
			trackClass(c);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		for (Class<?> c : classes)
			createTable(db, c, true);		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		upgradeDatabase(db);
	}
	
	/**
	 * Drops table in the database
	 * @param database SQLite database
	 * @param type Class of the database to be dropped
	 */
	public static void dropTable(SQLiteDatabase database, Class<?> type)
	{
		database.execSQL(new Drop(type).build());
	}
	
	public static void dropTable(SQLiteDatabase database, Class<?> type, boolean ifExists)
	{
		database.execSQL(new Drop(type).setIfExists(ifExists).build());
	}
	
	/**
	 * Delete Rows from database. Wrapper for SQLiteDatabase.delete()
	 */
	public static void deleteFrom(SQLiteDatabase database, Class<?> type, String whereClause, String [] whereArgs)
	{
		database.delete(TABLE_CACHES.get(type).getName(), whereClause, whereArgs);
	}
	
	private static int delete(SQLiteDatabase database, Table table, String whereClause, String [] whereArgs)
	{
		return database.delete(table.getName(), whereClause, whereArgs);
	}
	
	private static int delete(SQLiteDatabase database, Table table, Object item)
	{
		final String whereClause;
		final String [] whereArgs;
		
		if (table.getPrimaryKeys().isEmpty())
		{
			whereClause = table.getFullWhereClause();
			whereArgs = table.getFullWhereArgs(item);
		}
		else
		{
			whereClause = table.getPrimaryWhereClause();
			whereArgs = table.getPrimaryWhereArgs(item);
		}
		
		return delete(database, table, whereClause, whereArgs);
	} 
	
	public static int delete(SQLiteDatabase database, Object item)
	{
		Table table = TABLE_CACHES.get(item.getClass());
		return delete(database, table, item);
	}
	
	public static <T> int delete(SQLiteDatabase database, Class<T> type, Collection<T> items)
	{
		Table table = TABLE_CACHES.get(type);
		int n=0;
		for (T item : items)
			n += delete(database, table, item);
		
		return n;			
	}
	
	public <T> int delete(Class<T> type, Collection<T> items)
	{
		SQLiteDatabase database = openDatabase();;
		int result = delete(database, type, items);
		closeDatabase();
		
		return result;
	}
	
	public int delete(Object item)
	{
		SQLiteDatabase database = openDatabase();;
		int result = delete(database, item);
		closeDatabase();
		
		return result;
	}
	
	
	/**
	 * Query database for a number of rows
	 * @param database SQLiteDatabase to use
	 * @param type Type of object expected
	 * @return List of objects selected from database
	 * @throws InstantiationException Exception thrown if selected type could not be instantiated.
	 */
	public static <T> List<T> select(SQLiteDatabase database, Class<T> type, String selection, String [] selectionArgs, String orderBy, String limit)
	{
		return select(database, type, null, selection, selectionArgs, orderBy, limit);
	}
	
	public static <T> List<T> select(SQLiteDatabase database, Class<T> type, String [] columns, String selection, String [] selectionArgs, String orderBy, String limit)
	{
		List<T> result = new ArrayList<T>();
		Table table = TABLE_CACHES.get(type);
		
		Cursor cursor = database.query(table.getName(), columns, selection, selectionArgs, null, null, orderBy, limit);
		while (cursor.moveToNext())
		{
			result.add(table.getRow(cursor, type));
		}
		cursor.close();
		return result;
	}
	
	public static <T> T selectFirst(SQLiteDatabase database, Class<T> type, String selection, String [] selectionArgs, String orderBy)
	{
		Table table = TABLE_CACHES.get(type);
		Cursor cursor = database.query(table.getName(), null, selection, selectionArgs, null, null, orderBy, "1");
		final T result;

		if (cursor.moveToFirst())
			result = table.getRow(cursor, type);
		else 
			result = null;

		cursor.close();
		return result;
	}
	
	/**
	 * Same as normal select() method, but uses callback to pass each row separately instead of returning a list.
	 */
	public static <T> void selectForeach(SQLiteDatabase database, Class<T> type, OnRowSelectedListener<T> onRowSelectedListener, String selection, String [] selectionArgs, String orderBy, String limit)
	{
		Table table = TABLE_CACHES.get(type);
		
		Cursor cursor = database.query(table.getName(), null, selection, selectionArgs, null, null, orderBy, limit);
		while (cursor.moveToNext())
			onRowSelectedListener.onRowSelected(cursor, table.getRow(cursor, type));
		cursor.close();
	}
	
	/**
	 * Insert multiple rows into database
	 */
	public static<T> void insert(SQLiteDatabase database, Class<T> typeOfItem, Collection<T> items)
	{
		Table table = TABLE_CACHES.get(typeOfItem);
		for (Object row : items)
		{
			insert(database, table, row);
		}
	}
	
	/**
	 * Update corresponding row in the database.
	 * 
	 * All values in existing row will be updated with values in the object.
	 * 
	 * Object must declare at least one primary key and values of 
	 * primary keys must be the same as values for this row existing 
	 * in the database.
	 * 
	 * @param database database to use
	 * @param object an object to be updated, must have at least 1 PrimaryKey
	 */
	public static int update(SQLiteDatabase database, Object object)
	{
		Table table = TABLE_CACHES.get(object.getClass());
		return update(database, table, object, table.getPrimaryWhereClause(), table.getPrimaryWhereArgs(object));
	}
	
	/**
	 * Update row(s) with values in given object.
	 */
	private static int update(SQLiteDatabase database, Table table, Object object, String whereClause, String[] whereArgs)
	{
		database.update(table.getName(), table.getContentValues(object), whereClause, whereArgs);
		return update(database, table, table.getContentValues(object), whereClause, whereArgs);
	}
	
	private static int update(SQLiteDatabase database, Table table, Object object, Collection<String> columns, String whereClause, String[] whereArgs)
	{
		return update(database, table, table.getContentValues(object, columns), whereClause, whereArgs);
	}
	
	/**
	 * Update selected columns with values form this object
	 */
	public static int update(SQLiteDatabase database, Object object, Collection<String> columns, String whereClause, String[] whereArgs)
	{
		Table table = TABLE_CACHES.get(object.getClass());
		return update(database, table, object, columns, table.getPrimaryWhereClause(), table.getPrimaryWhereArgs(object));
	}
	
	private static int update(SQLiteDatabase database, Table table, ContentValues contentValues, String whereClause, String [] whereArgs)
	{
		return database.update(table.getName(), contentValues, whereClause, whereArgs);
	}
	
	/**
	 * Update multiple rows in database
	 * @param database database to use
	 * @param type Type of objects updated
	 * @param objects Collection of objects to be updated
	 * @return Collective number of affected rows
	 */
	public static <T> int update(SQLiteDatabase database, Class<T> type, Collection<T> objects)
	{
		Table table = TABLE_CACHES.get(type);
		String whereClause = table.getPrimaryWhereClause();
		int affectedRows = 0;
		
		for (T object : objects)
			affectedRows += update(database, table, object, whereClause, table.getPrimaryWhereArgs(object));
		
		return affectedRows;
	}
	
	/**
	 * Convenience method for static {@code update()}
	 * Gets its own instance of Writable database and disposes of it afterwards
	 * @param object
	 */
	public int update(Object object)
	{
		SQLiteDatabase database = openDatabase();;
		int affectedRows = update(database, object);
		closeDatabase();
		
		return affectedRows;
	}
	
	/**
	 * Convenience method for calling static version of this method.
	 * 
	 * This method gets it's own instance of {@link android.database.sqlite.SQLiteDatabase} and closes it afterwards.
	 */
	public <T> int update(Class<T> type, Collection<T> objects)
	{
		SQLiteDatabase database = openDatabase();
		int affectedRows = update(database, type, objects);
		closeDatabase();
		
		return affectedRows;
	}
	
	
	/**
	 * Selects multiple rows from an array. 
	 * This method gets its own instance of Database
	 */
	public <T> List<T> select(Class<T> type, String selection, String [] selectionArgs, String orderBy, String limit)
	{
		SQLiteDatabase database = openDatabase();
		List<T> result = select(database, type, selection, selectionArgs, orderBy, limit);
		closeDatabase();
		return result;
	}
	
	/**
	 * Selected all rows from given table
	 */
	public <T> List<T> selectAll(Class<T> type)
	{
		return select(type, null, null, null, null);
	}
	
	/**
	 * Insert single row to database
	 * If one of the fields is a Private Key, id will be assigned to it.
	 * @return id of the row
	 */
	public static long insert(SQLiteDatabase database, Object item)
	{
		return insert(database, TABLE_CACHES.get(item.getClass()), item);
	}
	
	public static long insert(SQLiteDatabase database, Table table, Object item)
	{
        Column primaryKey = table.getIntegerPrimaryKey();
        ContentValues v = table.getContentValues(item);
        // CAIXU: don't touch the primary key which is generated by sqlite
        v.remove(primaryKey.getName());
        
        long id = database.insert(table.getName(), null, v);
		table.setRowID(item, id);
		return id;
	}
		
	/**
	 * Insert single row to database.
	 * If one of the fields is a Private Key, id will be assigned to it.
	 * This method creates new instance of SQLiteDatabase
	 */
	public long insert(Object item)
	{
		SQLiteDatabase database = openDatabase();;
		long insertId = insert(database, item);
		closeDatabase();
		return insertId;
	}
	
	/**
	 * Insert multiple rows to database.
	 * If one of the fields is a Private Key, id will be assigned to it.
	 * This method creates new instance of SQLiteDatabase
	 */
	public <T> void insert(Class<T> classOfItem, Collection<T> items)
	{
		
		SQLiteDatabase database = openDatabase();;
		insert(database, classOfItem, items);
		closeDatabase();
	}
	
	private static long replace(SQLiteDatabase database, Table table, Object item)
	{
		long id = database.replace(table.getName(), null, table.getContentValues(item));
		table.setRowID(item, id);
		return id;
	}
	
	/**
	 * Replace item into database.
	 * If row already exists, it will be replaced. Otherwise new row is inserted,
	 * @return id of the replaced row
	 */
	public static long replace(SQLiteDatabase database, Object item)
	{
		return replace(database, TABLE_CACHES.get(item.getClass()), item);
	}
	
	/**
	 * Replace items into database.
	 * If row already exists, it will be replaced. Otherwise new row is inserted,
	 */
	public static <T> void replace(SQLiteDatabase database, Class<T> type, Collection<T> items)
	{
		Table table = TABLE_CACHES.get(type);
		for (T item : items)
			replace(database, table, item);
	}
	
	/**
	 * Convenience method for static method replace()
	 * gets an instance of writable SQLiteDatabase and closes it afterwards.
	 */
	public <T> void replace(Class<T> type, Collection<T> items)
	{
		SQLiteDatabase database = openDatabase();;
		replace(database, type, items);
		closeDatabase();
	}
	
	/**
	 * Convenience method for static method replace()
	 * gets an instance of writable SQLiteDatabase and closes it afterwards.
	 */
	public long replace(Object item)
	{
		SQLiteDatabase database = openDatabase();;
		long id = replace(database, item);
		closeDatabase();
		return id;
	}
	
	/**
	 * Runs a create table query on the database.
	 * This method creates new instance of SQLiteDatabase
	 * @param type Class defining the table
	 * @param ifNotExist table will not be created if already exists in database
	 */
	public void createTable(Class<?> type, boolean ifNotExist)
	{
		SQLiteDatabase database = openDatabase();;
		createTable(database, type, ifNotExist);
		closeDatabase();
	}
}

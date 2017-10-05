package cn.cxw.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;

import cn.cxw.armymap.MainApplication;


public class SharedPrefUtil {

	public static String getStringFromSP(String KEY){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApplication.INSTANCE);
        return sp.getString(KEY, "");
	}
	
    public static void saveStringToSP(String KEY, String str){	
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApplication.INSTANCE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY, str);
        editor.commit();
    }
	
	/**
	 * 将对象转换成json格式用于保存
	 * @param t
	 * @return
	 */
	public static <T> String castObjectToJson(T t){
		JSONObject data = new JSONObject();
		Field[] fields = t.getClass().getDeclaredFields();
		for (int index = 0; index < fields.length; index++) { 
			fields[index].setAccessible(true);
			try { 
				String fieldName = fields[index].getName();
				String fieldValue = fields[index].get(t) + "";
				data.put(fieldName, fieldValue == null ? "" : fieldValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return data.toString();
	}
	
	/**
	 * 从配置中取得对象
	 * @param cls
	 * @param KEY
	 * @return
	 */
    public static <T> T getObjectFromSP(Class<T> cls, String KEY){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApplication.INSTANCE);
        String json = sp.getString(KEY, "");
        if(TextUtils.isEmpty(json))
        	return null;
        
        return getObjectFromJsonString(cls, json);
    }

    /**
     * 将返回的对象数据直接以json形式保存到配置文件
     * @param t
     */
	public static <T> void saveObjectToSP(String KEY, T t){		
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApplication.INSTANCE);
        SharedPreferences.Editor editor = sp.edit();
        System.out.println("saveObjectToSP--将要写入配置的串是：Key:" + KEY + "Value:" + castObjectToJson(t));
        editor.putString(KEY, castObjectToJson(t));
        editor.commit();
	}
	

	/**
	 * 清除配置文件中的授权文件
	 */
	public static void deleteObjectFromSP(String KEY){	
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApplication.INSTANCE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(KEY);
        editor.commit();
	}
	
	/**
	 * 从json格式的字符串中获取对象
	 * @param cls
	 * @param jsonString
	 * @return
	 */
	public static <T> T getObjectFromJsonString(Class<T> cls, String jsonString){
		System.out.println("正准备转换json－－T getObjectFromJsonString");
		try {
			JSONObject jObj = new JSONObject(jsonString);
			return getObjectFromJsonObject(cls, jObj);
		} catch (Exception e) {
			System.out.println("json转换错误1，返回空");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 从json对象中获得数据
	 * @param cls
	 * @param jObj
	 * @return
	 */
	public static <T> T getObjectFromJsonObject(Class<T> cls, JSONObject jObj){
		System.out.println("正准备转换json－－T getObjectFromJsonObject" + jObj.toString());
		T t = null;
		try {
			t = cls.newInstance();
			Field[] fields = cls.getDeclaredFields();
			for (int index = 0; index < fields.length; index++) { 
				fields[index].setAccessible(true);
				String filedName = fields[index].getName();	
				if (fields[index].getType().getName().equalsIgnoreCase("java.lang.String")){
					fields[index].set(t, jObj.optString(filedName, ""));
				} else if (fields[index].getType().getName().equalsIgnoreCase("int")){
					fields[index].set(t, jObj.optInt(filedName, 0));
				} else if (fields[index].getType().getName().equalsIgnoreCase("double")){
					fields[index].set(t, jObj.optDouble(filedName, 0.0));
				} else if (fields[index].getType().getName().equalsIgnoreCase("boolean")){
					fields[index].set(t, jObj.optBoolean(filedName, false));
				} else if (fields[index].getType().getName().equalsIgnoreCase("long")){
					fields[index].set(t, jObj.optLong(filedName, 0));
				}				
			}		
		} catch (Exception e) {
			System.out.println("json转换错误");
			e.printStackTrace();
		}

		System.out.println("－－－对象转换成功");
		return t;
	}

}

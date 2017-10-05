package cn.cxw.util;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
	
	/**
	 * 判断字符串是否为有效的数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if(isEmpty(str))
			return false;
			
		Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches())
			return false;
		return true;
	}
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		if(str == null || str.isEmpty())
			return true;
		return false;
	}
	
	public static JSONObject getJSONFromString(String str){
		try {
			return new JSONObject(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

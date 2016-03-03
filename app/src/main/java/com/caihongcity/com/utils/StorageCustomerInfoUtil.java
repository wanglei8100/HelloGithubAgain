package com.caihongcity.com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/** 
* @author yuanjigong
* @ClassName: StorageCustomerInfoUtil 
* @Description: TODO   存储查询参数类
* @date 2014-8-12 下午10:59:43  
*/
public class StorageCustomerInfoUtil {
	
	public static void putInfo(Context context,String key,String value){
		
		SharedPreferences sps = context.getSharedPreferences("channelsignup", Context.MODE_PRIVATE);
	     Editor editor = sps.edit();
	     editor.putString(key, value);
	     
	     editor.commit();
	}

	
	public static String getInfo(String key,Context context){
		
		SharedPreferences sps = context.getSharedPreferences("channelsignup", Context.MODE_PRIVATE);
		String value = sps.getString(key, "");
		
		return value;
	}
	
	/**
	 * @param key
	 * @param context
	 * @return 
	 */
	public static boolean removeKey(String key,Context context){
		
	    SharedPreferences sp = context.getSharedPreferences("channelsignup",  
	    		Context.MODE_PRIVATE);  
	    Editor editor = sp.edit();
	    editor.remove(key);
	    editor.commit();
		return true;  
         
	}
	
	public static boolean clearKey(Context context){
			
		    SharedPreferences sp = context.getSharedPreferences("channelsignup",  
		    		Context.MODE_PRIVATE);  
		    Editor editor = sp.edit();
		    editor.clear();
		    editor.commit();
			return true;  
	         
		}
	 
}

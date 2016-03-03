package com.caihongcity.com.utils;

import com.google.gson.Gson;

/**
 * 解析json数据的工具类，解析方式 gson工具包解析
 * @author wangShan
 *
 */
public class GsonUtil {
	/**
	 * 使用gson工具包解析json的方法
	 * @param json 要解析的json字符串
	 * @param clazz 解析出来的类型的字节码文件
	 * @return
	 */
	public static <T> T jsonToBean(String json,Class<T> clazz){
		Gson gson = new Gson();
		return gson.fromJson(json, clazz);
	}
}

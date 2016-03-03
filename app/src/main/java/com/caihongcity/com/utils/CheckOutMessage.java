package com.caihongcity.com.utils;

import android.content.Context;
import android.text.TextUtils;

public class CheckOutMessage {

	
	/**
	 * 非空校验
	 * 如果校验的信息msg为空，返回true，切提示信息"name不能为空"
	 * @param context
	 * @param name
	 * @param msg
	 * @return
	 */
	public static boolean isEmpty(Context context,String name,String msg){
		if (TextUtils.isEmpty(msg)) {
			ViewUtils.makeToast(context, name+"不能为空", 1000);
			return true;
		}else {
			return false;
		}
	}
}

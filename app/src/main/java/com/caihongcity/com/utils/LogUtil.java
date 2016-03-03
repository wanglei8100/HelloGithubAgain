package com.caihongcity.com.utils;

import android.util.Log;

public class LogUtil {

//	public static  boolean IS_LOG = !(Constant.BASE_URL.equals("http://120.132.39.35:9000"));
	public static  boolean IS_LOG = true;

	private final static String LOG_TAG_STRING = "HNA_Android";

	public static void d(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.d(tag, tag + " : " + msg);
			}
		} catch (Throwable t) {
		    t.printStackTrace();
		}
	}

	public static void i(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.i(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}
	
	public static void v(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.v(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}

	public static void e(String tag, String msg) {
		try {
				Log.e(LOG_TAG_STRING, tag + " : " + msg);
		} catch (Throwable t) {
		}
	}
	public static void e(String msg) {
		try {
			Log.e(LOG_TAG_STRING," : " + msg);
		} catch (Throwable t) {
		}
	}
	
	public static void w(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.w(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}
	public static void syso(String msg) {
		try {
			if (IS_LOG) {
				System.out.println(msg);
			}
		} catch (Throwable t) {
		}
	}
	
	
}

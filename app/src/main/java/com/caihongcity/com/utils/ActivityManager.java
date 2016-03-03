package com.caihongcity.com.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/** 
 * @ClassName: ActivityManager 
 * @Description: TODO 
 * @author LiangYanQiao
 * @date 2014 2014-10-15 下午5:00:13 
 *  
 */
public class ActivityManager {

	private static List<Activity> mList = getInstance();
	private static LinkedList<Activity> instance;

	private ActivityManager() {
	}

	public synchronized static LinkedList<Activity> getInstance() {
		if (null == instance) {
			instance = new LinkedList<Activity>();
		}
		return instance;
	}

	/** add Activity */
	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	public static void exit() {
		try {
			for (Activity act : instance) {
				if (act != null)
					act.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	public static void exit2() {
		try {
			for (Activity act : mList) {
				if (act != null)
					act.finish();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}


	public static void finishActivity(int number) {
		try {
			for (int i = 0 ;i<number;i++) {
				Activity act = mList.get(mList.size()-i-1);
				if (act != null)
					act.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

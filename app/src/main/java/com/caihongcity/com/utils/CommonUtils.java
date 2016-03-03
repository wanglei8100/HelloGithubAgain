package com.caihongcity.com.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hkrt 常用的工具类
 */
public class CommonUtils {
	private static boolean checkNetState = true;

	public static String getTime() {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = df.format(new Date());
		return time;
	}

	public static String getTime(String format) {
		DateFormat df = new SimpleDateFormat(format);
		String time = df.format(new Date());
		return time;
	}
	private static long lastClickTime;
    /**
     * 设置事件不可连续点击
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
		LogUtil.e("timeD=="+timeD);
        if ( 0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

	/**
	 * 设置验证码不可连续点击
	 * @return
	 */
	public static boolean isFastDoubleClick2() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		LogUtil.e("timeD=="+timeD);
		if ( 0 < timeD && timeD < 1500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}


	/**
	   * 获取现在时间
	   *
	   * @return返回短时间格式 yyyy-MM-dd
	   */
	public static Date getDateFromTime(String time){


		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
//		DateFormat format 2= new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");         
		Date date = null;

		// String转Date
//		str = "2007-1-18";          
		try {
		           date = format1.parse(time);
//		           data = format2.parse(str); 
		} catch (ParseException e) {
		           e.printStackTrace();
		}

		return date;

	};

	/**
	 *
	 * @param d
	 *            今天
	 * @param day
	 *            几天前
	 * @return
	 */
	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	public static String getTime(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		String time = df.format(date);
		return time;
	}

	public static String getYearTime() {
		DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		String time = df.format(new Date());
		return time;
	}

	public static String getMonthTime() {
		DateFormat df = new SimpleDateFormat("MMddHHmmss");
		String time = df.format(new Date());
		return time;
	}

	public static String getFormatTime(String date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String time = df.format(new Date(date));
		return time;
	}

	public static String getFormatTime_(String date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		DateFormat df_ = new SimpleDateFormat(format);
		Date date_ = null;
		try {
			date_ = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String time = df_.format(date_);
		return time;
	}

	public static boolean isTaday(String time) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		boolean isTaday = false;
		try {
			date = format.parse(time);
			long dateTime = date.getTime();
			isTaday = DateUtils.isToday(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return isTaday;

	}
	/**
	 * 判断身份证号 中国的身份证为15位或18位
	 *
	 * @param idCardNum
	 * @return
	 */
	public static boolean isIdCard(String idCardNum) {

		Pattern p = Pattern
				.compile("(\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");
		Matcher m = p.matcher(idCardNum);
		LogUtil.v("isIdCard", m.matches() + "");
		if (!m.matches()) {
			return false;
		}
		return true;
	}
	/**
	 *
	 * @param time
	 *            如：20130809121308
	 * @param timeFormat
	 *            日期格式，如：yyyyMMddHHmmss,yyyy-MM-dd HH:mm:ss
	 * @return
	 */

	public static long getTimeLong(String time, String timeFormat) {

		SimpleDateFormat format = new SimpleDateFormat(timeFormat);
		Date date = null;
		long dayTime = 0;
		try {
			date = format.parse(time);
			dayTime = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dayTime;

	}

	/**
	 *
	 * @param context
	 * @return
	 */

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}


	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static int getConnectedType(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}
	/**
	 * 检查网络状态
	 *
	 * @param context
	 */
	public static boolean checkTheNetState(Context context) {
		int netStatus = CommonUtils.getConnectedType(context);
		if (netStatus == -1) {
			ViewUtils.makeToast(context, "请检查手机网络", 1500);
			return false;
		}
		return true;
	}

	/**
	 * 把字符串的中间某几位位用“*”号代替
	 *
	 * @param str
	 *            要代替的字符串
	 * @param n
	 *            代替的位数
	 * @return
	 */

	public static String replaceSubString(String str, int n) {
		String sub = "";
		try {
			if (str == "") {
				return "";
			}
			if (str == null) {
				return "";
			}
			if (str.length() < 16) {
				return str;
			}
			LogUtil.syso("str old==" + str);
			String strs = str.substring(13);
			sub = str.substring(0, str.length() - n);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				sb = sb.append("*");
			}
			sub += sb.toString();
			sub += strs;
			LogUtil.syso("str new==" + sub);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sub;
	}

	public static String replaceSubStringNew(String str) {

		String sub = "";
		try {
			if (str == "") {
				return "";
			}
			if (str == null) {
				return "";
			}
			if (str.length() < 16) {
				return str;
			}
			sub = str.substring(0, 6) + "******"
					+ str.substring(str.length() - 4);
			LogUtil.syso("str new==" + sub);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sub;
	}

	public static String toHexString(String s) {

		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	public static String byteToString(byte b) {
		byte high, low;
		byte maskHigh = (byte) 0xf0;
		byte maskLow = 0x0f;
		high = (byte) ((b & maskHigh) >> 4);
		low = (byte) (b & maskLow);
		StringBuffer buf = new StringBuffer();
		buf.append(findHex(high));
		buf.append(findHex(low));
		return buf.toString();
	}

	private static char findHex(byte b) {
		int t = new Byte(b).intValue();
		t = t < 0 ? t + 16 : t;
		if ((0 <= t) && (t <= 9)) {
			return (char) (t + '0');
		}
		return (char) (t - 10 + 'A');
	}

	public static String moneyToZeroFormat(String money) {

		String money_ = null;
		if (money.contains(".")) {

			String[] moneyStr = money.split("\\.");
			LogUtil.syso("moneyStr length=" + moneyStr.length);
			String centMoney = moneyStr[1];
			if (centMoney.length() < 2) {
				centMoney = centMoney + "0";
			}
			money_ = moneyStr[0] + centMoney;
		} else {
			money_ = money + "00";
		}

		int len = money_.length();

		int left = 12 - len;
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < left; i++) {
			str.append("0");
		}

		String s = str + money_;
		return s;
	}

	/**
	 * 16进制格式的字符串转成16进制byte 44 --> byte 0x44
	 *
	 * @param hexString
	 * @return
	 */
	public static byte[] HexStringToByteArray(String hexString) {//
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		if (hexString.length() == 1 || hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 对double数据进行取精度.
	 * <p>
	 * For example: <br>
	 * double value = 100.345678; <br>
	 * double ret = round(value,4,BigDecimal.ROUND_HALF_UP); <br>
	 * ret为100.3457 <br>
	 *
	 * @param value
	 *            double数据.
	 * @param scale
	 *            精度位数(保留的小数位数).
	 * @param roundingMode
	 *            精度取值方式.
	 * @return 精度计算后的数据.
	 */
	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, roundingMode);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}

	/**
	 * 返回当前程序版本名 如1.2.0
	 */
	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}

	/**
	 * 获取当前程序的版本号 如 14
	 * @param context
	 * @return 版本code
	 */
	public static int getAppVersionCode(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}
	/**
	 * 保留小数点后2位
	 *
	 * @param s
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String format(String s) {
		if (s == null || s.length() < 1) {
			return "";
		}
		if (s.contains(",")) {
			s = s.replace(",", "");
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(s);
		LogUtil.syso("num=="+num);
		formater = new DecimalFormat("#,##0.00");

		formater.setRoundingMode(RoundingMode.DOWN);
		String moneyVal = formater.format(num);

		// if(Long.valueOf(moneyVal)<1){
		//
		// moneyVal = "0" + moneyVal;
		//
		// }
		return moneyVal;

	}
	/**
	 * 保留小数点后2位
	 * 如果为空则返回0.00
	 *
	 * @param s
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String format00(String s) {
		if (s.contains(",")) {
			s = s.replace(",", "");
		}
		if (s == null || s.length() < 1) {
			return "0.00";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(s);
		LogUtil.syso("num=="+num);
		formater = new DecimalFormat("#,##0.00");

		formater.setRoundingMode(RoundingMode.DOWN);
		String moneyVal = formater.format(num);

		// if(Long.valueOf(moneyVal)<1){
		//
		// moneyVal = "0" + moneyVal;
		//
		// }
		return TextUtils.isEmpty(moneyVal)?"0.00":moneyVal;

	}

	/**
	 * 将12位数值的数据转换成###,##0.00格式显示
	 *
	 * @param s
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String format02(String s) {
		if (s.contains(",")) {
			s = s.replace(",", "");
		}
		if (s == null || s.length() < 1) {
			return "";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(s)/100;
		LogUtil.syso("num=="+num);
		formater = new DecimalFormat("###,##0.00");

		formater.setRoundingMode(RoundingMode.DOWN);
		String moneyVal = formater.format(num);

		// if(Long.valueOf(moneyVal)<1){
		//
		// moneyVal = "0" + moneyVal;
		//
		// }
		return moneyVal;

	}

	/**
	 * 保留小数后1位
	 *
	 * @param
	 * @return
	 */
	public static String formatNum(String str) {
		if (str == null || str.length() < 1) {
			return "";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(str);
		formater = new DecimalFormat("#,##0.0");

		String moneyVal = formater.format(num);
		return moneyVal;

	}


	/**
	 * 保留小数后0位
	 *
	 * @param
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String formatNum_(String str) {
		if (str == null || str.length() < 1) {
			return "";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(str);
		formater = new DecimalFormat("#,##0");

		formater.setRoundingMode(RoundingMode.DOWN);
		String moneyVal = formater.format(num);

		return moneyVal;

	}


	/**
	 * 保留小数后3位
	 *
	 * @param
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String formatNumThree(String str) {
		if (str == null || str.length() < 1) {
			return "";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(str);
		formater = new DecimalFormat("#,##0.00");

		formater.setRoundingMode(RoundingMode.DOWN);
		String moneyVal = formater.format(num);

		return moneyVal;

	}
	/**
	 * 显示短卡号
	 * 如前6后4 1122334455667788——112233******7788
	 * @param str
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String translateShortNumber(String str,int start,int end) {
	if(str.length()<start+end)return str;
		String middle = str.substring(start,str.length()-end);
		StringBuffer middle_new = new StringBuffer();
		for (int i = 0; i < middle.length(); i++) {
			middle_new.append("*");
		}
		String startStr = str.substring(0,6);
		String endStr = str.substring(6);
		String s = startStr + endStr.replace(middle,middle_new);
		return s;
	}

	public static boolean containStr(String str) {

		int count = 0;
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				count = count + 1;
			}
		}
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	private void setIsClose(AlertDialog dialog, boolean isClose) {

		try {

			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, false);

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/** toast */
	public static void mMakeTextToast(Context context, String text,
			boolean isLong) {

		if (isLong == true) {
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();

		} else {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}

	/** toast */
	public static void mMakeResIdToast(Context context, int resId,
			boolean isLong) {

		if (isLong == true) {
			Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 * @param s
	 * @return
	 */
	public static String formatTo12Zero(String s) {//89.90
		if (s == null || s.length() < 1 || s.length() > 12) {
			return "";
		}
		if (s.contains(",")) {
			s = s.replace(",", "");
		}
		s = s.replace(".", "");
		int len = s.length();
		int len_ = 12 - len;
		StringBuffer moneyNum = new StringBuffer();
		for (int i = 0; i < len_; i++) {

			moneyNum.append("0");

		}

		moneyNum.append(s);
		return moneyNum.toString();

	}
	/**
	 *
	 * @param s
	 * @return
	 */
	public static String formatTo8Zero(String s) {
		if (s == null || s.length() < 1 || s.length() > 12) {
			return "";
		}
		s = s.replace(".", "");
		int len = s.length();
		int len_ = 8 - len;
		StringBuffer moneyNum = new StringBuffer();
		for (int i = 0; i < len_; i++) {

			moneyNum.append("0");

		}

		moneyNum.append(s);
		return moneyNum.toString();

	}
	/**
	 *
	 * @param s
	 * @return
	 */
	public static String formatTo3Zero(String s) {
		if (s == null || s.length() < 1 || s.length() > 12) {
			return "";
		}
		s = s.replace(".", "");
		int len = s.length();
		int len_ = 3 - len;
		StringBuffer moneyNum = new StringBuffer();
		for (int i = 0; i < len_; i++) {

			moneyNum.append("0");

		}

		moneyNum.append(s);
		return moneyNum.toString();

	}
	/**
	 *
	 * @param s
	 * @return
	 */
	public static Long formatMoneyToFen(String s) {
		if (s == null || s.length() < 1) {
			return Long.valueOf(0);
		}
		s = s.replace(".", "");
		s = s.replace(",", "");
		return Long.valueOf(s);
	}

	// 把yyyymmdd转成yyyy-MM-dd格式

	public static String formatDate(String str) {
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sfstr = "";
		try {
			sfstr = sf2.format(sf1.parse(str));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sfstr;
	}

	// 把yyyymmdd转成指定格式例如yyyy-MM-dd

	public static String getDateFromFormat(String str,String format) {
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sf2 = new SimpleDateFormat(format);
		String sfstr = "";
		try {
			sfstr = sf2.format(sf1.parse(str));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sfstr;
	}

	public static String getDateFromFormat_(String str,String oldFormat,String newFormat) {
		SimpleDateFormat sf1 = new SimpleDateFormat(oldFormat);
		SimpleDateFormat sf2 = new SimpleDateFormat(newFormat);
		String sfstr = "";
		try {
			sfstr = sf2.format(sf1.parse(str));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sfstr;
	}

	public static void bankCardNumAddSpace(final TextView creditCardNum) {

		creditCardNum.addTextChangedListener(new TextWatcher() {
			int beforeTextLength = 0;
			int onTextLength = 0;
			boolean isChanged = false;

			int location = 0;// 记录光标的位置
			private char[] tempChar;
			private StringBuffer buffer = new StringBuffer();
			int konggeNumberB = 0;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				beforeTextLength = s.length();
				if (buffer.length() > 0) {
					buffer.delete(0, buffer.length());
				}
				konggeNumberB = 0;
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == ' ') {
						konggeNumberB++;
					}
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				onTextLength = s.length();
				buffer.append(s.toString());
				if (onTextLength == beforeTextLength || onTextLength <= 3
						|| isChanged) {
					isChanged = false;
					return;
				}
				isChanged = true;
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (isChanged) {
					location = creditCardNum.getSelectionEnd();
					int index = 0;
					while (index < buffer.length()) {
						if (buffer.charAt(index) == ' ') {
							buffer.deleteCharAt(index);
						} else {
							index++;
						}
					}

					index = 0;
					int konggeNumberC = 0;
					while (index < buffer.length()) {
						if ((index == 4 || index == 9 || index == 14 || index == 19)) {
							buffer.insert(index, ' ');
							konggeNumberC++;
						}
						index++;
					}

					if (konggeNumberC > konggeNumberB) {
						location += (konggeNumberC - konggeNumberB);
					}

					tempChar = new char[buffer.length()];
					buffer.getChars(0, buffer.length(), tempChar, 0);
					String str = buffer.toString();
					if (location > str.length()) {
						location = str.length();
					} else if (location < 0) {
						location = 0;
					}

					creditCardNum.setText(str);
					Editable etable = (Editable) creditCardNum.getText();
					Selection.setSelection(etable, location);
					isChanged = false;
				}
			}
		});
	}


	public static void phoneNumAddSpace(final TextView creditCardNum) {

		creditCardNum.addTextChangedListener(new TextWatcher() {
			int beforeTextLength = 0;
			int onTextLength = 0;
			boolean isChanged = false;

			int location = 0;// 记录光标的位置
			private char[] tempChar;
			private StringBuffer buffer = new StringBuffer();
			int konggeNumberB = 0;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				beforeTextLength = s.length();
				if (buffer.length() > 0) {
					buffer.delete(0, buffer.length());
				}
				konggeNumberB = 0;
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == ' ') {
						konggeNumberB++;
					}
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				onTextLength = s.length();
				buffer.append(s.toString());
				if (onTextLength == beforeTextLength || onTextLength <= 3
						|| isChanged) {
					isChanged = false;
					return;
				}
				isChanged = true;
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (isChanged) {
					location = creditCardNum.getSelectionEnd();
					int index = 0;
					while (index < buffer.length()) {
						if (buffer.charAt(index) == ' ') {
							buffer.deleteCharAt(index);
						} else {
							index++;
						}
					}

					index = 0;
					int konggeNumberC = 0;
					while (index < buffer.length()) {
						if ((index == 3 || index == 8)) {
							buffer.insert(index, ' ');
							konggeNumberC++;
						}
						index++;
					}

					if (konggeNumberC > konggeNumberB) {
						location += (konggeNumberC - konggeNumberB);
					}

					tempChar = new char[buffer.length()];
					buffer.getChars(0, buffer.length(), tempChar, 0);
					String str = buffer.toString();
					if (location > str.length()) {
						location = str.length();
					} else if (location < 0) {
						location = 0;
					}

					creditCardNum.setText(str);
					Editable etable = (Editable) creditCardNum.getText();
					Selection.setSelection(etable, location);
					isChanged = false;
				}
			}
		});
	}

	public static Double div(Double v1,Double v2,int scale){

        if(scale<0){

            throw new IllegalArgumentException(

            "The scale must be a positive integer or zero");

        }

        BigDecimal b1 = new BigDecimal(v1.toString());

        BigDecimal b2 = new BigDecimal(v2.toString());

        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();

    }


	/**
     * 把中文转成Unicode码
     * @param str
     * @return
     */
    public static String chinaToUnicode(String str){
        String result="";
        for (int i = 0; i < str.length(); i++){
            int chr1 = (char) str.charAt(i);
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)
                result+="\\u" + Integer.toHexString(chr1);
            }else{
                result+=str.charAt(i);
            }
        }
        return result;
    }

    /**
     * 判断是否是全是中文
     * @param str
     * @return
     */
    public static boolean isChina(String str){
        String result="";
        int isChinese = 0;
        int noChinese = 0;
        for (int i = 0; i < str.length(); i++){
            int chr1 = (char) str.charAt(i);
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)
                isChinese++;
            }else{
                noChinese++;
            }
        }
        if(noChinese>0){
        	return false;
        }else{
        	return true;
        }
    }

    public static String Md5(String plainText ) {
    	StringBuffer buf = new StringBuffer("");
    	try {
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	md.update(plainText.getBytes());
    	byte b[] = md.digest();

    	int i;


    	for (int offset = 0; offset < b.length; offset++) {
    	i = b[offset];
    	if(i<0) i+= 256;
    	if(i<16)
    	buf.append("0");
    	buf.append(Integer.toHexString(i));
    	}

    	LogUtil.syso("result: " + buf.toString());//32位的加密

    	} catch (NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}
    	return buf.toString();
    	}

    /**
	 * 字节数组转换十六进制
	 *
	 * @param bts
	 * @return
	 */
	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		if (null != bts) {
			for (int i = 0; i < bts.length; i++) {
				tmp = (Integer.toHexString(bts[i] & 0xFF));
				if (tmp.length() == 1) {
					des += "0";
				}
				des += tmp;
			}
		}
		return des;
	}
	/**
	 *
	 * @param s
	 * @return
	 */
	public static String formatTo6Zero(String s) {
		if (s == null || s.length() < 1 || s.length() > 6) {
			return "";
		}
		int len = s.length();
		int len_ = 6 - len;
		StringBuffer moneyNum = new StringBuffer();
		for (int i = 0; i < len_; i++) {

			moneyNum.append("0");

		}
		moneyNum.append(s);

		return moneyNum.toString();

	}

	/**
	 * 调节系统音量为最大
	 * @param mContext
	 */
public static  void adjustAudioMax(Context mContext){
//	如何获取声音管理器：
	AudioManager audioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
	//最大音量
	int maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
//当前音量
	int currentVolume =audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

//	A、设置声音模式
//声音模式
	audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//静音模式
//	audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//震动模式
//	audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

//	B、调整声音大小
//减少声音音量
//	audioManager.adjustVolume(AudioManager.ADJUST_LOWER,  0);
//调大声音音量
	for (int i = 0; i <maxVolume - currentVolume ; i++) {
		audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
	}

//	（当传入的第一个参数为 AudioManager.ADJUST_LOWER 时，可将音量调小一个单位，传入AudioManager.ADJUST_RAISE 时，则可以将音量调大一个单位。）
}

	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	public static int dp2px(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (int) ((dp * displayMetrics.density) + 0.5);
	}

	/**
	 * 获取缓存路径
	 * @param context
	 * @param uniqueName 文件夹名称
	 * @return 缓存路径的文件夹对象
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();// /sdcard/Android/data/<application package>/cache
		} else {
			cachePath = context.getCacheDir().getPath();// /data/data/<application package>/cache
		}
		File cacheFile = new File(cachePath + File.separator + uniqueName);
		if (!cacheFile.exists())cacheFile.mkdirs();
		return cacheFile;
	}
}

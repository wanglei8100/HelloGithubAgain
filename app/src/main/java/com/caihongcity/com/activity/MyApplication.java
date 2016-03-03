package com.caihongcity.com.activity;

import android.bluetooth.BluetoothDevice;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.caihongcity.com.db.MySQLiteOpenHelper;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.litepal.LitePalApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author yuanjigong 初始化数据库
 */
public class MyApplication extends LitePalApplication {



	private static final String[] sqls = { "CREATE TABLE IF NOT EXISTS terminalinfo (_id INTEGER PRIMARY KEY AUTOINCREMENT,voucherNo varchar(10),batchNo varchar(10),terminalNo varchar(10));" };
	// voucherNo 流水号,batchNo 批次号,terminalNo 终端号
	public static HashMap<String, String> responseCodeMap;
	public static HashMap<String, String> bankNameList;
	public static BluetoothDevice bluetoothDevice;

	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoader();
		responseCodeMap = new HashMap<String, String>();
		bankNameList = new HashMap<String, String>();
		initResponseCode(responseCodeMap);
		initBankNameList(bankNameList);
		initDb();


	}

	private void initImageLoader() {
		//创建默认的ImageLoader配置参数
		ImageLoaderConfiguration configuration = ImageLoaderConfiguration
				.createDefault(this);
		ImageLoader.getInstance().init(configuration);
	}

	private void initDb() {
		String isCreateTable = StorageCustomerInfoUtil.getInfo("iscreatetable", this);
		if (!isCreateTable.equals("")) {
			return;
		} else {
			SQLiteDatabase sd = null;
			try {
				MySQLiteOpenHelper myOpenHelper = new MySQLiteOpenHelper(this);
				sd = myOpenHelper.getWritableDatabase();
				for (String sqls_ : sqls) {
					sd.execSQL(sqls_);
					Log.i("create table run----", sqls_);
				}
			} catch (Exception e) {
				Toast.makeText(this, " 初始化数据失败原因" + e.getMessage(),
						Toast.LENGTH_LONG).show();
			} finally {
				sd.close();
			}
			StorageCustomerInfoUtil.putInfo(this, "iscreatetable", "true");
		}
	}

	/**
 * 北京银行	313003
光大银行	303
广发银行	306
建设银行	105
交通银行	301
民生银行	305
农业银行	103
平安银行	307
浦发银行	310
邮政储蓄银行	403
招商银行	308
中国工商银行	102
中国银行	104
中信银行	302
上海银行	313062
杭州银行	313027

 * @param list
 */
	private void initBankNameList(HashMap<String, String> list) {
		list.put("北京银行","313003");
		list.put("光大银行","303");
		list.put("广发银行","306");
		list.put("建设银行","105");
		list.put("交通银行","301");
		list.put("民生银行","305");
		list.put("农业银行","103");
		list.put("平安银行","307");
		list.put("浦发银行","310");
		list.put("邮政储蓄银行","403");
		list.put("招商银行","308");
		list.put("中国工商银行","102");
		list.put("中国银行","104");
		list.put("中信银行","302");
		list.put("上海银行","313062");
		list.put("杭州银行","313027");
	}

	private void initResponseCode(Map<String, String> responseCodeMap) {
		responseCodeMap.put("00", "交易成功");
		responseCodeMap.put("01", "请持卡人与发卡银行联系");
		responseCodeMap.put("03", "无效商户");
		responseCodeMap.put("04", "此卡被没收");
		responseCodeMap.put("05", "持卡人认证失败");
		responseCodeMap.put("10", "显示部分批准金额，提示操作员");
		responseCodeMap.put("11", "成功，VIP客户");
		responseCodeMap.put("12", "无效交易");
		responseCodeMap.put("13", "无效金额");
		responseCodeMap.put("14", "无效卡号");
		responseCodeMap.put("15", "此卡无对应发卡方");
		responseCodeMap.put("21", "该卡未初始化或睡眠卡");
		responseCodeMap.put("22", "操作有误，或超出交易允许天数");
		responseCodeMap.put("25", "没有原始交易，请联系发卡方");
		responseCodeMap.put("30", "请重试");
		responseCodeMap.put("34", "作弊卡,呑卡");
		responseCodeMap.put("38", "密码错误次数超限，请与发卡方联系");
		responseCodeMap.put("40", "发卡方不支持的交易类型");
		responseCodeMap.put("41", "挂失卡，请没收");
		responseCodeMap.put("43", "被窃卡，请没收");
		responseCodeMap.put("45", "芯片卡交易，请插卡操作");
		responseCodeMap.put("51", "可用余额不足");
		responseCodeMap.put("54", "该卡已过期");
		responseCodeMap.put("55", "密码错误");
		responseCodeMap.put("57", "不允许此卡交易");
		responseCodeMap.put("58", "发卡方不允许该卡在本终端进行此交易");
		responseCodeMap.put("59", "卡片校验错");
		responseCodeMap.put("61", "交易金额超限");
		responseCodeMap.put("62", "受限制的卡");
		responseCodeMap.put("64", "交易金额与原交易不匹配");
		responseCodeMap.put("65", "超出消费次数限制");
		responseCodeMap.put("68", "交易超时，请重试");
		responseCodeMap.put("75", "密码错误次数超限");
		responseCodeMap.put("90", "系统日切，请稍后重试");
		responseCodeMap.put("91", "发卡方状态不正常，请稍后重试");
		responseCodeMap.put("92", "发卡方线路异常，请稍后重试");
		responseCodeMap.put("94", "拒绝，重复交易，请稍后重试");
		responseCodeMap.put("96", "拒绝，交换中心异常，请稍后重试");
		responseCodeMap.put("97", "终端未登记");
		responseCodeMap.put("98", "发卡方超时");
		responseCodeMap.put("99", "PIN格式错，请重新签到");
		responseCodeMap.put("A0", "MAC校验错，请重新签到");
		responseCodeMap.put("A1", "转账货币不一致");
		responseCodeMap.put("A2", "交易成功，请向发卡行确认");
		responseCodeMap.put("A3", "账户不正确");
		responseCodeMap.put("A4", "交易成功，请向发卡行确认");
		responseCodeMap.put("A5", "交易成功，请向发卡行确认");
		responseCodeMap.put("A6", "交易成功，请向发卡行确认");
		responseCodeMap.put("A7", "拒绝，交换中心异常，请稍后重试");
		responseCodeMap.put("B1", "不支持该终端");
		responseCodeMap.put("F0", "拒绝，终端初始化失败");
		responseCodeMap.put("W1", "不允许提现");
		responseCodeMap.put("W2", "当前时间不允许提现");
		responseCodeMap.put("W3", "节假日不允许提现");
		responseCodeMap.put("W4", "提现受理失败，小于提现金额");
		responseCodeMap.put("W5", "提现受理失败，超出提现次数");
		responseCodeMap.put("W6", "超过终端单笔提现金额");
		responseCodeMap.put("W7", "小于终端日限额");
		responseCodeMap.put("W8", "商户资料审核不通过，请重新提交");
		responseCodeMap.put("W9", "提现失败，提现过于频繁");
		responseCodeMap.put("W10", "代理商不存在");
		responseCodeMap.put("ZC", "验证码错误");
		responseCodeMap.put("ZD", "手机号码已注册，请直接登录");
		responseCodeMap.put("ZE", "银行卡实名验证失败");
		responseCodeMap.put("ZV", "请更新到最新版本");
		responseCodeMap.put("ZZ", "操作失败");
		responseCodeMap.put("ZZ0", "版本号为空");
		responseCodeMap.put("ZZ1", "短信验证操作异常");
		responseCodeMap.put("ZZ2", "商户不存在");
		responseCodeMap.put("ZZ3", "验证码为空");
		responseCodeMap.put("ZZ4", "资质上传失败，请重新上传");
		responseCodeMap.put("ZZ5", "签名上传失败，请重新上传");
		responseCodeMap.put("ZZ6", "提现操作异常");
		responseCodeMap.put("ZZ7", "刷卡头检测及绑定操作异常");
		responseCodeMap.put("ZZ8", "实名认证异常，请重试");
		responseCodeMap.put("ZZ9", "商户同步操作异常，请重试");
		responseCodeMap.put("ZZ10", "保存APP提额商户异常");
		responseCodeMap.put("ZZ11", "查询APP提额商户异常");
		responseCodeMap.put("ZZ12", "APP提额商户资质上传异常");
		responseCodeMap.put("ZZ13", "APP提额商户已存在");
		responseCodeMap.put("T1", "交易失败，正在重新审核");
		responseCodeMap.put("S5", "APP超过交易次数");
	}
	public static String getErrorHint(String code){
		return TextUtils.isEmpty(responseCodeMap.get(code))?"未知错误："+code:responseCodeMap.get(code);
	}
}

package com.caihongcity.com.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * 
 * @author yuanjigong 刷卡界面
 */
@EActivity(R.layout.swipe)
public class SwipeActivity extends BaseActivity {

	@ViewById(R.id.money)
	TextView money_text;
	private StringBuffer content = null;// 输入的金额
	private StringBuffer calculator_num = null;// 计算器的输入数字
	private String tradeType = "";
	private String feeRate;
	private String isT0;
	private String topFeeRate;
	private String TAG="SwipeActivity";


	@AfterViews
	void initData(){
		((TextView) findViewById(R.id.tv_title_des)).setText("消费");
		tradeType = getIntent().getStringExtra("tradetype");
		feeRate = getIntent().getStringExtra("feeRate");
		isT0 = getIntent().getStringExtra("isT0");
		topFeeRate = getIntent().getStringExtra("topFeeRate");
		LogUtil.i(TAG,tradeType+" : "+feeRate);
	}

	@Click({R.id.calculator_one_menu,R.id.calculator_two_menu,R.id.calculator_three_menu,R.id.calculator_four_menu,R.id.calculator_five_menu,R.id.calculator_six_menu,
			R.id.calculator_seven_menu,R.id.calculator_eight_menu,R.id.calculator_nine_menu,R.id.calculator_zero_menu,R.id.calculator_point_menu,R.id.calculator_twozero_menu,
			R.id.calculator_csign_menu,R.id.calculator_clear_menu,R.id.confirmswipe,R.id.ll_back})
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.calculator_one_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("1");
			String str1 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str1);
			break;
		case R.id.calculator_two_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("2");
			String str2 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str2);
			break;
		case R.id.calculator_three_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("3");
			String str3 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str3);
			break;
		case R.id.calculator_four_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("4");
			String str4 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str4);
			break;
		case R.id.calculator_five_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("5");
			String str5 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str5);
			break;
		case R.id.calculator_six_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("6");
			String str6 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str6);
			break;
		case R.id.calculator_seven_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("7");
			String str7 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str7);
			break;
		case R.id.calculator_eight_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("8");
			String str8 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str8);
			break;
		case R.id.calculator_nine_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("9");
			String str9 = CommonUtils.format(calculator_num.toString());
			money_text.setText(str9);
			break;
		case R.id.calculator_point_menu:
			if (calculator_num.length()<=0||(calculator_num.toString().contains("."))) {
				return;
			}
			calculator_num.append(".");
			String strPoint = CommonUtils.format(calculator_num.toString());
			money_text.setText(strPoint);
			break;
		case R.id.calculator_zero_menu:
			if (limitNumberLength(calculator_num.length()))
				return;
			calculator_num.append("0");
			String strZero = CommonUtils.format(calculator_num.toString());
			money_text.setText(strZero);
			break;
		case R.id.calculator_twozero_menu:
			if (limitNumberLength(calculator_num.length() + 1))
				return;
			calculator_num.append("00");
			String strTwoZero = CommonUtils.format(calculator_num.toString());
			money_text.setText(strTwoZero);
			break;
		case R.id.calculator_csign_menu:
			if (calculator_num.length() < 1)
				return;
			if (calculator_num.charAt(calculator_num.length() - 1) == '.') {
				calculator_num.deleteCharAt(calculator_num.length() - 1);
				calculator_num.deleteCharAt(calculator_num.length() - 1);
			} else {
				calculator_num.deleteCharAt(calculator_num.length() - 1);
			}
			String csign_menu = CommonUtils.format00(calculator_num.toString());
			money_text.setText(csign_menu);
			break;
		case R.id.calculator_clear_menu:
			calculator_num = new StringBuffer();
			money_text.setText(R.string.defaultnum);
			break;
		case R.id.confirmswipe:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			String moneyVal = money_text.getText().toString();
			if (StringUtil.isEmpty(moneyVal)||"0.00".equals(moneyVal)) {
				ViewUtils.makeToast(this, getString(R.string.money_is_null),
						1500);
				return;
			}
			Intent intent = new Intent();
			String bluetooth_address = StorageAppInfoUtil.getInfo("bluetooth_address", this);
			String terminal_type = StorageAppInfoUtil.getInfo("terminal_type", this);
			if ("1".equals(terminal_type)) {
				if (TextUtils.isEmpty(bluetooth_address)) {
					intent.setClass(this, BluetoothSelectActivity_.class);
				}else{
					intent.setClass(this, SwipeWaitBluetoothActivity_.class);
					intent.putExtra("blue_address", bluetooth_address);
				}
			}else if ("2".equals(terminal_type)) {
				intent.setClass(this, SwipeWaitHuiXingActivity_.class);
			}else if ("3".equals(terminal_type)) {
				intent.setClass(this, SwipeWaitMoFangActivity_.class);
			}else if ("4".equals(terminal_type)) {
				intent.setClass(this, SwipeWaitZhongCiActivity_.class);
			}else if("5".equals(terminal_type)){
				if (TextUtils.isEmpty(bluetooth_address)) {
					intent.setClass(this, BluetoothSelectActivity_.class);
				}else{
					intent.setClass(this, SwipeWaitMoFangBlueActivity_.class);
					intent.putExtra("blue_address", bluetooth_address);
				}
			}else if("6".equals(terminal_type)){
				if (TextUtils.isEmpty(bluetooth_address)) {
					intent.setClass(this, BluetoothSelectActivity_.class);
				}else{
					intent.setClass(this, SwipeWaitYiFengBlueActivity_.class);
					intent.putExtra("blue_address", bluetooth_address);
				}
			}else if("7".equals(terminal_type)){
				if (TextUtils.isEmpty(bluetooth_address)) {
					intent.setClass(this, BluetoothSelectActivity_.class);
				}else{
					intent.setClass(this, SwipeWaitXinNuoBlueActivity_.class);
					intent.putExtra("blue_address", bluetooth_address);
				}
			}else if("8".equals(terminal_type)){
				intent.setClass(this, SwipeWaitBBPoseBuleActivity_.class);
			}else if ("9".equals(terminal_type)) {
				intent.setClass(this, SwipeWaitBBPoseActivity_.class);
			}else if ("10".equals(terminal_type)) {
				intent.setClass(this, SwipeWaitXinLianDaActivity_.class);
			} else {
				intent.setClass(this, SwipeWaitActivity.class);
			}
			intent.putExtra("money", moneyVal);
			intent.putExtra("tradetype", tradeType);
			intent.putExtra("feeRate", feeRate);
			intent.putExtra("topFeeRate", topFeeRate);
			startActivity(intent);
			ViewUtils.overridePendingTransitionCome(this);
			break;
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;
		default:
			break;
		}

	}

	/**
	 * 限制金额为100万以下
	 * 
	 * @param length
	 * @return
	 */
	private boolean limitNumberLength(int length) {
		String temp = new String(calculator_num);
		if (temp.contains(".")) {
			if (length >= 9) {
				return true;
			} else {
				return false;
			}
		} else {
			if (length >= 6) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMoneyShow();
	}

	public void initMoneyShow() {

		calculator_num = new StringBuffer();
		money_text.setText(R.string.defaultnum);
	}

}

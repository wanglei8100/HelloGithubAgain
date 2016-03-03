package com.caihongcity.com.activity;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.db.MyDBOperate;
import com.caihongcity.com.model.SerializableMap;
import com.caihongcity.com.model.TerminalInfo;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.EncryptUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.MyAsyncTask.LoadResourceCall;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;

/**
 * 
 * @author yuanjigong 余额查询
 */
public class QueryBalanceActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "QueryBalanceActivity";
	private TextView customernameval, trade_type_val, tradecardnumber_val;
	private EditText passwordval_val;
	private Button autoquerybalance;
	private String money, tradeType;
	private SerializableMap serializableMap;
	private Map<String, Object> map;
	private String feeRate;
	private String cardType;
	private String cardTypeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.querybalance);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView) findViewById(R.id.tv_title_des)).setText("余额查询");
		customernameval = (TextView) findViewById(R.id.customernameval);
		trade_type_val = (TextView) findViewById(R.id.trade_type_val);
		tradecardnumber_val = (TextView) findViewById(R.id.tradecardnumber_val);
		passwordval_val = (EditText) findViewById(R.id.passwordval_val);
		autoquerybalance = (Button) findViewById(R.id.autoquerybalance);
		autoquerybalance.setOnClickListener(this);
		money = getIntent().getStringExtra("money");
		tradeType = getIntent().getStringExtra("tradeType");
		feeRate = getIntent().getStringExtra("feeRate");
		trade_type_val.setText(getString(R.string.querybalance));
		Bundle bundle = getIntent().getExtras();
		serializableMap = (SerializableMap) bundle.get("trackinfo");
		map = serializableMap.getMap();
		String maskedPAN = (String) map.get("maskedPAN");
		cardType = (String) map.get("cardType");
		if ("1".equals(cardType)) {
			cardTypeValue = "051";
		} else {
			cardTypeValue = "021";
		}
		String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length()-4), "****");
		tradecardnumber_val.setText(maskedPANValue);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		
		String customerName = StorageCustomerInfoUtil.getInfo("customerName", this);
		customernameval.setText(customerName);
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {  
	        return;  
	    }
		int id = v.getId();
		switch (id) {
		case R.id.autoquerybalance:
			if (StringUtil.isEmpty(passwordval_val.getText().toString())||passwordval_val.getText().toString().length()<6) {
				ViewUtils.makeToast(this, getString(R.string.login_pwd_iserror),
						1500);
				return;
			}
			String pwdVal = passwordval_val.getText().toString();
			initUrl(pwdVal);
			//检查网络状态
			if(CommonUtils.getConnectedType(QueryBalanceActivity.this)==-1){
				ViewUtils.makeToast(QueryBalanceActivity.this, getString(R.string.nonetwork), 1500);
				return;
			}
			query();
			break;
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;

		default:
			break;
		}
	}

	private void initUrl(String pwdVal) {

		String tractData = (String) map.get("encTracks");
		String randomNumber = (String) map.get("randomNumber");
		String maskedPAN = (String) map.get("maskedPAN");
		String expiryDate = (String) map.get("expiryDate");
		int track1Length = Integer.parseInt((String) map.get("track1Length"));
		int track2Length = Integer.parseInt((String) map.get("track2Length"));
		String tract1Data =tractData.substring(track1Length, track2Length*2);

		String workkey = StorageCustomerInfoUtil.getInfo("workkey", this);

		LogUtil.syso("workkey====" + workkey);
		if ("".equals(workkey)) {
			ViewUtils.makeToast(this, "workkey异常", 1500);
			return;
		}
		String pinkey = workkey.substring(0, 38);
		byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
		String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);
		String terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
		MyDBOperate myDBOperate = new MyDBOperate(this);
		List<TerminalInfo> info = myDBOperate.getTerminalInfo(terminal);
		int len = info.size();
		String batchNo = "";
		String voucherNo = null;
		if (len > 0) {
			TerminalInfo terminalInfo = info.get(len - 1);
			batchNo = terminalInfo.getBatchNo();
			voucherNo = terminalInfo.getVoucherNo();
		}

		if (!TextUtils.isEmpty(voucherNo)) {
			int voucherNoInt = Integer.valueOf(voucherNo);
			voucherNoInt = voucherNoInt + 1;
			String voucherNoStr = CommonUtils.formatTo6Zero(String
					.valueOf(voucherNoInt));
			//更新数据库数据
			ContentValues values = new ContentValues();
			values.put("voucherNo",voucherNoStr);
			myDBOperate.update(values, terminal);
			StorageCustomerInfoUtil.putInfo(this, "voucherNo", voucherNoStr);
		}
		String customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
		String forpinkey = CommonUtils.Md5(pwdVal
				+ StorageCustomerInfoUtil.getInfo("pinkey", this));
		String voucherNo_ = StorageCustomerInfoUtil.getInfo("voucherNo", this);
		LogUtil.syso("terminal==" + terminal);
		String moneyVal = CommonUtils.formatTo12Zero(money);
		String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
		String forMd5Data = "0200" + maskedPAN + "310000"+feeRateVal + voucherNo_
				+ expiryDate + cardTypeValue + "12" + tract1Data.toUpperCase()
				+ terminal + customer + "156" + pinbyteHex.toUpperCase()
				+ randomNumber +Constant.VERSION+ "01" + batchNo + "003";
		LogUtil.syso("forMd5Data====" + forMd5Data);
		String data = "0=0200&2=" + maskedPAN + "&3=310000"+"&9="+feeRateVal + "&11="
				+ voucherNo_ + "&14=" + expiryDate + "&22="+cardTypeValue+"&26=12" + "&35="
				+ tract1Data.toUpperCase() + "&41=" + terminal + "&42="
				+ customer + "&49=156" + "&52=" + pinbyteHex.toUpperCase()
				+ "&53="
				+ randomNumber// 随机密钥
				 +"&59="+Constant.VERSION
				+ "&60=01" + batchNo + "003" + "&64="
				+ CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
		// String data =
		// "0=0200&2="+maskedPAN+"&3=310000"+"&11=000011&14="+expiryDate+"&22=022&26=12"
		// +
		// "&35="+tract1Data.toUpperCase()+"&41="+terminal+"&42="+customer+"&49=156"
		// + "&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
		// + "&60=01"+batchNo+"003"
		// + "&64=" + CommonUtils.Md5(forMd5Data +
		// Constant.mainKey).toUpperCase();
		String url = Constant.REQUEST_API + data;//0=0200&2=6225760009310363&3=000000&11=&14=1507&22=022&26=12&35=49B9DB7CE8D6C9E9E57FA7ED0E44C1B8B4A208F3DAE8C406&41=99977411&42=220558015061077&49=156&52=040777000103&53=2197D5BC00000008&60=01null003&64=A8D097DBDC969D45A96F092D5C02CAA1
		Constant.URL = url;
	}

	public void query() {

		MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {
			@Override
			public void isLoadingContent() {
				loadingDialog.show();
			}

			@Override
			public void isLoadedContent(String content) {
				loadingDialog.dismiss();
				LogUtil.syso("content==" + content);//{"0":"0200","35":"49B9DB7CE8D6C9E9E57FA7ED0E44C1B8B4A208F3DAE8C406","2":"6225760009310363","3":"000000","64":"A8D097DBDC969D45A96F092D5C02CAA1","39":"96","42":"220558015061077","11":"","41":"99977411","14":"1507","49":"156","53":"2197D5BC00000008","22":"022","52":"040777000103","26":"12","60":"01null003"}
				if (StringUtil.isEmpty(content)) {
					ViewUtils.makeToast(QueryBalanceActivity.this,
							getString(R.string.reponse_content_isnull),
							1500);
					return;

				}
				try {
					JSONObject obj = new JSONObject(content);
					String result = (String) obj.get("39");
					String resultValue = MyApplication.getErrorHint(result);
					if ("00".equals(result)) {
						String cardNo = (String) obj.get("2");
						String moneyCon = obj.getString("54");
						if (!"".equals(moneyCon)) {
							int len = moneyCon.length();
							if (len > 12) {
								moneyCon = moneyCon.substring(len - 12, len);
							}
						}
						Intent intent = new Intent();
						intent.putExtra("cardNo", cardNo);
						intent.putExtra("money", moneyCon);
						intent.setClass(QueryBalanceActivity.this,
								QueryBalancceResultActivity.class);
						startActivity(intent);
						finish();
						ViewUtils.overridePendingTransitionCome(QueryBalanceActivity.this);

					} else {
						if (TextUtils.isEmpty(resultValue)) {
							ViewUtils.makeToast(QueryBalanceActivity.this, "系统异常"+result, 1500);
						} else {
							ViewUtils.makeToast(QueryBalanceActivity.this, resultValue, 1500);
						}
/*						ViewUtils.makeToast(QueryBalanceActivity.this,
								getString(R.string.queryfailure),
								1500);
*/						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		myAsyncTask.execute(Constant.URL);
		LogUtil.d(TAG, "url==" + Constant.URL);
	}
}

package com.caihongcity.com.activity;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.db.MyDBOperate;
import com.caihongcity.com.model.QueryModel;
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
 * @author yuanjigong
 * 消费输入密码
 */
public class ConsumeInputPwdActivity extends BaseActivity implements OnClickListener{

	private static final String TAG = "ConsumeInputPwdActivity";
	private LinearLayout confirmToTrade;
	private TextView tradeCardNumber,tradeTypeText,moneyText;
	private SerializableMap serializableMap;
	private EditText consumepwd;
	private Map<String,Object> map;
	private String money;
	private String tradeType;
	private QueryModel queryModel;
	private String feeRate;
	private String cardType;
	private String cardTypeValue;
	private TextView customername;
	private TextView tradetype_pro;
	private String formatmoney;
	private TextView trade_feerate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consume_inputpwd_layout);
		confirmToTrade = (LinearLayout) findViewById(R.id.confirmtotrade);
		confirmToTrade.setOnClickListener(this);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView) findViewById(R.id.tv_title_des)).setText("输入密码");
		Bundle bundle = getIntent().getExtras();
		serializableMap = (SerializableMap) bundle.get("trackinfo");
		consumepwd = (EditText) findViewById(R.id.consumepwd);
		money = getIntent().getStringExtra("money");
		tradeType = getIntent().getStringExtra("tradeType"); 
		feeRate = getIntent().getStringExtra("feeRate"); 
		tradeCardNumber = (TextView) findViewById(R.id.tradecardnumber);
		tradeTypeText = (TextView) findViewById(R.id.tradetype);
		tradetype_pro = (TextView) findViewById(R.id.tradetype_pro);
		trade_feerate = (TextView) findViewById(R.id.trade_feerate);
		if (Constant.CONSUME.equals(tradeType)) {
			tradetype_pro.setText("消费");
		} else if (Constant.CANCEL.equals(tradeType)) {
			tradetype_pro.setText("消费撤销");
		}else{
			tradetype_pro.setText("余额查询");
		}
		customername = (TextView) findViewById(R.id.customername);
		moneyText = (TextView) findViewById(R.id.money);
		formatmoney = CommonUtils.format(money);
		moneyText.setText(formatmoney);
		String customerName = StorageCustomerInfoUtil.getInfo("customerName", this);
		customername.setText(customerName);
		map = serializableMap.getMap();
		String maskedPAN = (String) map.get("maskedPAN");
		cardType = (String) map.get("cardType");
		if ("01".equals(cardType)) {
			cardTypeValue = "051";
		} else {
			cardTypeValue = "021";
		}
		String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length()-4), "****");
		tradeCardNumber.setText(maskedPANValue);
		queryModel = (QueryModel) getIntent().getSerializableExtra("queryModel");
		trade_feerate.setText(feeRate);//消费时设置费率字段
		if (TextUtils.isEmpty(feeRate)) {//撤销交易时设置费率字段
			trade_feerate.setText(queryModel.getFeeRate());
		}
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	@Override
	protected void onResume() {
		super.onResume();
//		initUrl();
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {  
	        return;  
	    }
		int id = v.getId();
		switch (id) {
		case R.id.confirmtotrade:
			if(StringUtil.isEmpty(consumepwd.getText().toString())||consumepwd.getText().toString().length()<6){
				ViewUtils.makeToast(ConsumeInputPwdActivity.this, getString(R.string.login_pwd_iserror), 1500);
				return;
			}
			initUrl() ;
			break;
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;

		default:
			break;
		}
		
	}
	private void requestFocusAndSoft(final EditText editText) {
		editText.setFocusableInTouchMode(true);
		editText.setFocusable(true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				// 自动弹出键盘
				InputMethodManager inputManager = (InputMethodManager) editText
						.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(editText, 0);
			}
		}, 998);
	}
	private void initUrl() {
		
		String tractData = (String) map.get("encTracks");
		String randomNumber = (String) map.get("randomNumber");
		String maskedPAN = (String) map.get("maskedPAN");
		String expiryDate = (String) map.get("expiryDate");
		int track1Length = Integer.parseInt((String) map.get("track1Length"));
		int track2Length = Integer.parseInt((String) map.get("track2Length"));
		String tract1Data =tractData.substring(track1Length, track2Length*2);
		String pwdVal = consumepwd.getText().toString();
		String workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
		
		
		LogUtil.syso("workkey===="+workkey);
		if("".equals(workkey)){
			ViewUtils.makeToast(this, "workkey异常", 1500);
			return;
		}
		String pinkey = workkey.substring(0, 38);
		byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
		String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);
		String terminal = StorageCustomerInfoUtil.getInfo("terminal",this);
		MyDBOperate myDBOperate = new MyDBOperate(this);
		List<TerminalInfo> info = myDBOperate.getTerminalInfo(terminal);
		int len = info.size();
		String batchNo = null;
		String voucherNo = null;
		if(len>0){
			TerminalInfo terminalInfo = info.get(len-1);
			batchNo = terminalInfo.getBatchNo();
			voucherNo = terminalInfo.getVoucherNo();
		}
		if(!TextUtils.isEmpty(voucherNo)){
			int voucherNoInt = Integer.valueOf(voucherNo);
			voucherNoInt = voucherNoInt+1;
			String voucherNoStr = CommonUtils.formatTo6Zero(String.valueOf(voucherNoInt));
			//更新数据库数据
			ContentValues values = new ContentValues();
			values.put("voucherNo",voucherNoStr);
			myDBOperate.update(values, terminal);
			StorageCustomerInfoUtil.putInfo(this, "voucherNo", voucherNoStr);
		}
		String customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
		String forpinkey = CommonUtils.Md5(pwdVal + StorageCustomerInfoUtil.getInfo("pinkey",this));
		String voucherNo_ = StorageCustomerInfoUtil.getInfo("voucherNo", this);
		LogUtil.syso("voucherNo_=="+voucherNo_);
		LogUtil.syso("terminal=="+terminal);
		String moneyVal = CommonUtils.formatTo12Zero(formatmoney);
		String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
		
		String sixtydata = null;
		if(Constant.CONSUME.equals(tradeType)){//消费
			sixtydata = "22"+batchNo+"003";
		}else{//撤销
			sixtydata = "23"+batchNo+"003";
		}
		String forMd5Data = null;
		String termianlVoucherNo =null;
		String terminalBatchNo = null;
		if(queryModel!=null){
			termianlVoucherNo = queryModel.getTermianlVoucherNo();
			terminalBatchNo = queryModel.getTerminalBatchNo();
		}
		if(Constant.CONSUME.equals(tradeType)){//消费
			forMd5Data = "0200" + maskedPAN + "000000"  + moneyVal +feeRateVal+ voucherNo_ + expiryDate + cardTypeValue +"000" + "12"+ tract1Data.toUpperCase()+
					terminal + customer +"156" + pinbyteHex.toUpperCase() + randomNumber+Constant.VERSION + sixtydata ;
		}else{
			forMd5Data = "0200" + maskedPAN + "000000"  + moneyVal + voucherNo_ + expiryDate + cardTypeValue + "000"+"12" + tract1Data.toUpperCase()+
					terminal + customer +"156" + pinbyteHex.toUpperCase() + randomNumber+Constant.VERSION + sixtydata + terminalBatchNo + termianlVoucherNo ;
		}
		
		LogUtil.syso("forMd5Data===="+forMd5Data);
		String data = null;
		if(Constant.CONSUME.equals(tradeType)){//消费
			data = "0=0200&2="+maskedPAN+"&3=000000&4="+ moneyVal +"&9="+feeRateVal+"&11="+voucherNo_+"&14="+expiryDate+"&22="+cardTypeValue+"&26=12"
					+"&23=000"
					+ "&35="+tract1Data.toUpperCase()+"&41="+terminal+"&42="+customer+"&49=156"
					+ "&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
					+"&59="+Constant.VERSION
					+ "&60="+sixtydata
					+ "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
		}else{//撤销
			data = "0=0200&2="+maskedPAN+"&3=000000&4="+ moneyVal +"&11="+voucherNo_+"&14="+expiryDate+"&22="+cardTypeValue+"&26=12"
					+"&23=000"
					+"&35="+tract1Data.toUpperCase()+"&41="+terminal+"&42="+customer+"&49=156"
					+"&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
					+"&59="+Constant.VERSION
					+ "&60="+sixtydata +"&61="+ terminalBatchNo + termianlVoucherNo 
					+ "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
		}
		
//		String data = "0=0200&2="+maskedPAN+"&3=310000"+"&11=000011&14="+expiryDate+"&22=022&26=12"
//				+ "&35="+tract1Data.toUpperCase()+"&41="+terminal+"&42="+customer+"&49=156"
//				+ "&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
//				+ "&60=01"+batchNo+"003"
//				+ "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
		String url = Constant.REQUEST_API + data;
		String substring = url.substring(225, 227);
		LogUtil.i("消费", url);
		//检查网络状态
		if(CommonUtils.getConnectedType(ConsumeInputPwdActivity.this)==-1){
			ViewUtils.makeToast(ConsumeInputPwdActivity.this, getString(R.string.nonetwork), 1500);
			return;
		}
		trade(url);
	}
	
	private void trade(String url) {

		MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {
		@Override
		public void isLoadingContent() {
			loadingDialog.show();
		}
		
		@Override
		public void isLoadedContent(String content) { 
			loadingDialog.dismiss();
			LogUtil.syso("content=="+content);//{"0":"0210","35":"6225760009310363=150710111399984","2":"6225760009310363","3":"310000","64":"4638454243313245","4":"000000000100","39":"94","42":"220558015061077","11":"000005","41":"99977411","14":"1507","49":"156","52":"10D7B1F75A1ABC8B","22":"022","26":"12","60":"22000001003","-1":null}
			if(StringUtil.isEmpty(content)){
				ViewUtils.makeToast(ConsumeInputPwdActivity.this, getString(R.string.reponse_content_isnull), 1500);
				return;
				
			}
			try {
				JSONObject obj = new JSONObject(content);
				String result = (String) obj.get("39");
				String resultValue = MyApplication.getErrorHint(result);
				if("00".equals(result)){
					String maskedPAN = (String) map.get("maskedPAN");
					String voucherNo = (String) obj.get("11");
					String voucherNo37 = (String) obj.get("37");
					Intent intent = new Intent();
					intent.setClass(ConsumeInputPwdActivity.this, SignNameActivity.class);
					intent.putExtra("tradeType", tradeType);
					intent.putExtra("queryModel", queryModel);
					intent.putExtra("cardNo", maskedPAN);
					intent.putExtra("voucherNo", voucherNo);
					intent.putExtra("voucherNo37", voucherNo37);
					intent.putExtra("money", money);
					intent.putExtra("feeRate", trade_feerate.getText().toString());
					startActivity(intent);
					ConsumeInputPwdActivity.this.finish();
					ViewUtils.overridePendingTransitionCome(ConsumeInputPwdActivity.this);
					
				}else{
//					ViewUtils.makeToast(ConsumeInputPwdActivity.this, getString(R.string.queryfailure), 1500);
					if (TextUtils.isEmpty(resultValue)) {
						ViewUtils.makeToast(ConsumeInputPwdActivity.this, "系统异常"+result, 1500);
					} else {
						ViewUtils.makeToast(ConsumeInputPwdActivity.this, resultValue, 1500);
					}
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	});
	myAsyncTask.execute(url);
	LogUtil.d(TAG, "url=="+url);
}
}

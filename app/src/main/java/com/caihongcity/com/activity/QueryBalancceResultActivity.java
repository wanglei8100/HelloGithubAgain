package com.caihongcity.com.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;

public class QueryBalancceResultActivity extends BaseActivity implements OnClickListener{

	private TextView balanceval,customernameval,trade_type_val,tradecardnumber_val;
	private String cardNo,money;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.querybalanceresult);
		findViewById(R.id.ll_back).setVisibility(View.GONE);
		((TextView) findViewById(R.id.tv_title_des)).setText("账户余额");
		balanceval = (TextView) findViewById(R.id.balanceval);
		customernameval = (TextView) findViewById(R.id.customernameval);
		trade_type_val = (TextView) findViewById(R.id.trade_type_val);
		tradecardnumber_val = (TextView) findViewById(R.id.tradecardnumber_val);
		findViewById(R.id.bt_confirm).setOnClickListener(this);
		cardNo = getIntent().getStringExtra("cardNo");
		money = getIntent().getStringExtra("money");
//		String switchMoney = switchMoney(money);
		String cardNoValue = cardNo.replace(cardNo.subSequence(6, cardNo.length()-4), "****");
		tradecardnumber_val.setText(cardNoValue);
		balanceval.setText("￥ "+CommonUtils.format02(money));
		
		String customerName = StorageCustomerInfoUtil.getInfo("customerName", this);
		customernameval.setText(customerName);
	}
	private String switchMoney(String money2) {
		Long money01 = Long.parseLong(money2);
		String money02 = String.valueOf(money01);
		if (money02.length()==0) {
			return "￥ "+"0.00";
		} else if (money02.length()==1) {
			return "￥ "+"0.0"+money02;
		} else if (money02.length()==2) {
			return "￥ "+"0."+money02;
		}else{
			String money03 = money02.substring(money02.length()-2, money02.length());
			String money04 = money02.substring(0, money02.length()-2);
			return "￥ "+money04+"."+money03;
		}
	}
	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.bt_confirm:
			finish();
		ViewUtils.overridePendingTransitionBack(this);
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}

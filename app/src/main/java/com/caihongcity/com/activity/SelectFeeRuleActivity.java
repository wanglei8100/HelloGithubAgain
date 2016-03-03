package com.caihongcity.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.ViewUtils;

public class SelectFeeRuleActivity extends BaseActivity implements
		OnClickListener {
	private String tradetype;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_feerule_layout);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView) findViewById(R.id.tv_title_des)).setText("选择费率");
		findViewById(R.id.ll_canyin_feerule).setOnClickListener(this);
		findViewById(R.id.ll_pifa_feerule).setOnClickListener(this);
		findViewById(R.id.ll_supermarket_feerule).setOnClickListener(this);
		findViewById(R.id.ll_shishijiesuan_feerule).setOnClickListener(this);
		tradetype = getIntent().getStringExtra("tradetype");
		intent = new Intent();
		intent.putExtra("tradetype", "consume");
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {  
	        return;  
	    }
		switch (v.getId()) {
		case R.id.ll_canyin_feerule:
			intent.putExtra("feeRate", "1.25");
			intent.putExtra("topFeeRate","0");
			intent.setClass(this, SwipeActivity_.class);
			startActivity(intent);
			ViewUtils.overridePendingTransitionCome(this);
			break;
		case R.id.ll_pifa_feerule:
			intent.putExtra("feeRate", "0.78");
			intent.putExtra("topFeeRate","0");
			intent.setClass(this, SwipeActivity_.class);
			startActivity(intent);
			ViewUtils.overridePendingTransitionCome(this);
			break;
		case R.id.ll_supermarket_feerule:
			intent.putExtra("feeRate", Constant.SUPERMAKET_FEERATE);
			intent.putExtra("topFeeRate","0");
			intent.setClass(this, SwipeActivity_.class);
			startActivity(intent);
			ViewUtils.overridePendingTransitionCome(this);
			break;
		case R.id.ll_shishijiesuan_feerule://T+0
			intent.putExtra("feeRate", "0.78");
			intent.putExtra("topFeeRate","35");
			intent.setClass(this, SwipeActivity_.class);
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
}

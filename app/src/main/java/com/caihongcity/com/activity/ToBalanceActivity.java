package com.caihongcity.com.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.ViewUtils;

/**
 * @author yuanjigong 申请结算
 */
public class ToBalanceActivity extends BaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tobalance);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView) findViewById(R.id.tv_title_des)).setText("申请结算");
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;
		default:
			break;
		}
	}
}

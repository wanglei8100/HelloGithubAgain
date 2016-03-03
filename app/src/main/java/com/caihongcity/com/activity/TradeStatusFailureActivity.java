package com.caihongcity.com.activity;

import com.caihongcity.com.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 
 * @author yuanjigong
 * 交易结果界面
 */
public class TradeStatusFailureActivity extends Activity implements OnClickListener{

	private TextView back_text;
	private ImageView back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tradestatus_failure);
		back = (ImageView) findViewById(R.id.back);
		back_text = (TextView) findViewById(R.id.back_text);
		back.setOnClickListener(this);
		back_text.setOnClickListener(this);
	}
	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.back:
			this.finish();
			break;
		case R.id.back_text:
			this.finish();
			break;

		default:
			break;
		}
	}
}

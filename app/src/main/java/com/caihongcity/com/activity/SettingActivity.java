package com.caihongcity.com.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.ActivityManager;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.ViewUtils;
/**
 * 
 * @author yuanjigong
 * 设置界面
 */
public class SettingActivity extends BaseActivity implements OnClickListener{
	private RelativeLayout rl_modifypwd;
	private RelativeLayout rl_update;
	private RelativeLayout rl_callservice;
	private RelativeLayout rl_setting_terminal;
	private TextView phonenum;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView)findViewById(R.id.tv_title_des)).setText("设置");
		rl_modifypwd = (RelativeLayout) findViewById(R.id.rl_modifypwd);
		rl_update = (RelativeLayout) findViewById(R.id.rl_update);
		rl_callservice = (RelativeLayout) findViewById(R.id.rl_callservice);
		rl_setting_terminal = (RelativeLayout) findViewById(R.id.rl_setting_terminal);

		rl_callservice.setOnClickListener(this);
		rl_update.setOnClickListener(this);
		rl_modifypwd.setOnClickListener(this);
		rl_setting_terminal.setOnClickListener(this);

		TextView tv_version = (TextView) findViewById(R.id.tv_version);
		tv_version.setText(CommonUtils.getAppVersionName(this));
		
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;
		case R.id.rl_modifypwd:
			Intent intent_setting = new Intent();
			intent_setting.setClass(this, ModifyPwdActivity.class);
			startActivity(intent_setting);
			ViewUtils.overridePendingTransitionCome(this);
			break;
		case R.id.rl_callservice:
			showCallServerDialog(this);
			break;
		case R.id.rl_setting_terminal:
			startActivity(new Intent(this,SelectTerminalActivity_.class));
			ViewUtils.overridePendingTransitionCome(this);
			break;

		default:
			break;
		}
	}
	
	private void showCallServerDialog(final Activity activity) {
		Button confirmBt, cancleBt;
		final Dialog mydialog = new Dialog(activity,R.style.MyProgressDialog);
		mydialog.setContentView(R.layout.callserver_dialog);
		mydialog.setCanceledOnTouchOutside(false);
		phonenum = (TextView) mydialog.findViewById(R.id.phonenum);
		confirmBt= (Button) mydialog.findViewById(R.id.left_bt);
		cancleBt = (Button) mydialog.findViewById(R.id.right_bt);
		confirmBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CommonUtils.isFastDoubleClick()) {  
			        return;  
			    }
				mydialog.dismiss();
				String serviceNumber = phonenum.getText().toString().replace("-","");
				Intent phoneIntent = new Intent(
						"android.intent.action.CALL", Uri.parse("tel:"
								+ serviceNumber));
				startActivity(phoneIntent);
			}
		});
		cancleBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mydialog.dismiss();
			}

		});
 
		mydialog.show();
	}
	
	public void saveExit(View v){
		finish();
		ActivityManager.exit();
		System.exit(0);
	}

}

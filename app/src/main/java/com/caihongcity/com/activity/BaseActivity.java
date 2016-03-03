package com.caihongcity.com.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.ActivityManager;
import com.caihongcity.com.utils.ViewUtils;

public class BaseActivity extends Activity {
	public RequestQueue newRequestQueue;
	public Dialog loadingDialog;
	public Dialog loadingDialogCanCancel;
	public Activity context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ActivityManager.getInstance().add(this);
		newRequestQueue = Volley.newRequestQueue(this);
		loadingDialog = ViewUtils.createLoadingDialog(BaseActivity.this, getString(R.string.loading_wait), false);
		loadingDialogCanCancel = ViewUtils.createLoadingDialog(BaseActivity.this, getString(R.string.loading_wait), true);
		context = this;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ViewUtils.overridePendingTransitionBack(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


}

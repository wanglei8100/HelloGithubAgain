package com.caihongcity.com.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.utils.ZoomImageView;

public class TradeStatusActivity extends BaseActivity implements OnClickListener {
private ZoomImageView iv_trade_reslut;
private String imageUrl;
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.trade_reslut_layout);
	findViewById(R.id.ll_back).setOnClickListener(this);
	((TextView) findViewById(R.id.tv_title_des)).setText("交易收据");
	imageUrl = getIntent().getStringExtra("imageUrl");
	iv_trade_reslut = (ZoomImageView) findViewById(R.id.iv_trade_reslut);
	loadingDialog.show();
	initData();
}

@SuppressLint("NewApi")
private void initData() {
    ImageRequest imageRequest = new ImageRequest(  
    		imageUrl,  
            new Response.Listener<Bitmap>() {  
                @Override  
                public void onResponse(Bitmap response) { 
                	loadingDialog.dismiss();
                	iv_trade_reslut.setBackground(new BitmapDrawable(response));
                }  
            }, 0, 0, Config.RGB_565, new Response.ErrorListener() {  
                @Override  
                public void onErrorResponse(VolleyError error) {
					loadingDialog.dismiss();
					ViewUtils.makeToast(TradeStatusActivity.this,getString(R.string.server_error),1000);
                }  
            });  
    newRequestQueue.add(imageRequest);
	
}

@Override
public void onClick(View v) {
	if (CommonUtils.isFastDoubleClick()) {  
        return;  
    }
switch (v.getId()) {
case R.id.ll_back:
	Intent intent = new Intent(this, StartActivity_.class);
	startActivity(intent);
	finish();
	break;

default:
	break;
}	
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
	if (keyCode == KeyEvent.KEYCODE_BACK) {
		
		Intent intent = new Intent(this, StartActivity_.class);
		startActivity(intent);
		finish();
		

	}
	return super.onKeyDown(keyCode, event);
}
}

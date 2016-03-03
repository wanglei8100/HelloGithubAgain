package com.caihongcity.com.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.CheckOutMessage;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;

public class ModifyPwdActivity extends BaseActivity implements OnClickListener {

	private TextView tv_title_des;
	private LinearLayout ll_back;
	private EditText et_old_pwd;
	private EditText et_new_pwd;
	private EditText et_new_pwd_confirm;
	private Button confirm_modify;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_pwd);
		tv_title_des = (TextView) findViewById(R.id.tv_title_des);
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		et_old_pwd = (EditText) findViewById(R.id.et_old_pwd);
		et_new_pwd = (EditText) findViewById(R.id.et_new_pwd);
		et_new_pwd_confirm = (EditText) findViewById(R.id.et_new_pwd_confirm);
		confirm_modify = (Button) findViewById(R.id.confirm_modify);
		tv_title_des.setText("修改登录密码");
		ll_back.setOnClickListener(this);
		confirm_modify.setOnClickListener(this);
		

	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {  
	        return;  
	    }
		switch (v.getId()) {
		
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;
		case R.id.confirm_modify:
			String old_pwd = et_old_pwd.getText().toString();
			String new_pwd = et_new_pwd.getText().toString();
			String new_pwd_confirm = et_new_pwd_confirm.getText().toString();
			if(CheckOutMessage.isEmpty(ModifyPwdActivity.this, et_old_pwd
					.getHint().toString(), et_old_pwd.getText().toString()))return;
			if(CheckOutMessage.isEmpty(ModifyPwdActivity.this, et_new_pwd
					.getHint().toString(), et_new_pwd.getText().toString()))return;
			if(CheckOutMessage.isEmpty(ModifyPwdActivity.this, "确认密码",
					et_new_pwd_confirm.getText().toString()))return;
			if (new_pwd.length() < 6 || new_pwd.length() > 16) {
				ViewUtils.makeToast(ModifyPwdActivity.this, "密码格式输入有误", 1000);
				return;
			}
			if (!new_pwd.equals(new_pwd_confirm)) {
				ViewUtils.makeToast(ModifyPwdActivity.this, "两次新密码输入不一致", 1000);
				return;
			}
			//检查网络状态
			if(CommonUtils.getConnectedType(ModifyPwdActivity.this)==-1){
				ViewUtils.makeToast(ModifyPwdActivity.this, getString(R.string.nonetwork), 1500);
				return;
			}
			sendToModifyPwd(old_pwd, new_pwd);
			break;

		default:
			break;
		}
	}

	private void sendToModifyPwd(final String old_pwd, final String new_pwd) {
		loadingDialog.show();
		String url = Constant.REQUEST_API;
		final String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum",
				ModifyPwdActivity.this);
		StringRequest stringRequest = new StringRequest(Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						loadingDialog.dismiss();
						LogUtil.i("ModifyPwdActivity", arg0);
						try {
							JSONObject obj = new JSONObject(arg0);
							String result = (String) obj.get("39");
							String resultValue = MyApplication.getErrorHint(result);
							if ("00".equals(result)) {
								// {"0":"0700","1":"15555808380","3":"190929","64":"D0EABB23F3E36E2BFBD740DEC62FEA9E",
								// "39":"00","42":"220558015061077","8":"96e79218965eb72c92a549dd5a330112","9":"96e79218965eb72c92a549dd5a330112"}
								ViewUtils.makeToast2(ModifyPwdActivity.this,
										"修改成功", 1500, MainActivity.class,
										"MOD_PWD");
							} else {
								// {"0":"0700","1":"15555808380","3":"190929","64":"2DF3A6A5A33090D814C5A644390F1FA7",
								// "39":"ZZ","8":"b71502e3f98933cdafb82b6bbfeebc2e","9":"96e79218965eb72c92a549dd5a330112"}
								if (TextUtils.isEmpty(resultValue)) {
									ViewUtils.makeToast(ModifyPwdActivity.this, "系统异常"+result, 1500);
								} else {
									ViewUtils.makeToast(ModifyPwdActivity.this, resultValue, 1500);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
							ViewUtils.makeToast(ModifyPwdActivity.this, "系统异常",
									1000);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						ViewUtils.makeToast(ModifyPwdActivity.this, "系统异常",
								1000);
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("0", "0700");
				map.put("1", phoneNum);
				map.put("3", "190929");
				map.put("8", CommonUtils.Md5(old_pwd));
				map.put("9", CommonUtils.Md5(new_pwd));
				map.put("59",Constant.VERSION);
				map.put("64",
						CommonUtils.Md5("0700" + phoneNum + "190929"
								+ CommonUtils.Md5(old_pwd)
								+ CommonUtils.Md5(new_pwd) + Constant.VERSION+Constant.mainKey));
				return map;
			}
		};
		newRequestQueue.add(stringRequest);
	}
}

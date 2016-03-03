package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.CheckOutMessage;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.MyAsyncTask.LoadResourceCall;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yuanjigong
 *         注册
 */
@EActivity(R.layout.register)
public class RegisterActivity extends BaseActivity implements OnClickListener {
    @ViewById
    Button register_imme, bt_get_check_code;
    private static String TAG = "RegisterActivity";
    @ViewById
    EditText phone, pwd, confirm_pwd, check_code;
    Timer timer;
    int time = 60;
    private Dialog backDialog;
    private Button dialog_confirmBt;
    private TextView dialog_title_text;

    @AfterViews
    void initData() {
        register_imme.setClickable(false);
        register_imme.setOnClickListener(this);
        phone = (EditText) findViewById(R.id.phone);
        findViewById(R.id.ll_back).setOnClickListener(this);
        findViewById(R.id.bt_get_check_code).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("注册");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @TextChange
    void phone() {
        String phoneValue = phone.getText().toString();
        if (phoneValue.length() == 11 && phoneValue.charAt(0) == '1') {
            check_code.requestFocus();
            isShowConfirmButton();
        }
    }


    @TextChange
    void pwd() {
        isShowConfirmButton();
    }

    @TextChange
    void check_code() {
        isShowConfirmButton();
    }

    @TextChange
    void confirm_pwd() {
        isShowConfirmButton();
    }

    private void isShowConfirmButton() {
        String pwdValue = pwd.getText().toString();
        String confirmPwdValue = confirm_pwd.getText().toString();
        String check_codeValue = check_code.getText().toString();
        String phoneNum = phone.getText().toString();
        if ((phoneNum.length() == 11) && (phoneNum.charAt(0) == '1') && (pwdValue.length() >= 6) && (confirmPwdValue.length() >= 6) && check_codeValue.length() > 0) {
            register_imme.setBackgroundResource(R.drawable.button_click_selector);
            register_imme.setClickable(true);
        } else {
            register_imme.setBackgroundResource(R.color.gray_light);
            register_imme.setClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick2()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.register_imme:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(RegisterActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String pwdValue = pwd.getText().toString();
                String confirmPwdValue = confirm_pwd.getText().toString();
                String phoneNum = phone.getText().toString();
                String check_codeValue = check_code.getText().toString();
                if (CheckOutMessage.isEmpty(RegisterActivity.this, "手机号", phoneNum)) return;
                if (CheckOutMessage.isEmpty(RegisterActivity.this, "密码", pwdValue)) return;
                if (CheckOutMessage.isEmpty(RegisterActivity.this, "确认密码", confirmPwdValue)) return;
                if (CheckOutMessage.isEmpty(RegisterActivity.this, "验证码", check_codeValue)) return;
                if (pwdValue.length() > 14) {
                    ViewUtils.makeToast(RegisterActivity.this, "密码位数超限，请重新录入", 1000);
                    return;
                }
                if (!pwdValue.equals(confirmPwdValue)) {
                    ViewUtils.makeToast(RegisterActivity.this, "两次新密码输入不一致", 1000);
                    return;
                }
                String md5Str_ = CommonUtils.Md5(pwdValue);
                String macStr = "0700" + phoneNum + "190918" + md5Str_ + Constant.AGENCY_CODE44 + check_codeValue + Constant.VERSION;
                String mac = CommonUtils.Md5(macStr + Constant.mainKey);
                String url = Constant.REQUEST_API + "0=0700&1=" + phoneNum + "&3=190918&64=" + mac + "&8=" + md5Str_ +
                        "&44=" + Constant.AGENCY_CODE44 + "&52=" + check_codeValue + "&59=" + Constant.VERSION;

                //检查网络状态
                if (CommonUtils.getConnectedType(RegisterActivity.this) == -1) {
                    ViewUtils.makeToast(RegisterActivity.this, getString(R.string.nonetwork), 1500);
                    return;
                }
                MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {

                    @Override
                    public void isLoadingContent() {
                        loadingDialog.show();
                    }

                    @Override
                    public void isLoadedContent(String content) {
                        loadingDialog.dismiss();
                        LogUtil.syso("content==" + content);//{"0":"0700","1":"15555808080","3":"190918","64":"35093CF7DA08EEA8CACD551927D6C043","39":"00","59":"CHDS-1.0.0","8":"96e79218965eb72c92a549dd5a330112"}
                        if (!TextUtils.isEmpty(content)) {
                            try {
                                JSONObject obj = new JSONObject(content);
                                if (obj.has("39")) {
                                    String code = (String) obj.get("39");
                                    if ("00".equals(code)) {//成功
                                        if (obj.has("1")) {//保存用户名，下次登录时直接填充
                                            String loginPhoneNum = (String) obj.get("1");
                                            StorageAppInfoUtil.putInfo(RegisterActivity.this, "phoneNum", loginPhoneNum);
                                        }
                                        ViewUtils.makeToast(RegisterActivity.this,
                                                "注册成功", 1500);
                                        ViewUtils.overridePendingTransitionBack(RegisterActivity.this);

                                    } else {//失败

                                        if("ZD".equals(code)){
                                            String resultValue = MyApplication.getErrorHint(code);
//                                            ViewUtils.makeToast(RegisterActivity.this, resultValue, 1000);
                                            backDialog();
                                        }else {
                                            String resultValue = "注册失败，请重新录入信息";
                                            ViewUtils.makeToast(RegisterActivity.this, resultValue, 1000);

                                        }
                                    }
                                } else {
                                    ViewUtils.makeToast(RegisterActivity.this, getString(R.string.server_error), 1000);
                                }
                            } catch (JSONException e) {
                                ViewUtils.makeToast(RegisterActivity.this, getString(R.string.server_error), 1000);
                                e.printStackTrace();
                            }
                        } else {
                            ViewUtils.makeToast(RegisterActivity.this, getString(R.string.server_error), 1000);
                        }
                    }
                });
                myAsyncTask.execute(url);
                LogUtil.d(TAG, "url==" + url);
                break;
            case R.id.bt_get_check_code:
                if ("获取验证码".equals(bt_get_check_code.getText().toString())) {
                    String phoneValue = phone.getText().toString();
                    if (phoneValue.length() != 11 || !phoneValue.substring(0, 1).equals("1")) {
                        ViewUtils.makeToast(this, "请输入正确的手机号", 1000);
                        return;
                    }
                    sendCheckCode(phoneValue);
                }
                break;
            case R.id.ll_back:
                this.finish();
                ViewUtils.overridePendingTransitionBack(this);
                break;

            default:
                break;
        }
    }

    private void backDialog() {
        backDialog = new Dialog(RegisterActivity.this, R.style.MyProgressDialog);
        backDialog.setContentView(R.layout.chose_dialog_upload);
        backDialog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) backDialog.findViewById(R.id.left_bt);
        Button cancleButton = (Button) backDialog.findViewById(R.id.right_bt);
        cancleButton.setVisibility(View.GONE);
        dialog_title_text = ((TextView) backDialog.findViewById(R.id.title_text));
        dialog_title_text.setText("手机号码已被注册\n点击确定立即登录");

        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmBt_des = dialog_confirmBt.getText().toString();
                if ("确定".equals(confirmBt_des)) {

                    ViewUtils.overridePendingTransitionBack(RegisterActivity.this);
                }

            }
        });
        backDialog.show();

    }

    private void sendCheckCode(final String phoneValue) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.REQUEST_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String arg0) {
                loadingDialog.dismiss();
                LogUtil.i("ModifyPwdActivity", arg0);
                try {
                    JSONObject obj = new JSONObject(arg0);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {
                        toast("00");
                    } else {
                        toast(resultValue);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast(getString(R.string.server_error));
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                toast(getString(R.string.server_error));
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("0", "0700");
                map.put("1", phoneValue);
                map.put("3", "190919");
                map.put("59", Constant.VERSION);
                map.put("64",
                        CommonUtils.Md5("0700" + phoneValue + "190919" + Constant.VERSION + Constant.mainKey));

                return map;
            }
        };
        newRequestQueue.add(stringRequest);
    }


    @UiThread
    void toast(String s) {
        if ("00".equals(s)) {
            time();
        } else if ("01".equals(s)) {
            if (time == 0) {
                timer.cancel();
                bt_get_check_code.setText("获取验证码");
                bt_get_check_code.setBackgroundResource(R.drawable.button_click_selector);
                bt_get_check_code.setClickable(true);
            } else {
                bt_get_check_code.setText(time + "秒后可重新发送");
                bt_get_check_code.setBackgroundResource(R.color.gray_light);
                bt_get_check_code.setClickable(false);
                time--;
            }
        } else {
            ViewUtils.makeToast(RegisterActivity.this, s, 1500);
        }
    }

    @Background
    void time() {
        timer = new Timer();
        time = 60;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                toast("01");
            }
        };
        timer.schedule(task, 1000, 1000);
        toast("验证码已发送，30分钟内有效，请注意查收");
    }

}

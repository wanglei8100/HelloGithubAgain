package com.caihongcity.com.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CheckOutMessage;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StringUtil;
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
import java.util.Timer;
import java.util.TimerTask;

@EActivity(R.layout.activity_find_pwd)
public class FindPwdActivity extends BaseActivity implements View.OnClickListener {
    @ViewById
    Button find_confirm, bt_get_check_code;
    @ViewById
    EditText phone, pwd, confirm_pwd, check_code;
    Timer timer;
    int time = 60;

    @AfterViews
    void initData() {
        String phoneNum = StorageAppInfoUtil.getInfo("phoneNum", this);
        phone.setText(phoneNum);
        find_confirm.setClickable(false);
        find_confirm.setOnClickListener(this);
        phone = (EditText) findViewById(R.id.phone);
        findViewById(R.id.ll_back).setOnClickListener(this);
        findViewById(R.id.bt_get_check_code).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("找回密码");
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
            find_confirm.setBackgroundResource(R.drawable.button_click_selector);
            find_confirm.setClickable(true);
        } else {
            find_confirm.setBackgroundResource(R.color.gray_light);
            find_confirm.setClickable(false);
        }

    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick2()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.find_confirm:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(FindPwdActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String pwdValue = pwd.getText().toString();
                String confirmPwdValue = confirm_pwd.getText().toString();
                String phoneNum = phone.getText().toString();
                String check_codeValue = check_code.getText().toString();
                if (CheckOutMessage.isEmpty(FindPwdActivity.this, "手机号", phoneNum)) return;
                if (CheckOutMessage.isEmpty(FindPwdActivity.this, "密码", pwdValue)) return;
                if (CheckOutMessage.isEmpty(FindPwdActivity.this, "确认密码", confirmPwdValue)) return;
                if (CheckOutMessage.isEmpty(FindPwdActivity.this, "验证码", check_codeValue)) return;
                if (pwdValue.length() < 6 || pwdValue.length() > 16) {
                    ViewUtils.makeToast(FindPwdActivity.this, "密码格式输入有误", 1000);
                    return;
                }
                if (!pwdValue.equals(confirmPwdValue)) {
                    ViewUtils.makeToast(FindPwdActivity.this, "两次新密码输入不一致", 1000);
                    return;
                }

                HashMap<Integer, String> requestData = new HashMap<Integer, String>();
                String md5Str_ = CommonUtils.Md5(pwdValue);
                requestData.put(0, "0700");
                requestData.put(1, phoneNum);
                requestData.put(3, "190930");
                requestData.put(9, md5Str_);
                requestData.put(52, check_codeValue);
                requestData.put(59, Constant.VERSION);
                requestData.put(64, Constant.getMacData(requestData));
                String url = Constant.getUrl(requestData);

                //检查网络状态
                if (CommonUtils.getConnectedType(FindPwdActivity.this) == -1) {
                    ViewUtils.makeToast(FindPwdActivity.this, getString(R.string.nonetwork), 1500);
                    return;
                }
                MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {

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
                                            StorageAppInfoUtil.putInfo(FindPwdActivity.this, "phoneNum", loginPhoneNum);
                                        }
                                        ViewUtils.makeToast2(FindPwdActivity.this,
                                                "重置密码成功", 1500, MainActivity.class,
                                                "REGISTER");
                                    } else {//失败
                                        String resultValue = MyApplication.getErrorHint(code);
                                        ViewUtils.makeToast(FindPwdActivity.this, resultValue, 1000);
                                    }
                                } else {
                                    ViewUtils.makeToast(FindPwdActivity.this, getString(R.string.server_error), 1000);
                                }
                            } catch (JSONException e) {
                                ViewUtils.makeToast(FindPwdActivity.this, getString(R.string.server_error), 1000);
                                e.printStackTrace();
                            }
                        } else {
                            ViewUtils.makeToast(FindPwdActivity.this, getString(R.string.server_error), 1000);
                        }
                    }
                });
                myAsyncTask.execute(url);
                LogUtil.syso("url==" + url);
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

    private void sendCheckCode(String phoneValue) {
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0700");
        requestData.put(1, phoneValue);
        requestData.put(3, "190919");
        requestData.put(59, Constant.VERSION);
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        MyAsyncTask localMyAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                // String s = content;
                //如果返回的json字符串为空，则提示网络连接异常
                if (StringUtil.isEmpty(content)) {
                    toast(getString(R.string.server_time_out));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);

                    String bindStatus = (String) obj.get("39");
                    //获取规定好的绑定响应码
                    String resultValue = MyApplication.getErrorHint(bindStatus);
                    if ("00".equals(bindStatus)) {
                        toast("00");
                    } else {
                        toast(resultValue);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    toast(getString(R.string.server_time_out));
                }
            }

            public void isLoadingContent() {

            }
        });
        LogUtil.syso("url===" + url);
        localMyAsyncTask.execute(url);
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
            ViewUtils.makeToast(FindPwdActivity.this, s, 1500);
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
        timer.schedule(task, 500, 1000);
    }
}

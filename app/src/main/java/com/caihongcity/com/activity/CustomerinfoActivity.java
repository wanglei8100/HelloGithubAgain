package com.caihongcity.com.activity;

import android.content.Intent;
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
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerinfoActivity extends BaseActivity implements
        OnClickListener {
    private EditText et_phone_number;
    private EditText et_card_number;
    private EditText et_bank_name;
    private EditText et_account_name;
    private EditText et_bank_number;
    private TextView tv_bank_number;
    private TextView tv_account_name;
    private TextView tv_bank_name;
    private TextView tv_card_number;
    private TextView tv_customer_status_desc;
    private TextView tv_customer_status,tv_checked_info;
    private Button bt_submit;
    private LinearLayout ll_submit,ll_checked_info,ll_checked_info_line,ll_customer_status_line,ll_customer_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customerinfo_layout);
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("商户信息");
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        et_card_number = (EditText) findViewById(R.id.et_card_number);
        et_bank_name = (EditText) findViewById(R.id.et_bank_name);
        et_account_name = (EditText) findViewById(R.id.et_account_name);
        et_bank_number = (EditText) findViewById(R.id.et_bank_number);
        tv_account_name = (TextView) findViewById(R.id.tv_account_name);
        tv_bank_name = (TextView) findViewById(R.id.tv_bank_name);
        tv_bank_number = (TextView) findViewById(R.id.tv_bank_number);
        tv_card_number = (TextView) findViewById(R.id.tv_card_number);
        tv_customer_status = (TextView) findViewById(R.id.tv_customer_status);
        tv_customer_status_desc = (TextView) findViewById(R.id.tv_customer_status_desc);
        tv_checked_info = (TextView) findViewById(R.id.tv_checked_info);
        bt_submit = (Button) findViewById(R.id.bt_submit);
        ll_submit = (LinearLayout) findViewById(R.id.ll_submit);
        ll_checked_info = (LinearLayout) findViewById(R.id.ll_checked_info);
        ll_checked_info_line = (LinearLayout) findViewById(R.id.ll_checked_info_line);
        ll_customer_status_line = (LinearLayout) findViewById(R.id.ll_customer_status_line);
        ll_customer_status = (LinearLayout) findViewById(R.id.ll_customer_status);
        ll_submit.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
         initView();
    }

    private void initView() {
        boolean isInfoComplete = getIntent().getBooleanExtra("isInfoComplete", false);
        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum",
                CustomerinfoActivity.this);
        String freezeStatus = StorageCustomerInfo02Util.getInfo("freezeStatus",
                CustomerinfoActivity.this);
        et_phone_number.setText(phoneNum);
        //10A 未审核，10B 审核通过，10C 审核拒绝，10D 重新审核
        /**
         * 1:未认证：未提交商户信息时显示
         2:未审核：已提交商户信息，未完成审核状态（后台取［风控审核］状态）
         3:审核拒绝：提交商户信息审核拒绝，（后台取［风控审核］状态）
         页面显示［审核拒绝原因］字段信息
         4:重新审核：提交商户信息正等待审核，（后台取［风控审核］状态）
         不显示［审核拒绝原因］字段信息
         5:审核通过：提交商户信息审核通过，（后台取［风控审核］状态）
         */
        if (isInfoComplete) {
            ll_customer_status_line.setVisibility(View.VISIBLE);
            ll_customer_status.setVisibility(View.VISIBLE);
            if ("10A".equals(freezeStatus)){
                tv_customer_status.setText("未审核");
                tv_customer_status_desc.setText("已提交商户信息，未完成审核状态");
            }else if ("10B".equals(freezeStatus)){
                et_phone_number.setTextColor(getResources().getColor(R.color.black_55));
                tv_customer_status.setText("审核通过");
                tv_customer_status_desc.setText("提交商户信息审核通过");
            }else if ("10C".equals(freezeStatus)){
                tv_customer_status.setText("审核拒绝");
                tv_customer_status_desc.setText("提交商户信息审核拒绝");
            }else if ("10D".equals(freezeStatus)){
                tv_customer_status.setText("重新审核");
                tv_customer_status_desc.setText("提交商户信息正等待审核");
            }
        } else {
            tv_customer_status.setText("未认证");
            ll_customer_status_line.setVisibility(View.GONE);
            ll_customer_status.setVisibility(View.GONE);
        }
        String bankAccount = StorageCustomerInfo02Util.getInfo("bankAccount",
                CustomerinfoActivity.this);
        bankAccount = CommonUtils.translateShortNumber(bankAccount,6,4);
        String bankAccountName = StorageCustomerInfo02Util.getInfo(
                "bankAccountName", CustomerinfoActivity.this);
        String idCardNumber = StorageCustomerInfo02Util.getInfo("idCardNumber",
                CustomerinfoActivity.this);
        idCardNumber = CommonUtils.translateShortNumber(idCardNumber,6,4);
        String bankDetail = StorageCustomerInfo02Util.getInfo("bankDetail",
                CustomerinfoActivity.this);
        if (!TextUtils.isEmpty(bankAccount)) {
            String examineResult = StorageCustomerInfo02Util.getInfo("examineResult", this);
            String examineState = StorageCustomerInfo02Util.getInfo("examineState", this);
            if("W8".equals(examineState)){//风控审核没有通过
                et_bank_number.setText(bankAccount);
                et_account_name.setText(bankAccountName);
                et_bank_name.setText(bankDetail);
                et_card_number.setText(idCardNumber);
                ll_checked_info_line.setVisibility(View.VISIBLE);
                ll_checked_info.setVisibility(View.VISIBLE);
                tv_checked_info.setText(examineResult);
                et_bank_number.setFocusable(false);
                et_account_name.setFocusable(false);
                et_bank_name.setFocusable(false);
                et_card_number.setFocusable(false);
                bt_submit.setText(getString(R.string.next_page));
                findViewById(R.id.iv_item_right).setVisibility(View.GONE);
            }else {
                et_bank_number.setText(CommonUtils.translateShortNumber(bankAccount, 6, 4));
                et_account_name.setText(bankAccountName.replace(bankAccountName.substring(0, 1), "*"));
                et_bank_name.setText(bankDetail);
                et_card_number.setText(CommonUtils.translateShortNumber(idCardNumber, 3, 2));
                ll_checked_info_line.setVisibility(View.GONE);
                ll_checked_info.setVisibility(View.GONE);
                et_bank_number.setFocusable(false);
                et_account_name.setFocusable(false);
                et_bank_name.setFocusable(false);
                et_card_number.setFocusable(false);
                bt_submit.setText(getString(R.string.next_page));
                findViewById(R.id.iv_item_right).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.ll_submit).setOnClickListener(this);
            et_bank_name.setOnClickListener(this);
            ViewUtils.showChoseDialog(this, true, "请确保信息填写正确，生效后不可修改",
                    View.GONE, null);
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.et_bank_name:
                startActivityForResult(
                        new Intent(this, BankNameListActivity.class), 1);
                break;
            case R.id.ll_submit:
                if (getString(R.string.next_step).equals(bt_submit.getText().toString())) {
                    String card_number = et_card_number.getText().toString();
                    String bank_name = et_bank_name.getText().toString();
                    String account_name = et_account_name.getText().toString();
                    String bank_number = et_bank_number.getText().toString();
                    String phone_number = et_phone_number.getText().toString();
                    if (!CommonUtils.isIdCard(card_number)) {
                        ViewUtils.makeToast(this, "身份证格式错误，请重新填写", 1000);
                        return;
                    }

                    if (TextUtils.isEmpty(bank_name)) {
                        ViewUtils.makeToast(this, tv_bank_name.getText().toString()
                                + "不能为空", 1000);
                        return;
                    }
                    if (TextUtils.isEmpty(account_name)) {
                        ViewUtils.makeToast(this, tv_account_name.getText().toString()
                                + "不能为空", 1000);
                        return;
                    }
                    if (TextUtils.isEmpty(bank_number)) {
                        ViewUtils.makeToast(this, tv_bank_number.getText().toString()
                                + "不能为空", 1000);
                        return;
                    }
                    String bank_name_code = MyApplication.bankNameList.get(bank_name);
                    // 检查网络状态
                    if (CommonUtils.getConnectedType(CustomerinfoActivity.this) == -1) {
                        ViewUtils.makeToast(CustomerinfoActivity.this,
                                getString(R.string.nonetwork), 1500);
                        return;
                    }
                    sendSubmit(card_number, bank_name_code, account_name, bank_number,
                            phone_number);
                } else if (getString(R.string.next_page).equals(bt_submit.getText().toString())) {
                    Intent cusPicIntent = new Intent();
                    cusPicIntent.setClass(CustomerinfoActivity.this, CustomerPicInfoActivity.class);
                    startActivity(cusPicIntent);
                    ViewUtils.overridePendingTransitionCome(CustomerinfoActivity.this);
                }

                break;
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
        }
    }

    private void sendSubmit(final String card_number,
                            final String bank_name_code, final String account_name,
                            final String bank_number, final String phone_number) {
        String url = Constant.REQUEST_API;
        loadingDialog.show();
        StringRequest stringRequest = new StringRequest(Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String arg0) {
                        loadingDialog.dismiss();
                        LogUtil.i("CustomerinfoActivity", arg0);
                        try {
                            JSONObject obj = new JSONObject(arg0);
                            String result = (String) obj.get("39");
                            String resultValue = MyApplication.responseCodeMap
                                    .get(result);
                            if ("00".equals(result)) {
                                ViewUtils.makeToast(CustomerinfoActivity.this,
                                        "提交成功", 1500);
                                StorageCustomerInfoUtil.putInfo(
                                        CustomerinfoActivity.this,
                                        "customerName", account_name);
                                StorageCustomerInfo02Util.putInfo(CustomerinfoActivity.this, "bankAccount", et_bank_number.getText().toString());
                                StorageCustomerInfo02Util.putInfo(CustomerinfoActivity.this, "bankAccountName", et_account_name.getText().toString());
                                StorageCustomerInfo02Util.putInfo(CustomerinfoActivity.this, "idCardNumber", et_card_number.getText().toString());
                                StorageCustomerInfo02Util.putInfo(CustomerinfoActivity.this, "bankDetail", et_bank_name.getText().toString());
                                StorageCustomerInfo02Util.putInfo(CustomerinfoActivity.this, "phone", et_phone_number.getText().toString());
                                Intent cusPicIntent = new Intent();
                                cusPicIntent.setClass(CustomerinfoActivity.this, CustomerPicInfoActivity.class);
                                startActivity(cusPicIntent);
                                ViewUtils.overridePendingTransitionCome(CustomerinfoActivity.this);
                            } else {
                                if (!TextUtils.isEmpty(resultValue)) {
                                    ViewUtils.makeToast(
                                            CustomerinfoActivity.this,
                                            resultValue, 1000);
                                } else {
                                    ViewUtils.makeToast(
                                            CustomerinfoActivity.this, "操作失败",
                                            1000);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ViewUtils.makeToast(CustomerinfoActivity.this,
                                    "数据解析异常", 1000);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                ViewUtils.makeToast(CustomerinfoActivity.this, "系统异常",
                        1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("0", "0700");
                map.put("1", phone_number);
                map.put("3", "190938");
                map.put("5", account_name);
                map.put("6", card_number);
                map.put("7", bank_number);
                map.put("43", bank_name_code);
                map.put("59", Constant.VERSION);
                map.put("64",
                        CommonUtils.Md5("0700" + phone_number + "190938"
                                + account_name + card_number + bank_number
                                + bank_name_code + Constant.VERSION
                                + Constant.mainKey));

                return map;
            }
        };
        newRequestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String selectBankname = data.getStringExtra("selectBankname");
            et_bank_name.setText(selectBankname);
        }
    }
}

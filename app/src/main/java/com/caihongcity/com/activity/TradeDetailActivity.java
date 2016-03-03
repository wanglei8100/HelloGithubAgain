package com.caihongcity.com.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.MyAsyncTask.LoadResourceCall;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yuanjigong
 *         交易详情
 */
public class TradeDetailActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = null;
    public QueryModel queryModel;
    private TextView consumeTradeMoney;
    private TextView consumetradetime_pro;
    private TextView cardbank_pro;
    private TextView cardnumber_pro;
    private TextView tradenumber_pro;
    private TextView shouquanma;
    private TextView tradetype_pro;
    private TextView tradestatus;
    private Button cancelTrade, bt_tixian,bt_cancel;
    private Dialog myDialog;
    private View view_ = null;
    private ImageView iv_sign_name;
    private LinearLayout signnameimage_ll;
    private LinearLayout consumetrademoney_ll;
    private LinearLayout consumetradetime_layout;
    private LinearLayout ll_tixian_satus;
    private LinearLayout ll_tixian_satus_view;
    private TextView trade_rate;
    private TextView tv_tixian_satus;
    private String maxFee;
    private TextView customer_name;
    private View shouquanma_layout;
    private View shouquanma_view;
    private String identity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tradedetail);
        findViewById(R.id.title).setBackgroundColor(Color.WHITE);
        TextView backText = (TextView) findViewById(R.id.back_text);
        backText.setTextColor(Color.BLACK);
        TextView tvTitleDes = (TextView) findViewById(R.id.tv_title_des);
        tvTitleDes.setTextColor(Color.BLACK);
        queryModel = (QueryModel) getIntent().getSerializableExtra("queryModel");
        customer_name = (TextView) findViewById(R.id.customer_name);
        consumeTradeMoney = (TextView) findViewById(R.id.consumetrademoney);
        consumetradetime_pro = (TextView) findViewById(R.id.consumetradetime_pro);
        cardbank_pro = (TextView) findViewById(R.id.cardbank_pro);
        cardnumber_pro = (TextView) findViewById(R.id.cardnumber_pro);
        tradenumber_pro = (TextView) findViewById(R.id.tradenumber_pro);
        shouquanma_layout = findViewById(R.id.shouquanma_layout);
        shouquanma = (TextView) findViewById(R.id.shouquanma);
        shouquanma_view = findViewById(R.id.shouquanma_view);
        tradetype_pro = (TextView) findViewById(R.id.tradetype_pro);
        tradestatus = (TextView) findViewById(R.id.tradestatus);
        iv_sign_name = (ImageView) findViewById(R.id.iv_sign_name);
        signnameimage_ll = (LinearLayout) findViewById(R.id.signnameimage_ll);
        consumetrademoney_ll = (LinearLayout) findViewById(R.id.consumetrademoney_ll);
        consumetradetime_layout = (LinearLayout) findViewById(R.id.consumetradetime_layout);
        ll_tixian_satus = (LinearLayout) findViewById(R.id.ll_tixian_satus);
        ll_tixian_satus_view = (LinearLayout) findViewById(R.id.ll_tixian_satus_view);
        tv_tixian_satus = (TextView) findViewById(R.id.tv_tixian_satus);
        trade_rate = (TextView) findViewById(R.id.trade_rate);
        maxFee = queryModel.getMaxFee();
        trade_rate.setText(queryModel.getFeeRate()+"-"+(maxFee==null?"0":maxFee));
        consumetradetime_pro.setText(queryModel.getTradeTime());
        cardbank_pro.setText(queryModel.getBankName());
        String bankAccountName = StorageCustomerInfo02Util.getInfo(
                "bankAccountName", TradeDetailActivity.this);
        customer_name.setText(bankAccountName);
        String cardNo = queryModel.getCardNo();
        String cardNoValue = cardNo.replace(cardNo.subSequence(6, cardNo.length() - 4), "****");
        cardnumber_pro.setText(cardNoValue);
        tradenumber_pro.setText(queryModel.getOrderNo());
        String acqAuthNo = queryModel.getAcqAuthNo();
        if(TextUtils.isEmpty(acqAuthNo)){
            shouquanma_layout.setVisibility(View.GONE);
            shouquanma_view.setVisibility(View.GONE);
        }else{
            shouquanma.setText(acqAuthNo);
        }

        String tradeTypeDes = queryModel.getTradeType();
        cancelTrade = (Button) findViewById(R.id.canceltrade);
        cancelTrade.setOnClickListener(this);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_tixian = (Button) findViewById(R.id.bt_tixian);
        bt_tixian.setOnClickListener(this);
        identity = getIntent().getStringExtra("identity");
        if("LEFT_SUPERMARKET".equals(identity)){
            tradetype_pro.setText("超市消费");
        }else if("LEFT_CANYIN".equals(identity)){
            tradetype_pro.setText("餐饮消费");
        }else if("LEFT_PIFA".equals(identity)){
            tradetype_pro.setText("批发消费");
        }else if("LEFT_BAIHUO".equals(identity)){
            tradetype_pro.setText("百货消费");
        }else {
            tradetype_pro.setText(tradeTypeDes);
        }
        if ("消费撤销".equals(tradeTypeDes)) {
            consumeTradeMoney.setTextColor(getResources().getColor(R.color.red));
        }
        String tradeMoney = queryModel.getTradeMoney();
        if (tradeMoney.contains("-")) {
            tradeMoney = tradeMoney.replace("-", "");
        }
        consumeTradeMoney.setText("￥ " + CommonUtils.format(tradeMoney));
        String tradeStatusDes = queryModel.getTradeStatus();
        String payStatus = queryModel.getPayStatus();
        if ("交易成功".endsWith(tradeStatusDes) && "消费".equals(tradeTypeDes) && "1".equals(queryModel.getSettleCycle())) {
            cancelTrade.setVisibility(View.VISIBLE);
            bt_tixian.setVisibility(View.VISIBLE);
        } else {
            cancelTrade.setVisibility(View.GONE);
            bt_tixian.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(payStatus)) {
            ll_tixian_satus.setVisibility(View.VISIBLE);
            ll_tixian_satus_view.setVisibility(View.VISIBLE);
            if ("10A".equals(payStatus)) {
                tv_tixian_satus.setText("提现受理失败");
            } else if ("10B".equals(payStatus)) {
                tv_tixian_satus.setText("提现中");
            } else if ("10C".equals(payStatus)) {
                tv_tixian_satus.setText("提现成功");
            } else if ("10D".equals(payStatus)) {
                tv_tixian_satus.setText("提现失败");
            }
        }
        if ("交易失败".endsWith(tradeStatusDes)) signnameimage_ll.setVisibility(View.GONE);
        if ("余额查询".equals(tradeTypeDes)) {
            signnameimage_ll.setVisibility(View.GONE);
            consumetradetime_layout.setVisibility(View.GONE);
            consumetrademoney_ll.setVisibility(View.GONE);
        }
        tradestatus.setText(tradeStatusDes);
//		sendphone_pro = (EditText) findViewById(R.id.sendphone_pro);
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("交易明细");
        String where_from = getIntent().getStringExtra("where_from");

        if ("QueryActivity".equals(where_from)) {//来自交易明细
            bt_cancel.setVisibility(View.VISIBLE);
            bt_cancel.setText("确定");
            bt_cancel.setOnClickListener(this);
            bt_tixian.setVisibility(View.GONE);
            cancelTrade.setVisibility(View.GONE);
        }else {//来自实时结算
            bt_cancel.setVisibility(View.VISIBLE);
            bt_cancel.setText("取消");
            bt_cancel.setOnClickListener(this);
            bt_tixian.setVisibility(View.VISIBLE);
            bt_tixian.setOnClickListener(this);
            cancelTrade.setVisibility(View.GONE);
        }
        loadSignNamePic(queryModel.getSignUrl());
    }

    private void loadSignNamePic(String signUrl) {
        if (TextUtils.isEmpty(signUrl)) {
            iv_sign_name.setVisibility(View.GONE);
            return;
        } else {
            iv_sign_name.setVisibility(View.VISIBLE);
        }
        loadingDialogCanCancel.show();
        ImageRequest imageRequest = new ImageRequest(
                signUrl,
                new Response.Listener<Bitmap>() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onResponse(Bitmap response) {
                        loadingDialogCanCancel.dismiss();
                        iv_sign_name.setBackground(new BitmapDrawable(response));
                    }
                }, 0, 0, Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.i(TAG, "图片加载失败");
                if (loadingDialogCanCancel != null) {
                    loadingDialogCanCancel.dismiss();
                }

            }
        });
        newRequestQueue.add(imageRequest);
    }

    @Override
    public void onClick(View view) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        int id = view.getId();
        switch (id) {
            case R.id.canceltrade:
                Intent intent = new Intent();
                String bluetooth_address = StorageAppInfoUtil.getInfo("bluetooth_address", this);
                String terminal_type = StorageAppInfoUtil.getInfo("terminal_type", this);
                if ("1".equals(terminal_type)) {
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent.setClass(this, BluetoothSelectActivity_.class);
                    }else{
                        intent.setClass(this, SwipeWaitBluetoothActivity_.class);
                        intent.putExtra("blue_address", bluetooth_address);
                    }
                }else if ("2".equals(terminal_type)) {
                    intent.setClass(this, SwipeWaitHuiXingActivity_.class);
                }else if ("3".equals(terminal_type)) {
                    intent.setClass(this, SwipeWaitMoFangActivity_.class);
                }else if ("4".equals(terminal_type)) {
                    intent.setClass(this, SwipeWaitZhongCiActivity_.class);
                }else if("5".equals(terminal_type)){
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent.setClass(this, BluetoothSelectActivity_.class);
                    }else{
                        intent.setClass(this, SwipeWaitMoFangBlueActivity_.class);
                        intent.putExtra("blue_address", bluetooth_address);
                    }

                }else {
                    intent.setClass(this, SwipeWaitActivity.class);
                }
                intent.putExtra("tradetype", "canceltrade");
                intent.putExtra("money", queryModel.getTradeMoney());
                intent.putExtra("queryModel", queryModel);
                intent.putExtra("topFeeRate",  (maxFee==null?"0":maxFee));
                intent.putExtra("feeRate",  queryModel.getFeeRate());
                startActivity(intent);
                TradeDetailActivity.this.finish();
                ViewUtils.overridePendingTransitionCome(TradeDetailActivity.this);
                break;
            case R.id.bt_tixian:
                ViewUtils.showChoseDialog(this, true, "是否提现", View.VISIBLE, new ViewUtils.OnChoseDialogClickCallback() {
                    @Override
                    public void clickOk() {
                        tiXian();
                    }

                    @Override
                    public void clickCancel() {
                        return;
                    }
                });
                break;
            case R.id.ll_back:
            case R.id.bt_cancel:
                ViewUtils.overridePendingTransitionBack(this);
                break;

            default:
                break;
        }

    }

    private void tiXian() {
        String termianlVoucherNo = queryModel.getTermianlVoucherNo();
        String orderNo = queryModel.getOrderNo();
        String data = "0=0200" + "&3=190989&11=" + termianlVoucherNo + "&59=" + Constant.VERSION + "&60=" + orderNo;
        String macData = "0200" + 190989 + termianlVoucherNo + Constant.VERSION + orderNo;
        String url = Constant.REQUEST_API + data + "&64=" + CommonUtils.Md5(macData + Constant.mainKey);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {
            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }
            @Override
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                loadingDialog.dismiss();
                if (StringUtil.isEmpty(content)) {
                    ViewUtils.makeToast(TradeDetailActivity.this, getString(R.string.server_error), 1500);
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {
                        ViewUtils.makeToast(TradeDetailActivity.this, "提现受理成功", 1500);
                        Intent intent = new Intent();
                        intent.setClass(TradeDetailActivity.this,RealTimeActivity.class);
                        startActivity(intent);
                        ViewUtils.overridePendingTransitionBack(TradeDetailActivity.this);

                    } else {
                        ViewUtils.makeToast(TradeDetailActivity.this, resultValue, 1500);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d(TAG, "url==" + url);

    }
    /*private void checkPwd(String password) {
        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
        String data = "0=0700&1=" + phoneNum + "&3=190928&8=" + CommonUtils.Md5(password) + "&59=" + Constant.VERSION;
        String macData = "0700" + phoneNum + "190928" + CommonUtils.Md5(password) + Constant.VERSION;
        String url = Constant.REQUEST_API + data + "&64=" + CommonUtils.Md5(macData + Constant.mainKey);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {

            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }

            @Override
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                loadingDialog.dismiss();
                if (StringUtil.isEmpty(content)) {
                    ViewUtils.makeToast(TradeDetailActivity.this, getString(R.string.server_error), 1500);
                    return;

                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {

                        Intent intent = new Intent();
                        if ("1".equals(StorageCustomerInfo02Util.getInfo("terminal_type", TradeDetailActivity.this))) {
                            intent.setClass(TradeDetailActivity.this, BluetoothSelectActivity_.class);
                        } else {
                            intent.setClass(TradeDetailActivity.this, SwipeWaitActivity.class);
                        }
                        intent.putExtra("tradetype", "canceltrade");
                        intent.putExtra("money", queryModel.getTradeMoney());
                        intent.putExtra("queryModel", queryModel);
                        startActivity(intent);
                        TradeDetailActivity.this.finish();
                    } else {
                        if (TextUtils.isEmpty(resultValue)) {
                            ViewUtils.makeToast(TradeDetailActivity.this, "系统异常" + result, 1500);
                        } else {
                            ViewUtils.makeToast(TradeDetailActivity.this, resultValue, 1500);
                        }
//						ViewUtils.makeToast(TradeDetailActivity.this, getString(R.string.login_failure), 1500);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d(TAG, "url==" + url);

    }*/

    public void inputPwdDialog() {
        if (myDialog == null) {
            myDialog = new Dialog(this, R.style.MyInvestDialog);
        }
        view_ = getLayoutInflater().inflate(R.layout.input_password_layout,
                null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        LinearLayout cancel_btn = (LinearLayout) view_
                .findViewById(R.id.cancel_layout);
        LinearLayout confirm_btn = (LinearLayout) view_
                .findViewById(R.id.confirm_layout);
        EditText password = (EditText) view_
                .findViewById(R.id.password);

        cancel_btn.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);

        myDialog.setContentView(view_, lp);
        myDialog.setCanceledOnTouchOutside(true);
        myDialog.show();
        requestFocusAndSoft(password);
    }

    private void requestFocusAndSoft(final EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                // 自动弹出键盘
                InputMethodManager inputManager = (InputMethodManager) editText
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 998);
    }
}

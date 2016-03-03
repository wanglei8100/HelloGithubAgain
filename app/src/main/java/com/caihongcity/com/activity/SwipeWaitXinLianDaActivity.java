package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.EncryptUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.view.CustomDialog;
import com.dspread.xpos.QPOSService;
import com.xino.minipos.pub.ExecutEmvResult;

import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitXinLianDaActivity extends BaseActivity {
    @Extra
    String feeRate;
    @Extra
    String topFeeRate;
    @Extra
    String money;
    @Extra
    QueryModel queryModel;
    @Extra
    String tradetype;
    @Extra
    String blue_address;

    @ViewById
    TextView tv_title_des;


    @ViewById
    ImageView swipe_flash;

    private Dialog dialog;
    private String formatmoney;
    private String ksn;
    private String cardType = ""; //用来判断刷卡的卡片类型
    private String cardTypeValue; //服务器需要根据此参数判定卡类型

    String voucherNo_Value_;
    String batchNo_Value_;
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    static final String WORK_KEY_OK = "WORK_KEY_OK";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    static final String OPEN_OK = "OPEN_OK";
    static final String CONN_OK = "CONN_OK";
    static final String INPUT_PWD_OK = "INPUT_PWD_OK";
    private String phoneNum;
    private String workkey;
    private String terminal;
    private String customer;
    private String feeRateVal;
    private String moneyVal;
    private String sPan; //主账号
    private String sExpData; //卡有效期
    private String sTrack2; //二磁道数据
    private ExecutEmvResult resultStart;
    private String pansn; //卡片序列号
    private String pwd;   // 密码
    private byte[] tlv;   //原始tlv数据
    private String ic55DataStr; //ic55域数据
    private MyPosListener listener;
    private QPOSService pos;
    private TransactionType transactionType;
    private CustomDialog customDialog;
    private CustomDialog.InputDialogListener inputDialogListener;
    private String pinkey;
    private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    private String amount;


    @AfterViews
    void initData() {
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean isAudioConnect = mAudioManager.isWiredHeadsetOn();
        if (!isAudioConnect) {
            showNoDeviceDialog();
            return;
        }
        dialogShow("正在检测设备...");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化
        feeRateVal = CommonUtils.formatTo8Zero(feeRate); //将传入的费率格式化
        //初始化连接
        initConn();
    }

    private void initConn() {
        open(CommunicationMode.AUDIO);
        pos.openAudio();


    }

    private void open(CommunicationMode mode) {
        listener = new MyPosListener();
        pos = QPOSService.getInstance(mode);
        pos.setConext(getApplicationContext());
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);


    }

    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitXinLianDaActivity.this, s, true);
        dialog.show();

    }


    //如果用户没有插入耳机，则显示此对话框
    private void showNoDeviceDialog() {
        ViewUtils.showChoseDialog(SwipeWaitXinLianDaActivity.this, true, "请插入刷卡头后重试", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitXinLianDaActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }

    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (BIND_TERMINAL_OK.equals(paramString)) {

            //开始刷卡
            startSwipe();

        } else if (CONN_OK.equals(paramString)) {
            if (pos != null) {
                pos.getQposId();
            } else {
                toast("设备未连接");
            }

        } else if (SWIPE_OK.equals(paramString)) {
            if ((Constant.CONSUME.equals(this.tradetype)) || (Constant.CANCEL.equals(this.tradetype))) {
                initDialogConsume();
            } else if (Constant.QUERYBALANCE.equals(this.tradetype)) {
                initDialogQuery();//初始化密码框
            }
        } else {
            if (this.dialog != null)
                this.dialog.dismiss();
            if (this.loadingDialog != null)
                this.loadingDialog.dismiss();
            ViewUtils.makeToast(this, paramString, 1000);
            ViewUtils.overridePendingTransitionBack(this);
            return;
        }

    }

    private void toBindTerminal() {
        phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0700");
        requestData.put(1, phoneNum);
        requestData.put(3, "190958");
        requestData.put(59, Constant.VERSION);
        requestData.put(62, ksn);
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        MyAsyncTask localMyAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                //如果返回的json字符串为空，则提示网络连接异常
                if (StringUtil.isEmpty(content)) {
                    toast(getString(R.string.server_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);

                    String bindStatus = (String) obj.get("39");
                    //11域为受卡方系统跟踪号（流水号）
                    if (obj.has("11")) {
                        int voucherNo_Value_int = Integer.valueOf(obj.getString("11")).intValue();
                        //将流水号变为六位数字
                        voucherNo_Value_ = CommonUtils.formatTo6Zero(String.valueOf(voucherNo_Value_int + 1));
                    }
                    //60域为批次号
                    if (obj.has("60"))
                        batchNo_Value_ = obj.getString("60");
                    //获取规定好的绑定响应码
                    String resultValue = MyApplication.getErrorHint(bindStatus);
                    if ((REPONSE_STATUS.equals(bindStatus)) || (REPEAT_BIND.equals(bindStatus))) {
                        //从62域中拿到工作密钥
                        workkey = (String) obj.get("62");
                        //从41域中拿到终端号
                        String terminal = obj.getString("41");
                        LogUtil.syso("workKey get=====" + workkey);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitXinLianDaActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitXinLianDaActivity.this, "workkey", workkey);
                        toast("BIND_TERMINAL_OK");
                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+bindStatus);
                        }else {
                            toast(resultValue);
                        }
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void isLoadingContent() {
                dialogShow("请稍等");
            }
        });
        LogUtil.d("SwipeWaitBluetoothActivity", "url===" + url);
        localMyAsyncTask.execute(url);
    }


    void startSwipe() {
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        pos.doTrade(30);

    }


    //查询界面需要输入的密码框
    private void initDialogQuery() {

        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }

        String maskedPANValue = sPan.replace(sPan.subSequence(6, sPan.length() - 4), "****");
        customDialog = new CustomDialog(SwipeWaitXinLianDaActivity.this, R.style.mystyle, R.layout.customdialog, "余额查询", maskedPANValue);
        customDialog.setCanceledOnTouchOutside(true);
        inputDialogListener = new CustomDialog.InputDialogListener() {
            @Override
            public void onOK(String text) {

                if (TextUtils.isEmpty(text)) {

                    text = "000000";
                    if ("1".equals(cardType) || "01".equals(cardType)) {
                        cardTypeValue = "052";
                    } else {
                        cardTypeValue = "022";
                    }
                }
                initUrlQuery(text);


            }

            @Override
            public void onCancel() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitXinLianDaActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();

    }

    private void initUrlQuery(String pwdVal) {

        String moneyVal = CommonUtils.formatTo12Zero(money);
        String workkey = StorageCustomerInfoUtil.getInfo("workkey",this);

        if ("".equals(workkey)) {
            ViewUtils.makeToast(this, "workkey异常", 1500);
            return;
        }
        if (TextUtils.isEmpty(workkey) || workkey.length() < 38) {
            toast("work异常");
            return;
        } else {
            pinkey = workkey.substring(0, 38);
        }

        byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
        String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);

        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, sPan); //卡号
        requestData.put(3, "310000");
        requestData.put(4, moneyVal);
        requestData.put(9, feeRateVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, sExpData);  //卡有效期
        requestData.put(22, cardTypeValue);
        requestData.put(23, pansn);
        requestData.put(26, "12");
        requestData.put(35, sTrack2.toUpperCase()); //2磁道明文
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pinbyteHex.toUpperCase()); //密码加密
        requestData.put(53, "");
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, "01" + batchNo_Value_ + "003");
        requestData.put(64, Constant.getMacData(requestData));

        query(Constant.getUrl(requestData));
        LogUtil.syso("url的拼接:" + Constant.getUrl(requestData));

    }

    //查询余额
    @Background
    void query(String url) {
        new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String paramString) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + paramString);
                if (StringUtil.isEmpty(paramString)) {
                    toast(getString(R.string.server_error));
                    return;
                }
                try {
                    JSONObject localJSONObject = new JSONObject(paramString);
                    String result = (String) localJSONObject.get("39");
                    String resultValue = (String) MyApplication.getErrorHint(result);
                    LogUtil.syso("返回的错误码为" + resultValue);

                    if ("00".equals(result)) {
                        String cardNo = (String) localJSONObject.get("2");
                        String moneyCon = localJSONObject.getString("54");
                        if (!"".equals(moneyCon)) {
                            int i = moneyCon.length();
                            if (i > 12)
                                moneyCon = moneyCon.substring(i - 12, i);
                        }
                        Intent localIntent = new Intent();
                        localIntent.putExtra("cardNo", cardNo);
                        localIntent.putExtra("money", moneyCon);
                        localIntent.setClass(SwipeWaitXinLianDaActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitXinLianDaActivity.this);
                        return;
                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+result);
                        }else {
                            toast(resultValue);
                        }
                    }
                } catch (JSONException localJSONException) {
                    localJSONException.printStackTrace();
                    return;
                }

            }

            public void isLoadingContent() {
                //loadingDialog.show();
                dialogShow("正在查询余额");
            }
        }).execute(url);
        LogUtil.d("SwipeWaitMoFangActivity", "url==" + url);
        return;

    }


    private void initDialogConsume() {

        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }

        String maskedPANValue = sPan.replace(sPan.subSequence(6, sPan.length() - 4), "****");
        formatmoney = CommonUtils.format(money);
        customDialog = new CustomDialog(SwipeWaitXinLianDaActivity.this, R.style.mystyle, R.layout.customdialog, "￥ " + formatmoney, maskedPANValue);
        customDialog.setCanceledOnTouchOutside(true);
        inputDialogListener = new CustomDialog.InputDialogListener() {

            @Override
            public void onOK(String text) {
                if (TextUtils.isEmpty(text)) {
                    text = "000000";
                    if ("1".equals(cardType) || "01".equals(cardType)) {
                        cardTypeValue = "052";
                    } else {
                        cardTypeValue = "022";
                    }
                }
                initConsumeUrl(text);
            }

            @Override
            public void onCancel() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitXinLianDaActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    //初始化消费的url
    private void initConsumeUrl(String pwdVal) {

        moneyVal = CommonUtils.formatTo12Zero(formatmoney);

        //获取sixtyData
        String sixtydata = null;
        if (Constant.CONSUME.equals(tradetype)) {//消费
            sixtydata = "22" + batchNo_Value_ + "003";
        } else {//撤销
            sixtydata = "23" + batchNo_Value_ + "003";
        }

        String termianlVoucherNo = null;
        String terminalBatchNo = null;
        if (queryModel != null) {
            termianlVoucherNo = queryModel.getTermianlVoucherNo();
            terminalBatchNo = queryModel.getTerminalBatchNo();
        }

        String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        String topFeeRateValue = CommonUtils.formatTo3Zero(topFeeRate);
        //费率是由8位组成,分别代表前两位00预留,中间三位 封顶费率,后三位 基础费率
        if (!"0".equals(topFeeRateValue)) {
            feeRateVal = "00" + topFeeRateValue + CommonUtils.formatTo3Zero(feeRate);
        }

        //根据workKey,得到pinbyteHex
        if ("".equals(workkey)) {
            ViewUtils.makeToast(this, "workkey异常", 1500);
            return;
        }
        String pinkey = workkey.substring(0, 38);
        byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
        String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);

        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, sPan);
        requestData.put(3, "000000");
        requestData.put(4, moneyVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, sExpData);
        requestData.put(22, cardTypeValue);
        requestData.put(23, pansn);
        requestData.put(26, "12");
        requestData.put(35, sTrack2.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pinbyteHex.toUpperCase());
        requestData.put(53, "");
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, sixtydata);
        if (Constant.CONSUME.equals(tradetype)) {//消费
            requestData.put(9, feeRateVal);
        } else {//撤销
            requestData.put(61, terminalBatchNo + termianlVoucherNo);
        }
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        //检查网络状态
        if (CommonUtils.getConnectedType(SwipeWaitXinLianDaActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitXinLianDaActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        trade(url);


    }


    //交易
    private void trade(String url) {
        LogUtil.syso("tradeUrl:" + url);
        //toast(url);
        //检查网络状态
        if (CommonUtils.getConnectedType(this) == -1) {
            toast(getString(R.string.nonetwork));
            return;
        }
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String paramString) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + paramString);
                if (StringUtil.isEmpty(paramString)) {
                    toast(getString(R.string.server_error));
                    return;
                }
                try {
                    JSONObject localJSONObject = new JSONObject(paramString);
                    String result = (String) localJSONObject.get("39");
                    String resultValue = (String) MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {
                        String voucherNo = (String) localJSONObject.get("11");
                        String voucherNo37 = (String) localJSONObject.get("37");
                        Intent localIntent = new Intent();
                        localIntent.setClass(SwipeWaitXinLianDaActivity.this, SignNameActivity.class);
                        localIntent.putExtra("tradeType", tradetype);
                        localIntent.putExtra("queryModel", queryModel);
                        localIntent.putExtra("cardNo", sPan);
                        localIntent.putExtra("voucherNo", voucherNo);
                        localIntent.putExtra("voucherNo37", voucherNo37);
                        localIntent.putExtra("money", formatmoney);
                        localIntent.putExtra("feeRate", feeRate);
                        localIntent.putExtra("topFeeRate", topFeeRate);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitXinLianDaActivity.this);
                        return;
                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+result);
                        }else {
                            toast(resultValue);
                        }
                    }
                } catch (JSONException localJSONException) {
                    localJSONException.printStackTrace();
                    return;
                }
            }

            public void isLoadingContent() {

                dialogShow("请稍后...");
            }
        });
        myAsyncTask.execute(url);


    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pos == null) {
            return;
        }
        pos.closeAudio();
        pos.onDestroy();

    }

    private class MyPosListener implements QPOSServiceListener {

        @Override
        public void onRequestWaitingUser() {
            dialogShow("请插卡或刷卡");

        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            ksn = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            LogUtil.syso("ksn:" + ksn);
            toast(GET_KSN_OK);

        }

        @Override
        public void onQposInfoResult(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
            LogUtil.syso("刷卡回调成功");
            LogUtil.syso("result回调：" + result);
            if (result == DoTradeResult.NONE) {
                toast("刷卡或插卡已超时，请重试");
            } else if (result == DoTradeResult.ICC) {
                pos.doEmvApp(EmvOption.START);
            } else if (result == DoTradeResult.NOT_ICC) {
                toast("不是正确的IC卡");
            } else if (result == DoTradeResult.BAD_SWIPE) {
                toast("刷卡接触不良");

            } else if (result == DoTradeResult.MCR) {
                String formatID = decodeData.get("formatID");
                LogUtil.syso("formatID"+formatID);
                sPan = decodeData.get("maskedPAN");
                sExpData = decodeData.get("expiryDate");
                sTrack2 = decodeData.get("encTrack2");
                LogUtil.syso("encTrack2"+sTrack2);
                toast(SWIPE_OK);
            }

        }

        @Override
        public void onRequestSetAmount() {

            if (Constant.CONSUME.equals(tradetype)) {
                transactionType = TransactionType.GOODS;
                amount = CommonUtils.formatMoneyToFen(money).toString();
            } else {
                transactionType = TransactionType.INQUIRY;
                amount = "";
            }
            String currencyCode = "156";
            pos.setAmount(amount, "0", currencyCode, transactionType);
            LogUtil.syso("amount:"+amount+";"+"currencyCode:"+currencyCode+";"+"transactionType:"+transactionType);

        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> arrayList) {

        }

        @Override
        public void onRequestIsServerConnected() {

            pos.isServerConnected(true);
        }

        @Override
        public void onRequestFinalConfirm() {

        }

        @Override
        public void onRequestOnlineProcess(String tlv) {
            Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
            LogUtil.syso(decodeData.toString());
            sTrack2 = decodeData.get("encTrack2");
            sPan = decodeData.get("maskedPAN");
            pansn = decodeData.get("cardSquNo") == null ? "" : "0" +
                    decodeData.get("cardSquNo");
            sExpData = decodeData.get("iccCardAppexpiryDate").substring(6, 10);
            ic55DataStr = decodeData.get("iccdata");

            LogUtil.syso("二磁道:" + sTrack2 + ";" + "卡号:" + sPan + ";" +
                    "序列号:" + pansn + ";" + "有效期:" + sExpData + ";" + "55域:" + ic55DataStr);
            pos.sendOnlineProcessResult("8A023030");
            toast(SWIPE_OK);

        }

        @Override
        public void onRequestTime() {
            pos.sendTime(terminalTime);
        }

        @Override
        public void onRequestTransactionResult(TransactionResult transactionResult) {

        }

        @Override
        public void onRequestTransactionLog(String s) {

        }

        @Override
        public void onRequestBatchData(String tlv) {



        }

        @Override
        public void onRequestQposConnected() {
            toast(CONN_OK);
        }

        @Override
        public void onRequestQposDisconnected() {

        }

        @Override
        public void onRequestNoQposDetected() {

        }

        @Override
        public void onError(Error errorState) {
            LogUtil.syso("error" + errorState);
             if (errorState == Error.TIMEOUT) {
                toast("装置没有回复");
            } else if (errorState == Error.DEVICE_RESET) {
                toast("装置已重置");
            } else if (errorState == Error.UNKNOWN) {
                toast("未知的错误");
            } else if (errorState == Error.DEVICE_BUSY) {
                toast("装置忙碌");
            } else if (errorState == Error.INPUT_OUT_OF_RANGE) {
                toast("超出范围的输入");
            } else if (errorState == Error.INPUT_INVALID_FORMAT) {
                toast("输入格式无效");
            } else if (errorState == Error.INPUT_INVALID) {
                toast("输入无效");
            } else if (errorState == Error.CRC_ERROR) {
                toast("CRC错误");
            }
        }

        @Override
        public void onRequestDisplay(Display displayMsg) {
            if (displayMsg == Display.PLEASE_WAIT) {
                dialogShow("请稍后");
            }else if (displayMsg == Display.PROCESSING) {
                dialogShow("处理中");
            } else if (displayMsg == Display.MAG_TO_ICC_TRADE) {
                dialogShow("磁条卡请刷卡");
            }

        }

        @Override
        public void onReturnReversalData(String s) {

        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnPowerOnIccResult(boolean b, String s, String s1, int i) {

        }

        @Override
        public void onReturnPowerOffIccResult(boolean b) {

        }

        @Override
        public void onReturnApduResult(boolean b, String s, int i) {

        }

        @Override
        public void onReturnSetSleepTimeResult(boolean b) {

        }

        @Override
        public void onGetCardNoResult(String s) {

        }

        @Override
        public void onRequestSignatureResult(byte[] bytes) {

        }

        @Override
        public void onRequestCalculateMac(String s) {

        }

        @Override
        public void onRequestUpdateWorkKeyResult(UpdateInformationResult updateInformationResult) {

        }

        @Override
        public void onReturnCustomConfigResult(boolean b, String s) {

        }

        @Override
        public void onRequestSetPin() {
            pos.sendPin("123456");

        }

        @Override
        public void onReturnSetMasterKeyResult(boolean b) {

        }

        @Override
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> linkedHashMap) {

        }

        @Override
        public void onBluetoothBonding() {

        }

        @Override
        public void onBluetoothBonded() {

        }

        @Override
        public void onBluetoothBondFailed() {

        }

        @Override
        public void onBluetoothBondTimeout() {

        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onLcdShowCustomDisplay(boolean b) {

        }

        @Override
        public void onUpdatePosFirmwareResult(UpdateInformationResult updateInformationResult) {

        }

        @Override
        public void onReturnDownloadRsaPublicKey(HashMap<String, String> hashMap) {

        }

        @Override
        public void onGetPosComm(int i, String s, String s1) {

        }

        @Override
        public void onUpdateMasterKeyResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onPinKey_TDES_Result(String s) {

        }

        @Override
        public void onEmvICCExceptionData(String s) {

        }

        @Override
        public void onSetParamsResult(boolean b, Hashtable<String, Object> hashtable) {

        }

        @Override
        public void onGetInputAmountResult(boolean b, String s) {

        }

        @Override
        public void onReturnNFCApduResult(boolean b, String s, int i) {

        }

        @Override
        public void onReturnPowerOnNFCResult(boolean b, String s, String s1, int i) {

        }

        @Override
        public void onReturnPowerOffNFCResult(boolean b) {

        }

        @Override
        public void onCbcMacResult(String s) {

        }

        @Override
        public void onReadBusinessCardResult(boolean b, String s) {

        }

        @Override
        public void onWriteBusinessCardResult(boolean b) {

        }

        @Override
        public void onConfirmAmountResult(boolean b) {

        }
    }
}

package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
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
import com.imagpay.Settings;
import com.imagpay.SwipeEvent;
import com.imagpay.SwipeListener;
import com.imagpay.emv.EMVConfigure;
import com.imagpay.emv.EMVHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitZhongCiActivity extends BaseActivity {
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    private static final String TAG = "SwipeWaitBluetoothActivity";
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    String batchNo_Value_;
    HashMap<String, Object> map = null;
    @Extra
    String feeRate;
    @Extra
    String topFeeRate;
    String ksn;

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
    String voucherNo_Value_;
    String cardTypeValue;
    Dialog dialog;
    private EMVHandler _handler;
    private Settings _settings;
    private ServiceReceiver serviceReceiver;

    void getKSN() {
        ksn = _settings.getSN().replace(" ","").trim();
        LogUtil.syso("ksn==" + ksn);
        if (!TextUtils.isEmpty(ksn)) {
            toast("GET_KSN_OK");
            return;
        }
        toast("获取KSN为空");
    }

    @AfterViews
    void initData() {
        this.tv_title_des.setText("检测刷卡器");
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        registerServiceReceiver();
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean isAudioConnect = mAudioManager.isWiredHeadsetOn();
        if (!isAudioConnect) {
            showNoDeviceDialog();
            return;
        }
        map = new HashMap<String, Object>();
        dialog = ViewUtils.createLoadingDialog(this, "正在检测设备...", false);
        dialog.show();
        _handler = new EMVHandler(this);
        _settings = new Settings(_handler);
        _handler.addSwipeListener(new SwipeListener() {
            @Override
            public void onReadData(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onReadData  " + event.getValue().toString());
            }

            @Override
            public void onParseData(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onParseData  " + event.getValue().toString());
            }

            @Override
            public void onDisconnected(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onDisconnected  " + event.getValue().toString());
                toast("设备连接失败");
            }

            @Override
            public void onConnected(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onConnected  " + event.getValue().toString());
                checkDevice();
            }


            @Override
            public void onStarted(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onStarted  " + event.getValue().toString());
            }

            @Override
            public void onStopped(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onStopped  " + event.getValue().toString());
            }

            @Override
            public void onICDetected(SwipeEvent event) {
                LogUtil.d(TAG, "SwipeListener onICDetected  " + event.getValue().toString());
            }
        });
    }

    @Background
    void checkDevice() {
        if (!_handler.isConnected()) {
            toast("设备连接失败");
            return;
        }
        if (_handler.isPowerOn()) {
            toast("设备连接失败");
            return;
        }
        if (_handler.isWritable()) {
            LogUtil.d(TAG, "Device is ready");
            _handler.powerOn();
            getKSN();
        } else {
            LogUtil.d(TAG, "Please wait! testing parameter now");
            if (_handler.test() && _handler.isWritable()) {
                LogUtil.d(TAG, "Device is ready");
                _handler.powerOn();
                getKSN();
            } else {
                LogUtil.d(TAG, "Device is not supported or Please close some audio effects(SRS/DOLBY/BEATS/JAZZ/Classic...) and insert device!");
                toast("手机不支持该设备");
            }
        }
    }

    @Click({R.id.ll_back})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
        LogUtil.e("SwipeWaitBluetoothActivity", "onStop");
        _handler.powerOff();
        unregisterServiceReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _handler.onDestroy();
    }
    @Background
    void statEmvSwiper() {
        int MAGNETIC_CARD_LENGTH = 15 * 3 + 5 * 3 + 7 * 3 + 8 * 3;// 2轨道数据长度(字节+空格)
        while (true) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            String atr = _settings.icReset();
            LogUtil.i(TAG,"atr=="+atr+" length "+atr.length());
            if (atr == null || atr.startsWith("ff")|| atr.startsWith("ff 3f")
                    || atr.startsWith("32 3f") || atr.startsWith("cc"))
                continue;
            else {
                if (atr.length() >= MAGNETIC_CARD_LENGTH) {
                    LogUtil.d(TAG, "----------------磁条卡-----------------");
                    LogUtil.d(TAG, "磁条卡PAN:" + _handler.getMagPan());
                    LogUtil.d(TAG, "Track1:" + _handler.getTrack1Data());
                    LogUtil.d(TAG, "Track2:" + _handler.getTrack2Data());
                    LogUtil.d(TAG, "Track3:" + _handler.getTrack3Data());
                    LogUtil.d(TAG, "有效期:" + _handler.getMagExpDate());
                    map.put("encTracks", _handler.getTrack2Data());
                    map.put("cardNo", _handler.getMagPan());
                    map.put("expiryDate", _handler.getMagExpDate());
                    map.put("cardType", "0");
                } else {
                    LogUtil.d(TAG, "----------------IC卡-----------------");
                    // 1.初始化参数
                    // 金额,分为单位(建议日期,APP同步传入)
                    EMVConfigure configure = new EMVConfigure(Integer.parseInt(CommonUtils.formatMoneyToFen(money).toString()));
                    // 2.执行EMV流程
                    // _handler.setAutoRead(false);默认为true,为false时必须添加EMV监听事件
                    _handler.emvProcess(configure.getEmvConfig());
                    // 3.下电
                    _handler.icOff();
                    LogUtil.d(TAG, "IC卡序列号:" + _handler.getIcSeq());
                    LogUtil.d(TAG, "PAN:" + _handler.getIcPan());
                    LogUtil.d(TAG, "2磁道:" + _handler.getICTrack2Data());
                    LogUtil.d(TAG, "IC卡第55域:" + _handler.getIcField55());
                    LogUtil.d(TAG, "IC卡有效期:" + _handler.getTLVData(0x5F25));//0x5F24 起始日期 0x5F25有效期
                    String expiryDate = _handler.getTLVData(0x5F25);
                    map.put("encTracks", _handler.getICTrack2Data());
                    map.put("cardNo", _handler.getIcPan());
                    map.put("expiryDate",expiryDate!=null?expiryDate.substring(0,4):"");
                    map.put("ic55DataStr", _handler.getIcField55());
                    map.put("cardType", "1");
                    map.put("icSeq", _handler.getIcSeq());

                }
                if (map.get("encTracks")!=null&&map.get("expiryDate")!=null&&map.get("cardNo")!=null){
                    toast(SWIPE_OK);
                }else {
                    statEmvSwiper();
                }
                break;
            }
        }
    }

    public void toBindTerminal() {
        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
        String macData = "0700" + phoneNum + "190958" + Constant.VERSION + ksn + Constant.mainKey;
        LogUtil.d(TAG, "macData==" + macData);
        String url = Constant.REQUEST_API + "0=0700&1=" + phoneNum + "&3=190958&62=" + ksn + "&59=" + Constant.VERSION + "&64=" + CommonUtils.Md5(macData);
        MyAsyncTask localMyAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String content) {
                LogUtil.d("SwipeWaitBluetoothActivity", "content==" + content);
                if (StringUtil.isEmpty(content)) {
                    toast(getString(R.string.server_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);

                    String bindStatus = (String) obj.get("39");
                    if (obj.has("11")) {
                        int voucherNo_Value_int = Integer.valueOf(obj.getString("11")).intValue();
                        voucherNo_Value_ = CommonUtils.formatTo6Zero(String.valueOf(voucherNo_Value_int + 1));
                    }
                    if (obj.has("60"))
                        batchNo_Value_ = obj.getString("60");
                    String resultValue = MyApplication.getErrorHint(bindStatus);
                    if ((REPONSE_STATUS.equals(bindStatus)) || (REPEAT_BIND.equals(bindStatus))) {
                        String workKey = (String) obj.get("62");
                        String terminal = obj.getString("41");
                        LogUtil.syso("workKey get=====" + workKey);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitZhongCiActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitZhongCiActivity.this, "workkey", workKey);
                        toast("BIND_TERMINAL_OK");
                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+bindStatus);
                        }else {
                            toast(resultValue);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void isLoadingContent() {
            }
        });
        LogUtil.d("SwipeWaitBluetoothActivity", "url===" + url);
        localMyAsyncTask.execute(url);
    }

    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (BIND_TERMINAL_OK.equals(paramString)) {
            if (dialog != null) dialog.dismiss();
            statEmvSwiper();
        } else if (SWIPE_OK.equals(paramString)) {
            if (Constant.CONSUME.equals(tradetype) || Constant.CANCEL.equals(tradetype)) {
                initDialog();
            } else if (Constant.QUERYBALANCE.equals(tradetype)) {
                initDialogQuery();
            }
        } else {
            if (this.dialog != null)
                this.dialog.dismiss();
            if (this.loadingDialog != null)
                this.loadingDialog.dismiss();
            ViewUtils.makeToast(this, paramString, 1000);
            ViewUtils.overridePendingTransitionBack(this);
        }
    }

    private CustomDialog customDialog;
    private CustomDialog.InputDialogListener inputDialogListener;
    String formatmoney;
    private void initDialogQuery() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }

        String maskedPAN = (String) map.get("cardNo");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        customDialog = new CustomDialog(this, R.style.mystyle, R.layout.customdialog, "余额查询", maskedPANValue);
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
                ViewUtils.overridePendingTransitionBack(SwipeWaitZhongCiActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    private void initUrlQuery(String pwdVal) {
        String tract1Data = (String) map.get("encTracks");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("cardNo");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSerial = CommonUtils.formatTo3Zero((String)map.get("icSeq"));
        String workkey = StorageCustomerInfoUtil.getInfo("workkey", this);

        LogUtil.syso("workkey====" + workkey);
        if ("".equals(workkey)||workkey.length()<39) {
            ViewUtils.makeToast(this, "workkey异常", 1500);
            return;
        }
        String pinkey = workkey.substring(0, 38);
        byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
        String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);
        String terminal = StorageCustomerInfoUtil.getInfo("terminal", this);

        String customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        String forpinkey = CommonUtils.Md5(pwdVal
                + StorageCustomerInfoUtil.getInfo("pinkey", this));
        String moneyVal = CommonUtils.formatTo12Zero(money);
        String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "310000");
        requestData.put(4, moneyVal);
        requestData.put(9, feeRateVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, expiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23,cardSerial);
        requestData.put(26, "12");
        requestData.put(35, tract1Data.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pinbyteHex.toUpperCase());
        requestData.put(53, randomNumber);
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, "01" + batchNo_Value_ + "003");
        requestData.put(64, Constant.getMacData(requestData));
        query(Constant.getUrl(requestData));
    }

    public void query(String url) {
        LogUtil.syso("需要传入的：" + url);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }

            @Override
            public void isLoadedContent(String content) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + content);//{"0":"0200","35":"49B9DB7CE8D6C9E9E57FA7ED0E44C1B8B4A208F3DAE8C406","2":"6225760009310363","3":"000000","64":"A8D097DBDC969D45A96F092D5C02CAA1","39":"96","42":"220558015061077","11":"","41":"99977411","14":"1507","49":"156","53":"2197D5BC00000008","22":"022","52":"040777000103","26":"12","60":"01null003"}
                if (StringUtil.isEmpty(content)) {
                    toast(getString(R.string.server_error));
                    return;

                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {
                        String cardNo = (String) obj.get("2");
                        String moneyCon = obj.getString("54");
                        if (!"".equals(moneyCon)) {
                            int len = moneyCon.length();
                            if (len > 12) {
                                moneyCon = moneyCon.substring(len - 12, len);
                            }
                        }
                        Intent intent = new Intent();
                        intent.putExtra("cardNo", cardNo);
                        intent.putExtra("money", moneyCon);
                        intent.setClass(SwipeWaitZhongCiActivity.this,
                                QueryBalancceResultActivity.class);
                        startActivity(intent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitZhongCiActivity.this);
                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+result);
                        }else {
                            toast(resultValue);
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
    }

    private void initDialog() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String maskedPAN = (String) map.get("cardNo");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        formatmoney = CommonUtils.format(money);
        customDialog = new CustomDialog(SwipeWaitZhongCiActivity.this, R.style.mystyle, R.layout.customdialog, "￥ " + formatmoney, maskedPANValue);
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
                initUrl(text);
            }

            @Override
            public void onCancel() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitZhongCiActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }


    private void initUrl(String pwdVal) {

        String tract1Data = (String) map.get("encTracks");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("cardNo");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSerial = CommonUtils.formatTo3Zero((String)map.get("icSeq"));
        String workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
        LogUtil.syso("workkey====" + workkey);

        if ("".equals(workkey)) {
            ViewUtils.makeToast(this, "workkey异常", 1500);
            return;
        }
        String pinkey = workkey.substring(0, 38);
        byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pinkey.getBytes());
        String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);
        String terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        String customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        String forpinkey = CommonUtils.Md5(pwdVal + StorageCustomerInfoUtil.getInfo("pinkey", this));
        LogUtil.syso("terminal==" + terminal);
        String moneyVal = CommonUtils.formatTo12Zero(formatmoney);
        String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        String topFeeRateValue = CommonUtils.formatTo3Zero(topFeeRate);
        //费率是由8位组成,分别代表前两位00预留,中间三位 封顶费率,后三位 基础费率
        if (!"0".equals(topFeeRateValue)) {
            feeRateVal = "00" + topFeeRateValue + CommonUtils.formatTo3Zero(feeRate);
        }

        String sixtydata = null;
        if (Constant.CONSUME.equals(tradetype)) {//消费
            sixtydata = "22" + batchNo_Value_ + "003";
        } else {//撤销
            sixtydata = "23" + batchNo_Value_ + "003";
        }
        String forMd5Data = null;
        String termianlVoucherNo = null;
        String terminalBatchNo = null;
        if (queryModel != null) {
            termianlVoucherNo = queryModel.getTermianlVoucherNo();
            terminalBatchNo = queryModel.getTerminalBatchNo();
        }
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "000000");
        requestData.put(4, moneyVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, expiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23, cardSerial);
        requestData.put(26, "12");
        requestData.put(35, tract1Data.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pinbyteHex.toUpperCase());
        requestData.put(53, randomNumber);
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
        if (CommonUtils.getConnectedType(SwipeWaitZhongCiActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitZhongCiActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        trade(url);
    }

    private void trade(String url) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }

            @Override
            public void isLoadedContent(String content) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + content);//{"0":"0210","35":"6225760009310363=150710111399984","2":"6225760009310363","3":"310000","64":"4638454243313245","4":"000000000100","39":"94","42":"220558015061077","11":"000005","41":"99977411","14":"1507","49":"156","52":"10D7B1F75A1ABC8B","22":"022","26":"12","60":"22000001003","-1":null}
                if (StringUtil.isEmpty(content)) {
                    toast(getString(R.string.server_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)) {
                        String maskedPAN = (String) map.get("cardNo");
                        String voucherNo = (String) obj.get("11");
                        String voucherNo37 = (String) obj.get("37");
                        Intent intent = new Intent();
                        intent.setClass(SwipeWaitZhongCiActivity.this, SignNameActivity.class);
                        intent.putExtra("tradeType", tradetype);
                        intent.putExtra("cardNo", maskedPAN);
                        intent.putExtra("voucherNo", voucherNo);
                        intent.putExtra("voucherNo37", voucherNo37);
                        intent.putExtra("money", money);
                        intent.putExtra("feeRate", feeRate);
                        intent.putExtra("topFeeRate", topFeeRate);
                        startActivity(intent);
                        SwipeWaitZhongCiActivity.this.finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitZhongCiActivity.this);

                    } else {
                        if(TextUtils.isEmpty(resultValue)){
                            toast("未知错误，错误码："+result);
                        }else {
                            toast(resultValue);
                        }
                        return;
                    }
                } catch (JSONException e) {
                    toast(getString(R.string.server_error));
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d(TAG, "url==" + url);
    }
    /**
     * 注册广播接收器
     */
    public void registerServiceReceiver() {
        if (this.serviceReceiver == null) {
            this.serviceReceiver = new ServiceReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            registerReceiver(this.serviceReceiver, intentFilter);
        }
    }

    /**
     * 监听耳机插入拔出事件
     *
     * @author toshiba
     */
    private class ServiceReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.HEADSET_PLUG")) {
                int headsetState = intent.getExtras().getInt("state");
                int microphoneState = intent.getExtras().getInt("microphone");
                if (headsetState == 0) {
                    showNoDeviceDialog();
                } else if (headsetState == 1) {
                    if (microphoneState == 1) {
                        CommonUtils.adjustAudioMax(SwipeWaitZhongCiActivity.this);
                    } else {
                    }
                }
            }
        }
    }


    /**
     * 释放广播接收器
     */
    public void unregisterServiceReceiver() {
        if (this.serviceReceiver != null) {
            unregisterReceiver(this.serviceReceiver);
            this.serviceReceiver = null;
        }
    }
    private void showNoDeviceDialog() {
        ViewUtils.showChoseDialog(SwipeWaitZhongCiActivity.this, true, "请插入刷卡头后重试", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitZhongCiActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }
}
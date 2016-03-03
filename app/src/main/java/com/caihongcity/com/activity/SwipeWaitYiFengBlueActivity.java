package com.caihongcity.com.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.model.SimpleSwiperListener;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.yfcomm.mpos.api.SwiperController;
import com.yfcomm.mpos.model.CardModel;
import com.yfcomm.mpos.model.TrxType;
import com.yfcomm.mpos.model.syn.WorkKey;
import com.yfcomm.mpos.utils.ByteUtils;

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
public class SwipeWaitYiFengBlueActivity extends BaseActivity {
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
    private SwiperController swiper;
    private String ksn;
    private final String TAG = "SwipeWaitYiFengBlueActivity";
    private String cardType = ""; //用来判断刷卡的卡片类型
    private String cardTypeValue; //服务器需要根据此参数判定卡类型
    private String mainKey = "950973182317F80B950973182317F80B00962B60AA556E65";


    String voucherNo_Value_;
    String batchNo_Value_;
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    private String phoneNum;
    private String workkey;
    private String terminal;
    private String customer;
    private Long amount;
    private String feeRateVal;
    private String moneyVal;


    @AfterViews
    void initData() {
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        dialogShow("正在检测设备...");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化
        //初始化刷卡器类
        swiper = new SwiperController(this, swiperListener);

        //注册连接断开通知
        this.registerReceiver(
                disconnectReceiver, new IntentFilter("yifeng.mpos.connect.close"));

        openBlue();
        initConn();

    }

    private void openBlue() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt != null) {
            bt.enable();
        }
    }

    void initConn() {
        if (blue_address != null) {
            swiper.connectBluetoothDevice(1000, blue_address);
        } else {
            Intent intent = new Intent(this, BluetoothSelectActivity_.class);
            startActivity(intent);
        }

    }


    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitYiFengBlueActivity.this, s, true);
        dialog.show();

    }


    private String maskedPAN;
    private String tract2Data;
    private String expiryDate;
    private String ic55DataStr;
    private String pansn;
    private String pwdVal;
    private SimpleSwiperListener swiperListener = new SimpleSwiperListener() {

        @Override
        public void onError(int code, String messsage) {
            StorageAppInfoUtil.putInfo(SwipeWaitYiFengBlueActivity.this, "bluetooth_address", "");
            toast(messsage);
            return;
        }


        @Override
        public void onDownloadProgress(long current, long total) {

        }

        @Override
        public void onResultSuccess(int nType) {

            if (nType == 0x30) {
                //连接刷卡器成功
                dialogShow("连接设备成功");
                getKsn();
                StorageAppInfoUtil.putInfo(SwipeWaitYiFengBlueActivity.this, "bluetooth_address", blue_address);
            } else if (nType == 0x39) {
                //固件更新成功
            } else if (nType == 0x35) {
                //toast("更新工作密钥成功");
            } else if (nType == 0x34) {
                //toast("写入主密钥成功");
            }
        }

        @Override
        public void onSwiperSuccess(CardModel cardModel) {

            //数据加密成功返回
            StringBuilder sb = new StringBuilder();
            sb.append("pan:").append(cardModel.getPan()).append("\n");
            sb.append("expireDate:").append(cardModel.getExpireDate()).append("\n");
            maskedPAN = cardModel.getPan();
            tract2Data = cardModel.getTrack2().substring(0, 37);
            expiryDate = cardModel.getExpireDate();
            ic55DataStr = cardModel.getIcData();
            pwdVal = cardModel.getPinBlock();
            pansn = CommonUtils.formatTo3Zero(cardModel.getIcSeq());
            sb.append("batchNo:").append(cardModel.getBatchNo()).append("\n");
            sb.append("serialNo:").append(cardModel.getSerialNo()).append("\n");
            sb.append("track2:").append(cardModel.getTrack2()).append("\n");
            sb.append("track3:").append(cardModel.getTrack3()).append("\n");
            sb.append("encryTrack2:").append(cardModel.getEncryTrack2()).append("\n");
            sb.append("encryTrack3:").append(cardModel.getEncryTrack3()).append("\n");

            sb.append("icData:").append(cardModel.getIcData()).append("\n");

            sb.append("mac:").append(cardModel.getMac()).append("\n");
            sb.append("pinBlock:").append(cardModel.getPinBlock()).append("\n");

            sb.append("icseq:").append(cardModel.getIcSeq()).append("\n");
            sb.append("random:").append(cardModel.getRandom()).append("\n");
            // dialogShow(sb.toString());
            toast(SWIPE_OK);
        }

        @Override
        public void onGetDeviceInfo(String customerNo, String termNo, String batchNo, boolean existsMainKey, String sn, String version) {

            ksn = sn.trim();
            LogUtil.syso("ksn==" + ksn);
            toast(GET_KSN_OK);
        }

        public void onDetectIc() {

            dialogShow("检测到IC卡");
            cardType = "01";
        }

        @Override
        public void onTimeout() {

            toast("刷卡超时,请重试");
        }


        public void onInputPin() {
            dialogShow("请输入密码...");
        }

        @Override
        public void onTradeCancel() {
            toast("取消刷卡");
        }


    };

    private void getKsn() {

        swiper.getDeviceInfo();
    }


    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (BIND_TERMINAL_OK.equals(paramString)) {
            dialogShow("请刷卡或插卡");
            //开始刷卡
//            writeMainKey();
            statSwiper();

        } else if (SWIPE_OK.equals(paramString)) {
            if ((Constant.CONSUME.equals(this.tradetype)) || (Constant.CANCEL.equals(this.tradetype))) {
                initConsumeUrl();
            } else if (Constant.QUERYBALANCE.equals(this.tradetype)) {
                initQueryUrl();
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

    private void initConsumeUrl() {
        moneyVal = CommonUtils.formatTo12Zero(money);

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

        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "000000");
        requestData.put(4, moneyVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, expiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23, pansn);
        requestData.put(26, "12");
        requestData.put(35, tract2Data.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pwdVal);
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
        if (CommonUtils.getConnectedType(SwipeWaitYiFengBlueActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitYiFengBlueActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        trade(url);

    }

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
                        localIntent.setClass(SwipeWaitYiFengBlueActivity.this, SignNameActivity.class);
                        localIntent.putExtra("tradeType", tradetype);
                        localIntent.putExtra("queryModel", queryModel);
                        localIntent.putExtra("cardNo", maskedPAN);
                        localIntent.putExtra("voucherNo", voucherNo);
                        localIntent.putExtra("voucherNo37", voucherNo37);
                        localIntent.putExtra("money", formatmoney);
                        localIntent.putExtra("feeRate", feeRate);
                        localIntent.putExtra("topFeeRate", topFeeRate);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitYiFengBlueActivity.this);
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

    private void writeMainKey() {
        swiper.writeMainKey(ByteUtils.hexToByte(mainKey));
    }

    private void initQueryUrl() {
        if ("01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
//        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
        String moneyVal = CommonUtils.formatTo12Zero(money);
        LogUtil.syso("workkey====" + workkey);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN); //卡号
        requestData.put(3, "310000");
        requestData.put(4, moneyVal);
        requestData.put(9, feeRateVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, expiryDate);  //卡有效期
        requestData.put(22, cardTypeValue);
        requestData.put(23, pansn);
        requestData.put(26, "12");
        requestData.put(35, tract2Data.toUpperCase()); //2磁道密文
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pwdVal); //密码加密
        requestData.put(53, "");
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, "01" + batchNo_Value_ + "003");
        requestData.put(64, Constant.getMacData(requestData));

        query(Constant.getUrl(requestData));
        LogUtil.syso("url的拼接:" + Constant.getUrl(requestData));

    }

    @Background
    void query(String url) {
        new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String paramString) {
                loadingDialog.dismiss();
                LogUtil.syso("queryContent==" + paramString);
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
                        localIntent.setClass(SwipeWaitYiFengBlueActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitYiFengBlueActivity.this);
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


    void writeWorkKey() {
        WorkKey wk = new WorkKey();
        workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
        LogUtil.syso("work==" + workkey);
        if (workkey != null && workkey.length() == 120) {
            String PINkey = workkey.substring(0, 32);
            String PINKeyChecked = workkey.substring(32, 40);
            LogUtil.syso("pinChecked==" + ByteUtils.hexToByte(PINKeyChecked));
            String MACkey = workkey.substring(40, 72);
            String MACkeyChecked = workkey.substring(72, 80);
            String DESkey = workkey.substring(80, 112);
            String DESkeyChecked = workkey.substring(112, 120);
            wk.setPik(ByteUtils.hexToByte(PINkey));
            wk.setPikCheckValue(ByteUtils.hexToByte(PINKeyChecked));
            wk.setMak(ByteUtils.hexToByte(MACkey));
            wk.setMakCheckValue(ByteUtils.hexToByte(MACkeyChecked));
            wk.setTdk(ByteUtils.hexToByte(DESkey));
            wk.setTdkCheckValue(ByteUtils.hexToByte(DESkeyChecked));
            swiper.writeWorkKey(wk);
        }

    }

    @Background
    void statSwiper() {
        writeWorkKey();
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        amount = CommonUtils.formatMoneyToFen(formatmoney);
        if (Constant.QUERYBALANCE.equals(this.tradetype)) {
            swiper.startSwiper(120, amount, 0, TrxType.QUERY);
        } else {
            swiper.startSwiper(120, amount, 0, TrxType.PURCHASE);
            LogUtil.syso("传入的金额" + amount.toString());
        }


    }

    @Background
    void toBindTerminal() {
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
                        String workKey = (String) obj.get("62");
                        //从41域中拿到终端号
                        String terminal = obj.getString("41");
                        LogUtil.syso("workKey get=====" + workKey);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitYiFengBlueActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitYiFengBlueActivity.this, "workkey", workKey);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        swiper.getMpos().close();
        this.unregisterReceiver(disconnectReceiver);
    }

    /**
     * 处理关闭通知
     */
    private BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("yifeng.mpos.connect.close")) {
                toast("设备断开连接，请重试");
            }
        }

    };


    //点击返回按钮所做的操作
    @Click(R.id.ll_back)
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
            default:
                break;
        }

    }

}

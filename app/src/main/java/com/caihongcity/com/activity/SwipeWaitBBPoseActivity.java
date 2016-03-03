package com.caihongcity.com.activity;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbpos.emvswipe.CAPK;
import com.bbpos.emvswipe.EmvSwipeController;
import com.bbpos.emvswipe.EmvSwipeController.EmvSwipeControllerListener;
import com.bbpos.emvswipe.EmvSwipeController.TransactionType;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.EncryptUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.view.CustomDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


@EActivity(R.layout.swipewait_layout)
public class SwipeWaitBBPoseActivity extends BaseActivity implements View.OnClickListener{
    @ViewById
    ImageView swipe_flash;


    @ViewById
    TextView tv_title_des;

    @Extra
    String tradetype;

    String  feeRate, money, topFeeRate;

    String voucherNo_Value_;
    String batchNo_Value_;
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    static final String CONN_OK = "CONN_OK";

    private String sPan;
    private String expiryDate; //卡有效期
    private String terminal;   //终端号
    private String customer;   //用户号码
    private String tract2Data; //2磁道密文
    private String randomNumber; //随机数
    private String workkey;    //工作密钥
    private String feeRateVal; //费率
    private CustomDialog customDialog; //自定义的密码输入框
    private CustomDialog.InputDialogListener inputDialogListener; //自定义的密码输入框监听
    private String phoneNum;  //用户手机号
    private String ic55DataStr; //ic卡55域的数据
    private String formatmoney;
    private String moneyVal;
    protected static EmvSwipeController emvSwipeController;
    private Dialog dialog;
    private String ksn;
    private ProgressDialog progressDialog;
    private EmvSwipeController.CheckCardMode checkCardMode;
    private String cardType;
    private String cardTypeValue;
    private String pansn;
    private TransactionType transactionType;


    @AfterViews
    void initData() {
        initEmvSwipeController();
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean isAudioConnect = mAudioManager.isWiredHeadsetOn();
        if (!isAudioConnect) {
            showNoDeviceDialog();
            return;
        }
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("检测刷卡器");
        feeRate = getIntent().getStringExtra("feeRate");
        topFeeRate = getIntent().getStringExtra("topFeeRate");
        money = getIntent().getStringExtra("money");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化
        getKsn();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(emvSwipeController != null) {
            emvSwipeController.stopAudio();
            emvSwipeController.resetEmvSwipeController();
            emvSwipeController = null;
        }
    }

    void getKsn() {
//        autoConfig();

//         try {
//             Thread.sleep(1000);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }
        emvSwipeController.startAudio();
        emvSwipeController.setDetectDeviceChange(true);
        emvSwipeController.getKsn();


    }


     void initEmvSwipeController() {

        if(emvSwipeController == null) {
            emvSwipeController  = EmvSwipeController.getInstance(this,new MyEmvSwipeControllerListener());
        }


    }

    private void autoConfig() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("自动配置");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                emvSwipeController.cancelAutoConfig();
            }
        });
        progressDialog.show();
        emvSwipeController.startAutoConfig();
    }


    //如果用户没有插入耳机，则显示此对话框
    private void showNoDeviceDialog() {
        ViewUtils.showChoseDialog(SwipeWaitBBPoseActivity.this, true, "请插入刷卡头后重试", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitBBPoseActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

     class MyEmvSwipeControllerListener implements EmvSwipeControllerListener {


         @Override
        public void onWaitingForCard(com.bbpos.emvswipe.EmvSwipeController.CheckCardMode checkCardMode) {

        }

        @Override
        public void onReturnCheckCardResult(EmvSwipeController.CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {

            if(checkCardResult == EmvSwipeController.CheckCardResult.NONE) {
                toast("刷卡或插卡已超时");
            } else if(checkCardResult == EmvSwipeController.CheckCardResult.ICC) {
                String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                data.put("terminalTime", terminalTime);
                data.put("checkCardTimeout", "120");
                data.put("setAmountTimeout", "120");
                data.put("selectApplicationTimeout", "120");
                data.put("finalConfirmTimeout", "120");
                data.put("onlineProcessTimeout", "120");
                data.put("pinEntryTimeout", "120");
                data.put("emvOption", "START");
                data.put("checkCardMode", checkCardMode);
                data.put("encOnlineMessageTags", new String[] {"9F09"});
                data.put("encBatchDataTags", new String[] {"9F09"});
                data.put("encReversalDataTags", new String[] {"9F09"});
                data.put("randomNumber", "0123456789ABCDEF");
                emvSwipeController.startEmv(data);
                LogUtil.d("swp","startemv");

            } else if(checkCardResult == EmvSwipeController.CheckCardResult.BAD_SWIPE) {
                toast("刷卡接触不良，请重试");
            } else if(checkCardResult == EmvSwipeController.CheckCardResult.MCR) {
                dialogShow("读取磁条卡成功");
                sPan = decodeData.get("PAN");
                expiryDate = decodeData.get("expiryDate");
                String ksn = decodeData.get("ksn");
                tract2Data = decodeData.get("encTrack2");
                cardType = "2";
//                dialogShow(sPan+";"+expiryDate+";"+tract2Data);
                toast(SWIPE_OK);


            } else if(checkCardResult == EmvSwipeController.CheckCardResult.NO_RESPONSE) {
                toast("刷卡或插卡不正常,请重试");
            }

        }

        @Override
        public void onOnlineProcessDataDetected() {

        }

        @Override
        public void onBatchDataDetected() {

        }

        @Override
        public void onReversalDataDetected() {

        }

        @Override
        public void onReturnCancelCheckCardResult(boolean b) {

        }

        @Override
        public void onReturnEncryptPinResult(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnEncryptDataResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnStartEmvResult(EmvSwipeController.StartEmvResult startEmvResult, String s) {

        }

        @Override
        public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {

        }

        @Override
        public void onReturnCAPKList(List<CAPK> list) {

        }

        @Override
        public void onReturnCAPKDetail(CAPK capk) {

        }

        @Override
        public void onReturnCAPKLocation(String s) {

        }

        @Override
        public void onReturnUpdateCAPKResult(boolean b) {

        }

        @Override
        public void onReturnEmvReportList(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnEmvReport(String s) {

        }

        @Override
        public void onReturnTransactionResult(EmvSwipeController.TransactionResult transactionResult) {

        }

        @Override
        public void onReturnTransactionResult(EmvSwipeController.TransactionResult transactionResult, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnBatchData(String s) {

        }

        @Override
        public void onReturnTransactionLog(String s) {

        }

        @Override
        public void onReturnReversalData(String s) {

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
        public void onReturnApduResultWithPkcs7Padding(boolean b, String s) {

        }

        @Override
        public void onReturnViposExchangeApduResult(String s) {

        }

        @Override
        public void onReturnViposBatchExchangeApduResult(Hashtable<Integer, String> hashtable) {

        }

        @Override
        public void onReturnEmvCardBalance(boolean b, String s) {

        }

        @Override
        public void onReturnEmvCardDataResult(boolean b, String s) {

        }

        @Override
        public void onReturnEmvCardNumber(String s) {

        }

        @Override
        public void onReturnEmvLoadLog(String[] strings) {

        }

        @Override
        public void onReturnEmvTransactionLog(String[] strings) {

        }

        @Override
        public void onReturnPowerOnNfcResult(boolean b, String s, int i) {

        }

        @Override
        public void onReturnPowerOffNfcResult(boolean b) {

        }

        @Override
        public void onReturnNfcDataResult(EmvSwipeController.NfcDataExchangeStatus nfcDataExchangeStatus, String s, int i) {

        }

        @Override
        public void onReturnKsn(Hashtable<String, String> ksnTable) {
                 ksn = ksnTable.get("pinKsn");
                 LogUtil.syso("ksn"+ksn);
                toast(GET_KSN_OK);


        }

        @Override
        public void onReturnUpdateTerminalSettingResult(EmvSwipeController.TerminalSettingStatus terminalSettingStatus) {

        }

        @Override
        public void onReturnReadTerminalSettingResult(EmvSwipeController.TerminalSettingStatus terminalSettingStatus, String s) {

        }

        @Override
        public void onRequestSelectApplication(ArrayList<String> arrayList) {

        }

        @Override
        public void onRequestSetAmount() {
            promptForAmount();

        }

         private void promptForAmount() {
             String amount = formatmoney;
             if (Constant.CONSUME.equals(tradetype)) {
                 transactionType = TransactionType.GOODS;
             } else {
                 transactionType = TransactionType.INQUIRY;
             }
             String currencyCode = "156";
             if (emvSwipeController.setAmount(amount, "0", currencyCode, transactionType)) {
                 if ("".equals(amount)) {
                     dialogShow("请稍后...");
                 } else {
                     dialogShow("请确认金额");
                 }
             } else {
                 toast("读取IC卡失败，请重试  ");
             }


         }

         @Override
        public void onRequestPinEntry() {

        }

        @Override
        public void onRequestVerifyID(String s) {

        }

        @Override
        public void onRequestCheckServerConnectivity() {
            emvSwipeController.sendServerConnectivity(true);
            dialogShow("发送服务确认");

        }

        @Override
        public void onRequestOnlineProcess(String tlv) {
            Hashtable<String, String> decodeData = EmvSwipeController.decodeTlv(tlv);
            tract2Data = decodeData.get("encTrack2Eq");
            sPan = decodeData.get("maskedPAN");
            pansn = decodeData.get("5F34") == null ? "" : "0" +
                    decodeData.get("5F34");
            expiryDate = decodeData.get("5F24").substring(0, 4);
            ic55DataStr = decodeData.get("encOnlineMessage");
            dialogShow("二磁道:" + tract2Data + ";" + "卡号:" + sPan + ";" +
                    "序列号:" + pansn + ";" + "有效期:" + expiryDate + ";" + "55域:" + ic55DataStr);
            emvSwipeController.sendOnlineProcessResult("8A023030");
        }

        @Override
        public void onRequestTerminalTime() {
            dialogShow("下一步执行");

        }

        @Override
        public void onRequestDisplayText(EmvSwipeController.DisplayText displayText) {

        }

        @Override
        public void onRequestClearDisplay() {

        }

        @Override
        public void onRequestReferProcess(String s) {

        }

        @Override
        public void onRequestAdviceProcess(String s) {

        }

        @Override
        public void onRequestFinalConfirm() {
            emvSwipeController.sendFinalConfirmResult(true);
            dialogShow("最终确认");
            LogUtil.syso("最终确认");
        }

        @Override
        public void onAutoConfigProgressUpdate(double percentage) {
            if(progressDialog != null) {
                progressDialog.setProgress((int)percentage);
            }

        }

        @Override
        public void onAutoConfigCompleted(boolean isDefaultSettings, String autoConfigSettings) {
            String outputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.emvswipe.ui/";
            String filename = "settings.txt";

            if(isDefaultSettings) {
                new File(outputDirectory + filename).delete();
            } else {

                try {
                    File directory = new File(outputDirectory);
                    if(!directory.isDirectory()) {
                        directory.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(outputDirectory + filename, false);
                    fos.write(autoConfigSettings.getBytes());
                    fos.flush();
                    fos.close();

                } catch(Exception e) {
                }
            }

        }

        @Override
        public void onAutoConfigError(EmvSwipeController.AutoConfigError autoConfigError) {
            if(autoConfigError == EmvSwipeController.AutoConfigError.PHONE_NOT_SUPPORTED) {
                toast("自动配置失败：手机型号不支持");
            } else if(autoConfigError == EmvSwipeController.AutoConfigError.INTERRUPTED) {
                toast("自动配置失败：自动配置中断");
            }

        }

        @Override
        public void onBatteryLow(EmvSwipeController.BatteryStatus batteryStatus) {

        }

        @Override
        public void onNoDeviceDetected() {

        }

        @Override
        public void onDevicePlugged() {
//            dialogShow("检测到刷卡头");



        }

        @Override
        public void onDeviceUnplugged() {
            toast("刷卡头已拔出");

        }

        @Override
        public void onDeviceHere(boolean b) {

        }

        @Override
        public void onError(EmvSwipeController.Error errorState, String s) {
             if(errorState == EmvSwipeController.Error.TIMEOUT){


             }

        }

        @Override
        public void onPowerDown() {

        }


     }

    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (BIND_TERMINAL_OK.equals(paramString)) {
            dialogShow("请刷卡或插卡");
            //开始刷卡
//            writeMainKey();
            startSwipe();

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

    private void initDialogQuery() {
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }

        String maskedPANValue = sPan.replace(sPan.subSequence(6, sPan.length() - 4), "****");
        customDialog = new CustomDialog(SwipeWaitBBPoseActivity.this, R.style.mystyle, R.layout.customdialog, "余额查询", maskedPANValue);
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
                ViewUtils.overridePendingTransitionBack(SwipeWaitBBPoseActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    private void initUrlQuery(String pwdVal) {
        String moneyVal = CommonUtils.formatTo12Zero(money);
        byte[] pinbyte = EncryptUtils.xor(pwdVal.getBytes(), pwdVal.getBytes());
        String pinbyteHex = CommonUtils.bytes2Hex(pinbyte);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, sPan); //卡号
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
        requestData.put(52, pinbyteHex.toUpperCase()); //密码加密
        requestData.put(53, "");
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, "01" + batchNo_Value_ + "003");
        requestData.put(64, Constant.getMacData(requestData));

        query(Constant.getUrl(requestData));
        LogUtil.syso("url的拼接:" + Constant.getUrl(requestData));

    }

    private void query(String url) {
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
                        localIntent.setClass(SwipeWaitBBPoseActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitBBPoseActivity.this);
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
                dialogShow("正在查询余额");
            }
        }).execute(url);
        LogUtil.d("SwipeWaitBBPoseActivity", "url==" + url);
        return;
    }

    private void initDialogConsume() {

    }

    @Background
     void startSwipe() {
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        feeRateVal = CommonUtils.formatTo8Zero(feeRate);

        checkCardMode = EmvSwipeController.CheckCardMode.SWIPE_OR_INSERT;

        Hashtable<String, Object> data = new Hashtable<String, Object>();
        data.put("checkCardTimeout", "120");
        data.put("checkCardMode", checkCardMode);
        data.put("randomNumber", "0123456789ABCDEF");
        emvSwipeController.checkCard(data);
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
                        String workKey = (String) obj.get("62");
                        //从41域中拿到终端号
                        String terminal = obj.getString("41");
                        LogUtil.syso("workKey get=====" + workKey);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBBPoseActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBBPoseActivity.this, "workkey", workKey);
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


    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitBBPoseActivity.this, s, true);
        dialog.show();

    }





}

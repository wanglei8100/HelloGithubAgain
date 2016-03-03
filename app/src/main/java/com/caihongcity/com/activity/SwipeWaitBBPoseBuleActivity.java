package com.caihongcity.com.activity;


import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bbpos.wisepad.WisePadController;
import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.xino.minipos.pub.ExecutEmvResult;
import com.yfcomm.mpos.api.SwiperController;
import com.yfcomm.mpos.utils.ByteUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitBBPoseBuleActivity extends BaseActivity {


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
    private String cardType = ""; //用来判断刷卡的卡片类型
    private String cardTypeValue; //服务器需要根据此参数判定卡类型


    String voucherNo_Value_;
    String batchNo_Value_;
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    static final String CONN_OK = "CONN_OK";
    private String phoneNum;
    private String workkey;
    private String terminal;
    private String customer;
    private String feeRateVal;
    private String moneyVal;
    private String sPan; //主账号
    private String sExpData; //卡有效期
    private String sTrack2; //二磁道数据
    private Long amount;
    private ExecutEmvResult resultStart;
    private String pansn; //卡片序列号
    private String pwd;   // 密码
    private byte[] tlv;   //原始tlv数据
    private String ic55DataStr; //ic55域数据

    private WisePadController wisePadController;
    private MyWisePadControllerListener listener;
    private ArrayAdapter<String> arrayAdapter;
    protected List<BluetoothDevice> foundDevices;
    private boolean isPinCanceled = false;
    protected static final String[] DEVICE_NAMES = new String[] { "WisePad", "WP", "MPOS", "M36", "M188" };



    @AfterViews
    void initData() {
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
//        dialogShow("正在检测设备...");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化

        initController();
        initConn();

    }


    void initController() {
        listener = new MyWisePadControllerListener();
        wisePadController = WisePadController.getInstance(this, listener);
    }


    void initConn() {
        dismissDialog();

        Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
        final BluetoothDevice[] pairedDevices = new BluetoothDevice[pairedObjects.length];
        for (int i = 0; i < pairedObjects.length; ++i) {
            pairedDevices[i] = (BluetoothDevice) pairedObjects[i];
        }

        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(SwipeWaitBBPoseBuleActivity.this, android.R.layout.simple_list_item_1);
        for (int i = 0; i < pairedDevices.length; ++i) {
            mArrayAdapter.add(pairedDevices[i].getName());
        }

        dismissDialog();

        dialog = new Dialog(SwipeWaitBBPoseBuleActivity.this);
        dialog.setContentView(R.layout.bluetooth_2_device_list_dialog);
        dialog.setTitle("蓝牙装置");

        ListView listView1 = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        listView1.setAdapter(mArrayAdapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice pairedDevice = pairedDevices[position];
                wisePadController.startBTv2(pairedDevice);
                dismissDialog();
            }

        });

        arrayAdapter = new ArrayAdapter<String>(SwipeWaitBBPoseBuleActivity.this, android.R.layout.simple_list_item_1);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView2.setAdapter(arrayAdapter);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice bluetoothDevice = foundDevices.get(position);
                wisePadController.startBTv2(bluetoothDevice);
                dismissDialog();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                wisePadController.stopScanBTv2();
                dismissDialog();
            }
        });


        dialog.setCancelable(false);
        if(MyApplication.bluetoothDevice== null) {
            dialog.show();
            wisePadController.scanBTv2(DEVICE_NAMES, 120);
        }else{
            dismissDialog();
            wisePadController.startBTv2(MyApplication.bluetoothDevice);
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
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBBPoseBuleActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBBPoseBuleActivity.this, "workkey", workKey);
                        toast("BIND_TERMINAL_OK");
                    } else {
                        toast(resultValue);
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


    @Background
    void statSwiper() {
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        amount = CommonUtils.formatMoneyToFen(formatmoney);
        writeWorkKey();
    }

    private void writeWorkKey() {
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
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            data.put("encPinKey", PINkey + PINKeyChecked);
            data.put("encDataKey", DESkey+DESkeyChecked);
            data.put("encMacKey", MACkey+MACkeyChecked);
            wisePadController.checkCard(data);
        }


    }


    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitBBPoseBuleActivity.this, s, true);
        dialog.show();

    }

    class MyWisePadControllerListener implements WisePadController.WisePadControllerListener {


        @Override
        public void onWaitingForCard(WisePadController.CheckCardMode checkCardMode) {

        }

        @Override
        public void onWaitingReprintOrPrintNext() {

        }

        @Override
        public void onBTv2Detected() {

        }

        @Override
        public void onBTv2DeviceListRefresh(List<BluetoothDevice> foundDevices) {
            SwipeWaitBBPoseBuleActivity.this.foundDevices = foundDevices;
            if(arrayAdapter != null) {
                arrayAdapter.clear();
                for(int i = 0; i < foundDevices.size(); ++i) {
                    arrayAdapter.add(foundDevices.get(i).getName());
                }
                arrayAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onBTv2Connected(BluetoothDevice bluetoothDevice) {
            dialogShow("设备连接成功");
            MyApplication.bluetoothDevice = bluetoothDevice;
            toast("CONN_OK");

        }

        @Override
        public void onBTv2Disconnected() {

        }

        @Override
        public void onBTv2ScanStopped() {

        }

        @Override
        public void onBTv2ScanTimeout() {

        }

        @Override
        public void onBTv4DeviceListRefresh(List<BluetoothDevice> list) {

        }

        @Override
        public void onBTv4Connected() {

        }

        @Override
        public void onBTv4Disconnected() {

        }

        @Override
        public void onBTv4ScanStopped() {

        }

        @Override
        public void onBTv4ScanTimeout() {

        }

        @Override
        public void onReturnCheckCardResult(WisePadController.CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {

            if (checkCardResult == WisePadController.CheckCardResult.NONE) {
                toast("无卡插入");
            } else if (checkCardResult == WisePadController.CheckCardResult.BAD_SWIPE) {
                toast("刷卡接触不良，请重试");
            } else if (checkCardResult == WisePadController.CheckCardResult.ICC) {
                dialogShow("读取IC卡成功");
                cardType = "1";
                if (workkey != null && workkey.length() == 120) {
                    String PINkey = workkey.substring(0, 32);
                    String PINKeyChecked = workkey.substring(32, 40);
                    LogUtil.syso("pinChecked==" + ByteUtils.hexToByte(PINKeyChecked));
                    String MACkey = workkey.substring(40, 72);
                    String MACkeyChecked = workkey.substring(72, 80);
                    String DESkey = workkey.substring(80, 112);
                    String DESkeyChecked = workkey.substring(112, 120);
                    Hashtable<String, Object> data = new Hashtable<String, Object>();
                    data.put("emvOption", WisePadController.EmvOption.START);
                    data.put("checkCardMode", WisePadController.CheckCardMode.SWIPE_OR_INSERT);
                    data.put("encPinKey", PINkey + PINKeyChecked);
                    data.put("encDataKey", DESkey + DESkeyChecked);
                    data.put("encMacKey", MACkey + MACkeyChecked);
                    wisePadController.startEmv(data);
                } else {
                    toast("workKey异常");
                }
            } else if (checkCardResult == WisePadController.CheckCardResult.MCR) {
                dialogShow("读取磁条卡成功");
                String PAN = decodeData.get("PAN");
                sPan = PAN;
                LogUtil.syso("sPan" + PAN);
                String expiryDate = decodeData.get("expiryDate");
                sExpData = expiryDate;
                cardType = "2";
                String encTrack2 = decodeData.get("encTrack2");
                sTrack2 = encTrack2;
                LogUtil.syso("encTrack2" + encTrack2);

                Hashtable<String, Object> data = new Hashtable<String, Object>();
                data.put("pinEntryTimeout", 120);
                wisePadController.startPinEntry(data);

            }
        }

        @Override
        public void onReturnCancelCheckCardResult(boolean b) {

        }

        @Override
        public void onReturnStartEmvResult(WisePadController.StartEmvResult startEmvResult, String s) {

            if (startEmvResult == WisePadController.StartEmvResult.SUCCESS) {
                toast("启动成功");
            } else {
                toast("启动失败");
            }
        }

        @Override
        public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {
            String pinKsn = deviceInfoData.get("pinKsn");
            ksn = pinKsn;
            LogUtil.syso("ksn==" + ksn);

//            String isSupportedTrack1 = deviceInfoData.get("isSupportedTrack1");
//            String isSupportedTrack2 = deviceInfoData.get("isSupportedTrack2");
//            String isSupportedTrack3 = deviceInfoData.get("isSupportedTrack3");
//            String bootloaderVersion = deviceInfoData.get("bootloaderVersion");
//            String firmwareVersion = deviceInfoData.get("firmwareVersion");
//            String mainProcessorVersion = deviceInfoData.get("mainProcessorVersion");
//            String coProcessorVersion = deviceInfoData.get("coprocessorVersion");
//            String isUsbConnected = deviceInfoData.get("isUsbConnected");
//            String isCharging = deviceInfoData.get("isCharging");
//            String batteryLevel = deviceInfoData.get("batteryLevel");
//            String batteryPercentage = deviceInfoData.get("batteryPercentage");
//            String hardwareVersion = deviceInfoData.get("hardwareVersion");
//            String productId = deviceInfoData.get("productId");
////            String pinKsn = deviceInfoData.get("pinKsn");
//            String emvKsn = deviceInfoData.get("emvKsn");
//            String trackKsn = deviceInfoData.get("trackKsn");
//            String csn = deviceInfoData.get("csn");
//            String vendorID = deviceInfoData.get("vendorID");
//            String terminalSettingVersion = deviceInfoData.get("terminalSettingVersion");
//            String deviceSettingVersion = deviceInfoData.get("deviceSettingVersion");
//            String formatID = deviceInfoData.get("formatID");


            LogUtil.syso("ksn==" + ksn);
            toast("GET_KSN_OK");



        }

        @Override
        public void onReturnEmvTransactionLog(String[] strings) {

        }

        @Override
        public void onReturnEmvLoadLog(String[] strings) {

        }

        @Override
        public void onReturnTransactionResult(WisePadController.TransactionResult transactionResult) {

        }

        @Override
        public void onReturnTransactionResult(WisePadController.TransactionResult transactionResult, Hashtable<String, String> hashtable) {

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
        public void onReturnAmountConfirmResult(boolean b) {

        }

        @Override
        public void onReturnPinEntryResult(WisePadController.PinEntryResult pinEntryResult, Hashtable<String, String> data) {

            if (pinEntryResult == WisePadController.PinEntryResult.ENTERED) {

                if (data.containsKey("epb")) {
                    pwd = data.get("epb");
                    LogUtil.syso("pwd" + pwd);
                    toast(SWIPE_OK);
                }
            } else if (pinEntryResult == WisePadController.PinEntryResult.CANCEL) {
                toast("密码取消");
            } else if (pinEntryResult == WisePadController.PinEntryResult.TIMEOUT) {
                toast("密码输入超时");
            } else if (pinEntryResult == WisePadController.PinEntryResult.KEY_ERROR) {
                toast("加密密钥错误");
            }else if(pinEntryResult == WisePadController.PinEntryResult.BYPASS) {
                pwd = "000000";
                if ("1".equals(cardType) || "01".equals(cardType)) {
                    cardTypeValue = "052";
                } else {
                    cardTypeValue = "022";
                }
                toast(SWIPE_OK);

            }


        }

        @Override
        public void onReturnPrintResult(WisePadController.PrintResult printResult) {

        }

        @Override
        public void onReturnAmount(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnUpdateTerminalSettingResult(WisePadController.TerminalSettingStatus terminalSettingStatus) {

        }

        @Override
        public void onReturnReadTerminalSettingResult(WisePadController.TerminalSettingStatus terminalSettingStatus, String s) {

        }

        @Override
        public void onReturnEnableInputAmountResult(boolean b) {

        }

        @Override
        public void onReturnDisableInputAmountResult(boolean b) {

        }

        @Override
        public void onReturnPhoneNumber(WisePadController.PhoneEntryResult phoneEntryResult, String s) {

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
        public void onReturnMagStripeCardNumber(WisePadController.CheckCardResult checkCardResult, String s) {

        }

        @Override
        public void onReturnEncryptPinResult(Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnEncryptDataResult(boolean b, Hashtable<String, String> hashtable) {

        }

        @Override
        public void onReturnInjectSessionKeyResult(boolean b) {

        }

        @Override
        public void onReturnViposBatchExchangeApduResult(Hashtable<Integer, String> hashtable) {

        }

        @Override
        public void onReturnViposExchangeApduResult(String s) {

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
        public void onRequestSelectApplication(ArrayList<String> arrayList) {

        }

        @Override
        public void onRequestSetAmount() {
            promptForAmount();

        }

        private void promptForAmount() {
            String amount = formatmoney;
            WisePadController.CurrencyCharacter[] currencyCharacters =
                    new WisePadController.CurrencyCharacter[]{
                            WisePadController.CurrencyCharacter.YUAN};
            WisePadController.TransactionType transactionType;
            if (Constant.CONSUME.equals(tradetype)) {
                transactionType = WisePadController.TransactionType.GOODS;
            } else {
                transactionType = WisePadController.TransactionType.INQUIRY;
            }
            String currencyCode = "156";
            if (wisePadController.setAmount(amount, "0", currencyCode, transactionType, currencyCharacters)) {
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
        public void onRequestPinEntry(WisePadController.PinEntrySource pinEntrySource) {
            if (pinEntrySource == WisePadController.PinEntrySource.KEYPAD) {
                dialogShow("请在键盘上输入密码");
            }
        }

        @Override
        public void onRequestCheckServerConnectivity() {
            wisePadController.sendServerConnectivity(true);

        }

        @Override
        public void onRequestOnlineProcess(String tlv) {
            Hashtable<String, String> decodeData = WisePadController.decodeTlv(tlv);
            sTrack2 = decodeData.get("encTrack2Eq");
            sPan = decodeData.get("maskedPAN");
            pansn = decodeData.get("5F34") == null ? "" : "0" +
                    decodeData.get("5F34");
            pwd = decodeData.get("99");
            sExpData = decodeData.get("5F24").substring(0, 4);
            ic55DataStr = decodeData.get("encOnlineMessage");
            dialogShow("二磁道:" + sTrack2 + ";" + "卡号:" + sPan + ";" +
                    "序列号:" + pansn + ";" + "有效期:" + sExpData + ";" + "55域:" + ic55DataStr);
            wisePadController.sendOnlineProcessResult("8A023030");
            toast(SWIPE_OK);

        }

        @Override
        public void onRequestTerminalTime() {

        }

        @Override
        public void onRequestDisplayText(WisePadController.DisplayText displayText) {

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
            if (!isPinCanceled) {
                wisePadController.sendFinalConfirmResult(true);
            } else {
                wisePadController.sendFinalConfirmResult(false);
            }


        }

        @Override
        public void onRequestVerifyID(String s) {

        }

        @Override
        public void onRequestInsertCard() {

        }

        @Override
        public void onRequestPrintData(int i, boolean b) {

        }

        @Override
        public void onPrintDataCancelled() {

        }

        @Override
        public void onPrintDataEnd() {

        }

        @Override
        public void onBatteryLow(WisePadController.BatteryStatus batteryStatus) {

        }

        @Override
        public void onBTv2DeviceNotFound() {
            toast("设备未找到，请重试");
        }

        @Override
        public void onAudioDeviceNotFound() {

        }

        @Override
        public void onDevicePlugged() {

        }

        @Override
        public void onDeviceUnplugged() {

        }

        @Override
        public void onError(WisePadController.Error errorState, String s) {
            if (errorState == WisePadController.Error.INPUT_OUT_OF_RANGE) {
                toast("超出输入范围，请重试");
            } else if (errorState == WisePadController.Error.INPUT_INVALID) {
                toast("输入无效");
            } else if (errorState == WisePadController.Error.FAIL_TO_START_BTV2) {
                toast("蓝牙连接失败,请重试");
            } else if (errorState == WisePadController.Error.FAIL_TO_START_AUDIO) {
                toast("音频连接失败，请重试");
            }

        }
    }


    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }


    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (CONN_OK.equals(paramString)) {
            wisePadController.getDeviceInfo();
            //开始刷卡
//            writeMainKey();
//            statSwiper();

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
        requestData.put(52, pwd);
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
        if (CommonUtils.getConnectedType(SwipeWaitBBPoseBuleActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitBBPoseBuleActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        trade(url);


    }

    @Background
    void trade(String url) {
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
                        localIntent.setClass(SwipeWaitBBPoseBuleActivity.this, SignNameActivity.class);
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
                        ViewUtils.overridePendingTransitionCome(SwipeWaitBBPoseBuleActivity.this);
                        return;
                    } else {
                        toast(resultValue);
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


    void initQueryUrl() {
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String moneyVal = CommonUtils.formatTo12Zero(money);
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
        requestData.put(35, sTrack2.toUpperCase()); //2磁道密文
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, pwd); //密码加密
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
                        localIntent.setClass(SwipeWaitBBPoseBuleActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitBBPoseBuleActivity.this);
                        return;
                    } else {
                        toast(resultValue);
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
        LogUtil.d("SwipeWaitMoFangActivity", "url==" + url);
        return;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopConnection();
        wisePadController.resetWisePadController();


    }


    public void stopConnection() {
        WisePadController.ConnectionMode connectionMode = wisePadController.getConnectionMode();
        if (connectionMode == WisePadController.ConnectionMode.BLUETOOTH_2) {
            wisePadController.stopBTv2();
        } else if (connectionMode == WisePadController.ConnectionMode.AUDIO) {
            wisePadController.stopAudio();
        }
    }
}

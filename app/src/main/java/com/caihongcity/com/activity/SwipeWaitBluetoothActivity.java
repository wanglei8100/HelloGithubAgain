package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.model.ITCommunicationCallBack;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.itron.android.ftf.Util;
import com.itron.android.lib.Logger;
import com.itron.protol.android.BLECommandController;
import com.itron.protol.android.CommandReturn;
import com.itron.protol.android.TransactionDateTime;
import com.itron.protol.android.TransactionInfo;
import com.itron.protol.android.TransationCurrencyCode;
import com.itron.protol.android.TransationTime;
import com.itron.protol.android.TransationType;

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
import java.util.Random;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitBluetoothActivity extends BaseActivity {
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    private static final String TAG = "SwipeWaitBluetoothActivity";
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    String batchNo_Value_;

    @Extra
    String feeRate;
    @Extra
    String topFeeRate;
    BLECommandController itcommm;
    String ksn;
    HashMap<String, Object> map = null;

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
    String voucherNo_Value_;
    String cardTypeValue;
    Dialog dialog;
    @ViewById
    ImageView swipe_flash;
    private String workkey;


    @Background
    void getKSN() {
        int i = itcommm.openDevice(blue_address);
        if (i != 0) {
            toast("设备连接失败,请重试");
            StorageAppInfoUtil.putInfo(this, "bluetooth_address", "");
            return;
        }
        CommandReturn localCommandReturn = this.itcommm.Get_ExtPsamNo();
        if (localCommandReturn == null) {
            toast("通信失败");
            return;
        }
        if (localCommandReturn.Return_Result == 0) {
            this.ksn = Util.BinToHex(localCommandReturn.Return_PSAMNo, 0, localCommandReturn.Return_PSAMNo.length);
            LogUtil.syso("ksn==" + this.ksn);
            toast("GET_KSN_OK");
            return;
        }
        toast("通信错误");
    }

    @AfterViews
    void initData() {
        Logger.getInstance(SwipeWaitBluetoothActivity.class).setDebug(true);
        this.tv_title_des.setText("检测刷卡器");
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        this.itcommm = BLECommandController.GetInstance(this, new ITCommunicationCallBack());
        this.map = new HashMap();
        dialog = ViewUtils.createLoadingDialog(this, "正在检测设备...", false);
        dialog.show();
        getKSN();
    }

    @Click({R.id.ll_back})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
        }

    }

    protected void onStop() {
        super.onStop();
        LogUtil.e("SwipeWaitBluetoothActivity", "onStop");
        this.itcommm.release();
    }

    public void query() {
        String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "052";
        } else {
            cardTypeValue = "022";
        }
        String randomNumber = (String) this.map.get("random");
        String maskedPAN = (String) this.map.get("cardNo");
        String cardexpiryDate = (String) this.map.get("cardexpiryDate");
        String tractData = (String) this.map.get("track");
        String ic55DataStr = (String) this.map.get("ic55DataStr");
        String psamPin = (String) this.map.get("psamPin");
        String moneyVal = CommonUtils.formatTo12Zero(money);
        String cardSerial = CommonUtils.formatTo3Zero((String) map.get("cardSerial"));
        String terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        String customerNum = StorageCustomerInfoUtil.getInfo("customerNum", this);
        String feeRateVal = CommonUtils.formatTo8Zero(this.feeRate);
//        String forMd5Data = "0200" + maskedPAN + "310000" + feeRateVal + this.voucherNo_Value_ + cardexpiryDate + cardTypeValue +cardSerial+ "12"
//                + tractData.toUpperCase() +
//                terminal + customerNum + "156" + randomNumber +ic55DataStr+ Constant.VERSION + "01" + this.batchNo_Value_ + "003";
//        LogUtil.syso("forMd5Data====" + forMd5Data);
//        String data = "0=0200&2=" + maskedPAN + "&3=310000" + "&9=" + feeRateVal + "&11=" + this.voucherNo_Value_ + "&14=" + cardexpiryDate
//                + "&22=" + cardTypeValue
//                + "&23=" + cardSerial
//                + "&26=12" + "&35=" + tractData.toUpperCase() + "&41=" + terminal + "&42=" + customerNum + "&49=156" + "&53=" + randomNumber
//                + "&55=" + ic55DataStr + "&59="
//                + Constant.VERSION + "&60=01" + this.batchNo_Value_ + "003" + "&64=" +
//                CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
//        String url = Constant.REQUEST_API + data;
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "310000");
        requestData.put(4, moneyVal);
        requestData.put(9, feeRateVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, cardexpiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23, cardSerial);
        requestData.put(26, "12");
        requestData.put(35, tractData.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customerNum);
        requestData.put(49, "156");
        requestData.put(52, psamPin);
        requestData.put(55, ic55DataStr);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, "01" + batchNo_Value_ + "003");
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
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
                        localIntent.setClass(SwipeWaitBluetoothActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitBluetoothActivity.this);
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
                loadingDialog.show();
            }
        }).execute(url);
        LogUtil.d("SwipeWaitBluetoothActivity", "url==" + url);
        return;
    }

    private void consumeRequest() {
        String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "052";
        } else {
            cardTypeValue = "022";
        }
        String randomNumber = (String) this.map.get("random");
        String maskedPAN = (String) this.map.get("cardNo");
        String cardexpiryDate = (String) this.map.get("cardexpiryDate");
        String tractData = (String) this.map.get("track");
        String ic55DataStr = (String) this.map.get("ic55DataStr");
        String psamPin = (String) this.map.get("psamPin");
        String cardSerial = CommonUtils.formatTo3Zero((String) map.get("cardSerial"));
        String moneyVal = CommonUtils.formatTo12Zero(money);
        String terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        String customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        String feeRateVal = CommonUtils.formatTo8Zero(this.feeRate);
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

        String termianlVoucherNo = null;
        String terminalBatchNo = null;
        if (queryModel != null) {
            termianlVoucherNo = queryModel.getTermianlVoucherNo();
            terminalBatchNo = queryModel.getTerminalBatchNo();
        }
//        String forMd5Data = null;
//        if (Constant.CONSUME.equals(tradetype)) {//消费
//            forMd5Data = "0200" + maskedPAN + "000000" + money + feeRateVal + voucherNo_Value_ + cardexpiryDate + cardTypeValue
//                    + cardSerial + "12" + tractData.toUpperCase() +
//                    terminal + customer + "156" + randomNumber + ic55DataStr + Constant.VERSION + sixtydata;
//        } else {
//            forMd5Data = "0200" + maskedPAN + "000000" + money + voucherNo_Value_ + cardexpiryDate + cardTypeValue + cardSerial + "12"
//                    + tractData.toUpperCase() +
//                    terminal + customer + "156" + randomNumber + ic55DataStr + Constant.VERSION + sixtydata + terminalBatchNo
//                    + termianlVoucherNo;
//        }
//
//        LogUtil.syso("forMd5Data====" + forMd5Data);
//        String data = null;
//        if (Constant.CONSUME.equals(tradetype)) {//消费
//            data = "0=0200&2=" + maskedPAN + "&3=000000&4=" + money + "&9=" + feeRateVal + "&11=" + voucherNo_Value_ + "&14="
//                    + cardexpiryDate + "&22=" + cardTypeValue + "&26=12"
//                    + "&23="+cardSerial
//                    + "&35=" + tractData.toUpperCase() + "&41=" + terminal + "&42=" + customer + "&49=156"
//                    + "&53=" + randomNumber//随机密钥
//                    + "&55=" + ic55DataStr
//                    + "&59=" + Constant.VERSION
//                    + "&60=" + sixtydata
//                    + "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
//        } else {//撤销
//            data = "0=0200&2=" + maskedPAN + "&3=000000&4=" + money + "&11=" + voucherNo_Value_ + "&14=" + cardexpiryDate + "&22="
//                    + cardTypeValue + "&26=12"
//                    + "&23="+cardSerial
//                    + "&35=" + tractData.toUpperCase() + "&41=" + terminal + "&42=" + customer + "&49=156"
//                    + "&53=" + randomNumber//随机密钥
//                    + "&55=" + ic55DataStr
//                    + "&59=" + Constant.VERSION
//                    + "&60=" + sixtydata + "&61=" + terminalBatchNo + termianlVoucherNo
//                    + "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
//        }
//
//        String url = Constant.REQUEST_API + data;
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "000000");
        requestData.put(4, moneyVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, cardexpiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23, cardSerial);
        requestData.put(26, "12");
        requestData.put(35, tractData.toUpperCase());
        requestData.put(41, terminal);
        requestData.put(42, customer);
        requestData.put(49, "156");
        requestData.put(52, psamPin);
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
                        String maskedPAN = (String) map.get("cardNo");
                        String voucherNo = (String) localJSONObject.get("11");
                        String voucherNo37 = (String) localJSONObject.get("37");
                        Intent localIntent = new Intent();
                        localIntent.setClass(SwipeWaitBluetoothActivity.this, SignNameActivity.class);
                        localIntent.putExtra("tradeType", tradetype);
                        localIntent.putExtra("queryModel", queryModel);
                        localIntent.putExtra("cardNo", maskedPAN);
                        localIntent.putExtra("voucherNo", voucherNo);
                        localIntent.putExtra("voucherNo37", voucherNo37);
                        localIntent.putExtra("money", CommonUtils.format02(money));
                        localIntent.putExtra("feeRate", feeRate);
                        localIntent.putExtra("topFeeRate", topFeeRate);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitBluetoothActivity.this);
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

                loadingDialog.show();
            }
        });
        myAsyncTask.execute(url);
    }

    @Background
    void statEmvSwiper() {
        workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
        if (workkey != null && workkey.length() == 120) {
           // String PINkey = workkey.substring(0, 40);
            String PINkey = "bc607fa9bc9756f7bc607fa9bc9756f7c4af51bd";
            //String MACkey = workkey.substring(40, 80);
            String MACkey = "bc607fa9bc9756f7bc607fa9bc9756f7c4af51bd";
            //String DESkey = workkey.substring(80, 120);
            String DESkey = "bc607fa9bc9756f7bc607fa9bc9756f7c4af51bd";
            byte[] PINkeyValue = CommonUtils.HexStringToByteArray(PINkey);
            byte[] MACkeyValue = CommonUtils.HexStringToByteArray(MACkey);
            byte[] DESkeyValue = CommonUtils.HexStringToByteArray(DESkey);
            CommandReturn commandReturn = itcommm.Get_RenewKey(PINkeyValue, MACkeyValue, DESkeyValue);
            if(commandReturn.Return_Result == 0){
                LogUtil.syso("更新工作密钥成功");
            }else{
                LogUtil.syso("密钥返回码"+commandReturn.Return_Result);
                LogUtil.syso("PINkeyValue:"+CommonUtils.bytes2Hex(PINkeyValue));
                LogUtil.syso("MACkeyValue:"+CommonUtils.bytes2Hex(MACkeyValue));
                LogUtil.syso("DESkeyValue:"+CommonUtils.bytes2Hex(DESkeyValue));
                toast("更新工作密钥失败");
                return;
            }
        }else{
            toast("workkey异常");
            return;
        }



        CommandReturn cmdret = new CommandReturn();



        TransactionInfo info = new TransactionInfo();
        // 设置交易日期 格式: YYMMDD
        TransactionDateTime dateTime = new TransactionDateTime();
        dateTime.setDateTime(Util.getCurrentDateYY());
        // 设置交易时间   hhssmm
        TransationTime time = new TransationTime();
        time.setTime(Util.getCurrentTime());
        // 设置货币代码
        TransationCurrencyCode currencyCode = new TransationCurrencyCode();
        currencyCode.setCode("0156");
        // 设置交易类型  00 消费  31余额查询
        TransationType type = new TransationType();
        if (Constant.CONSUME.equals(tradetype)) {
            type.setType("00");
        } else if (Constant.QUERYBALANCE.equals(tradetype)) {
            type.setType("31");
        } else {
            type.setType("20");
        }

        info.setDateTime(dateTime);
        info.setCurrencyCode(currencyCode);
        info.setTime(time);
        info.setType(type);
        String byte0 = "00010000";
        String byte1 = "01111011";
        String byte2 = "00001110";

        int flag0 = Util.binaryStr2Byte(byte0);
        int flag1 = Util.binaryStr2Byte(byte1);
        int flag2 = Util.binaryStr2Byte(byte2);
        if (Constant.QUERYBALANCE.equals(this.tradetype)) {
            money = "000000000000";
        } else {
            money = CommonUtils.formatTo12Zero(CommonUtils.format(money));
        }

        String random_String = "";
        for (int m = 0; m < 3; m++) {
            random_String = random_String + new Random().nextInt(9);
        }


        byte[] random_byte = random_String.getBytes();
        String random_hex = Util.BinToHex(random_byte, 0, random_byte.length);
        LogUtil.i("SwipeWaitBluetoothActivity", "random_String:" + random_String + "   random_byte:" + random_byte + "  random_hex:" + random_hex);
        cmdret = itcommm.statEmvSwiper(1, 1, 1, 1, new byte[]{(byte) flag0, (byte) flag1, (byte) flag2, 0x01}, null, CommonUtils.formatMoneyToFen(money).toString(), null, 50, info);

        LogUtil.syso("cmdret:"+cmdret.toString());
        if ((cmdret != null) && (cmdret.Return_Result == 10)) {
            toast("用户取消了本次操作");
            return;
        }
        LogUtil.syso("cmdret.Return_PSAMTrack:" + CommonUtils.bytes2Hex(cmdret.Return_PSAMTrack));
        LogUtil.syso("cmdret.Return_PSAMTrack:" + cmdret.Return_PSAMTrack);
        LogUtil.syso("cmdret.Return_Track2:" + CommonUtils.bytes2Hex(cmdret.Return_Track2));
        LogUtil.syso("cmdret.Return_Track3:" + cmdret.Return_Track3);

        if ((cmdret != null) && (cmdret.Return_Track2 != null)) {
            StringBuffer localStringBuffer = new StringBuffer();
            if (cmdret.Return_PSAMNo != null) {
                localStringBuffer.append("PSAMNo:" + Util.BinToHex(cmdret.Return_PSAMNo, 0, cmdret.Return_PSAMNo.length) + "\n");
                this.map.put("pSAMNo", Util.BinToHex(cmdret.Return_PSAMNo, 0, cmdret.Return_PSAMNo.length));
            }
            if (cmdret.Return_Track2 != null) {
                localStringBuffer.append("PSAMTrack:" + Util.BinToHex(cmdret.Return_Track2, 0, cmdret.Return_Track2.length) + "\n");
                String PSAMTrack = Util.BinToHex(cmdret.Return_Track2, 0, cmdret.Return_Track2.length);
//                String Track = PSAMTrack.substring(0, 48);
                LogUtil.i("SwipeWaitBluetoothActivity", PSAMTrack + "  lenth:" + PSAMTrack.length());
                localStringBuffer.append("Track:" + PSAMTrack + "\n");
                localStringBuffer.append("Random:" + PSAMTrack.substring(PSAMTrack.length() - 8, PSAMTrack.length()) + "\n");
                this.map.put("track", PSAMTrack);
                this.map.put("random", "FF" + PSAMTrack.substring(PSAMTrack.length() - 8, PSAMTrack.length()) + random_hex);
            }
            if (cmdret.Return_PSAMPIN != null) {
                localStringBuffer.append("PSAMPIN:" + Util.BinToHex(cmdret.Return_PSAMPIN, 0, cmdret.Return_PSAMPIN.length) + "\n");
                this.map.put("psamPin", Util.BinToHex(cmdret.Return_PSAMPIN, 0, cmdret.Return_PSAMPIN.length));
            }
            if (cmdret.Return_ENCCardNo != null) {
                localStringBuffer.append("CardNo:" + new String(cmdret.Return_ENCCardNo).toString() + "\n");
                this.map.put("cardNo", new String(cmdret.Return_ENCCardNo).toString());
            }
            if (cmdret.cardexpiryDate != null) {
                localStringBuffer.append("cardexpiryDate:" + new String(cmdret.cardexpiryDate).toString() + "\n");
                this.map.put("cardexpiryDate", new String(cmdret.cardexpiryDate).toString());
            }
            if (cmdret.CardType != 0) {
                localStringBuffer.append("CardType:" + cmdret.CardType + "\n");
                this.map.put("cardType", cmdret.CardType + "");
            }
            if (cmdret.emvDataInfo != null) {
                localStringBuffer.append("ic55DataStr:" + Util.BinToHex(cmdret.emvDataInfo, 0, cmdret.emvDataInfo.length) + "\n");
                this.map.put("ic55DataStr", Util.BinToHex(cmdret.emvDataInfo, 0, cmdret.emvDataInfo.length) + "");
            }
            if (cmdret.CardSerial != null) {
                localStringBuffer.append("CardSerial:" + Util.BinToHex(cmdret.CardSerial, 0, cmdret.CardSerial.length) + "\n");
                this.map.put("cardSerial", Util.BinToHex(cmdret.CardSerial, 0, cmdret.CardSerial.length) + "");
            }
            LogUtil.i("SwipeWaitBluetoothActivity", localStringBuffer.toString());
            toast(SWIPE_OK);
            return;
        }

        toast("操作失败");
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
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBluetoothActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitBluetoothActivity.this, "workkey", workKey);
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
            ViewUtils.makeToast(this,"刷卡器正常，请刷/插卡",1500);
            statEmvSwiper();
        } else if (SWIPE_OK.equals(paramString)) {
            if ((Constant.CONSUME.equals(this.tradetype)) || (Constant.CANCEL.equals(this.tradetype))) {
                consumeRequest();
            } else if (Constant.QUERYBALANCE.equals(this.tradetype)) {
                query();
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
}
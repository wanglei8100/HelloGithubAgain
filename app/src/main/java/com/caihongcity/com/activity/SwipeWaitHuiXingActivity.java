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
import com.itron.android.ftf.Util;
import com.itron.protol.android.BLECommandController;
import com.msafepos.sdk.HXPos;
import com.msafepos.sdk.PBOCUtil;
import com.msafepos.sdk.listener.PosEvent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitHuiXingActivity extends BaseActivity implements PosEvent {
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
    HashMap<String, Object> map = null;

    @Extra
    String money;

    @Extra
    QueryModel queryModel;

    @Extra
    String tradetype;

    @ViewById
    TextView tv_title_des;
    String voucherNo_Value_;
    Dialog dialog;
    private ServiceReceiver serviceReceiver;
    boolean recvedICData = false;
    @ViewById
    ImageView swipe_flash;
    @AfterViews
    void initData() {
        this.tv_title_des.setText("检测刷卡器");
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        registerServiceReceiver();
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean isAudioConnect = mAudioManager.isWiredHeadsetOn();
        if (!isAudioConnect){
            showNoDeviceDialog();
            return;
        }
        dialog = ViewUtils.createLoadingDialog(this, "正在检测设备...", false);
        dialog.show();
        map = new HashMap<String, Object>();
        //初始化
        HXPos.init(this, HXPos.COMM_VOC);
        HXPos.getObj().Start(HXPos.COMM_VOC);
        //生产版本请把debug设置为false
        HXPos.debug = true;

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

    @Override
    public void onRecvData(int errCode, byte[] recvdata) {
        LogUtil.d(TAG, "onRecvData errCode:" + errCode + "  recvdata:" + Util.BinToHex(recvdata, 0, recvdata.length));
        if (errCode != 0) {
            // 设备返回数据错误，此处可以提醒用户
            LogUtil.syso("刷卡器返回数据解码失败\n");
            return;
        }
        if (HXPos.getObj().mFskDecode.GetDataType() == 'T') {
            // 测试报文应答，忽略处理
            return;
        }
        // 显示原始报文
        LogUtil.syso("RECV:" + Util.BinToHex(recvdata, 0, recvdata.length));

        // 解析原始报文，结果放入HXPos.PosData结构
        HXPos.PosData res = HXPos.getObj().Parse(recvdata);
        // 解析错误
        if (res == null) {
            LogUtil.syso("刷卡器返回数据格式错误\n");
            return;
        }

        if (res.batLeft != null) {
            // 电池电量小于3.7V
            if (Integer.parseInt(Util.BytesToString(res.batLeft)) < 3700) {
                LogUtil.syso("电量不足");
            }
        }

        switch (res.cmdType) {
            case HXPos.CMD_SWIPECARD: {
                // 刷卡指令返回数据
                LogUtil.d(TAG, "HXPos.CMD_SWIPECARD  刷卡指令返回数据");
                if (res.result == 1) {
                    LogUtil.syso("刷卡器收到指令");
                } else if (res.result == 100) {
                    LogUtil.syso("IC卡插入");
                    recvedICData = false;
                    //等待12秒，如果没有收到ic卡数据，可以启动重发
//                    runnableWaitSNo();

                } else if (res.result == 23) {
                    LogUtil.syso("IC卡拔出");
                } else if (res.result == 35) {
                    LogUtil.syso("IC卡读取失败");
                } else if (res.result == 22) {
                    //磁条卡刷卡失败，无需再次发送刷卡指令，界面提示用户重新刷卡即可
                    LogUtil.syso("磁条刷卡失败");
                } else if (res.result == 5) {
                    LogUtil.syso("刷卡指令超时，用户无操作");
                } else if (res.result == 0) {
                    // 刷卡成功，读取结果数据
                    LogUtil.syso("磁道2数字个数：" + res.cd2[0]);

                    if (res.cd2[0] < 22) {
                        LogUtil.syso("磁道2长度错误，请重刷\n");
                    }
                    LogUtil.syso("磁道2："
                            + Util.BinToHex(res.cd2, 1, res.cd2.length - 1));
                    map.put("track02",Util.BinToHex(res.cd2, 1, res.cd2.length - 1));
                    if (res.cd3 != null) {
                        LogUtil.syso("磁道3数字个数：" + res.cd3[0]);
                        if (res.cd3[0] < 66) {
                            LogUtil.syso("磁道3长度错误,请不要送磁道3数据\n");
                        }
                        LogUtil.syso("磁道3："
                                + Util.BinToHex(res.cd3, 1, res.cd3.length - 1));
                        map.put("track03", Util.BinToHex(res.cd3, 1, res.cd3.length - 1));
                        String s1 = Util.BinToHex(res.cd3, 1, res.cd3.length - 1)
                                .replace('D', '=');
                        String s2 = Util.BinToHex(s1.getBytes(), 0, s1.length());

                    }
                    if (res.cardValidDate != null) {
                        LogUtil.syso("卡有效期：" + Util.BytesToString(res.cardValidDate));
                        map.put("expiryDate", Util.BytesToString(res.cardValidDate));
                    }
                    if (res.cardMingWen != null) {
                        LogUtil.syso("卡号：" + Util.BytesToString(res.cardMingWen));
                        map.put("maskedPAN", Util.BytesToString(res.cardMingWen));
                    }
                    if (res.field55 != null) {
                        LogUtil.syso("55域：" + Util.BinToHex(res.field55, 0, res.field55.length));
                        map.put("ic55DataStr", Util.BinToHex(res.field55, 0, res.field55.length));
                    }
                    if (res.userPDNo != null)
                        LogUtil.syso("用户设备编号:" + Util.BytesToString(res.userPDNo));

                    if (res.cardType != null){
                        // 2-磁条,1-ic卡
                        LogUtil.syso("卡片类型:(1-ic卡;2-磁条;4-ic卡刷卡)"
                                + Util.BinToHex(res.cardType, 0, res.cardType.length));
                    map.put("cardType", Util.BinToHex(res.cardType, 0, res.cardType.length));}
                    if (res.batLeft != null)
                        LogUtil.syso("剩余电量:" + Util.BytesToString(res.batLeft));

                    if (res.hardNo != null)
                        LogUtil.syso("汇兴设备编号:" + Util.BinToHex(res.hardNo, 0, res.hardNo.length));

                    if (res.pan != null) {
                        LogUtil.syso("PAN:" + Util.BinToHex(res.pan, 0, res.pan.length));
                    }

                    if (res.t5f34 != null){
                        LogUtil.syso("卡片序列号(23域):" + Util.BinToHex(res.t5f34, 0, res.t5f34.length));
                        map.put("cardSerial",Util.BinToHex(res.t5f34, 0, res.t5f34.length));
                    }


                    if (res.random != null){
                        LogUtil.syso("加密随机数:" + Util.BinToHex(res.random, 0, res.random.length));
                    map.put("randomNumber", Util.BinToHex(res.random, 0, res.random.length));}
                    if (res.cardType != null) {
                        if (res.cardType[0] == 4)
                            LogUtil.syso("!!!IC卡刷卡使用!!!");
                    }
                    recvedICData = true;
                    toast(SWIPE_OK);
                } else {
                    // 刷卡失败，
                    LogUtil.syso("其他IC卡失败:" + res.result);
                }
                break;
            }
            case HXPos.CMD_PBOC_END_DEAL: {
                // pboc ic卡交易结束
                LogUtil.d(TAG, "HXPos.CMD_PBOC_END_DEAL  ic卡交易结束");
                if (res.result == 0) {
                    LogUtil.syso("ic卡交易结束，返回TC:" + Util.BinToHex(res.gen_ac2_retData, 0, res.gen_ac2_retData.length));
                    if (res.gen_ac2_retData != null) {
                        PBOCUtil.GenAC2Res res1 = PBOCUtil
                                .ParseGenAC2(res.gen_ac2_retData);
                        if (res1.cid == 0x40) {
                            // 成功交易,卡片返回TC
                            LogUtil.syso("交易TC值:" + Util.BinToHex(res1.tc, 0, res1.tc.length));
                            // 获取交易信息,卡号码,交易金额
                            LogUtil.syso("交易卡号:" + HXPos.getObj().getCardNo());
                            LogUtil.syso("交易金额:" + HXPos.getObj().getMoney());
                        } else if (res1.cid == 0x00) {

                        }
                    }

                } else
                    LogUtil.syso("ic卡交易结束错误:" + res.result);
                break;
            }
            case HXPos.CMD_READ_NO: {
                // 读取ksn返回数据
                LogUtil.d(TAG, "HXPos.CMD_READ_NO  读取ksn返回数据");
                String hinfo = Util.BinToHex(res.hardNo, 0, res.hardNo.length
                );
                LogUtil.d(TAG, "res.hardNo " + hinfo);

                LogUtil.syso("用户设备编号:" + Util.BytesToString(res.userPDNo,0,1));//测试用的设备ksn为默认值9
                LogUtil.syso("剩余电量:" + Util.BytesToString(res.batLeft));
//                String ksn = Util.BytesToString(res.userPDNo, 0, 1);//测试设备，生产要更改
                String ksn = "HX30000000000001";//测试设备，生产要更改
                LogUtil.d(TAG, "客户:" + hinfo.substring(0, 2) + " 型号:" + hinfo.substring(2, 4) +
                        " 软件版本:" + hinfo.substring(4, 8) + "硬件版本:" + hinfo.substring(8, 12) + " 用户设备编号:"
                        + Util.BytesToString(res.userPDNo) + "  剩余电量:" + Util.BytesToString(res.batLeft));
                //等待1秒，自动发送刷卡指令
                toBindTerminal(ksn);

                break;
            }

            case HXPos.CMD_UPDATE_KEY: {
                LogUtil.d(TAG, "CMD_UPDATE_KEY 保存主密钥返回");
                // 保存主密钥返回
                if (res.result == 0)
                    LogUtil.syso("更新工作密钥成功");
                else
                    LogUtil.syso("更新工作密钥失败");

                break;
            }
            case HXPos.CMD_DES: {
                // 加密结果
                LogUtil.d(TAG, "CMD_DES 加密结果");
                if (res.result == 0)
                    LogUtil.syso("加密结果:" + Util.BinToHex(res.desData, 0, res.desData.length));
                else
                    LogUtil.syso("加密失败");
                break;
            }
            case HXPos.CMD_SAVE_TMK_IN_CPU: {
                LogUtil.d(TAG, "CMD_SAVE_TMK_IN_CPU");
                if (res.result == 0)
                    LogUtil.syso("更新主密钥成功");
                else
                    LogUtil.syso("更新主密钥失败");
            }
            break;
        }

    }

    @Background
    void runnableWaitSNo() {
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if (recvedICData == false)
            LogUtil.syso("请求重发IC卡数据");
        //要求刷卡器重发ic卡数据
        HXPos.getObj().SendCmd(HXPos.CMD_RESEND);
    }

    @Override
    public void OnLogInfo(String s) {
        LogUtil.d(TAG, "OnLogInfo log:" + s.toString());
    }

    @Override
    public void OnPosConnect(boolean connected) {
        LogUtil.d(TAG, "OnPosConnect connected:" + connected);
        if (connected) {
            if (HXPos.getObj().getCommMode() == HXPos.COMM_BLUE) {
                LogUtil.d(TAG, "蓝牙连接上");
            } else {
                LogUtil.d(TAG, "音频连接上");
              /*  //延迟0.5秒发送
                new Thread()
                {
                    public void run()
                    {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        HXPos.getObj().SendReadNo();
                        mHandler.sendEmptyMessage(1001);
                    }
                }.start();*/
                getKsn();
            }
        } else {
            LogUtil.d(TAG, "未插入");
            if (this.dialog != null)
                this.dialog.dismiss();
            if (this.loadingDialog != null)
                this.loadingDialog.dismiss();
            showNoDeviceDialog();
        }
    }

    @UiThread
    void toast(String paramString) {
        if (BIND_TERMINAL_OK.equals(paramString)) {
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

    @Background
    void getKsn() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HXPos.getObj().SendReadNo();
    }

    public void toBindTerminal(String ksn) {
        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
//        String macData = "0700" + phoneNum + "190958" + Constant.VERSION + ksn + Constant.mainKey;
//        LogUtil.d(TAG, "macData==" + macData);
//        String url = Constant.REQUEST_API + "0=0700&1=" + phoneNum + "&3=190958"+ "&59=" + Constant.VERSION+"&62=" + ksn  + "&64=" + CommonUtils.Md5(macData);
        HashMap<Integer,String> requestData = new HashMap<Integer,String>();
        requestData.put(0,"0700");
        requestData.put(1,phoneNum);
        requestData.put(3,"190958");
        requestData.put(59,Constant.VERSION);
        requestData.put(62,ksn);
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {

            @Override
            public void isLoadingContent() {
                LogUtil.d(TAG, "isLoadingContent()===");
            }

            @Override
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
                        StorageCustomerInfoUtil.putInfo(SwipeWaitHuiXingActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitHuiXingActivity.this, "workkey", workKey);
                        toast(BIND_TERMINAL_OK);
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
        });
        LogUtil.d(TAG, "url===" + url);
        myAsyncTask.execute(url);
    }

    @Background
    void statEmvSwiper() {
        String type = null;//01 查询余额 ，22 消费
        if (Constant.CONSUME.equals(tradetype)) {
            type = "01";
        } else if (Constant.QUERYBALANCE.equals(tradetype)) {
            type = "22";
        }

        LogUtil.d(TAG, "自动发送刷卡指令");
        HXPos.getObj().SendSwipeCard2((byte) 35,
                HXPos.makeKeyIndex(0, 0, 0/* 加密密钥索引0-5 */), null,
                true, true, false, "1234".getBytes(), (byte) 1, type);
    }
    /**
     * init Dialog
     */
    private void initDialog() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType)||"01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String maskedPAN = (String) map.get("maskedPAN");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        formatmoney = CommonUtils.format(money);
        customDialog = new CustomDialog(SwipeWaitHuiXingActivity.this, R.style.mystyle, R.layout.customdialog, "￥ " + formatmoney, maskedPANValue);
        customDialog.setCanceledOnTouchOutside(true);
        inputDialogListener = new CustomDialog.InputDialogListener() {

            @Override
            public void onOK(String text) {
                if (TextUtils.isEmpty(text)) {
                    text = "000000";
                    if ("1".equals(cardType)||"01".equals(cardType)) {
                        cardTypeValue = "052";
                    } else {
                        cardTypeValue = "022";
                    }
                }
                initUrl(text);
            }

            @Override
            public void onCancel() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitHuiXingActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    String cardTypeValue;

    private void initUrl(String pwdVal) {

        String tractData = (String) map.get("track02");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("maskedPAN");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSerial = CommonUtils.formatTo3Zero((String)map.get("cardSerial"));
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
        if (Constant.CONSUME.equals(tradetype)) {//消费
            forMd5Data = "0200" + maskedPAN + "000000" + moneyVal + feeRateVal + voucherNo_Value_ + expiryDate + cardTypeValue + cardSerial + "12" + tractData.toUpperCase() +
                    terminal + customer + "156" + pinbyteHex.toUpperCase() + randomNumber +ic55DataStr+ Constant.VERSION + sixtydata;
        } else {
            forMd5Data = "0200" + maskedPAN + "000000" + moneyVal + voucherNo_Value_ + expiryDate + cardTypeValue + cardSerial + "12" + tractData.toUpperCase() +
                    terminal + customer + "156" + pinbyteHex.toUpperCase() + randomNumber +ic55DataStr+ Constant.VERSION + sixtydata + terminalBatchNo + termianlVoucherNo;
        }

        LogUtil.syso("forMd5Data====" + forMd5Data);
        String data = null;
        if (Constant.CONSUME.equals(tradetype)) {//消费
            data = "0=0200&2=" + maskedPAN + "&3=000000&4=" + moneyVal + "&9=" + feeRateVal + "&11=" + voucherNo_Value_ + "&14=" + expiryDate + "&22=" + cardTypeValue + "&26=12"
                    + "&23="+cardSerial
                    + "&35=" + tractData.toUpperCase() + "&41=" + terminal + "&42=" + customer + "&49=156"
                    + "&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
                    + "&55=" + ic55DataStr
                    + "&59=" + Constant.VERSION
                    + "&60=" + sixtydata
                    + "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
        } else {//撤销
            data = "0=0200&2=" + maskedPAN + "&3=000000&4=" + moneyVal + "&11=" + voucherNo_Value_ + "&14=" + expiryDate + "&22=" + cardTypeValue + "&26=12"
                    + "&23="+cardSerial
                    + "&35=" + tractData.toUpperCase() + "&41=" + terminal + "&42=" + customer + "&49=156"
                    + "&52=" + pinbyteHex.toUpperCase() + "&53=" + randomNumber//随机密钥
                    + "&55=" + ic55DataStr
                    + "&59=" + Constant.VERSION
                    + "&60=" + sixtydata + "&61=" + terminalBatchNo + termianlVoucherNo
                    + "&64=" + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
        }

        String url = Constant.REQUEST_API + data;
        //检查网络状态
        if (CommonUtils.getConnectedType(SwipeWaitHuiXingActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitHuiXingActivity.this, getString(R.string.nonetwork), 1500);
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
                        String maskedPAN = (String) map.get("maskedPAN");
                        String voucherNo = (String) obj.get("11");
                        String voucherNo37 = (String) obj.get("37");
                        Intent intent = new Intent();
                        intent.setClass(SwipeWaitHuiXingActivity.this, SignNameActivity.class);
                        intent.putExtra("tradeType", tradetype);
                        intent.putExtra("queryModel", queryModel);
                        intent.putExtra("cardNo", maskedPAN);
                        intent.putExtra("voucherNo", voucherNo);
                        intent.putExtra("voucherNo37", voucherNo37);
                        intent.putExtra("money", money);
                        intent.putExtra("feeRate", feeRate);
                        startActivity(intent);
                        SwipeWaitHuiXingActivity.this.finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitHuiXingActivity.this);

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
        LogUtil.d(TAG, "url==" + url);
    }
    private CustomDialog customDialog;
    private CustomDialog.InputDialogListener inputDialogListener;
    String formatmoney;
    /**
     * init Dialog
     */
    private void initDialogQuery() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType)||"01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String maskedPAN = (String) map.get("maskedPAN");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        customDialog = new CustomDialog(SwipeWaitHuiXingActivity.this, R.style.mystyle, R.layout.customdialog, "余额查询", maskedPANValue);
        customDialog.setCanceledOnTouchOutside(true);
        inputDialogListener = new CustomDialog.InputDialogListener() {

            @Override
            public void onOK(String text) {
                if (TextUtils.isEmpty(text)) {
                    text = "000000";
                    if ("1".equals(cardType)||"01".equals(cardType)) {
                        cardTypeValue = "052";
                    } else {
                        cardTypeValue = "022";
                    }
                }
                initUrlQuery(text);

            }

            @Override
            public void onCancel() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitHuiXingActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    private void initUrlQuery(String pwdVal) {

        String tractData = (String) map.get("track02");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("maskedPAN");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSerial = CommonUtils.formatTo3Zero((String)map.get("cardSerial"));
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
        String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        String forMd5Data = "0200" + maskedPAN + "310000" + feeRateVal + voucherNo_Value_
                + expiryDate + cardTypeValue + "12"  + cardSerial + tractData.toUpperCase()
                + terminal + customer + "156" + pinbyteHex.toUpperCase()
                + randomNumber +ic55DataStr+ Constant.VERSION + "01" + batchNo_Value_ + "003";
        LogUtil.syso("forMd5Data====" + forMd5Data);
        String data = "0=0200&2=" + maskedPAN + "&3=310000" + "&9=" + feeRateVal + "&11="
                + voucherNo_Value_ + "&14=" + expiryDate + "&22=" + cardTypeValue+ "&23=" + cardSerial + "&26=12" + "&35="
                + tractData.toUpperCase() + "&41=" + terminal + "&42="
                + customer + "&49=156" + "&52=" + pinbyteHex.toUpperCase()
                + "&53="
                + randomNumber// 随机密钥
                + "&55=" + ic55DataStr
                + "&59=" + Constant.VERSION
                + "&60=01" + batchNo_Value_ + "003" + "&64="
                + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
        String url = Constant.REQUEST_API + data;//0=0200&2=6225760009310363&3=000000&11=&14=1507&22=022&26=12&35=49B9DB7CE8D6C9E9E57FA7ED0E44C1B8B4A208F3DAE8C406&41=99977411&42=220558015061077&49=156&52=040777000103&53=2197D5BC00000008&60=01null003&64=A8D097DBDC969D45A96F092D5C02CAA1
        query(url);
    }

    public void query(String url) {
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
                        intent.setClass(SwipeWaitHuiXingActivity.this,
                                QueryBalancceResultActivity.class);
                        startActivity(intent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitHuiXingActivity.this);
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
        LogUtil.d(TAG, "url==" + url);
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
                        CommonUtils.adjustAudioMax(SwipeWaitHuiXingActivity.this);
                    } else {
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        LogUtil.d(TAG,"onStop");
        unregisterServiceReceiver();
        HXPos.getObj().onStop();

    }

    protected void onStart() {
        super.onStart();
        HXPos.getObj().onStart(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        HXPos.getObj().close();
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
        ViewUtils.showChoseDialog(this, true, "请插入刷卡头后重试", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitHuiXingActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }
}
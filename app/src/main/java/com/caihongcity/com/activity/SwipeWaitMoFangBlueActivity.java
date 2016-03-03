package com.caihongcity.com.activity;

import android.app.Dialog;
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
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.view.CustomDialog;
import com.itron.android.ftf.Util;
import com.itron.android.lib.Logger;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ConnectPosResult;
import com.mf.mpos.pub.result.GetEmvDataResult;
import com.mf.mpos.pub.result.ICAidResult;
import com.mf.mpos.pub.result.InputPinResult;
import com.mf.mpos.pub.result.LoadWorkKeyResult;
import com.mf.mpos.pub.result.OpenCardReaderResult;
import com.mf.mpos.pub.result.ReadMagcardResult;
import com.mf.mpos.pub.result.ReadPosInfoResult;
import com.mf.mpos.pub.result.StartEmvResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.itron.android.lib.TypeConversion.byte2hex;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitMoFangBlueActivity extends BaseActivity implements View.OnClickListener {


    private ServiceReceiver serviceReceiver;
    private Dialog dialog;
    private String cardType = ""; //用来判断刷卡的卡片类型
    private String cardTypeValue; //服务器需要根据此参数判定卡类型

    String isT0, feeRate, money, topFeeRate;

    HashMap<String, Object> map = null;
    @Extra
    QueryModel queryModel;

    @Extra
    String blue_address;

    private int swipTime = 60;//等待刷卡时间
    String voucherNo_Value_;
    String batchNo_Value_;
    String REPEAT_BIND = "94";
    String REPONSE_STATUS = "00";
    static final String GET_KSN_OK = "GET_KSN_OK";
    static final String BIND_TERMINAL_OK = "BIND_TERMINAL_OK";
    static final String SWIPE_OK = "SWIPE_OK";
    static final String CON_FAIL = "CON_FAIL";


    @Extra
    String ksn;
    @Extra
    String tradetype;
    @ViewById
    ImageView swipe_flash;
    Logger logger = Logger.getInstance(SwipeWaitActivity.class);
    private String expiryDate; //卡有效期
    private String terminal;   //终端号
    private String customer;   //用户号码
    private String maskedPAN;  //卡号
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
    private OpenCardReaderResult result; //开启读卡器的返回结果
    private StartEmvResult resultStartEmv; //开启IC卡返回对象
    private String pansn; //卡片序列号

    @AfterViews
    void initData() {
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        dialogShow("正在检测设备...");
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("检测刷卡器");
        feeRate = getIntent().getStringExtra("feeRate");
        topFeeRate = getIntent().getStringExtra("topFeeRate");
        queryModel = (QueryModel) getIntent().getSerializableExtra("queryModel");
        money = getIntent().getStringExtra("money");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化

        //初始化连接
        initConn();
        //获取ksn
        getKSN();

    }

    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitMoFangBlueActivity.this, s, true);
        dialog.show();

    }


    void initConn() {
        Controler.Init(this, CommEnum.CONNECTMODE.BLUETOOTH);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Controler.ResetPos();
        Controler.disconnectPos();
        Controler.Destory();


        if (dialog != null) {
            dialog.dismiss();
        }
        if (customDialog != null) {
            customDialog.dismiss();
        }

        //unregisterServiceReceiver();
    }


    /**
     * 释放广播接收器
     */
    public void unregisterServiceReceiver() {
        if (this.serviceReceiver != null) {
            unregisterReceiver(this.serviceReceiver);
            this.serviceReceiver = null;
            logger.debug("unregister ServiceReceiver");
        }
    }


    @Background
    void getKSN() {

        //连接pos
        connPos();
        Controler.ResetPos();
        ReadPosInfoResult result = Controler.ReadPosInfo();
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            this.ksn = result.sn;
            logger.error("ksn的数据" + ksn);
            toast("GET_KSN_OK");
            return;
        }


    }

    private void connPos() {
        //POS未连接，连接POS
        ConnectPosResult rc = Controler.connectPos(blue_address);
        if (rc.bConnected) {
            StorageAppInfoUtil.putInfo(this, "bluetooth_address", blue_address);
            dialogShow("设备连接成功");
        } else {
            StorageAppInfoUtil.putInfo(this, "bluetooth_address", "");
            toast("设备连接失败,请重试");
            return;
        }

    }

    @UiThread
    void toast(String paramString) {
        if (GET_KSN_OK.equals(paramString)) {
            toBindTerminal();
        } else if (BIND_TERMINAL_OK.equals(paramString)) {
            dialogShow("请刷卡或插卡");
            //开始刷卡
            statSwiper();
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
        }

    }

    @Background
    void initDialogConsume() {
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
            getEmv();
        } else {
            cardTypeValue = "021";
        }


        dialogShow("请输入密码");

        InputPinResult result = Controler.InputPin((byte) 6, (byte) 60, maskedPAN);

        if (result.commResult == (CommEnum.COMMRET.NOERROR)) {

            if (result.keyType.equals(CommEnum.POSKEYTYPE.OK)) {
                String pwd = byte2hex(result.pinBlock);
                if (TextUtils.isEmpty(pwd)) {
                    pwd = "000000";
                    if ("1".equals(cardType) || "01".equals(cardType)) {
                        cardTypeValue = "052";
                    } else {
                        cardTypeValue = "022";
                    }
                }
                initConsumeUrl(pwd);
            } else {
                toast("用户取消");
            }
        } else {
            toast("请重试");
        }

    }

    //初始化消费的url
    private void initConsumeUrl(String pwdVal) {

        if ("1".equals(cardType) || "01".equals(cardType)) {
            Controler.EndEmv();
        }
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
        if (CommonUtils.getConnectedType(SwipeWaitMoFangBlueActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitMoFangBlueActivity.this, getString(R.string.nonetwork), 1500);
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
                        localIntent.setClass(SwipeWaitMoFangBlueActivity.this, SignNameActivity.class);
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
                        ViewUtils.overridePendingTransitionCome(SwipeWaitMoFangBlueActivity.this);
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

    //查询界面需要输入的密码
    @Background
    void initDialogQuery() {

        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
            getEmv();
        } else {
            cardTypeValue = "021";
        }

        dialogShow("请输入密码");

        InputPinResult result = Controler.InputPin((byte) 6, (byte) 60, maskedPAN);
        LogUtil.syso("输入完成");
        if (result.commResult == (CommEnum.COMMRET.NOERROR)) {

            if (result.keyType.equals(CommEnum.POSKEYTYPE.OK)) {
                String msg = byte2hex(result.pinBlock);
                initUrlQuery(msg);
            } else {
                toast("用户取消");
            }
        } else {
            LogUtil.syso("密码输入结束...");
            LogUtil.syso(result.commResult.toString());


        }


    }

    //查询余额
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
                        localIntent.setClass(SwipeWaitMoFangBlueActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitMoFangBlueActivity.this);
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

    private void initUrlQuery(String pwdVal) {
        //ReadMagcardResult result1 = Controler.ReadMagcard(CommEnum.READCARDTRACK.COMBINED_N, CommEnum.PANMASK.NOMASK);
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
            Controler.EndEmv();
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


    //绑定终端后开始刷卡
    @Background
    void statSwiper() {
        workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        byte[] workKeyVaule = Util.hexStringToByteArray(workkey);
        LogUtil.syso(workkey);
        LoadWorkKeyResult resultKey = Controler.LoadWorkKey(
                CommEnum.KEYINDEX.INDEX0,
                CommEnum.WORKKEYTYPE.DOUBLEMAG,
                workKeyVaule,
                workKeyVaule.length);
        if (resultKey.commResult.equals(CommEnum.COMMRET.NOERROR)) {

            if (resultKey.loadResult) {
                LogUtil.syso("工作密钥下载成功");
            } else {
                toast("工作密钥下载失败");
                return;
            }
        } else {
            LogUtil.syso("workKey状态返回：" + resultKey.commResult);
            LogUtil.syso("workKey长度：" + workkey.length());
            LogUtil.syso("workKeyVaule长度" + workKeyVaule.length);
            toast("工作密钥传入失败");
            return;
        }

        if (Constant.QUERYBALANCE.equals(this.tradetype)) {
            result = Controler.OpenCardReader("余额查询", 0, swipTime, CommEnum.OPENCARDTYPE.COMBINED, "");
        } else {
            LogUtil.syso("传入的金额" + CommonUtils.formatMoneyToFen(formatmoney).toString());
            result = Controler.OpenCardReader("消费", CommonUtils.formatMoneyToFen(formatmoney), swipTime, CommEnum.OPENCARDTYPE.COMBINED, "");
        }

        feeRateVal = CommonUtils.formatTo8Zero(feeRate);
        if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            toast("读卡器打开失败");
            return;
        }

        //根据刷卡类型执行不同操作
        if (result.cardType.equals(CommEnum.SWIPECARDTYPE.MAGCARD)) {
            //用户刷磁条卡，读取磁条卡信息
            dialogShow("正在读取磁条卡信息...");

            ReadMagcardResult readMagResult = Controler.ReadMagcard(CommEnum.READCARDTRACK.COMBINED, CommEnum.PANMASK.NOMASK);

            if (!readMagResult.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                toast("读取磁条卡信息失败");
                return;
            }
            if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {

                //获取必要的卡信息
                ReadMagcardResult result1 = Controler.ReadMagcard(CommEnum.READCARDTRACK.COMBINED_N, CommEnum.PANMASK.NOMASK);

                String moneyVal = CommonUtils.formatTo12Zero(money);

                expiryDate = result1.sExpData;  //有效期
                randomNumber = Util.BinToHex(result1.randomdata, 0, result1.randomdata.length);
                maskedPAN = result1.sPan;  //卡号
                tract2Data = result1.sTrack2; //2磁道密文


                toast(SWIPE_OK);
            }

            //用户插入IC
        } else if (result.cardType.equals(CommEnum.SWIPECARDTYPE.ICCARD)) {


            dialogShow("正在读取IC卡信息");

            cardType = "01";
            //消费金额10.00元，禁止使用电子现金，禁止离线，执行完整EMV流程
            if (Constant.QUERYBALANCE.equals(this.tradetype)) {
                resultStartEmv = Controler.StartEmv(CommonUtils.formatMoneyToFen(formatmoney), 0, CommEnum.TRANSTYPE.FUNC_BALANCE, CommEnum.ECASHPERMIT.FORBIT, CommEnum.EMVEXECTYPE.FULL, CommEnum.FORCEONLINE.YES);
            } else if (Constant.CONSUME.equals(this.tradetype)) {
                LogUtil.syso("传入的金额" + CommonUtils.formatMoneyToFen(formatmoney).toString());
                resultStartEmv = Controler.StartEmv(CommonUtils.formatMoneyToFen(formatmoney), 0, CommEnum.TRANSTYPE.FUNC_SALE, CommEnum.ECASHPERMIT.FORBIT, CommEnum.EMVEXECTYPE.FULL, CommEnum.FORCEONLINE.YES);
            } else if (Constant.CANCEL.equals(this.tradetype)) {
                LogUtil.syso("消费撤销:" + tradetype);
                resultStartEmv = Controler.StartEmv(CommonUtils.formatMoneyToFen(formatmoney), 0, CommEnum.TRANSTYPE.FUNC_VOID_SALE, CommEnum.ECASHPERMIT.FORBIT, CommEnum.EMVEXECTYPE.FULL, CommEnum.FORCEONLINE.YES);
            }
            if (resultStartEmv.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                LogUtil.i("TAG", resultStartEmv.commResult.toString());
                if (resultStartEmv.execResult.equals(CommEnum.STARTEMVRESULT.SUCC)
                        || resultStartEmv.execResult.equals(CommEnum.STARTEMVRESULT.ONLINE)
                        || resultStartEmv.execResult.equals(CommEnum.STARTEMVRESULT.ACCEPT)) {
                    toast(SWIPE_OK);
                }else{
                    toast("IC卡读取失败，请确认插入的为芯片卡，请确认正反面");
                    Controler.EndEmv();
                }

                if (!resultStartEmv.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                    toast("StartEmv 失败:" + resultStartEmv.commResult.toDisplayName());
                    return;
                }

                if (resultStartEmv.execResult.equals(CommEnum.STARTEMVRESULT.REJECT)
                        || resultStartEmv.execResult.equals(CommEnum.STARTEMVRESULT.FAIL)) {
                    toast("IC卡执行流程出错:" + resultStartEmv.execResult.name());
                    return;
                }


            } else {
                toast("用卡出错:" + result.cardType.toDisplayName());
                return;
            }


        }
    }

    void getEmv() {
        //交易类型方式方式获取
        GetEmvDataResult getResult = Controler.GetEmvData(CommEnum.TRANSTYPE.FUNC_SALE, true);
        if (getResult.commResult.equals(CommEnum.COMMRET.NOERROR)) {


            if (getResult.pan != null) {

                randomNumber = Util.BinToHex(getResult.randomdata, 0, getResult.randomdata.length);
                maskedPAN = getResult.pan;
                pansn = CommonUtils.formatTo3Zero(getResult.pansn);
                tract2Data = getResult.track2;
                expiryDate = getResult.expiredate.substring(0, 4);
                ic55DataStr = Util.BinToHex(getResult.tlvData, 0, getResult.tlvData.length);
                LogUtil.syso("pansn:" + pansn);
                LogUtil.syso("ic55:" + ic55DataStr);

            }
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
                // String s = content;

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
                        StorageCustomerInfoUtil.putInfo(SwipeWaitMoFangBlueActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitMoFangBlueActivity.this, "workkey", workKey);
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


    private void registerServiceReceiver() {
        if (this.serviceReceiver == null) {
            this.serviceReceiver = new ServiceReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            registerReceiver(this.serviceReceiver, intentFilter);
            logger.debug("register ServiceReceiver");
        }
    }

    //点击返回按钮所做的操作
    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.ll_back:

//                if (cSwiperController != null) {
//                    thrun = false;
//                    intest = false;
//                    testtime = 0;
//                }
                ViewUtils.overridePendingTransitionBack(this);
                break;
            default:
                break;
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
                logger.error("----state:" + headsetState + "microphone"
                        + microphoneState);
                if (headsetState == 0) {
                    logger.error("未检测到耳机插入");
                    showNoDeviceDialog();

                } else if (headsetState == 1) {

                    dialogShow("正在检测设备...");
                    if (microphoneState == 1) {
                        logger.error("带麦克风的耳机插入");
                        CommonUtils.adjustAudioMax(SwipeWaitMoFangBlueActivity.this);
                    } else {
                        logger.error("无麦克风的耳机插入");
                    }
                }
            }
        }
    }

    //如果用户没有插入耳机，则显示此对话框
    private void showNoDeviceDialog() {
        ViewUtils.showChoseDialog(SwipeWaitMoFangBlueActivity.this, true, "请插入刷卡头后重试", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitMoFangBlueActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }

    /**
     * IC参数下载(AID)
     */
    void downLoadAID() {
        ICAidResult result = Controler.ICAidManage(CommEnum.ICAIDACTION.CLEAR, null);
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            String[] keys = new String[]{
                    "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101",
                    "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101",
                    "9F0608A000000333010103DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101",
                    "9F0608A000000333010106DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180101",
                    "9F0607A0000003330101DF0101009F08020020DF1105D84000A800DF1205D84000F800DF130500100000009F1B0400000000DF150400000000DF160125DF170123DF14039F3704DF180101",
                    "9F0605A000000333DF0101009F08020020DF1105D84000A800DF1205D84000F800DF130500100000009F1B0400000000DF150400000000DF160125DF170123DF14039F3704DF180101"
            };

            for (int j = 0; j < keys.length; j++) {
                String key = keys[j];
                dialogShow("正在下载IC卡公钥：(" + (j + 1) + "/" + keys.length + ")");
                result = Controler.ICAidManage(CommEnum.ICAIDACTION.ADD, CommonUtils.HexStringToByteArray(key));

                if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                    dialogShow("下载成功");
                    StorageAppInfoUtil.putInfo(this, "AID", ksn);
                } else {
                    toast(result.commResult.toDisplayName());
                    break;
                }
            }
        }
    }




}

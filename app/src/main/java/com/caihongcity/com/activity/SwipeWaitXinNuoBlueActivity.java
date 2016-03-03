package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.Intent;
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
import com.itron.android.ftf.Util;
import com.xino.minipos.pub.BluetoothCommEnum;
import com.xino.minipos.pub.BluetoothControler;
import com.xino.minipos.pub.ConnectPosResult;
import com.xino.minipos.pub.ExecutEmvResult;
import com.xino.minipos.pub.InputPinResult;
import com.xino.minipos.pub.LoadWorkKeyResult;
import com.xino.minipos.pub.OpenCardReaderResult;
import com.xino.minipos.pub.ReadMagcardResult;
import com.xino.minipos.pub.ReadPosInfoResult;

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

import static com.itron.android.lib.TypeConversion.byte2hex;

@EActivity(R.layout.swipewait_layout)
public class SwipeWaitXinNuoBlueActivity extends BaseActivity {

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
    private Long amount;
    private ExecutEmvResult resultStart;
    private String pansn; //卡片序列号
    private String pwd;   // 密码
    private byte[] tlv;   //原始tlv数据
    private String ic55DataStr; //ic55域数据


    @AfterViews
    void initData() {
        AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        dialogShow("正在检测设备...");
        formatmoney = CommonUtils.format(money); //将传入的金额格式化
        feeRateVal = CommonUtils.formatTo8Zero(feeRate); //将传入的费率格式化
        //初始化连接
        initConn();



    }

    private String getTradeType(String tradetype) {
        if (tradetype.equals(Constant.QUERYBALANCE)) {
            return "查询余额";
        } else {
            return "消费";
        }
    }


    void getKSN() {
        ReadPosInfoResult result = BluetoothControler.ReadPosInfo(this);
        if (result.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR)) {
            ksn = result.sn;
            toast("GET_KSN_OK");
            LogUtil.syso("ksn==" + ksn);
        } else {
//            BluetoothControler.disconnectPos(this);
            toast("读取设备信息失败");
        }
    }

    @Background
    void initConn() {
         BluetoothControler.ResetPos(this);

//        断开原来的连接
//        if(BluetoothControler.posConnected(this)) {
//            BluetoothControler.disconnectPos(this);
//        }
        ConnectPosResult rc = BluetoothControler.connectPos(this, blue_address);
        if (rc.bConnected) {
            StorageAppInfoUtil.putInfo(this, "bluetooth_address", blue_address);
            dialogShow("设备连接成功");
//            BluetoothControler.ResetPos(this);
            //获取ksn
            getKSN();
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
//          写入workKey
            writeWorkKey();
        } else if (WORK_KEY_OK.equals(paramString)) {
//            dialogShow("正在打开读卡器...");
            dialogShow("请插卡或刷卡");
//          打开读卡器
            openReaderCard();
        } else if (INPUT_PWD_OK.equals(paramString)) {

            dialogShow("密码输入完成，请稍后...");
            if ((Constant.CONSUME.equals(this.tradetype)) || (Constant.CANCEL.equals(this.tradetype))) {
                initConsumeUrl(pwd);
            } else if (Constant.QUERYBALANCE.equals(this.tradetype)) {
                initQueryUrl(pwd);
            }
        } else if (SWIPE_OK.equals(paramString)) {
            dialogShow("请输入密码");
            initPassWord();
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

    @Background
    void initPassWord() {
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        dialogShow("请输入密码");
        InputPinResult result = BluetoothControler.InputPin(this, (byte) 6, (byte) 60, sPan);
        if (result.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR)) {
            pwd = byte2hex(result.pinBlock);
            if (TextUtils.isEmpty(pwd)) {
                pwd = "000000";
                if ("1".equals(cardType) || "01".equals(cardType)) {
                    cardTypeValue = "052";
                } else {
                    cardTypeValue = "022";
                }
            }

            toast(INPUT_PWD_OK);
        } else {
            toast("用户取消");
        }


    }

    @Background
    void initConsumeUrl(String pwd) {
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
        if (CommonUtils.getConnectedType(SwipeWaitXinNuoBlueActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitXinNuoBlueActivity.this, getString(R.string.nonetwork), 1500);
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
                        localIntent.setClass(SwipeWaitXinNuoBlueActivity.this, SignNameActivity.class);
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
                        ViewUtils.overridePendingTransitionCome(SwipeWaitXinNuoBlueActivity.this);
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

    @Background
    void initQueryUrl(String pwd) {
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
                        localIntent.setClass(SwipeWaitXinNuoBlueActivity.this, QueryBalancceResultActivity.class);
                        startActivity(localIntent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitXinNuoBlueActivity.this);
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
        LogUtil.d("SwipeWaitMoFangActivity", "url==" + url);
        return;

    }


    void startSwipe() {
        terminal = StorageCustomerInfoUtil.getInfo("terminal", this);
        customer = StorageCustomerInfoUtil.getInfo("customerNum", this);
        if ("1".equals(cardType)) {
            String type = getTradeType(tradetype);
            dialogShow("请输入密码");
            if (type.equals("查询余额")) {
                resultStart = BluetoothControler.ExecutEmv(this, amount, 0, BluetoothCommEnum.TRANSTYPE.FUNC_BALANCE, BluetoothCommEnum.ECASHPERMIT.FORBIT, BluetoothCommEnum.EMVEXECTYPE.FULL, BluetoothCommEnum.FORCEONLINE.YES, null, 6, 60);
            } else {
                resultStart = BluetoothControler.ExecutEmv(this, amount, 0, BluetoothCommEnum.TRANSTYPE.FUNC_SALE, BluetoothCommEnum.ECASHPERMIT.FORBIT, BluetoothCommEnum.EMVEXECTYPE.FULL, BluetoothCommEnum.FORCEONLINE.YES, null, 6, 60);
            }
            if (resultStart.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR)) {
                if (resultStart.emvResult == true) {
                    sPan = resultStart.tracksResult.pan;
                    sTrack2 = resultStart.tracksResult.Track2;
                    sExpData = resultStart.tracksResult.expridDate.substring(0, 4);
//                    pansn = resultStart.tracksResult.panserial;
                    pansn = CommonUtils.formatTo3Zero(resultStart.tracksResult.panserial);
                    tlv = resultStart.dataResult.tlvData;
                    ic55DataStr = Util.BinToHex(tlv, 0, tlv.length);
                    cardTypeValue = "051";
                    pwd = byte2hex(resultStart.pinResult.pinBlock);
                    if (TextUtils.isEmpty(pwd)) {
                        pwd = "000000";
                        if ("1".equals(cardType) || "01".equals(cardType)) {
                            cardTypeValue = "052";
                        }
                    }
                    toast(INPUT_PWD_OK);


                } else {
                    toast("读取IC卡失败");
                }
            }


        } else if ("2".equals(cardType)) {
            ReadMagcardResult result = BluetoothControler.ReadMagcard(this, BluetoothCommEnum.READCARDTRACK.COMBINED,
                    BluetoothCommEnum.PANMASK.NOMASK);
            LogUtil.syso("result.commResult2:" + result.commResult);
            LogUtil.syso("result.readResult2:" + result.readResult);
            if (result.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR) && result.readResult == true) {
                sPan = result.sPan;
                sExpData = result.sExpData;
                if (result.sTrack2.length() > 0) {
                    sTrack2 = result.sTrack2;
                }
                toast(SWIPE_OK);
                LogUtil.syso("卡号：" + sPan + ";" + "有效期" + sExpData + ";" + "二磁道数据:" + sTrack2);
            } else {
                toast("读取磁条卡失败");
            }

        }

    }


    @Background
    void openReaderCard() {
        amount = CommonUtils.formatMoneyToFen(formatmoney);
        String type = getTradeType(tradetype);
        OpenCardReaderResult result = BluetoothControler.OpenCardReader(this, type, amount, 60, BluetoothCommEnum.OPENCARDTYPE.COMBINED, "开启读卡器");
        LogUtil.syso("result.commResult:" + result.commResult.toString());
        if (result.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR)) {

            switch (result.cardType) {
                case USERCACEL:
                    toast("用户取消");
                    break;
                case MAGCARD:
                    dialogShow("正在读取磁条卡");
                    cardType = "2";
                    break;
                case ICCARD:
                    dialogShow("正在读取IC卡");
                    cardType = "1";

                    break;
            }
        } else {
            toast("打开读卡器失败");
        }

        startSwipe();
    }


    @Background
    void writeWorkKey() {
        workkey = StorageCustomerInfoUtil.getInfo("workkey", this);
        if(workkey.length()>=80){
            workkey = workkey.substring(0,80)+ "0000000000000000000000000000000000000000";
        }
        byte[] workKeyVaule = Util.hexStringToByteArray(workkey);
        LogUtil.syso(workkey);
        LoadWorkKeyResult result = BluetoothControler.LoadWorkKey(
                this,
                BluetoothCommEnum.KEYINDEX.INDEX0,
                BluetoothCommEnum.WORKKEYTYPE.DOUBLEMAG,
                workKeyVaule,
                workKeyVaule.length);

        if (result.commResult.equals(BluetoothCommEnum.BLUETOOTHCOMMRET.NOERROR)
                && result.loadResult) {
            LogUtil.syso("workKey下载成功");
            toast(WORK_KEY_OK);

        } else {
            toast("工作密钥下载失败");
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
                        StorageCustomerInfoUtil.putInfo(SwipeWaitXinNuoBlueActivity.this, "terminal", terminal);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitXinNuoBlueActivity.this, "workkey", workKey);
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

                dialogShow("请稍等...");
            }
        });
        LogUtil.d("SwipeWaitXinNuoBlueActivity", "url===" + url);
        localMyAsyncTask.execute(url);
    }


    @UiThread
    void dialogShow(String s) {
        if (this.dialog != null)
            this.dialog.dismiss();
        if (this.loadingDialog != null)
            this.loadingDialog.dismiss();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitXinNuoBlueActivity.this, s, true);
        dialog.show();

    }


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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothControler.ResetPos(this);
        disConn();

    }





    void disConn() {
        //        断开原来的连接
        if (BluetoothControler.posConnected(this)) {
            BluetoothControler.disconnectPos(this);

        }


    }
}

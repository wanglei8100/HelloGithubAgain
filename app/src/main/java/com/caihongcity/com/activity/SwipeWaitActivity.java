package com.caihongcity.com.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.model.SerializableMap;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.EncryptUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.utils.ViewUtils.OnChoseDialogClickCallback;
import com.caihongcity.com.view.CustomDialog;
import com.itron.android.ftf.Util;
import com.itron.android.lib.Logger;
import com.itron.cswiper4.CSwiper;
import com.itron.cswiper4.CSwiperStateChangedListener;
import com.itron.cswiper4.DecodeResult;
import com.itron.protol.android.BLECommandController;
import com.itron.protol.android.TransactionDateTime;
import com.itron.protol.android.TransactionInfo;
import com.itron.protol.android.TransationCurrencyCode;
import com.itron.protol.android.TransationNum;
import com.itron.protol.android.TransationTime;
import com.itron.protol.android.TransationType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yuanjigong
 *         刷卡等待界面
 */
public class SwipeWaitActivity extends BaseActivity implements OnClickListener {
    private StartCSwiperThread getEncCardThread;
    int testtime = 1, time, ttime;
    int oktime, boktime;
    boolean thrun = false;
    boolean saveRecord = false;
    boolean intest = false;
    boolean instopthread = false;
    boolean inKsntest = false;
    public CSwiper cSwiperController;
    CSwiperListener cSwiperListener;
    ProgressDialog btDialog = null;
    private Dialog myDialog;

    Logger logger = Logger.getInstance(SwipeWaitActivity.class);
    private EditText edt_content;
    private static final String BIND_STATUS = "1";
    private static final String REPONSE_STATUS = "00";
    private static final String REPEAT_BIND = "94";//重复绑定标志
    private static final String TAG = "SwipeWaitActivity";
    private String ksn;
    private BLECommandController itcommm;
    private String money;
    HashMap<String, Object> map = null;
    private String password;
    View view = null;
    private String tradeType, isT0, feeRate, topFeeRate = "";
    private Dialog dialog;
    private QueryModel queryModel;
    private int swipTime = 60;//等待刷卡时间
    private ServiceReceiver serviceReceiver;
    private int tryNumber;//获取KSN的次数
    private ImageView swipe_flash;
    private int tag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipewait_layout);
        swipe_flash = (ImageView) findViewById(R.id.swipe_flash);
       AnimationDrawable animationDrawable = (AnimationDrawable) swipe_flash.getBackground();
        animationDrawable.start();
        registerServiceReceiver();
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean isAudioConnect = mAudioManager.isWiredHeadsetOn();
        if (!isAudioConnect) {
            showNoDeviceDialog();
            return;
        }
        logger.setDebug(true);
        tryNumber = 3;
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("检测刷卡器");
        edt_content = (EditText) findViewById(R.id.edt_content);
        String bindStatus = StorageCustomerInfoUtil.getInfo("isbind", this);
        tradeType = getIntent().getStringExtra("tradetype");
        feeRate = getIntent().getStringExtra("feeRate");
        isT0 = getIntent().getStringExtra("isT0");
        topFeeRate = getIntent().getStringExtra("topFeeRate");
        queryModel = (QueryModel) getIntent().getSerializableExtra("queryModel");
        money = getIntent().getStringExtra("money");
        map = new HashMap<String, Object>();
        dialog = ViewUtils.createLoadingDialog(SwipeWaitActivity.this, "正在检测设备...", true);
        dialog.show();
        //创建刷卡器对象及注册监听器，绑定终端及检测是否绑定
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                cSwiperListener = new CSwiperListener();
                cSwiperController = CSwiper.GetInstance(SwipeWaitActivity.this, cSwiperListener);
            }
        };
        thread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //连续刷卡测试
    private void getEncCard() {
        if (!thrun) {
            thrun = true;
            getEncCardThread = new StartCSwiperThread();
            getEncCardThread.start();
        } else {
            LogUtil.d(TAG,"线程已经启动");
        }
    }

    private Timer timer;
    private Dialog loadingDialogParseCardInfo;
    /**
     * 回调更新界面
     */
    public Handler updateUI = new Handler() {
        public void handleMessage(Message msg) {
            LogUtil.e("UI:" + (String) msg.obj);
            edt_content.setText((String) msg.obj);
            switch (msg.what) {
                case 1:

                    break;
                case 2:
                    String terminal = StorageCustomerInfoUtil.getInfo("terminal", SwipeWaitActivity.this);
                    LogUtil.syso("terminal==" + terminal);
                    SerializableMap tmpmap = new SerializableMap();
                    tmpmap.setMap(map);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("trackinfo", tmpmap);
                    if (timer != null) {
                        timer.cancel();
                    }
                    ((TextView) findViewById(R.id.tv_title_des)).setText("请输入密码");
                    if (Constant.CONSUME.equals(tradeType) || Constant.CANCEL.equals(tradeType)) {
                        initDialog();
                    } else if (Constant.QUERYBALANCE.equals(tradeType)) {
                        initDialogQuery();
                    }
                    break;
                case 3:
                    if(msg.obj != null) {
                        if(msg.obj.toString().equals("密码错误")){
                            tag = 1;
                            toBindTerminal();
                            ViewUtils.makeToast(SwipeWaitActivity.this,
                                    msg.obj.toString(), 1500);

                        }else{
                            ViewUtils.makeToast(SwipeWaitActivity.this,
                                    msg.obj.toString(), 1500);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
                        }
                    }else{
                        ViewUtils.makeToast(SwipeWaitActivity.this,
                                "未知错误", 1500);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
                    }

                    break;
                case 4:

                    break;
                case 5:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    ViewUtils.makeToast(SwipeWaitActivity.this,"刷卡器正常，请刷/插卡",1500);
                    timer = new Timer();

                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            if (swipTime < 1) {
                                updateUI.sendMessage(updateUI.obtainMessage(8, "交易超时，请重试"));
                                timer.cancel();
                            } else {
                                swipTime--;
                                LogUtil.i(TAG, "swipTime:" + swipTime);
                            }
                        }
                    }, 1000, 1000);
                    break;
                case 6:
                    StorageCustomerInfoUtil.putInfo(SwipeWaitActivity.this, "isbind", BIND_STATUS);
                    getEncCard();
                    break;
                case 7:
                    //检查网络状态
                    if (CommonUtils.getConnectedType(SwipeWaitActivity.this) == -1) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        ViewUtils.makeToast(SwipeWaitActivity.this, getString(R.string.nonetwork), 1500);
                        return;
                    }


                    new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(800);//睡会儿，等待cSwiperController对象创建出来
                                if (TextUtils.isEmpty(ksn)) {
                                    if (cSwiperController == null) {
                                        updateUI.sendMessage(updateUI.obtainMessage(3, "请开启录音权限后重试"));
                                        return;
                                    }
                                    ksn = cSwiperController.getKSN();
                                    LogUtil.i(TAG, "ksn:" + ksn);
                                    if (TextUtils.isEmpty(ksn)) {
                                        if (tryNumber <= 0) {
                                            updateUI.sendMessage(updateUI.obtainMessage(3, "无法获取设备SN"));
                                            return;
                                        } else {
                                            updateUI.sendMessage(updateUI.obtainMessage(7, tryNumber-- + ""));
                                        }
                                    }else {
                                        toBindTerminal();
                                    }
                                }else {
                                    toBindTerminal();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;

                case 8:
                    ViewUtils.makeToast(SwipeWaitActivity.this, msg.obj.toString(), 1000);
                    ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
                    break;
                case 9:
                    loadingDialogParseCardInfo = ViewUtils.createLoadingDialog(SwipeWaitActivity.this, "刷卡成功,解析中...", true);
                    loadingDialogParseCardInfo.show();
                    break;

                default:
                    break;
            }
            LogUtil.e("testtime=" + testtime + "        time=" + time + "        thrun=" + thrun);
        }
    };
    private String voucherNo_Value_;
    private String batchNo_Value_;

    public void toBindTerminal() {
        String phoneNum = StorageCustomerInfoUtil.getInfo("phoneNum", this);
        String macData = "0700" + phoneNum + "190958" + Constant.VERSION + ksn + Constant.mainKey;
        LogUtil.d(TAG, "macData==" + macData);
        String url = Constant.REQUEST_API + "0=0700&1=" + phoneNum + "&3=190958&62=" + ksn + "&59=" + Constant.VERSION + "&64=" + CommonUtils.Md5(macData);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {

            @Override
            public void isLoadingContent() {
                LogUtil.d(TAG, "isLoadingContent()===");
            }

            @Override
            public void isLoadedContent(String content) {
                LogUtil.d(TAG, "content==" + content);
                //{"0":"0700","1":"15555808380","3":"190958","64":"802E67F4AAE371851980B539AB25D498","39":"94","41":"99977411",
                //"62":"56F1027B66A9C882DE6120C067EF4E4EFA194FCA60C5EE1D58821D14B4DEC889759FE867092056F1027B66A9C882DE6120C067EF4E4EFA194F"}
                if (StringUtil.isEmpty(content)) {
                    updateUI.sendMessage(updateUI.obtainMessage(3, getString(R.string.server_error)));
                    return;
                }
                JSONObject obj = null;
                try {
                    obj = new JSONObject(content);
                    String bindStatus = (String) obj.get("39");
                    if (obj.has("11")) {
                        int voucherNo_Value_int = Integer.valueOf(obj.getString("11"));
                        voucherNo_Value_ = CommonUtils.formatTo6Zero(String.valueOf(voucherNo_Value_int + 1));
                    }
                    if (obj.has("60")) {
                        batchNo_Value_ = obj.getString("60");
                    }
                    String resultValue = MyApplication.getErrorHint(bindStatus);
                    if (REPONSE_STATUS.equals(bindStatus) || REPEAT_BIND.equals(bindStatus)) {
                        String workKey = (String) obj.get("62");
                        String terminal = obj.getString("41");
                        LogUtil.syso("workKey get=====" + workKey);
                        StorageCustomerInfoUtil.putInfo(SwipeWaitActivity.this, "terminal", terminal);
                        String workKey_ = "A288542EDFAA2C4092E74D9F37DF2A66DB4A03F0C0E7989F6CB3A1EDFAA95ADDBEED81215D22A288542EDFAA2C4092E74D9F37DF2A66DB4A03";
                        StorageCustomerInfoUtil.putInfo(SwipeWaitActivity.this, "workkey", workKey);
                        if(tag == 1){
                            updateUI.sendMessage(updateUI.obtainMessage(2, ""));
                            tag = 0;
                            return;
                        }
                        Message msg = Message.obtain();
                        msg.what = 6;
                        updateUI.sendMessage(msg);
                    } else {
                        updateUI.sendMessage(updateUI.obtainMessage(3, resultValue));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        LogUtil.d(TAG, "url===" + url);
        myAsyncTask.execute(url);
    }


    class StartCSwiperThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            oktime = 0;
            boktime = 0;
            for (time = 0; time < testtime; time++) {
                if (thrun) {
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    intest = true;
                    try {
                        LogUtil.e("start cswiper  " + cSwiperController);
                        ttime = time;

                        TransactionInfo info = new TransactionInfo();
                        // 设置交易日期 格式: YYMMDD
                        TransactionDateTime dateTime = new TransactionDateTime();
                        String data=  CommonUtils.getTime("yyMMdd");
                        dateTime.setDateTime(data);
                        // 设置交易时间   hhssmm
                        TransationTime time = new TransationTime();
                        String tranTime =  CommonUtils.getTime("HHmmss");
                        time.setTime(tranTime);
                        // 设置货币代码
                        TransationCurrencyCode currencyCode = new TransationCurrencyCode();

                        currencyCode.setCode("0156");
                        // 设置交易流水号
                        TransationNum num = new TransationNum();
                        num.setNum(voucherNo_Value_);
                        // 设置交易类型  00 消费
                        TransationType type = new TransationType();
                        if (Constant.CONSUME.equals(tradeType)) {
                            type.setType("00");
                        } else if (Constant.QUERYBALANCE.equals(tradeType)) {
                            type.setType("31");
                        } else {
                            type.setType("20");
                        }
LogUtil.e("TransactionInfo====","data "+data+" tranTime "+tranTime+" voucherNo_Value_ "+voucherNo_Value_+" money "+CommonUtils.formatMoneyToFen(money).toString());

                        info.setDateTime(dateTime);
                        info.setCurrencyCode(currencyCode);
                        info.setNum(num);
                        info.setTime(time);
                        info.setType(type);
                        Random random = new Random();
                        int randomInt = random.nextInt(3);
                        String randomStr = String.valueOf(randomInt);
                        String byte0 = "10000010";
                        String byte1 = "10000010";
                        String byte2 = "00000000";

                        int flag0 = Util.binaryStr2Byte(byte0);
                        int flag1 = Util.binaryStr2Byte(byte1);
                        int flag2 = Util.binaryStr2Byte(byte2);
                        if (Constant.QUERYBALANCE.equals(tradeType)) {
                            money = "000000000000";
                        }
                        byte[] flags = new byte[]{(byte) flag0, (byte) flag1, (byte) flag2, 0x00};
                        cSwiperController.statEmvSwiper((byte) 0,
                                flags,
                                randomStr.getBytes(), CommonUtils.formatMoneyToFen(money).toString(),
                                randomStr.getBytes(), 50, info);
//							cSwiperController.startCSwiper();
                        intest = false;

                    } catch (IllegalStateException e) {
//							updateUI.obtainMessage(1, "请在IDLE状态操作此命令");
                        intest = false;
                    }
                    while (intest) {

                    }
                    LogUtil.e("NEXT TEST");
                }

            }

            if (!instopthread) {
                LogUtil.e("FINISHN CARD");
//					cSwiperController.deleteCSwiper();
            }
            thrun = false;
        }
    }


    class CSwiperListener implements CSwiperStateChangedListener {
        public void onCardSwipeDetected() {
            LogUtil.e("CSwiperListener 用户已刷卡");
            updateUI.sendMessage(updateUI.obtainMessage(9, "IC卡插入，请勿拔出"));
        }

        @Override
        public void onInterrupted() {
            LogUtil.e("CSwiperListener 用户中断操作");
        }

        @Override
        public void onNoDeviceDetected() {
            LogUtil.e("CSwiperListener 未检测到刷卡设备");
        }

        @Override
        public void onTimeout() {
            LogUtil.e("CSwiperListener time out err");
        }

        @Override
        public void onDecodingStart() {
            LogUtil.e("CSwiperListener 开始解码");
        }

        @Override
        public void onWaitingForCardSwipe() {
            // TODO Auto-generated method stub
            // edt_state.setText("找到刷卡设备等待刷卡");
            LogUtil.e("CSwiperListener 找到刷卡设备等待刷卡");
            //edt_content.setText("请刷卡");
            updateUI.sendMessage(updateUI.obtainMessage(5, "请刷卡或者插卡"));
        }

        @Override
        public void onWaitingForDevice() {
            // TODO Auto-generated method stub
            LogUtil.e("CSwiperListener 查找设备中...");
            // edt_state.setText("查找设备中.....");
            updateUI.sendMessage(updateUI.obtainMessage(1, "查找设备中..."));
        }

        @Override
        public void onDevicePlugged() {
            // TODO Auto-generated method stub
            LogUtil.e("CSwiperListener 刷卡器插入手机");
            updateUI.sendMessageDelayed(updateUI.obtainMessage(7, "设备插入"), 100);

        }

        @Override
        public void onDeviceUnplugged() {
            if (dialog != null) {
                dialog.dismiss();
            }
            showNoDeviceDialog();
            updateUI.sendMessage(updateUI.obtainMessage(1, "设备拔出"));
            thrun = false;
        }

        @Override
        public void onDecodeCompleted(String formatID, String ksn,
                                      String encTracks, int track1Length, int track2Length,
                                      int track3Length, String randomNumber, String maskedPAN,
                                      String pan,
                                      String expiryDate, String cardHolderName, String mac
                , int cardType, byte[] cardSeriNo, byte[] ic55Data) {
            LogUtil.e("CSwiperListener 刷卡返回数据");
            oktime++;
            boktime++;
            String cardSeriNoStr = "";
            String ic55DataStr = "";
            if (cardSeriNo != null) {
                cardSeriNoStr = Util.BinToHex(cardSeriNo, 0, cardSeriNo.length);
            }

            if (ic55Data != null) {
                ic55DataStr = Util.BinToHex(ic55Data, 0, ic55Data.length);
            }
            StringBuffer strb = new StringBuffer();
            strb.append("设备型号:" + getDeviceName() + "\n" + "系统版本:" + getDeviceOS() + "\n");
            strb.append("通讯成功/总数: " + oktime + "/" + (ttime + 1) + "\n");
            strb.append("刷卡成功/总数: " + boktime + "/" + (ttime + 1) + "\n");
            strb.append("formatID:" + formatID + "\n");
            strb.append("ksn:" + ksn + "\n");
            strb.append("track1Length :" + track1Length + "\n");
            strb.append("track2Length:" + track2Length + "\n");
            strb.append("track3Length:" + track3Length + "\n");
            strb.append("encTracks:" + encTracks + "\n");
            strb.append("randomNumber: " + randomNumber + "\n");
            strb.append("maskedPAN :" + maskedPAN + "\n");
            strb.append("pan :" + pan + "\n");
            strb.append("expiryDate:" + expiryDate + "\n");
            strb.append("cardHolderName : " + cardHolderName + "\n");
            strb.append("mac: " + mac + "\n");
            strb.append("cardType: " + cardType + "\n");
            strb.append("cardSeriNo: " + cardSeriNoStr + "\n");
            strb.append("ic55Data: " + ic55DataStr);
            map.put("ksn", ksn);
            map.put("track1Length", track1Length + "");
            map.put("track2Length", track2Length + "");
            map.put("track3Length", track3Length + "");
            map.put("encTracks", encTracks);
            map.put("randomNumber", randomNumber);
            map.put("maskedPAN", maskedPAN);
            map.put("pan", pan);
            map.put("expiryDate", expiryDate);
            map.put("cardHolderName", cardHolderName);
            map.put("mac", mac);
            map.put("cardType", cardType + "");
            map.put("cardSeriNoStr", cardSeriNoStr);
            map.put("ic55DataStr", ic55DataStr);
            LogUtil.d(TAG, strb.toString());
            updateUI.sendMessage(updateUI.obtainMessage(2, strb.toString()));
            if (btDialog != null)
                btDialog.dismiss();
            if (loadingDialogParseCardInfo != null)
                loadingDialogParseCardInfo.dismiss();
        }


        @Override
        public void onError(int errcode, String paramString) {
            // TODO Auto-generated method stub
            // edt_state.setText(paramString);
            logger.debug("CSwiperListener" + paramString);
            updateUI.sendMessage(updateUI.obtainMessage(1, paramString));
        }

        @Override
        public void onDecodeError(DecodeResult paramDecodeResult) {
            // TODO Auto-generated method stub
            logger.debug("CSwiperListener" + paramDecodeResult);
            if (paramDecodeResult != DecodeResult.DECODE_SWIPE_FAIL)
                cSwiperController.stopCSwiper();
            intest = false;
            String cardresult = "设备型号:" + getDeviceName() + "\n" + "系统版本:" + getDeviceOS() + "\n";
            if (paramDecodeResult == DecodeResult.DECODE_SWIPE_FAIL) {
                oktime++;
                cardresult += "通讯成功/总数: " + oktime + "/" + (ttime + 1) + "\n" + "刷卡成功/总数: " + boktime + "/" + (ttime + 1) + "\n";
                updateUI.sendMessage(updateUI.obtainMessage(1, cardresult + "请重新刷卡"));
            }
            if (paramDecodeResult == DecodeResult.DECODE_CRC_ERROR) {
                if (!inKsntest) {
                    cardresult += "通讯成功/总数: " + oktime + "/" + (ttime + 1) + "\n" + "刷卡成功/总数: " + boktime + "/" + (ttime + 1) + "\n";
                    updateUI.sendMessage(updateUI.obtainMessage(1, cardresult + "校验和错误"));
                }
            }
            if (paramDecodeResult == DecodeResult.DECODE_UNKNOWN_ERROR) {
                cardresult += "通讯成功/总数: " + oktime + "/" + (ttime + 1) + "\n" + "刷卡成功/总数: " + boktime + "/" + (ttime + 1) + "\n";
                updateUI.sendMessage(updateUI.obtainMessage(1, cardresult + "未知错误"));
            }
            if (paramDecodeResult == DecodeResult.DECODE_COMM_ERROR) {
                cardresult += "通讯成功/总数: " + oktime + "/" + (ttime + 1) + "\n" + "刷卡成功/总数: " + boktime + "/" + (ttime + 1) + "\n";
                updateUI.sendMessage(updateUI.obtainMessage(1, cardresult + "通讯错误"));
            }
        }

        @Override
        public void onICResponse(int result, byte[] resuiltScript, byte[] data) {
            String resultScriptStr = "";
            String dataStr = "";
            if (resuiltScript != null) {
                resultScriptStr = Util.BinToHex(resuiltScript, 0, resuiltScript.length);
            }
            if (data != null) {
                dataStr = Util.BinToHex(data, 0, data.length);
            }
            updateUI.sendMessage(updateUI.obtainMessage(1, "result:" + result +
                    "\nresuiltScript:" + resultScriptStr +
                    "\ndata:" + dataStr));
        }

        @Override
        public void EmvOperationWaitiing() {
            updateUI.sendMessage(updateUI.obtainMessage(9, "IC卡插入，请勿拔出"));

        }

    }

    private void showNoDeviceDialog() {
        ViewUtils.showChoseDialog(SwipeWaitActivity.this, true, "请插入刷卡头后重试", View.GONE, new OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {
                ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
            }

            @Override
            public void clickCancel() {
            }
        });
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        } else {
            return manufacturer.toUpperCase() + " " + model.toUpperCase();
        }
    }

    private static String getDeviceOS() {

        String androidVersion = Build.VERSION.RELEASE;
        return androidVersion.toUpperCase();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cSwiperController != null) {
            cSwiperController.deleteCSwiper();
            cSwiperController = null;
        }
        if (timer != null) {
            timer.cancel();
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        if (customDialog != null) {
            customDialog.dismiss();
        }

        unregisterServiceReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (cSwiperController != null)
                    thrun = false;
                intest = false;
                testtime = 0;
                cSwiperController.deleteCSwiper();
                cSwiperController = null;
                this.finish();
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.ll_back:
                if (cSwiperController != null) {
                    thrun = false;
                    intest = false;
                    testtime = 0;
                }
                ViewUtils.overridePendingTransitionBack(this);
                break;
            default:
                break;
        }
    }

    private CustomDialog customDialog;
    private CustomDialog.InputDialogListener inputDialogListener;
    TextView tv_money;
    TextView tv_bank_card_number;
    String formatmoney;

    /**
     * init Dialog
     */
    private void initDialog() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String maskedPAN = (String) map.get("maskedPAN");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        formatmoney = CommonUtils.format(money);
        customDialog = new CustomDialog(SwipeWaitActivity.this, R.style.mystyle, R.layout.customdialog, "￥ " + formatmoney, maskedPANValue);
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
                ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    String cardTypeValue;

    private void initUrl(String pwdVal) {

        String tractData = (String) map.get("encTracks");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("maskedPAN");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSeriNoStr = CommonUtils.formatTo3Zero((String) map.get("cardSeriNoStr"));
        int track1Length = Integer.parseInt((String) map.get("track1Length"));
        int track2Length = Integer.parseInt((String) map.get("track2Length"));
        String tract1Data = tractData.substring(track1Length, track2Length * 2);
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
        if (Constant.CONSUME.equals(tradeType)) {//消费
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
        requestData.put(23,cardSeriNoStr);
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
        if (Constant.CONSUME.equals(tradeType)) {//消费
            requestData.put(9, feeRateVal);
        } else {//撤销
            requestData.put(61, terminalBatchNo + termianlVoucherNo);
        }
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        //检查网络状态
        if (CommonUtils.getConnectedType(SwipeWaitActivity.this) == -1) {
            ViewUtils.makeToast(SwipeWaitActivity.this, getString(R.string.nonetwork), 1500);
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
                    updateUI.sendMessage(updateUI.obtainMessage(3, getString(R.string.server_error)));
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
                        intent.setClass(SwipeWaitActivity.this, SignNameActivity.class);
                        intent.putExtra("tradeType", tradeType);
                        intent.putExtra("cardNo", maskedPAN);
                        intent.putExtra("voucherNo", voucherNo);
                        intent.putExtra("voucherNo37", voucherNo37);
                        intent.putExtra("money", money);
                        intent.putExtra("feeRate", feeRate);
                        intent.putExtra("topFeeRate", topFeeRate);
                        startActivity(intent);
                        SwipeWaitActivity.this.finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitActivity.this);

                    } else {
                        updateUI.sendMessage(updateUI.obtainMessage(3, resultValue));
                        return;
                    }
                } catch (JSONException e) {
                    updateUI.sendMessage(updateUI.obtainMessage(3, getString(R.string.server_error)));
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d(TAG, "url==" + url);
    }

    /**
     * init Dialog
     */
    private void initDialogQuery() {
        final String cardType = (String) map.get("cardType");
        if ("1".equals(cardType) || "01".equals(cardType)) {
            cardTypeValue = "051";
        } else {
            cardTypeValue = "021";
        }
        String maskedPAN = (String) map.get("maskedPAN");
        String maskedPANValue = maskedPAN.replace(maskedPAN.subSequence(6, maskedPAN.length() - 4), "****");
        customDialog = new CustomDialog(SwipeWaitActivity.this, R.style.mystyle, R.layout.customdialog, "余额查询", maskedPANValue);
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
                ViewUtils.overridePendingTransitionBack(SwipeWaitActivity.this);
            }
        };
        customDialog.setListener(inputDialogListener);
        customDialog.show();
    }

    private void initUrlQuery(String pwdVal) {
        String tractData = (String) map.get("encTracks");
        String randomNumber = (String) map.get("randomNumber");
        String maskedPAN = (String) map.get("maskedPAN");
        String expiryDate = (String) map.get("expiryDate");
        String ic55DataStr = (String) map.get("ic55DataStr");
        String cardSeriNoStr = CommonUtils.formatTo3Zero((String) map.get("cardSeriNoStr"));
        int track1Length = Integer.parseInt((String) map.get("track1Length"));
        int track2Length = Integer.parseInt((String) map.get("track2Length"));
        String tract1Data = tractData.substring(track1Length, track2Length * 2);

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
        String forpinkey = CommonUtils.Md5(pwdVal
                + StorageCustomerInfoUtil.getInfo("pinkey", this));
        String moneyVal = CommonUtils.formatTo12Zero(money);
        String feeRateVal = CommonUtils.formatTo8Zero(feeRate);
//        String forMd5Data = "0200" + maskedPAN + "310000" + feeRateVal + voucherNo_Value_
//                + expiryDate + cardTypeValue + "12" + tract1Data.toUpperCase()
//                + terminal + customer + "156" + pinbyteHex.toUpperCase()
//                + randomNumber + Constant.VERSION + "01" + batchNo_Value_ + "003";
//        LogUtil.syso("forMd5Data====" + forMd5Data);
//        String data = "0=0200&2=" + maskedPAN + "&3=310000" + "&9=" + feeRateVal + "&11="
//                + voucherNo_Value_ + "&14=" + expiryDate + "&22=" + cardTypeValue + "&26=12" + "&35="
//                + tract1Data.toUpperCase() + "&41=" + terminal + "&42="
//                + customer + "&49=156" + "&52=" + pinbyteHex.toUpperCase()
//                + "&53="
//                + randomNumber// 随机密钥
//                + "&59=" + Constant.VERSION
//                + "&60=01" + batchNo_Value_ + "003" + "&64="
//                + CommonUtils.Md5(forMd5Data + Constant.mainKey).toUpperCase();
//        String url = Constant.REQUEST_API + data;//0=0200&2=6225760009310363&3=000000&11=&14=1507&22=022&26=12&35=49B9DB7CE8D6C9E9E57FA7ED0E44C1B8B4A208F3DAE8C406&41=99977411&42=220558015061077&49=156&52=040777000103&53=2197D5BC00000008&60=01null003&64=A8D097DBDC969D45A96F092D5C02CAA1
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0200");
        requestData.put(2, maskedPAN);
        requestData.put(3, "310000");
        requestData.put(4, moneyVal);
        requestData.put(9, feeRateVal);
        requestData.put(11, voucherNo_Value_);
        requestData.put(14, expiryDate);
        requestData.put(22, cardTypeValue);
        requestData.put(23,cardSeriNoStr);
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
                    updateUI.sendMessage(updateUI.obtainMessage(3, getString(R.string.server_error)));
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
                        intent.setClass(SwipeWaitActivity.this,
                                QueryBalancceResultActivity.class);
                        startActivity(intent);
                        finish();
                        ViewUtils.overridePendingTransitionCome(SwipeWaitActivity.this);
                    } else {
                        updateUI.sendMessage(updateUI.obtainMessage(3, resultValue));
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
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
            logger.debug("register ServiceReceiver");
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
                    if (microphoneState == 1) {
                        logger.error("带麦克风的耳机插入");
                        CommonUtils.adjustAudioMax(SwipeWaitActivity.this);
                    } else {
                        logger.error("无麦克风的耳机插入");
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (cSwiperController != null)
        cSwiperController.unregisterServiceReceiver();
        unregisterServiceReceiver();

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

    @Override
    protected void onStart() {
        super.onStart();
        if (cSwiperController != null)
            cSwiperController.registerServiceReceiver();
    }
}

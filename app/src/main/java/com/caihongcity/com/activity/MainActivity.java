package com.caihongcity.com.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.db.NotificationData;
import com.caihongcity.com.utils.ActivityManager;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.MyAsyncTask.LoadResourceCall;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.view.CustomDialog;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@NoTitle
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    static final String TAG = "MainActivity";
    @ViewById
    TextView register_text,tv_version_number;
    @ViewById
    Button login;
    @ViewById
    ImageButton ib_txt_clear;
    @ViewById
    EditText login_phone, login_pwd;
    Dialog loadingDialog;
    Dialog checkDialog;
    private CustomDialog customDialog;
    private CustomDialog.InputDialogListener inputDialogListener;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dialog_confirmBt.setClickable(true);
                    dialog_confirmBt.setText("安装");
                    dialog_title_update.setText("下载完成是否安装");
                    dialog_progressBar_update.setVisibility(View.GONE);

                    break;
                case 2:
                    if (downLoadDialog!=null){
                        downLoadDialog.dismiss();
                    }
                    ViewUtils.makeToast(MainActivity.this,getString(R.string.server_error),1000);
                    break;
            }
        }
    };
    private CircleProgressBar dialog_progressBar_update;
    private TextView dialog_title_update;
    private Timer timer;
    private int swipTime = 0;
    private boolean stop = false;
    private Dialog Disabledialog;
    private Activity context;


    @AfterViews
    void initData() {
        context = this;
       LogUtil.e("======"+new byte[]{(byte)-97, (byte)9}.toString());
        ActivityManager.getInstance().add(this);
        //清除存储终端信息的缓存数据
        StorageCustomerInfoUtil.clearKey(this);
        StorageCustomerInfo02Util.clearKey(this);
        String phoneNum = StorageAppInfoUtil.getInfo("phoneNum", this);
        tv_version_number.setText("版本信息:"+CommonUtils.getAppVersionName(this));
        if (!TextUtils.isEmpty(phoneNum)) {
            login_phone.setText(phoneNum);
        }
        isShowConfirmButton();
//		startService(new Intent(this, GetKsnService.class));
        loadingDialog = ViewUtils.createLoadingDialog(this, "正在登录中...", false);
        getLocation();
    }
    private double latitude=0.0;
    private double longitude =0.0;
    private double latitude_new=0.0;
    private double longitude_new =0.0;
    public void getLocation() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if (latitude!=0&&longitude!=0){
                    StorageAppInfoUtil.putInfo(MainActivity.this, "latitude", latitude+"");//纬度
                    StorageAppInfoUtil.putInfo(MainActivity.this, "longitude", longitude+"");//经度
                }
            }
        }else{
            LocationListener locationListener = new LocationListener() {

                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {

                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {

                }

                //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        Log.e("Map", "Location changed : Lat: "
                                + location.getLatitude() + " Lng: "
                                + location.getLongitude());
                        latitude = location.getLatitude(); //经度
                        longitude = location.getLongitude(); //纬度
                        if (latitude!=0&&longitude!=0){
                            StorageAppInfoUtil.putInfo(MainActivity.this, "latitude", latitude+"");//纬度
                            StorageAppInfoUtil.putInfo(MainActivity.this, "longitude", longitude+"");//经度
                        }
                    }
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000, 0,locationListener);
            getBackgroundLocation(locationManager);

        }

    }

    private void getBackgroundLocation(final LocationManager locationManager) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                swipTime++;
                if (swipTime % (60 * 60 * 1000) == 0) {//每间隔一个小时更新一次经纬度
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude_new = location.getLatitude(); //经度
                        longitude_new = location.getLongitude(); //纬度
                        Log.e("Map", "getBackgroundLocation : Lat: "
                                + location.getLatitude() + " Lng: "
                                + location.getLongitude());
                        if (latitude != latitude_new || longitude != longitude_new) {//位置改变则重新存储
                            StorageAppInfoUtil.putInfo(MainActivity.this, "latitude", latitude + "");//纬度
                            StorageAppInfoUtil.putInfo(MainActivity.this, "longitude", longitude + "");//经度
                            latitude = latitude_new;
                            longitude = longitude_new;
                        }
                    }
                }
                LogUtil.i(TAG, "swipTime:" + swipTime);
            }
        }, 1000,30* 1000);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer!=null){
            timer.cancel();
        }
    }

    @TextChange
    void login_phone() {
        if (login_phone.getText().length()!=0){
            ib_txt_clear.setVisibility(View.VISIBLE);
        }else {
            ib_txt_clear.setVisibility(View.GONE);
        }

        login_pwd.setText("");
    }

    @TextChange
    void login_pwd(){
        isShowConfirmButton();
    }



    private void isShowConfirmButton() {
        String pwdValue = login_pwd.getText().toString();
        String phoneNum = login_phone.getText().toString();
        if ((phoneNum.length() == 11) && (phoneNum.charAt(0) == '1') && (pwdValue.length() >= 6) ) {
            login.setBackgroundResource(R.drawable.button_click_selector);
            login.setClickable(true);
        } else {
            login.setBackgroundResource(R.color.gray_light);
            login.setClickable(false);
        }
    }


    @Click({R.id.register_text, R.id.login, R.id.ib_txt_clear,R.id.find_pwd})
    void click(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.register_text:
                Intent intent = new Intent();
                intent.setClass(this, RegisterActivity_.class);
                startActivity(intent);
                ViewUtils.overridePendingTransitionCome(this);
                break;
            case R.id.login:
                String loginPhoneNum = login_phone.getText().toString();
                String loginPwdVal = login_pwd.getText().toString();
                if (StringUtil.isEmpty(loginPhoneNum)) {
                    ViewUtils.makeToast(this, getString(R.string.login_phone_isnull), 1500);
                    return;
                }
                if (StringUtil.isEmpty(loginPwdVal)) {
                    ViewUtils.makeToast(this, getString(R.string.login_pwd_isnull), 1500);
                    return;
                }
                //检查网络状态
                if (CommonUtils.getConnectedType(MainActivity.this) == -1) {
                    ViewUtils.makeToast(MainActivity.this, getString(R.string.nonetwork), 1500);
                    return;
                }
                login(loginPhoneNum, loginPwdVal);
                break;
            case R.id.ib_txt_clear:
                login_phone.setText("");
                break;
            case R.id.find_pwd:
                Intent intentFind = new Intent();
                intentFind.setClass(this,FindPwdActivity_.class);
                startActivity(intentFind);
                break;
        }
    }


    private void login(final String loginPhoneNum, String loginPwdVal) {
        String version = CommonUtils.getAppVersionName(this);
        HashMap<Integer,String> requestData = new HashMap<Integer,String>();
        requestData.put(0,"0700");
        requestData.put(1,loginPhoneNum);
        requestData.put(3,"190928");
        requestData.put(8,CommonUtils.Md5(loginPwdVal));
        requestData.put(59,Constant.VERSION);
        requestData.put(64,Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        LogUtil.i("login", url);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new LoadResourceCall() {

            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }
            @Override
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                loadingDialog.dismiss();//{"0":"0700","1":"15555808380","3":"190928","64":"D931BC896F7738426CB9F15718A9A703","4":"0","39":"00","42":"220558015061077","8":"96e79218965eb72c92a549dd5a330112","57":"[]"}
                if (StringUtil.isEmpty(content)) {
                    ViewUtils.makeToast(MainActivity.this, getString(R.string.server_error), 1500);
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    if (obj.has("63")) {
                        String customerName = (String) obj.get("63");
                        try {
                            String customerNameValue = URLDecoder.decode(customerName, "utf-8");
                            StorageCustomerInfoUtil.putInfo(MainActivity.this, "customerName", customerNameValue);
                            LogUtil.i("customerNameValue", customerNameValue);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(result)||"W8".equals(result)) {
                        String customerInfosString = obj.getString("42");
                        if (!TextUtils.isEmpty(customerInfosString)) {
                            JSONArray customerInfos = new JSONArray(customerInfosString);
                            JSONObject customerInfo = new JSONObject(customerInfos.get(0).toString());
                            String merchantNo = (String) customerInfo.get("merchantNo");
                            String bankAccount = (String) customerInfo.get("bankAccount");
                            String bankAccountName = (String) customerInfo.get("bankAccountName");
                            String idCardNumber = (String) customerInfo.get("idCardNumber");
                            String bankCode = (String) customerInfo.get("bankCode");
                            String bankDetail = (String) customerInfo.get("bankDetail");
                            String source = (String) customerInfo.get("merchantSource");
                            String phone = (String) customerInfo.get("phone");
                            String useStatus = (String) customerInfo.get("useStatus");
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "bankAccount", bankAccount);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "bankAccountName", bankAccountName);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "idCardNumber", idCardNumber);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "bankDetail", bankDetail);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "bankCode", bankCode);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "phone", phone);
                            StorageCustomerInfoUtil.putInfo(MainActivity.this, "customerNum", merchantNo);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "source", source);
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "useStatus", useStatus);
                            if("10B".equals(useStatus)){
                                 Disabledialog = ViewUtils.showChoseDialog(MainActivity.this, false, "风险账号，暂被停用", View.GONE, new ViewUtils.OnChoseDialogClickCallback() {
                                    @Override
                                    public void clickOk() {
                                        Disabledialog.dismiss();
                                    }

                                    @Override
                                    public void clickCancel() {
                                    }
                                });
                                Disabledialog.show();
                                return;

                            }
                            if (customerInfo.has("freezeStatus")){
                                String freezeStatus = (String) customerInfo.get("freezeStatus");//10A 未审核，10B 审核通过，10C 审核拒绝，10D 重新审核
                                StorageCustomerInfo02Util.putInfo(MainActivity.this, "freezeStatus", freezeStatus);
                            }
                            if ("W8".equals(result)) {//实名认证信息有误 存储审核意见信息
                                String examineResult = (String) customerInfo.get("rcexamineResult");//审核意见
                                StorageCustomerInfo02Util.putInfo(MainActivity.this, "examineResult", examineResult);
                                StorageCustomerInfo02Util.putInfo(MainActivity.this, "examineState", "W8");//审核状态
                            }else{
                                StorageCustomerInfo02Util.putInfo(MainActivity.this, "examineResult", "");
                                StorageCustomerInfo02Util.putInfo(MainActivity.this, "examineState", "");
                            }
                        }
                        String batchNoArray = obj.getString("57");
                        JSONArray jarray = new JSONArray(batchNoArray);
                        int len = jarray.length();
                        for (int i = 0; i < len; i++) {
                            JSONObject jobj = (JSONObject) jarray.get(i);
                            String imageUrl = (String) jobj.get("imageUrl");
                            String imageType = jobj.getString("type");
                            StorageCustomerInfo02Util.putInfo(MainActivity.this, "infoImageUrl_"+imageType, imageUrl);
                        }
                        StorageCustomerInfoUtil.putInfo(MainActivity.this, "phoneNum", loginPhoneNum);
                        StorageAppInfoUtil.putInfo(MainActivity.this, "phoneNum", loginPhoneNum);//保存用户名，下次登录时直接填充
                        //新增公告信息 60 最近的一条公告信息
                        if (obj.has("60")&&!TextUtils.isEmpty(obj.getString("60"))){
                            JSONArray jsonArray = new JSONArray(obj.getString("60"));
                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONObject object = (JSONObject) jsonArray.get(j);
                                if (object.has("id")&&!TextUtils.isEmpty(object.getString("id"))){
                                    String notificationId = object.getString("id");
                                    StorageAppInfoUtil.putInfo(MainActivity.this, "newNotificationId", notificationId);//保存最新的一条通告，主页面会用到
                                    List<NotificationData> notificationDatas = DataSupport.where("notificationId = ?", notificationId).find(NotificationData.class);
                                    NotificationData notificationData;
                                    if (notificationDatas.size() <= 0) {
                                        notificationData = new NotificationData();
                                        String updateDateStr = object.getString("updateDateStr").substring(5, 10);
                                        notificationData.setNotificationDate(updateDateStr.equals(CommonUtils.getTime("MM-dd")) ? "今天" : updateDateStr);
                                        notificationData.setNotificationId(notificationId);
                                        notificationData.setNotificationTitle(object.getString("title"));
                                        notificationData.setNotificationContent(object.getString("content"));
                                        notificationData.setUserPhoneNumer(loginPhoneNum);
                                        notificationData.save();
                                    }else {
                                        notificationData = notificationDatas.get(0);
                                        String updateDateStr = object.getString("updateDateStr").substring(5, 10);
                                        notificationData.setNotificationDate(updateDateStr.equals(CommonUtils.getTime("MM-dd")) ? "今天" : updateDateStr);
                                        notificationData.setUserPhoneNumer(loginPhoneNum);
                                        notificationData.updateAll("notificationId = ?", notificationId);
                                    }
                                }
                            }
                        }


                        if("W8".equals(result)){//审核未通过，返回W8
                            checkDialog();
                        }else{
                            if(obj.has("44")&&!TextUtils.isEmpty(obj.getString("44"))){
                                String constant = obj.getString("44");
                                int newVerCode = Integer.parseInt(constant.substring(7).replace(".", ""));
                                int curVerCode = Integer.parseInt( Constant.VERSION.substring(7).replace(".",""));
                                if(newVerCode>curVerCode){
                                    showChoseDialogUpdate(context,"发现新版本");
                                }else {
                                    Intent intent_start = new Intent();
                                    intent_start.setClass(MainActivity.this, StartActivity_.class);
                                    startActivity(intent_start);
                                    ViewUtils.overridePendingTransitionCome(MainActivity.this);
                                }
                            }else {
                                Intent intent_start = new Intent();
                                intent_start.setClass(MainActivity.this, StartActivity_.class);
                                startActivity(intent_start);
                                ViewUtils.overridePendingTransitionCome(MainActivity.this);
                            }

                        }


                    } else if ("ZV".equals(result)) {//请更新到最新版本
                        showChoseDialogDownLoad(resultValue);
                    } else {
//						ViewUtils.makeToast(MainActivity.this, getString(R.string.login_failure), Toast.LENGTH_LONG).show();
//						ViewUtils.makeToast(MainActivity.this, resultValue, Toast.LENGTH_LONG).show();
                        if (TextUtils.isEmpty(resultValue)) {
                            ViewUtils.makeToast(MainActivity.this, "系统异常" + result, 1500);
                        } else {
                            ViewUtils.makeToast(MainActivity.this, resultValue, 1500);
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
     * 风控审核提示框
     */
    private void checkDialog() {
        checkDialog = new Dialog(MainActivity.this, R.style.MyProgressDialog);
        checkDialog.setContentView(R.layout.chose_dialog_upload);
        checkDialog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) checkDialog.findViewById(R.id.left_bt);
        Button cancleButton = (Button) checkDialog.findViewById(R.id.right_bt);
        dialog_title_text = ((TextView) checkDialog.findViewById(R.id.title_text));
        dialog_title_text.setText("商户资料审核不通过\n请重新提交");
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmBt_des = dialog_confirmBt.getText().toString();
                if ("确定".equals(confirmBt_des)) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CustomerinfoActivity.class);
                    intent.putExtra("isInfoComplete", true);
                    startActivity(intent);
                    ViewUtils.overridePendingTransitionCome(MainActivity.this);
                }


            }
        });


        checkDialog.show();

    }

    /**
     * 选择提示框
     */
    public Dialog showChoseDialogDownLoad(String msg) {
        downLoadDialog = new Dialog(MainActivity.this, R.style.MyProgressDialog);
        downLoadDialog.setContentView(R.layout.chose_dialog_update);
        downLoadDialog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) downLoadDialog.findViewById(R.id.left_bt);
        Button cancleBt = (Button) downLoadDialog.findViewById(R.id.right_bt);
        progressBar = (ProgressBar) downLoadDialog.findViewById(R.id.progressBar);
        dialog_title_text = ((TextView) downLoadDialog.findViewById(R.id.title_text));
        dialog_progressBar_update = (CircleProgressBar)downLoadDialog.findViewById(R.id.progressBar_update);
        dialog_title_update = (TextView)downLoadDialog.findViewById(R.id.title_update);
        dialog_title_text.setText(msg);
        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop = true;
                downLoadDialog.dismiss();
            }
        });
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmBt_des = dialog_confirmBt.getText().toString();
                if ("确定".equals(confirmBt_des)) {
                    downFile(Constant.DOWNLOAD_APK);
                    dialog_confirmBt.setClickable(false);
                    dialog_progressBar_update.setColorSchemeResources(R.color.title_bg);
                    dialog_title_text.setVisibility(View.GONE);
                    dialog_title_update.setVisibility(View.VISIBLE);
                    dialog_progressBar_update.setVisibility(View.VISIBLE);
                } else if ("安装".equals(confirmBt_des)) {
                    installApk();
                }

            }
        });
        downLoadDialog.show();
        return downLoadDialog;
    }


    /**
     * 更新提示框
     */
    public Dialog showChoseDialogUpdate(Context context,String msg) {
        final Dialog updateDialog = new Dialog(context, R.style.MyProgressDialog);
        updateDialog.setContentView(R.layout.chose_dialog_update);
        updateDialog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) updateDialog.findViewById(R.id.left_bt);
        final Button cancleBt = (Button) updateDialog.findViewById(R.id.right_bt);
        cancleBt.setText("继续登录");
        dialog_confirmBt.setText("更新版本");
         dialog_title_text = ((TextView) updateDialog.findViewById(R.id.title_text));
         dialog_progressBar_update = (CircleProgressBar)updateDialog.findViewById(R.id.progressBar_update);
         dialog_title_update = (TextView)updateDialog.findViewById(R.id.title_update);
        dialog_title_text.setText(msg);
        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
                Intent intent_start = new Intent();
                intent_start.setClass(MainActivity.this, StartActivity_.class);
                startActivity(intent_start);
                ViewUtils.overridePendingTransitionCome(MainActivity.this);
            }
        });
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmBt_des = dialog_confirmBt.getText().toString();
                if ("更新版本".equals(confirmBt_des)) {
                    downFile(Constant.DOWNLOAD_APK);
                    dialog_confirmBt.setClickable(false);
                    cancleBt.setClickable(false);
                    dialog_progressBar_update.setColorSchemeResources(R.color.title_bg);
                    dialog_title_text.setVisibility(View.GONE);
                    dialog_title_update.setVisibility(View.VISIBLE);
                    dialog_progressBar_update.setVisibility(View.VISIBLE);
                } else if ("安装".equals(confirmBt_des)) {
                    installApk();
                }

            }
        });
        updateDialog.show();
        return updateDialog;
    }




    File file;
    TextView dialog_title_text;
    Button dialog_confirmBt;
    ProgressBar progressBar;
    Dialog downLoadDialog;

    /**
     * 后台在下面一个Apk 下载完成后返回下载好的文件
     *
     * @param httpUrl
     * @return
     */
    private File downFile(final String httpUrl) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL(httpUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    FileOutputStream fileOutputStream = null;
                    InputStream inputStream;
                    int responCode = connection.getResponseCode();
                    if (responCode == 200) {
                        inputStream = connection.getInputStream();
                        int MaxLength = connection.getContentLength();
                        dialog_progressBar_update.setMax(MaxLength);
                        if (inputStream != null) {
                            file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), httpUrl.substring(httpUrl.lastIndexOf("/"), httpUrl.length()));
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int length = 0;
                            int alreadylength = 0;
                            while ((length = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, length);
                                alreadylength+=length;
//                               LogUtil.syso("progress:"+alreadylength*100/MaxLength);
                                dialog_progressBar_update.setProgress(alreadylength*100/MaxLength);
                                if(stop){
                                    stop = false;
                                    return;
                                }
                            }
                            fileOutputStream.close();
                            fileOutputStream.flush();
                        }
                        inputStream.close();
                        System.out.println("已经下载完成");
                        // 往handler发送一条消息 更改button的text属性
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        handler.sendMessage(message);
                    }else{
                        Message message = handler.obtainMessage();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return file;
    }



    /**
     * 安装APK
     */
    private void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}

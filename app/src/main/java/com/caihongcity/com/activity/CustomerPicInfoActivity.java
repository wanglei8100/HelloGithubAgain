package com.caihongcity.com.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.utils.ActivityManager;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.ImageUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class CustomerPicInfoActivity extends BaseActivity implements View.OnClickListener {
       Button bt_handle_idcard, bt_idcard_a, bt_idcard_b;
    ImageView iv_handle_idcard, iv_idcard_a, iv_idcard_b;
    ImageLoader imageLoader;
    HashMap<Integer, String> imagePaths;
    private String TAG = "CustomerPicInfoActivity";
    private Button bt_submit;
    private TextView tv_right;
    String examineState;
    private boolean syncState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_pic_info);
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("照片上传");
        tv_right = ((TextView) findViewById(R.id.tv_right));
        tv_right.setOnClickListener(this);
        tv_right.setText("首页");
        bt_handle_idcard = (Button) findViewById(R.id.bt_handle_idcard);
        bt_idcard_a = (Button) findViewById(R.id.bt_idcard_a);
        bt_idcard_b = (Button) findViewById(R.id.bt_idcard_b);
        bt_submit = (Button) findViewById(R.id.bt_submit);
        iv_handle_idcard = (ImageView) findViewById(R.id.iv_handle_idcard);
        iv_idcard_a = (ImageView) findViewById(R.id.iv_idcard_a);
        iv_idcard_b = (ImageView) findViewById(R.id.iv_idcard_b);
        bt_handle_idcard.setOnClickListener(this);
        bt_idcard_a.setOnClickListener(this);
        bt_idcard_b.setOnClickListener(this);
        iv_handle_idcard.setOnClickListener(this);
        iv_idcard_a.setOnClickListener(this);
        iv_idcard_b.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        imageLoader = ImageLoader.getInstance();
        imagePaths = new HashMap<Integer, String>();

        initDate();

        examineState = StorageCustomerInfo02Util.getInfo("examineState", this);

        if (checkButtonStatusInit()&&TextUtils.isEmpty(examineState)) {//图片都已经上传成功
            tv_right.setVisibility(View.VISIBLE);
            bt_submit.setVisibility(View.GONE);
        } else {
            tv_right.setVisibility(View.GONE);
            bt_submit.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkButtonStatusInit() {
        return (getString(R.string.upload_sucess).equals(bt_handle_idcard.getText()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_a.getText()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_b.getText())) ||
                ((!TextUtils.isEmpty(StorageCustomerInfo02Util.getInfo("infoImageUrl_10M", this))) &&
                        (!TextUtils.isEmpty(StorageCustomerInfo02Util.getInfo("infoImageUrl_10E", this))) &&
                        (!TextUtils.isEmpty(StorageCustomerInfo02Util.getInfo("infoImageUrl_10F", this))));
    }
    private boolean checkButtonStatusSubmit() {
        return ((getString(R.string.upload_sucess).equals(bt_handle_idcard.getText())||getString(R.string.click_select_again).equals(bt_handle_idcard.getText()))
                && (getString(R.string.upload_sucess).equals(bt_idcard_a.getText())||getString(R.string.click_select_again).equals(bt_idcard_a.getText()))
                &&(getString(R.string.upload_sucess).equals(bt_idcard_b.getText())||getString(R.string.click_select_again).equals(bt_idcard_b.getText())));
    }

    private void initDate() {
        //检查网络状态
        if (CommonUtils.getConnectedType(this) == -1) {
            ViewUtils.makeToast(this, getString(R.string.nonetwork), 1500);
            return;
        }
        String infoImageUrl_10M = StorageCustomerInfo02Util.getInfo("infoImageUrl_10M", this);
        String infoImageUrl_10E = StorageCustomerInfo02Util.getInfo("infoImageUrl_10E", this);
        String infoImageUrl_10F = StorageCustomerInfo02Util.getInfo("infoImageUrl_10F", this);
        if (!TextUtils.isEmpty(infoImageUrl_10M)) {
            bt_handle_idcard.setText(getString(R.string.upload_sucess));
            loadSignNamePic(infoImageUrl_10M, "10M");
        }
        if (!TextUtils.isEmpty(infoImageUrl_10E)) {
            bt_idcard_a.setText(getString(R.string.upload_sucess));
            loadSignNamePic(infoImageUrl_10E, "10E");
        }
        if (!TextUtils.isEmpty(infoImageUrl_10F)) {
            bt_idcard_b.setText(getString(R.string.upload_sucess));
            loadSignNamePic(infoImageUrl_10F, "10F");
        }
    }

    private void loadSignNamePic(String signUrl, final String imageType) {

        loadingDialogCanCancel.show();
        ImageRequest imageRequest = new ImageRequest(
                signUrl,
                new Response.Listener<Bitmap>() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onResponse(Bitmap response) {
                        loadingDialogCanCancel.dismiss();
                        if ("10M".equals(imageType)) {
                            iv_handle_idcard.setImageBitmap(response);
                        } else if ("10E".equals(imageType)) {
                            iv_idcard_a.setImageBitmap(response);
                        } else if ("10F".equals(imageType)) {
                            iv_idcard_b.setImageBitmap(response);
                        }
                        if (!TextUtils.isEmpty(examineState)){
                            bt_handle_idcard.setText(getString(R.string.click_select_again));
                            bt_idcard_a.setText(getString(R.string.click_select_again));
                            bt_idcard_b.setText(getString(R.string.click_select_again));
                        }
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.i(TAG, "图片加载失败");
                ViewUtils.makeToast(CustomerPicInfoActivity.this,"服务器异常，图片加载失败",1500);
                if (!TextUtils.isEmpty(examineState)){
                    bt_handle_idcard.setText(getString(R.string.click_select_again));
                    bt_idcard_a.setText(getString(R.string.click_select_again));
                    bt_idcard_b.setText(getString(R.string.click_select_again));
                }
                if (loadingDialogCanCancel != null) {
                    loadingDialogCanCancel.dismiss();
                }
            }
        });
        newRequestQueue.add(imageRequest);
    }

    int id = 0;

    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.bt_handle_idcard:
            case R.id.iv_handle_idcard:
                id = 1;
                String status = bt_handle_idcard.getText().toString();
                if (getString(R.string.click_select).equals(status)|getString(R.string.click_select_again).equals(status)) {
                    choseCamers();
                } else if (getString(R.string.click_upload).equals(status)) {
                    confirmUpload(imagePaths.get(1), "10M");
                } else {
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.already_exsit), 1000);
                }
                break;
            case R.id.bt_idcard_a:
            case R.id.iv_idcard_a:
                id = 2;
                String status02 = bt_idcard_a.getText().toString();
                if (getString(R.string.click_select).equals(status02)|getString(R.string.click_select_again).equals(status02)) {
                    choseCamers();
                } else if (getString(R.string.click_upload).equals(status02)) {
                    confirmUpload(imagePaths.get(2), "10E");
                } else {
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.already_exsit), 1000);
                }
                break;
            case R.id.bt_idcard_b:
            case R.id.iv_idcard_b:
                id = 3;
                String status03 = bt_idcard_b.getText().toString();
                if (getString(R.string.click_select).equals(status03)||getString(R.string.click_select_again).equals(status03)) {
                    choseCamers();
                } else if (getString(R.string.click_upload).equals(status03)) {
                    confirmUpload(imagePaths.get(3), "10F");
                } else {
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.already_exsit), 1000);
                }
                break;
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
            case R.id.bt_submit:
                if (!checkButtonStatusSubmit()) {
                    ViewUtils.makeToast(CustomerPicInfoActivity.this, "图片必须全部上传", 1000);
                } else {
                    if (!TextUtils.isEmpty(StorageCustomerInfo02Util.getInfo("examineState", this))){
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, "请根据审核信息重新上传照片", 1000);
                        return;
                    }
                    ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.submit_success), 1000);

                    Intent intent = new Intent();
                    intent.setClass(this,StartActivity_.class);
                    startActivity(intent);
                    ActivityManager.finishActivity(2);
                    ViewUtils.overridePendingTransitionBack(this);
                }
//                CustomerSy();//商户同步

                break;
            case R.id.tv_right:
                ActivityManager.finishActivity(2);
                ViewUtils.overridePendingTransitionBack(this);
                break;
        }
    }

    /**
     * 商户同步
     */
    private void CustomerSy() {

        String phone = StorageCustomerInfo02Util.getInfo("phone", this);
        String name = StorageCustomerInfo02Util.getInfo("bankAccountName", this);
        String idCardNumber = StorageCustomerInfo02Util.getInfo("idCardNumber", this);
        String bankAccount = StorageCustomerInfo02Util.getInfo("bankAccount", this);
        String bankDetail = StorageCustomerInfo02Util.getInfo("bankDetail", this);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(1, "0700");
        requestData.put(2, phone);
        requestData.put(3, "190101");
        requestData.put(5, name);
        requestData.put(6, idCardNumber);
        requestData.put(7, bankAccount);
        requestData.put(43, bankDetail);
        requestData.put(44, Constant.AGENCY_CODE44);
        requestData.put(64, Constant.getMacData(requestData));
        sync(Constant.getUrl(requestData));
    }


    void sync(String url) {
        new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            public void isLoadedContent(String paramString) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + paramString);
                if (StringUtil.isEmpty(paramString)) {
//                  toast(getString(R.string.server_error));
                    ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.server_error), 1000);
                    return;
                }
                try {
                    JSONObject localJSONObject = new JSONObject(paramString);
                    String result = (String) localJSONObject.get("39");
                    String resultValue = (String) MyApplication.getErrorHint(result);
                    LogUtil.syso("返回的错误码为" + resultValue);

                    if ("00".equals(result)) {
                        StorageCustomerInfo02Util.putInfo(CustomerPicInfoActivity.this, "examineState", "");
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.submit_success), 1000);
                        ActivityManager.finishActivity(2);
                        ViewUtils.overridePendingTransitionBack(CustomerPicInfoActivity.this);


                    } else {
                        ViewUtils.makeToast(CustomerPicInfoActivity.this,resultValue,1000);
                    }
                } catch (JSONException localJSONException) {
                    localJSONException.printStackTrace();
                    return;
                }

            }

            public void isLoadingContent() {
                //loadingDialog.show();
//                dialogShow("正在查询余额");
            }
        }).execute(url);
        LogUtil.d("SwipeWaitMoFangActivity", "url==" + url);
        return;

    }

    private void confirmUpload(final String imagePath, final String imageType) {
        ViewUtils.showChoseDialog02(CustomerPicInfoActivity.this, true, "请确认图片,上传完成之后不能修改", "重拍",  "上传",new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {//相册
                uploadImage(imagePath, imageType);
            }

            @Override
            public void clickCancel() {//拍照
                choseCamers();
            }
        });
    }

    private void uploadImage(String signPath, String imageType) {
        // 检查网络状态
        if (CommonUtils.getConnectedType(CustomerPicInfoActivity.this) == -1) {
            ViewUtils.makeToast(CustomerPicInfoActivity.this,
                    getString(R.string.nonetwork), 1500);
            return;
        }
        loadingDialog = ViewUtils.createLoadingDialog(this, getString(R.string.loading_wait), false);
        loadingDialog.show();
        UploadFileAAsyncTask asyncTask_pic = new UploadFileAAsyncTask();
        asyncTask_pic.execute(signPath, imageType);

    }


    class UploadFileAAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String str = params[0];
            LogUtil.d(TAG, "imagePath " + str);
            String imageType = params[1];
            File file = new File(str);
            String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
                    CustomerPicInfoActivity.this);
            String data = "0=0700" + "&3=190948&9=" + imageType + "&42=" + customerNum + "&59="
                    + Constant.VERSION;
            String macData = "0700" + "190948" + imageType
                    + customerNum + Constant.VERSION;
            String url = Constant.UPLOADIMAGE + data + "&64="
                    + CommonUtils.Md5(macData + Constant.mainKey);
            String result = ImageUtils.uploadFile(file, url);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {// 处理UI
            super.onPostExecute(result);
            loadingDialog.dismiss();
            LogUtil.syso("图片上传返回："+result);
            try {
                if (StringUtil.isEmpty(result)) {
                    ViewUtils.makeToast(CustomerPicInfoActivity.this,
                            getString(R.string.server_error), 1500);
                    return;
                }
                JSONObject jsonResult = new JSONObject(result);
                String strCode = jsonResult.getString("39");
                String resultValue = MyApplication.getErrorHint(result);
                if ("00".equals(strCode)) {// 上传成功
                    if (!TextUtils.isEmpty(examineState)){//审核拒绝时会有审核意见 只要成功重新上传一张图片 状态就会变为重新审核
                        StorageCustomerInfo02Util.putInfo(context, "freezeStatus", "10D");
                        StorageCustomerInfo02Util.putInfo(CustomerPicInfoActivity.this, "examineState", "");//针对风控审核不通过的情况，只要成功重新上传一张图片就通过
                    }
                    ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.upload_sucess), 1000);
                    modifyButtonStatus(getString(R.string.upload_sucess));
                    String imageUrl = jsonResult.getString("57");
                    saveImagePath(imageUrl);
                } else {// 上传失败
                    ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.server_error), 1000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.server_error), 1000);
            }
        }

    }

    private void saveImagePath(String imageUrl) {
        switch (id) {
            case 1:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10M", imageUrl);
                break;
            case 2:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10E", imageUrl);
                break;
            case 3:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10F", imageUrl);
                break;
        }
    }

    private void modifyButtonStatus(String buttonStatus) {
        switch (id) {
            case 1:
                bt_handle_idcard.setText(buttonStatus);
                break;
            case 2:
                bt_idcard_a.setText(buttonStatus);
                break;
            case 3:
                bt_idcard_b.setText(buttonStatus);
                break;
        }
    }

    public final static int CONSULT_DOC_PICTURE = 1000;
    public final static int CONSULT_DOC_CAMERA = 1001;
    Bitmap bmp;
    Uri outputFileUri;

    void chosePhoto() {
        ViewUtils.showChoseDialog02(CustomerPicInfoActivity.this, true, "选择获取照片方式", "拍照", "取消", new ViewUtils.OnChoseDialogClickCallback() {
            @Override
            public void clickOk() {//相册
//                chosePhotos();
            }

            @Override
            public void clickCancel() {//拍照
                choseCamers();
            }
        });
    }

    void choseCamers() {
        File file = new File(Environment.getExternalStorageDirectory(), "textphoto.jpg");
        if (file.exists())file.delete();
        outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CONSULT_DOC_CAMERA);
    }

    private void chosePhotos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), CONSULT_DOC_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONSULT_DOC_PICTURE) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            String[] proj = {MediaStore.Images.Media.DATA};
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Cursor cursor = CustomerPicInfoActivity.this.managedQuery(uri, proj, // Which
//                    null, // WHERE clause; which rows to return (all rows)
//                    null, // WHERE clause selection arguments (none)
//                    null); // Order-by clause (ascending by name)
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            String path = cursor.getString(column_index);
//
//            setImage(path);
//            Bitmap bitmap = ImageUtils.getZoomBitmap(path, null);
            bitmap = ImageUtils.scaleImg(bitmap,480,800);//压缩图片
            String path =  saveImage(bitmap);//保存图片返回路径
            setImage(path);//根据路径设置图片
        } else if (requestCode == CONSULT_DOC_CAMERA) {

            Bitmap bitmap = ImageUtils.getZoomBitmap(outputFileUri.getPath(), null);
            if (bitmap==null)return;
            String path = saveImage(bitmap);
            LogUtil.syso("path:"+path);
            setImage(path);
        } else {
            ViewUtils.makeToast(this, "请重新选择图片", 1000);
        }

    }

    private String saveImage(Bitmap bitmap) {
        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + CustomerPicInfoActivity.this.getPackageName() + "/Image/";
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
           boolean isSuccess =  dirFile.mkdirs();
            LogUtil.d(TAG,"isSuccess="+isSuccess);
        }
        File oldFile = new File(dirPath + id + ".jpg");//删除旧数据
        if (oldFile.exists())oldFile.delete();
        File myCaptureFile = new File(dirPath + id + ".jpg");
        imagePaths.put(id, myCaptureFile.getPath());
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myCaptureFile.getPath();
    }

    void setImage(String imagePath) {
//        imagePaths.put(id, imagePath);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();
        String imageUrl = ImageDownloader.Scheme.FILE.wrap(imagePath);
        switch (id) {
            case 1:
                imageLoader.displayImage(imageUrl, iv_handle_idcard, options);
                bt_handle_idcard.setText(getString(R.string.click_upload));
                break;
            case 2:
                imageLoader.displayImage(imageUrl, iv_idcard_a, options);
                bt_idcard_a.setText(getString(R.string.click_upload));
                break;
            case 3:
                imageLoader.displayImage(imageUrl, iv_idcard_b, options);
                bt_idcard_b.setText(getString(R.string.click_upload));
                break;
        }
    }

}

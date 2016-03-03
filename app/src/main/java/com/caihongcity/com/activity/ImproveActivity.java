package com.caihongcity.com.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.caihongcity.com.R;
import com.caihongcity.com.model.BindCard;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.ImageUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.Utils;
import com.caihongcity.com.utils.ViewUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_creditrepay)
public class ImproveActivity extends BaseActivity {

    @ViewById(R.id.ll_back)
    View ll_back;
    @ViewById(R.id.et_name)
    EditText et_name;
    @ViewById(R.id.et_card_number)
    EditText et_card_number;
    @ViewById(R.id.ll_bank_name)
    View ll_bank_name;
    @ViewById(R.id.et_bank_name)
    EditText et_bank_name;
    @ViewById(R.id.et_bank_number)
    EditText et_bank_number;
    @ViewById(R.id.iv_idcard_a)
    ImageView iv_idcard_a;
    @ViewById(R.id.ll_check_state)
    View ll_check_state;
    @ViewById(R.id.ll_check_info)
    View ll_check_info;
    @ViewById(R.id.text_check_state)
    TextView text_check_state;
    @ViewById(R.id.text_check_info)
    TextView text_check_info;
    @ViewById(R.id.ll_tixian_money)
    View ll_tixian_money;
    @ViewById(R.id.text_tixian_money)
    TextView text_tixian_money;
    @ViewById(R.id.bt_idcard_a)
    Button bt_idcard_a;
    @ViewById(R.id.iv_idcard_b)
    ImageView iv_idcard_b;
    @ViewById(R.id.bt_idcard_b)
    Button bt_idcard_b;
    @ViewById(R.id.iv_iccard_a)
    ImageView iv_iccard_a;
    @ViewById(R.id.bt_iccard_a)
    Button bt_iccard_a;
    @ViewById(R.id.iv_iccard_b)
    ImageView iv_iccard_b;
    @ViewById(R.id.bt_iccard_b)
    Button bt_iccard_b;
    @ViewById(R.id.iv_handle_idcard)
    ImageView iv_handle_idcard;
    @ViewById(R.id.bt_handle_idcard)
    Button bt_handle_idcard;
    @ViewById(R.id.iv_item_right)
    ImageView iv_item_right;
    @ViewById(R.id.ll_idCard)
    View ll_idCard;
    @ViewById(R.id.ll_icCard)
    View ll_icCard;
    @ViewById(R.id.ll_submit)
    View ll_submit;
    @Extra
    BindCard bindCard;
    private Uri outputFileUri;
    private int CONSULT_DOC_CAMERA = 1001;
    private int CONSULT_DOC_PICTURE = 1000;
    private String TAG = "ImproveActivity";
    private int id = 0;
    HashMap<Integer, String> imagePaths = new HashMap<Integer, String>();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private String bank_number;
    private Dialog tipDailog;
    private Button dialog_confirmBt;
    private TextView dialog_title_text;
    private boolean infoComplete = false;
    private String limitStatus;
    private boolean clickable = true;
    private Dialog cancleDialog;


    @AfterViews
    void initData() {
        if (bindCard != null) {
            limitStatus = bindCard.getIncreaseLimitStatus();
            ((TextView) findViewById(R.id.tv_title_des)).setText("审核结果");
            ll_check_state.setVisibility(View.VISIBLE);
            if ("审核通过".equals(limitStatus)) {
                ll_tixian_money.setVisibility(View.VISIBLE);
                text_tixian_money.setText(bindCard.getSingleLimit());
            }
            if ("审核拒绝".equals(limitStatus)) {
                ll_check_info.setVisibility(View.VISIBLE);
                text_check_info.setText(bindCard.getExamineResult());
                ll_submit.setVisibility(View.VISIBLE);
            } else {
                ll_submit.setVisibility(View.GONE);
            }
            ll_idCard.setVisibility(View.VISIBLE);
            ll_icCard.setVisibility(View.VISIBLE);
            et_name.setText(bindCard.getBankAccountName());
            String cardNumberVal = CommonUtils.translateShortNumber(bindCard.getIdCardNumber(), 6, 4);
            et_card_number.setText(cardNumberVal);
            String bankNumberVal = CommonUtils.translateShortNumber(bindCard.getBankAccount(), 6, 4);
            et_bank_number.setText(bankNumberVal);
            et_bank_name.setText(bindCard.getBankName());
            text_check_state.setText(bindCard.getIncreaseLimitStatus());
            clickable = false;  //判断按钮是否可点击
            et_name.setFocusable(false);
            et_card_number.setFocusable(false);
            et_bank_number.setFocusable(false);
            et_bank_name.setClickable(false);
            iv_item_right.setVisibility(View.GONE);
            infoComplete = true;
            loadPic();

        } else {
            ((TextView) findViewById(R.id.tv_title_des)).setText("提交信息");
            tip();
        }
    }


    @TextChange
    void bt_idcard_a() {
        if ("上传成功".equals(bt_idcard_a.getText().toString())) {
            bt_idcard_a.setBackgroundColor(getResources().getColor(R.color.title_bg));
            bt_idcard_a.setClickable(false);
            iv_idcard_a.setClickable(false);
        }
        if ("重新上传".equals(bt_idcard_a.getText().toString())) {
            bt_idcard_a.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }
        if ("点击上传".equals(bt_idcard_a.getText().toString())) {
            bt_idcard_a.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }

    }
    @TextChange
    void bt_idcard_b() {
        if ("上传成功".equals(bt_idcard_b.getText().toString())) {
            bt_idcard_b.setBackgroundColor(getResources().getColor(R.color.title_bg));
            bt_idcard_b.setClickable(false);
            iv_idcard_b.setClickable(false);
        }
        if ("重新上传".equals(bt_idcard_b.getText().toString())) {
            bt_idcard_b.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }
        if ("点击上传".equals(bt_idcard_b.getText().toString())) {
            bt_idcard_b.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }

    }

    @TextChange
    void bt_iccard_a() {
        if ("上传成功".equals(bt_iccard_a.getText().toString())) {
            bt_iccard_a.setBackgroundColor(getResources().getColor(R.color.title_bg));
            bt_iccard_a.setClickable(false);
            iv_iccard_a.setClickable(false);
        }
        if ("重新上传".equals(bt_iccard_a.getText().toString())) {
            bt_iccard_a.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }
        if ("点击上传".equals(bt_iccard_a.getText().toString())) {
            bt_iccard_a.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }

    }

    @TextChange
    void bt_iccard_b() {
        if ("上传成功".equals(bt_iccard_b.getText().toString())) {
            bt_iccard_b.setBackgroundColor(getResources().getColor(R.color.title_bg));
            bt_iccard_b.setClickable(false);
            iv_iccard_b.setClickable(false);

        }
        if ("重新上传".equals(bt_iccard_b.getText().toString())) {
            bt_iccard_b.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }
        if ("点击上传".equals(bt_iccard_b.getText().toString())) {
            bt_iccard_b.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }

    }


    @TextChange
    void bt_handle_idcard() {
        if ("上传成功".equals(bt_handle_idcard.getText().toString())) {
            bt_handle_idcard.setBackgroundColor(getResources().getColor(R.color.title_bg));
            bt_handle_idcard.setClickable(false);
            iv_handle_idcard.setClickable(false);
        }
        if ("重新上传".equals(bt_handle_idcard.getText().toString())) {
            bt_handle_idcard.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }
        if ("点击上传".equals(bt_handle_idcard.getText().toString())) {
            bt_handle_idcard.setBackgroundColor(getResources().getColor(R.color.text_color_deepgray));
        }

    }

    /**
     * 加载已经上传的图片
     */
    private void loadPic() {
        //检查网络状态
        if (CommonUtils.getConnectedType(this) == -1) {
            ViewUtils.makeToast(this, getString(R.string.nonetwork), 1500);
            return;
        }
        // 从本地取图片
        String infoImageUrl_10A = StorageCustomerInfo02Util.getInfo("infoImageUrl_10A", this);
        String infoImageUrl_10B = StorageCustomerInfo02Util.getInfo("infoImageUrl_10B", this);
        String infoImageUrl_10D = StorageCustomerInfo02Util.getInfo("infoImageUrl_10D", this);
        String infoImageUrl_10E = StorageCustomerInfo02Util.getInfo("infoImageUrl_10E", this);
        String infoImageUrl_10F = StorageCustomerInfo02Util.getInfo("infoImageUrl_10F", this);



            if (!TextUtils.isEmpty(infoImageUrl_10A)) {
                if ("审核拒绝".equals(limitStatus)) {
                    bt_idcard_a.setText("重新上传");
                }else {
                    bt_idcard_a.setText(getString(R.string.upload_sucess));
                }
                loadSignNamePic(infoImageUrl_10A, "10A");

            }
            if (!TextUtils.isEmpty(infoImageUrl_10B)) {
                if ("审核拒绝".equals(limitStatus)) {
                    bt_idcard_b.setText("重新上传");
                }else {
                    bt_idcard_b.setText(getString(R.string.upload_sucess));
                }
                loadSignNamePic(infoImageUrl_10B, "10B");
            }
            if (!TextUtils.isEmpty(infoImageUrl_10D)) {
                if ("审核拒绝".equals(limitStatus)) {
                    bt_iccard_a.setText("重新上传");
                }else {
                    bt_iccard_a.setText(getString(R.string.upload_sucess));
                }
                loadSignNamePic(infoImageUrl_10D, "10D");
            }
            if (!TextUtils.isEmpty(infoImageUrl_10E)) {
                if ("审核拒绝".equals(limitStatus)) {
                    bt_iccard_b.setText("重新上传");
                }else {
                    bt_iccard_b.setText(getString(R.string.upload_sucess));
                }
                loadSignNamePic(infoImageUrl_10E, "10E");
            }
            if (!TextUtils.isEmpty(infoImageUrl_10F)) {
                if ("审核拒绝".equals(limitStatus)) {
                    bt_handle_idcard.setText("重新上传");
                }else {
                    bt_handle_idcard.setText(getString(R.string.upload_sucess));
                }
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
                        if ("10A".equals(imageType)) {
                            iv_idcard_a.setImageBitmap(response);
                        } else if ("10B".equals(imageType)) {
                            iv_idcard_b.setImageBitmap(response);
                        } else if ("10D".equals(imageType)) {
                            iv_iccard_a.setImageBitmap(response);
                        } else if ("10E".equals(imageType)) {
                            iv_iccard_b.setImageBitmap(response);
                        } else if ("10F".equals(imageType)) {
                            iv_handle_idcard.setImageBitmap(response);
                        }

                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.i(TAG, "图片加载失败");
                ViewUtils.makeToast(ImproveActivity.this, "服务器异常，图片加载失败", 1500);

                if (loadingDialogCanCancel != null) {
                    loadingDialogCanCancel.dismiss();
                }
            }
        });
        newRequestQueue.add(imageRequest);
    }

    private void tip() {
        tipDailog = new Dialog(this, R.style.MyProgressDialog);
        tipDailog.setContentView(R.layout.tip_dialog);
        tipDailog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) tipDailog.findViewById(R.id.confirm_btn);
        Button cancleBt = (Button) tipDailog.findViewById(R.id.cancel_btn);
        dialog_title_text = ((TextView) tipDailog.findViewById(R.id.title_text));
        dialog_title_text.setText("提额说明");
        tipDailog.setCancelable(false);
        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDailog.dismiss();
                ViewUtils.overridePendingTransitionBack(ImproveActivity.this);
            }
        });
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDailog.dismiss();
            }
        });
        tipDailog.show();
    }

    @Click({R.id.iv_idcard_a, R.id.iv_idcard_b,
            R.id.iv_iccard_a, R.id.iv_iccard_b, R.id.iv_handle_idcard,
            R.id.bt_iccard_a, R.id.bt_iccard_b, R.id.bt_idcard_a,
            R.id.bt_idcard_b, R.id.bt_handle_idcard, R.id.ll_back,
            R.id.ll_submit, R.id.iv_item_right, R.id.et_bank_name})
    void onClick(View view) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }

        switch (view.getId()) {
            case R.id.et_bank_name:
                if(!clickable){
                    return;
                }
                startActivityForResult(
                        new Intent(this, BankNameListActivity.class), 1);
                break;
            case R.id.ll_back:
                if(!checkButtonStatusInit()&&infoComplete&&!"审核拒绝".equals(limitStatus)) {
                    cancleDialog();
                }else {
                    ViewUtils.overridePendingTransitionBack(ImproveActivity.this);
                }
                break;
            case R.id.iv_idcard_a:
                id = 1;
                choseCamers();
                break;
            case R.id.bt_idcard_a:
                id = 1;
                if(!TextUtils.isEmpty(imagePaths.get(1))) {
                    confirmUpload(imagePaths.get(1), "10A");
                }else{
                   ViewUtils.makeToast(this,"请先选择照片",1500);
                }
                break;
            case R.id.iv_idcard_b:
                id = 2;
                choseCamers();
                break;
            case R.id.bt_idcard_b:
                id = 2;
                if(!TextUtils.isEmpty(imagePaths.get(2))) {
                    confirmUpload(imagePaths.get(2), "10B");
                }else{
                    ViewUtils.makeToast(this,"请先选择照片",1500);
                }
                break;
            case R.id.iv_iccard_a:
                id = 3;
                choseCamers();
                break;
            case R.id.bt_iccard_a:
                id = 3;
                if(!TextUtils.isEmpty(imagePaths.get(3))) {
                    confirmUpload(imagePaths.get(3), "10D");
                }else{
                    ViewUtils.makeToast(this,"请先选择照片",1500);
                }
                break;
            case R.id.iv_iccard_b:
                id = 4;
                choseCamers();
                break;
            case R.id.bt_iccard_b:
                id = 4;
                if(!TextUtils.isEmpty(imagePaths.get(4))) {
                    confirmUpload(imagePaths.get(4), "10E");
                }else{
                    ViewUtils.makeToast(this,"请先选择照片",1500);
                }
                break;
            case R.id.iv_handle_idcard:
                id = 5;
                choseCamers();
                break;
            case R.id.bt_handle_idcard:
                id = 5;
                if(!TextUtils.isEmpty(imagePaths.get(5))) {
                    confirmUpload(imagePaths.get(5), "10F");
                }else{
                    ViewUtils.makeToast(this,"请先选择照片",1500);
                }
                break;
            case R.id.ll_submit:
                String name = et_name.getText().toString();
                String card_number ;
                if(bindCard!= null) {
                    card_number = bindCard.getIdCardNumber();
                }else{
                    card_number = et_card_number.getText().toString();
                }
                String bank_name = et_bank_name.getText().toString();
                bank_number = et_bank_number.getText().toString();
                if (Utils.checkNameChinese(name)) {
                    ViewUtils.makeToast(this, "姓名只能是中文", 1000);
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    ViewUtils.makeToast(this, "姓名不能为空", 1000);
                    return;
                }
                if (name.length()<=1) {
                    ViewUtils.makeToast(this, "姓名长度最少为2位", 1000);
                    return;
                }
                if (name.length()>8) {
                    ViewUtils.makeToast(this, "姓名长度不能超过8位", 1000);
                    return;
                }
                if (!CommonUtils.isIdCard(card_number)) {
                    ViewUtils.makeToast(this, "身份证格式错误，请重新填写", 1000);
                    return;
                }
                if (TextUtils.isEmpty(bank_number)) {
                    ViewUtils.makeToast(this, "银行卡号不能为空", 1000);
                    return;
                }
                if (bank_number.length()<10) {
                    ViewUtils.makeToast(this, "银行卡号不得小于10位", 1000);
                    return;
                }
                if (bank_number.length()>20) {
                    ViewUtils.makeToast(this, "银行卡号不得超过20位", 1000);
                    return;
                }
                if (TextUtils.isEmpty(bank_name)) {
                    ViewUtils.makeToast(this, "银行名称不能为空", 1000);
                    return;
                }
                String bank_name_code = MyApplication.bankNameList.get(bank_name);
                // 检查网络状态
                if (CommonUtils.getConnectedType(ImproveActivity.this) == -1) {
                    ViewUtils.makeToast(ImproveActivity.this,
                            getString(R.string.nonetwork), 1500);
                    return;
                }
                if (infoComplete) {
                    if (checkButtonStatusInit()) {
                        if(getString(R.string.click_select_again).equals(bt_handle_idcard.getText().toString()) &&
                                getString(R.string.click_select_again).equals(bt_idcard_a.getText().toString()) &&
                                getString(R.string.click_select_again).equals(bt_idcard_b.getText().toString()) &&
                                getString(R.string.click_select_again).equals(bt_iccard_a.getText().toString()) &&
                                getString(R.string.click_select_again).equals(bt_iccard_b.getText().toString())) {
                           ViewUtils.makeToast(ImproveActivity.this,"请先上传照片",1500);
                        }else{
                            confirm();
                        }
                    } else {
                        ViewUtils.makeToast(ImproveActivity.this, "图片必须全部上传", 1000);
                    }
                } else {
                    sendSubmit(name, bank_name_code, card_number, bank_number
                    );
                }

                break;

        }
    }

    private void cancleDialog() {
        cancleDialog = new Dialog(this, R.style.MyProgressDialog);
        cancleDialog.setContentView(R.layout.tip_dialog);
        cancleDialog.setCanceledOnTouchOutside(false);
        cancleDialog.setCancelable(false);
        Button dialog_confirmBt = (Button) cancleDialog.findViewById(R.id.confirm_btn);
        Button cancleBt = (Button) cancleDialog.findViewById(R.id.cancel_btn);
        dialog_title_text = ((TextView) cancleDialog.findViewById(R.id.title_text));
        View tip = cancleDialog.findViewById(R.id.ll_tip);
        tip.setVisibility(View.GONE);
        dialog_title_text.setText("确认取消提额申请");
        cancleBt.setText("确认取消");
        dialog_confirmBt.setText("继续申请");
        cancleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean upState = checkButtonStatusInit();
                if(infoComplete&&!upState) {
                    cancleImprove();
                }
                cancleDialog.dismiss();
                ViewUtils.overridePendingTransitionBack(ImproveActivity.this);
            }
        });
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancleDialog.dismiss();
            }
        });
        cancleDialog.show();

    }

    /**
     * 取消交易
     */
    private void cancleImprove() {
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        String bankNumber = et_bank_number.getText().toString();
        String customerNum = StorageCustomerInfoUtil.getInfo("customerNum", this);
        requestData.put(0, "0700");
        requestData.put(3, "190943");
        requestData.put(42, customerNum);
        requestData.put(7, bankNumber);
        requestData.put(59, Constant.VERSION);
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {

            @Override
            public void isLoadingContent() {
            }

            @Override
            public void isLoadedContent(String content) {
                LogUtil.syso("content==" + content);
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.responseCodeMap
                            .get(result);
                    if ("00".equals(result)) {
                    }else{
                        if(!TextUtils.isEmpty(resultValue)) {
                            ViewUtils.makeToast(ImproveActivity.this, resultValue, 1500);
                        }else{
                            ViewUtils.makeToast(ImproveActivity.this,"操作失败,错误码："+result,1500);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d("BinfCardList", "url==" + url);


    }

    private void confirm() {
        tipDailog = new Dialog(this, R.style.MyProgressDialog);
        tipDailog.setContentView(R.layout.chose_dialog);
        tipDailog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) tipDailog.findViewById(R.id.left_bt);
        Button cancleBt = (Button) tipDailog.findViewById(R.id.right_bt);
        cancleBt.setVisibility(View.GONE);
        dialog_title_text = ((TextView) tipDailog.findViewById(R.id.title_text));
        dialog_title_text.setText(R.string.improveconfirm);
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDailog.dismiss();
                String count = "1";
                StorageCustomerInfo02Util.putInfo(ImproveActivity.this, "cardsize", count);
                ViewUtils.overridePendingTransitionBack(ImproveActivity.this);

            }
        });
        tipDailog.show();

    }

    private boolean checkButtonStatusInit() {
        return ( getString(R.string.upload_sucess).equals(bt_handle_idcard.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_a.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_b.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_iccard_a.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_iccard_b.getText().toString())  ||
                        getString(R.string.click_select_again).equals(bt_handle_idcard.getText().toString()) |
                        getString(R.string.click_select_again).equals(bt_idcard_a.getText().toString()) |
                        getString(R.string.click_select_again).equals(bt_idcard_b.getText().toString()) |
                        getString(R.string.click_select_again).equals(bt_iccard_a.getText().toString()) |
                        getString(R.string.click_select_again).equals(bt_iccard_b.getText().toString()));
    }

    private boolean checkButtonStatusInit2() {
        return ( getString(R.string.upload_sucess).equals(bt_handle_idcard.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_a.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_idcard_b.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_iccard_a.getText().toString()) &&
                getString(R.string.upload_sucess).equals(bt_iccard_b.getText().toString()));
    }

    private void sendSubmit(final String name, final String bank_name_code, final String card_number, final String bank_number) {

        String url = Constant.REQUEST_API;
        loadingDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String arg0) {

                        loadingDialog.dismiss();
                        LogUtil.i("ImproveActivity", arg0);
                        try {
                            JSONObject obj = new JSONObject(arg0);
                            String result = (String) obj.get("39");
                            String resultValue = MyApplication.responseCodeMap
                                    .get(result);
                            if ("00".equals(result)) {
                                ViewUtils.makeToast(ImproveActivity.this,
                                        "请按要求上传照片", 1500);
                                infoComplete = true;
                                clickable = false;
                                et_name.setFocusable(false);
                                et_card_number.setFocusable(false);
                                et_bank_number.setFocusable(false);
                                et_bank_name.setFocusable(false);
                                ll_bank_name.setFocusable(false);
                                ll_idCard.setVisibility(View.VISIBLE);
                                ll_icCard.setVisibility(View.VISIBLE);

                            } else {
                                if (!TextUtils.isEmpty(resultValue)) {
                                    ViewUtils.makeToast(
                                            ImproveActivity.this,
                                            resultValue, 1000);
                                } else {
                                    ViewUtils.makeToast(
                                            ImproveActivity.this, "未知错误，错误码："+result,
                                            1000);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ViewUtils.makeToast(ImproveActivity.this,
                                    "数据解析异常", 1000);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                loadingDialog.dismiss();
                ViewUtils.makeToast(ImproveActivity.this, "系统异常",
                        1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
                        ImproveActivity.this);
                map.put("0", "0700");
                map.put("3", "190925");
                map.put("5", name);
                map.put("6", card_number);
                map.put("7", bank_number);
                map.put("42", customerNum);
                map.put("43", bank_name_code);
                map.put("59", Constant.VERSION);
                map.put("64",
                        CommonUtils.Md5("0700" + "190925"
                                + name + card_number + bank_number + customerNum
                                + bank_name_code + Constant.VERSION
                                + Constant.mainKey));
                LogUtil.syso("request:" + Constant.getUrl2(map));
                return map;
            }
        };
        newRequestQueue.add(stringRequest);
    }

    /**
     * 资质上传
     *
     * @param signPath 本地图片路径
     */
    private void confirmUpload(String signPath, final String imageType) {
        // 检查网络状态
        if (CommonUtils.getConnectedType(ImproveActivity.this) == -1) {
            ViewUtils.makeToast(ImproveActivity.this,
                    getString(R.string.nonetwork), 1500);
            return;
        }
        loadingDialog = ViewUtils.createLoadingDialog(this, getString(R.string.loading_wait), false);
        loadingDialog.show();
        UploadFileAAsyncTask asyncTask_pic = new UploadFileAAsyncTask();
        asyncTask_pic.execute(signPath, imageType);

    }

    private void choseCamers() {
        File file = new File(Environment.getExternalStorageDirectory(), "textphoto.jpg");
        if (file.exists()) file.delete();
        outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CONSULT_DOC_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String selectBankname = data.getStringExtra("selectBankname");
            et_bank_name.setText(selectBankname);
        } else if (requestCode == CONSULT_DOC_PICTURE) {
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
            bitmap = ImageUtils.scaleImg(bitmap, 720, 1080);//压缩图片
            String path = saveImage(bitmap);//保存图片返回路径
            setImage(path);//根据路径设置图片
        } else if (requestCode == CONSULT_DOC_CAMERA) {
            Bitmap bitmap = ImageUtils.getZoomBitmap(outputFileUri.getPath(), null);
            if (bitmap == null) return;
            String path = saveImage(bitmap);
            LogUtil.syso("path:" + path);
            setImage(path);
        } else {
            ViewUtils.makeToast(this, "请重新选择图片", 1000);
        }
    }


    private String saveImage(Bitmap bitmap) {
        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + ImproveActivity.this.getPackageName() + "/Image/";
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            boolean isSuccess = dirFile.mkdirs();
            LogUtil.d(TAG, "isSuccess=" + isSuccess);
        }
        File oldFile = new File(dirPath + id + ".jpg");//删除旧数据
        if (oldFile.exists()) oldFile.delete();
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
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();
        String imageUrl = ImageDownloader.Scheme.FILE.wrap(imagePath);
        switch (id) {
            case 1:
                imageLoader.displayImage(imageUrl, iv_idcard_a, options);
                break;
            case 2:
                imageLoader.displayImage(imageUrl, iv_idcard_b, options);
                break;
            case 3:
                imageLoader.displayImage(imageUrl, iv_iccard_a, options);
                break;
            case 4:
                imageLoader.displayImage(imageUrl, iv_iccard_b, options);
                break;
            case 5:
                imageLoader.displayImage(imageUrl, iv_handle_idcard, options);
                break;
        }
    }


    private class UploadFileAAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String str = params[0];
            LogUtil.d(TAG, "imagePath " + str);
            String imageType = params[1];
            File file = new File(str);
            String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
                    ImproveActivity.this);
            if(bindCard!= null){
                bank_number = bindCard.getBankAccount();
            }else{
                bank_number = et_bank_number.getText().toString();
            }
            String data = "0=0700" + "&3=190956" + "&7=" + bank_number + "&9=" + imageType + "&42=" + customerNum + "&59="
                    + Constant.VERSION;
            String macData = "0700" + "190956" + bank_number + imageType +
                    customerNum + Constant.VERSION;
            String url = Constant.UPLOADIMAGE + data + "&64="
                    + CommonUtils.Md5(macData + Constant.mainKey);
            String result = ImageUtils.uploadFile(file, url);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {// 处理UI
            super.onPostExecute(result);
            loadingDialog.dismiss();
            LogUtil.syso("图片上传返回：" + result);
            try {
                if (StringUtil.isEmpty(result)) {
                    ViewUtils.makeToast(ImproveActivity.this,
                            getString(R.string.server_error), 1500);
                    return;
                }
                JSONObject jsonResult = new JSONObject(result);

                String strCode = jsonResult.getString("39");
                String resultValue = MyApplication.getErrorHint(result);
                if ("00".equals(strCode)) {// 上传成功
                    StorageCustomerInfo02Util.putInfo(ImproveActivity.this, "examineState", "");//针对风控审核不通过的情况，只要成功重新上传一张图片就通过
                    ViewUtils.makeToast(ImproveActivity.this, getString(R.string.upload_sucess), 1000);
                    modifyButtonStatus(getString(R.string.upload_sucess));
                    String imageUrl = jsonResult.getString("57");
                    saveImagePath(imageUrl);
                } else {// 上传失败
                    ViewUtils.makeToast(ImproveActivity.this, getString(R.string.server_error), 1000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ViewUtils.makeToast(ImproveActivity.this, getString(R.string.server_error), 1000);
            }
        }
    }

    private void saveImagePath(String imageUrl) {
        switch (id) {
            case 1:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10A", imageUrl);
                break;
            case 2:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10B", imageUrl);
                break;
            case 3:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10D", imageUrl);
                break;
            case 4:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10E", imageUrl);
                break;
            case 5:
                StorageCustomerInfo02Util.putInfo(this, "infoImageUrl_10F", imageUrl);
                break;
        }
    }

    private void modifyButtonStatus(String buttonStatus) {
        switch (id) {
            case 1:
                bt_idcard_a.setText(buttonStatus);
                break;
            case 2:
                bt_idcard_b.setText(buttonStatus);
                break;
            case 3:
                bt_iccard_a.setText(buttonStatus);
                break;
            case 4:
                bt_iccard_b.setText(buttonStatus);
                break;
            case 5:
                bt_handle_idcard.setText(buttonStatus);
                break;
        }



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!checkButtonStatusInit()) {
                cancleDialog();
            }else {
                ViewUtils.overridePendingTransitionBack(ImproveActivity.this);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

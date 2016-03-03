package com.caihongcity.com.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.caihongcity.com.R;


public class CustomDialog extends Dialog implements OnClickListener {

    private  String bankCardNumber;
    private  String moneyValue;
    private GridPasswordView gridpassword;
    public static PopupWindow pop;
    private int[] a;
    public static boolean falg = false;
    private String passwordStr = "";

    int layoutRes;
    Context context;
    private Button confirmBtn;
    private Button cancelBtn;
    TextView tv_money;
    TextView tv_bank_card_number;

    private InputDialogListener mDialogListener;
    private Button confirmBtnFalse;

    public interface InputDialogListener {
        void onOK(String text);
        void onCancel();
    }

    public void setListener(InputDialogListener inputDialogListener) {
        this.mDialogListener = inputDialogListener;
    }

    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }

    public CustomDialog(Context context, int resLayout) {
        super(context);
        this.context = context;
        this.layoutRes = resLayout;
    }
    public CustomDialog(Context context, int theme, int resLayout) {
        super(context, theme);
        this.context = context;
        this.layoutRes = resLayout;
    }
    public CustomDialog(Context context, int theme, int resLayout,String moneyValue,String bankCardNumber) {
        super(context, theme);
        this.context = context;
        this.layoutRes = resLayout;
        this.moneyValue = moneyValue;
        this.bankCardNumber = bankCardNumber;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setContentView(layoutRes);

        gridpassword = (GridPasswordView) findViewById(R.id.password);
        gridpassword.setOnPasswordChangedListener(passlistener);
        confirmBtnFalse = (Button) findViewById(R.id.confirm_btn_fasle);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        if (!TextUtils.isEmpty(moneyValue) && !TextUtils.isEmpty(bankCardNumber)) {
            tv_money = (TextView) findViewById(R.id.tv_money);
            tv_bank_card_number = (TextView) findViewById(R.id.tv_bank_card_number);
            tv_money.setText(moneyValue);
            tv_bank_card_number.setText(bankCardNumber);
        }


//        cancelBtn.setTextColor(0xff000000);
        // 判断密码长度是否满足6位， 如果不满足 确定按钮不能点击，文字颜色变灰色
//        if (passwordStr.length() != 6) {
//            confirmBtn.setEnabled(false);
//            confirmBtn.setTextColor(Color.GRAY);
//        }
        // 确定按钮点击事件
        confirmBtn.setOnClickListener(this);

        // 取消按钮点击事件
        cancelBtn.setOnClickListener(this);
        //
        gridpassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int view_id = v.getId();
        switch (view_id) {
            case R.id.confirm_btn:
                if (mDialogListener != null) {
                    mDialogListener.onOK(passwordStr);
                    dismiss();
                }
                break;
            case R.id.cancel_btn:
                if (mDialogListener != null) {
                    mDialogListener.onCancel();
                    dismiss();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 监听输入的密码
     */
    GridPasswordView.OnPasswordChangedListener passlistener = new GridPasswordView.OnPasswordChangedListener() {

        // 密码
        @Override
        public void onMaxLength(String psw) {
            // 获取密码
            passwordStr = psw;
        }

        // 密码长度
        @Override
        public void onChanged(String psw) {
            if (0< psw.length()&&psw.length()< 6) {
                confirmBtn.setEnabled(false);
                confirmBtn.setVisibility(View.GONE);
                confirmBtnFalse.setVisibility(View.VISIBLE);
            } else {
                confirmBtn.setEnabled(true);
                confirmBtnFalse.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.VISIBLE);
            }
        }

    };
}
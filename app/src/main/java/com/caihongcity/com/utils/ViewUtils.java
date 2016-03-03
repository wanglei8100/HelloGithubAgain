package com.caihongcity.com.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caihongcity.com.R;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.Timer;
import java.util.TimerTask;


public class ViewUtils {
    /**
     * 自定义吐司
     *
     * @param context  上下文
     * @param text     吐司内容
     * @param duration 显示时长
     */
    public static void makeToast01(Context context,
                                   String text, int duration) {
        Toast result = new Toast(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_layout, null);
        TextView textView = (TextView) layout.findViewById(R.id.toast_text);
        textView.setText(text);
        result.setView(layout);
        result.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        result.setDuration(duration);
        result.show();
    }

    /**
     * 自定义吐司
     *
     * @param context  上下文
     * @param text     吐司内容
     * @param duration 显示时长
     */
    public static void makeToast(Context context,
                                 String text, int duration) {
        /*Toast result = new Toast(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.toast_layout, null);
		TextView textView = (TextView) layout.findViewById(R.id.toast_text);
		textView.setText(text);
		result.setView(layout);
		result.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		result.setDuration(duration);
		result.show();*/

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_toast_layout, null);
        TextView chapterNameTV = (TextView) view.findViewById(R.id.chapterName);
        chapterNameTV.setText(text);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setDuration(duration);
        toast.setView(view);
        toast.show();
    }

    /**
     * 显示文字的 Toast,以及Intent跳转
     *
     * @param context
     * @param text
     * @param duration
     * @param intentClassName 不为空表示不关闭当前
     * @param intentType      为""表示不跳转
     */
    public static void makeToast2(final Activity context,
                                  CharSequence text, int duration,
                                  final Class<?> intentClassName, final String intentType) {
        if (!TextUtils.isEmpty(text)) {
            Toast result = new Toast(context);

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.toast_layout, null);
            TextView textView = (TextView) layout.findViewById(R.id.toast_text);
            textView.setText(text);

            result.setView(layout);
            result.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            result.setDuration(duration);
            result.show();
        }


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                if (null == intentClassName) {
                    context.finish();
                    return;
                }
                if (!(null == intentClassName) && !"".equals(intentType)) {
                    Intent intent = context
                            .getPackageManager()
                            .getLaunchIntentForPackage(context.getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    ActivityManager.exit2();
                    return;
                }
            }
        }, 2000);
    }


    public static void overridePendingTransitionCome(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public static void overridePendingTransitionBack(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg, boolean isCancel) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view  
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局  
        // main.xml中的ImageView  
        CircleProgressBar img = (CircleProgressBar) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        img.setColorSchemeResources(R.color.title_bg);

        tipTextView.setText(msg);// 设置加载信息  

        Dialog loadingDialog = new Dialog(context, R.style.MyProgressDialog);// 创建自定义样式dialog  
        loadingDialog.setCancelable(isCancel);// 可以用“返回键”取消  
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局  
        return loadingDialog;

    }


    /**
     * 选择提示框
     *
     * @param context                    上下文
     * @param isCanceledOnTouchOutside   是否可以点击其他区域
     * @param msg                        提示内容
     * @param onChoseDialogClickCallback 点击事件的回调
     * @param cancelVisibale             取消按键是否可视
     * @return
     */
    public static Dialog showChoseDialog(Context context, boolean isCanceledOnTouchOutside, String msg, int cancelVisibale,
                                         final OnChoseDialogClickCallback onChoseDialogClickCallback) {
        final Dialog dialog = new Dialog(context, R.style.MyProgressDialog);
        dialog.setContentView(R.layout.chose_dialog);
        dialog.setCanceledOnTouchOutside(false);
        Button confirmBt = (Button) dialog.findViewById(R.id.left_bt);
        Button cancleBt = (Button) dialog.findViewById(R.id.right_bt);
        dialog.findViewById(R.id.verticalbars_iv).setVisibility(cancelVisibale);
        cancleBt.setVisibility(cancelVisibale);
        ((TextView) dialog.findViewById(R.id.title_text)).setText(msg);
        cancleBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChoseDialogClickCallback != null) {
                    dialog.dismiss();
                    onChoseDialogClickCallback.clickCancel();
                } else {
                    dialog.dismiss();
                }
            }
        });
        confirmBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChoseDialogClickCallback != null) {
                    dialog.dismiss();
                    onChoseDialogClickCallback.clickOk();
                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        return dialog;
    }

    /**
     * 选择提示框
     *
     * @param context                    上下文
     * @param isCanceledOnTouchOutside   是否可以点击其他区域
     * @param msg                        提示内容
     * @param onChoseDialogClickCallback 点击事件的回调
     * @param cancelDes 取消按钮文字设置
     * @param confrimDes 确定按钮文字设置
     * @return
     */
    public static Dialog showChoseDialog02(Context context, boolean isCanceledOnTouchOutside, String msg, String cancelDes, String confrimDes,
                                           final OnChoseDialogClickCallback onChoseDialogClickCallback) {
        final Dialog dialog = new Dialog(context, R.style.MyProgressDialog);
        dialog.setContentView(R.layout.chose_dialog);
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);

        Button confirmBt = (Button) dialog.findViewById(R.id.left_bt);
        Button cancleBt = (Button) dialog.findViewById(R.id.right_bt);
        ((TextView) dialog.findViewById(R.id.title_text)).setText(msg);
        confirmBt.setText(confrimDes);
        cancleBt.setText(cancelDes);

        cancleBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChoseDialogClickCallback != null) {
                    dialog.dismiss();
                    onChoseDialogClickCallback.clickCancel();
                } else {
                    dialog.dismiss();
                }
            }
        });
        confirmBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChoseDialogClickCallback != null) {
                    dialog.dismiss();
                    onChoseDialogClickCallback.clickOk();
                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        return dialog;
    }

    public interface OnChoseDialogClickCallback {
        public void clickOk();

        public void clickCancel();
    }

    public static Dialog showHintDialog(Context context, String msg) {
        final Dialog dialog = new Dialog(context, R.style.MyProgressDialog);
        dialog.setContentView(R.layout.hint_dialog);
        dialog.setCanceledOnTouchOutside(true);
        ImageButton ib_close = (ImageButton) dialog.findViewById(R.id.ib_close);
        TextView tv_hint_des = (TextView) dialog.findViewById(R.id.tv_hint_des);
        tv_hint_des.setText(msg);
        ib_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    dialog.dismiss();
            }
        });
        dialog.show();
        return dialog;
    }



}

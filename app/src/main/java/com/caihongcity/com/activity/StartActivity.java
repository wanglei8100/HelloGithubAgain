package com.caihongcity.com.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.caihongcity.com.R;
import com.caihongcity.com.db.NotificationData;
import com.caihongcity.com.utils.ActivityManager;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.litepal.crud.DataSupport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjigong 首页面
 */
@EActivity(R.layout.start)
public class StartActivity extends BaseActivity {

    @ViewById
    ViewPager viewPager;
    @ViewById
    ImageView iv_notification;

    private long firstime;
    int[] imageIds = new int[]{R.drawable.start_img1, R.drawable.start_img2, R.drawable.start_img3, R.drawable.start_img4};

    PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViews.get(position));
            return imageViews.get(position);
        }
    };
    ArrayList<ImageView> imageViews;
    ArrayList<View> dots;
    private int currentItem = 0; // 当前图片的索引号
    private String bankAccount;
    private String TAG = "StartActivity";
    private String source = "APP";
    private String freezeStatus;
    private boolean recheck = false;
    private Dialog checkDialog;
    private Button dialog_confirmBt;
    private TextView dialog_title_text;

    @AfterViews
    void initDate() {
        //获取审核状态
        freezeStatus = StorageCustomerInfo02Util.getInfo("freezeStatus",
                StartActivity.this);
        //判断用户是否是重新审核状态
        if ("10D".equals(freezeStatus)){
            recheck = true;
            checkDialog();
        }

        imageViews = new ArrayList<ImageView>();
        for (int i = 0; i < imageIds.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(imageIds[i]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageViews.add(imageView);
        }
        dots = new ArrayList<View>();
        dots.add(findViewById(R.id.v_dot0));
        dots.add(findViewById(R.id.v_dot1));
        dots.add(findViewById(R.id.v_dot2));
        dots.add(findViewById(R.id.v_dot3));
        viewPager.setAdapter(mPagerAdapter);
        // 设置一个监听器，当ViewPager中的页面改变时调用
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
        try {//设置Viewpager的滚动速度
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(),
                    new AccelerateInterpolator());
            field.set(viewPager, scroller);
            scroller.setmDuration(1000);
        } catch (Exception e) {
            LogUtil.e("FixedSpeedScroller", e.toString());
        }

        //判断用户是否从app登陆
        source = StorageCustomerInfo02Util.getInfo("source", StartActivity.this);
        //新通告提示
        String newNotificationId = StorageAppInfoUtil.getInfo("newNotificationId", context);
        if (!TextUtils.isEmpty(newNotificationId)) {
            List<NotificationData> notificationDatas = DataSupport.where("notificationId = ?", newNotificationId).find(NotificationData.class);
            if (notificationDatas.size()>0){
                final NotificationData notificationData = notificationDatas.get(0);
                if (!notificationData.isNotificationIsRead()){
                    ViewUtils.showChoseDialog02(context, true, "查看新的公告", "先不看了", "查看", new ViewUtils.OnChoseDialogClickCallback() {
                        @Override
                        public void clickOk() {
                            Intent intent = new Intent(context,NotificationDetailActivity_.class);
                            intent.putExtra("title",notificationData.getNotificationTitle());
                            intent.putExtra("id",notificationData.getNotificationId());
                            intent.putExtra("content",notificationData.getNotificationContent());
                            startActivity(intent);
                            ViewUtils.overridePendingTransitionCome(context);
                        }

                        @Override
                        public void clickCancel() {

                        }
                    });
                }
            }
        }
    }

    /**
     * 风控审核提示框
     */
    private void checkDialog() {
        checkDialog = new Dialog(StartActivity.this, R.style.MyProgressDialog);
        checkDialog.setContentView(R.layout.chose_dialog_upload);
        checkDialog.setCanceledOnTouchOutside(false);
        dialog_confirmBt = (Button) checkDialog.findViewById(R.id.left_bt);
        Button cancleButton = (Button) checkDialog.findViewById(R.id.right_bt);
        cancleButton.setVisibility(View.GONE);
        dialog_title_text = ((TextView) checkDialog.findViewById(R.id.title_text));
        dialog_title_text.setText("您的信息已经重新提交，我们正在加紧审核，请稍侯");
        dialog_confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmBt_des = dialog_confirmBt.getText().toString();
                if ("确定".equals(confirmBt_des)) {
                    checkDialog.dismiss();
                }


            }
        });
        checkDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        List<NotificationData> notificationDataList = DataSupport.findAll(NotificationData.class);
        iv_notification.setBackgroundResource(R.drawable.no_new_msg);
        for (int i = 0; i < notificationDataList.size(); i++) {
            if (!notificationDataList.get(i).isNotificationIsRead()){
                iv_notification.setBackgroundResource(R.drawable.have_new_msg);
                break;
            }
        }
    }

    @Click({R.id.ll_consume, R.id.ll_realtime, R.id.ll_tradelist, R.id.setting, R.id.ll_querybalance, R.id.ll_transfer
            , R.id.ll_creditrepay, R.id.ll_customerinfo, R.id.ll_cardchongzhi, R.id.ll_dianping, R.id.ll_dianping, R.id.ll_gupiao
            , R.id.ll_jiaoche, R.id.ll_jipiao, R.id.ll_lvyou, R.id.ll_phonechongzhi, R.id.ll_shuidianmei, R.id.ll_tuangou,
            R.id.ll_rongtongbao, R.id.ll_notification, R.id.ll_oprate_declar,R.id.ll_improve})
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }

        int id = v.getId();
        switch (id) {
            case R.id.ll_consume:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }

                if (!checkCustomerInfoComplete()) {
                    ViewUtils.showChoseDialog(StartActivity.this, true, "请先进入商户信息进行实名认证",
                            View.GONE, null);
                    return;
                }
                Intent intent_consume = new Intent();
                intent_consume.setClass(this, SelectFeeRuleActivity.class);
                intent_consume.putExtra("tradetype", "consume");
                startActivity(intent_consume);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_customerinfo:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }

                Intent intent_customerinfo = new Intent(this, CustomerinfoActivity.class);
                intent_customerinfo.putExtra("isInfoComplete", checkCustomerInfoComplete());
                startActivity(intent_customerinfo);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_tradelist:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                Intent intent_tradelist = new Intent();
                if(!"APP".equals(source)){
                    intent_tradelist.setClass(this, QueryOldActivity.class);
                }else{
                    intent_tradelist.setClass(this, QueryActivity.class);
                }
                startActivity(intent_tradelist);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.setting:
                Intent intent_setting = new Intent();
                intent_setting.setClass(this, SettingActivity.class);
                startActivity(intent_setting);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_querybalance:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }
                if (!checkCustomerInfoComplete()) {
                    ViewUtils.showChoseDialog(StartActivity.this, true, "请先进入商户信息进行实名认证",
                            View.GONE, null);
                    return;
                }
                Intent intent_querybalance = new Intent();
                String terminal_type = StorageAppInfoUtil.getInfo("terminal_type", this);
                String bluetooth_address = StorageAppInfoUtil.getInfo("bluetooth_address", this);
                if ("1".equals(terminal_type)) {
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent_querybalance.setClass(this, BluetoothSelectActivity_.class);
                    } else {
                        intent_querybalance.setClass(this, SwipeWaitBluetoothActivity_.class);
                        intent_querybalance.putExtra("blue_address", bluetooth_address);
                    }

                } else if ("2".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitHuiXingActivity_.class);
                } else if ("3".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitMoFangActivity_.class);
                } else if ("4".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitZhongCiActivity_.class);
                } else if ("5".equals(terminal_type)) {
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent_querybalance.setClass(this, BluetoothSelectActivity_.class);
                    } else {
                        intent_querybalance.setClass(this, SwipeWaitMoFangBlueActivity_.class);
                        intent_querybalance.putExtra("blue_address", bluetooth_address);
                    }
                } else if ("6".equals(terminal_type)) {
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent_querybalance.setClass(this, BluetoothSelectActivity_.class);
                    } else {
                        intent_querybalance.setClass(this, SwipeWaitYiFengBlueActivity_.class);
                        intent_querybalance.putExtra("blue_address", bluetooth_address);
                    }
                } else if ("7".equals(terminal_type)) {
                    if (TextUtils.isEmpty(bluetooth_address)) {
                        intent_querybalance.setClass(this, BluetoothSelectActivity_.class);
                    } else {
                        intent_querybalance.setClass(this, SwipeWaitXinNuoBlueActivity_.class);
                        intent_querybalance.putExtra("blue_address", bluetooth_address);
                    }
                } else if ("8".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitBBPoseBuleActivity_.class);
                }else if ("9".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitBBPoseActivity_.class);
                }else if ("10".equals(terminal_type)) {
                    intent_querybalance.setClass(this, SwipeWaitXinLianDaActivity_.class);
                } else {
                    intent_querybalance.setClass(this, SwipeWaitActivity.class);
                }
                intent_querybalance.putExtra("tradetype", "querybalance");
                intent_querybalance.putExtra("feeRate", Constant.SUPERMAKET_FEERATE);
                startActivity(intent_querybalance);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_realtime:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }
                startActivity(new Intent(this,RealTimeActivity.class));
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_transfer:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_cardchongzhi://使用说明书
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_dianping:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_gupiao:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_jiaoche:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_jipiao:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_lvyou:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_phonechongzhi:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_shuidianmei:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_tuangou:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_improve:
                if(recheck){
                    ViewUtils.makeToast(this,"您的信息已经重新提交，我们正在加紧审核，请稍侯",1500);
                    return;
                }
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }
                Intent intent_creditrepay = new Intent();
                intent_creditrepay.setClass(this, BindCardListActivity_.class);
                startActivity(intent_creditrepay);
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_creditrepay:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_rongtongbao:
                ViewUtils.makeToast(this, "暂未开放", 1000);
                break;
            case R.id.ll_oprate_declar:
                if(!"APP".equals(source)){
                    ViewUtils.makeToast(this,"暂未开放",1500);
                    return;
                }
                startActivity(new Intent(this,InstructionsAct_.class));
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            case R.id.ll_notification://通知公告
                startActivity(new Intent(this,NotificationListActivity_.class));
                ViewUtils.overridePendingTransitionCome(StartActivity.this);
                break;
            default:
                break;
        }

    }




    /**
     * 判断是否完成实名认证
     *
     * @return
     */
    private boolean checkCustomerInfoComplete() {
        bankAccount = StorageCustomerInfo02Util.getInfo("bankAccount",
                StartActivity.this);//用于判断是否进行过实名认证
        String infoImageUrl_10M = StorageCustomerInfo02Util.getInfo("infoImageUrl_10M", this);
        String infoImageUrl_10E = StorageCustomerInfo02Util.getInfo("infoImageUrl_10E", this);
        String infoImageUrl_10F = StorageCustomerInfo02Util.getInfo("infoImageUrl_10F", this);
        if (TextUtils.isEmpty(bankAccount) || TextUtils.isEmpty(infoImageUrl_10M) ||
                TextUtils.isEmpty(infoImageUrl_10E) || TextUtils.isEmpty(infoImageUrl_10F)) {

            return false;
        }
        return true;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            viewPager.setCurrentItem(currentItem);// 切换当前显示的图片
        }
    };
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    protected void onStart() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 当Activity显示出来后，每两秒钟切换一次图片显示
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 5, TimeUnit.SECONDS);
        super.onStart();
    }

    /**
     * 换行切换任务
     *
     * @author Administrator
     */
    private class ScrollTask implements Runnable {

        public void run() {
            synchronized (viewPager) {
                System.out.println("currentItem: " + currentItem);
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget(); // 通过Handler切换图片
            }
        }

    }

    @Override
    protected void onStop() {
        // 当Activity不可见的时候停止切换
        scheduledExecutorService.shutdown();
        super.onStop();
    }

    /**
     * 当ViewPager中页面的状态发生改变时调用
     *
     * @author Administrator
     */
    class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        private int oldPosition = 0;

        /**
         * This method will be invoked when a new page becomes selected.
         * position: Position index of the new selected page.
         */
        public void onPageSelected(int position) {
            currentItem = position;
            dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
            dots.get(position).setBackgroundResource(R.drawable.dot_focused);
            oldPosition = position;
        }

        public void onPageScrollStateChanged(int arg0) {

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }
    }

    public class FixedSpeedScroller extends Scroller {
        private int mDuration = 1500;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setmDuration(int time) {
            mDuration = time;
        }

        public int getmDuration() {
            return mDuration;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /** 设置双击退出 */
            long secondtime = System.currentTimeMillis();
            if (secondtime - firstime > 3000) {
                // ViewUtils.makeToast(this,"再按一次返回键退出", 1000);
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                firstime = System.currentTimeMillis();
                return true;
            } else {
                finish();
                ActivityManager.exit();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}

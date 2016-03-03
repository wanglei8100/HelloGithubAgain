package com.caihongcity.com.activity;

import android.view.View;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.db.NotificationData;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Administrator on 2015/10/29 0029.
 */
@EActivity(R.layout.activity_notification_detail)
public class NotificationDetailActivity extends BaseActivity{
    @ViewById
    TextView tv_title_des,tv_title,tv_content;
    @Extra
    String title,content,id;
    @AfterViews
    void initView() {
        tv_title_des.setText("公告详情");
        tv_title.setText(title);
        tv_content.setText(content);
        NotificationData notificationData = new NotificationData();
        notificationData.setNotificationIsRead(true);
        notificationData.updateAll("notificationId=?", id);
    }


    @Click({R.id.ll_back, R.id.rl_operate_declare, R.id.rl_rate_declare, R.id.rl_tixian_declare, R.id.rl_error_reminder})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                ViewUtils.overridePendingTransitionBack(context);
                break;
        }
    }
}

package com.caihongcity.com.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Administrator on 2015/10/28 0028.
 */
@EActivity(R.layout.activity_instructions)
public class InstructionsAct extends BaseActivity {
    @ViewById
    TextView tv_title_des;

    @AfterViews
    void initView() {
        tv_title_des.setText("说明书");
    }

    @Click({R.id.ll_back, R.id.rl_operate_declare, R.id.rl_rate_declare, R.id.rl_tixian_declare, R.id.rl_error_reminder})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                ViewUtils.overridePendingTransitionBack(context);
                break;
            case R.id.rl_operate_declare:
                toDetail("operate_declare");
                break;
            case R.id.rl_rate_declare:
                toDetail("rate_declare");
                break;
            case R.id.rl_tixian_declare:
                toDetail("tixian_declare");
                break;
            case R.id.rl_error_reminder:
                toDetail("error_reminder");
                break;
        }

    }

    private void toDetail(String type) {
        Intent intent = new Intent(context,InstructionsDetailAct_.class);
        intent.putExtra("type", type);
        startActivity(intent);
        ViewUtils.overridePendingTransitionCome(context);
    }
}

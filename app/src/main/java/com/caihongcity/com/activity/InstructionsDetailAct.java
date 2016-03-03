package com.caihongcity.com.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.view.ZoomImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Administrator on 2015/10/28 0028.
 */
@EActivity(R.layout.activity_instructions_detail)
public class InstructionsDetailAct extends BaseActivity {
    @ViewById
    TextView tv_title_des;
    @ViewById
    LinearLayout ll_instructions_operate_declare;
    @ViewById
    ZoomImageView iv_instructions;
    @Extra
    String type;
//    Map<View,int[]> recycleViews = new HashMap<>();
    @AfterViews
    void initView() {
//        recycleViews.put(iv_instructions,new int[]{R.id.iv_instructions});
        if ("operate_declare".equals(type)) {
            tv_title_des.setText("操作说明");
            iv_instructions.setVisibility(View.GONE);
            ll_instructions_operate_declare.setVisibility(View.VISIBLE);
        } else if ("rate_declare".equals(type)) {
            tv_title_des.setText("费率说明");
            iv_instructions.setImageResource(R.drawable.instructions_rate_declare);
        } else if ("tixian_declare".equals(type)) {
            tv_title_des.setText("提现说明");
            iv_instructions.setImageResource(R.drawable.instructions_tixian_declare);
        } else if ("error_reminder".equals(type)) {
            tv_title_des.setText("错误提示");
            iv_instructions.setImageResource(R.drawable.instructions_error_reminder);
        }
    }

    @Click({R.id.ll_back})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                ViewUtils.overridePendingTransitionBack(context);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        RecycleBitmap.recycle(recycleViews);
    }
}

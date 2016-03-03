package com.caihongcity.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.ViewUtils;

public class LaunchActivity extends BaseActivity implements Animation.AnimationListener {

    private ImageView iv_launch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        iv_launch = (ImageView) findViewById(R.id.iv_launch);
        /** 设置缩放动画 */
        final ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        animation.setDuration(2000);
        iv_launch.setAnimation(animation);
        /** 开始动画 */
        animation.startNow();
        animation.setAnimationListener(this);
    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity_.class);
        startActivity(intent);
        ViewUtils.overridePendingTransitionCome(LaunchActivity.this);
        finish();

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}

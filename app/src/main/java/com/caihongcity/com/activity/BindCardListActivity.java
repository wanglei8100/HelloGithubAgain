package com.caihongcity.com.activity;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.pager.BindCardListPager;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_bind_card_list)
public class BindCardListActivity extends BaseActivity {
    @ViewById
    TextView tv_title_des, tv_1, tv_2, tv_3;
    @ViewById
    ViewPager vp_pager_content;
    private List<BindCardListPager> pagerList = new ArrayList<BindCardListPager>();

    @AfterViews
    void initData() {
        tv_title_des.setText("绑定列表");
        initVisibility(0);
        initViewPager();
        vp_pager_content.setCurrentItem(0);
    }

    @Click({R.id.ll_back, R.id.add_card, R.id.tv_1, R.id.tv_2, R.id.tv_3})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                ViewUtils.overridePendingTransitionBack(this);
                break;
            case R.id.add_card:
                Intent intent = new Intent();
                intent.setClass(this, ImproveActivity_.class);
                startActivity(intent);
                ViewUtils.overridePendingTransitionCome(this);
                break;
            case R.id.tv_1:
                initVisibility(0);
                vp_pager_content.setCurrentItem(0);
                break;
            case R.id.tv_2:
                initVisibility(1);
                vp_pager_content.setCurrentItem(1);
                break;
            case R.id.tv_3:
                initVisibility(2);
                vp_pager_content.setCurrentItem(2);
                break;
        }
    }
    private void initViewPager() {
        if (pagerList != null) pagerList.clear();
        pagerList.add(new BindCardListPager(context, 1));
        pagerList.add(new BindCardListPager(context, 2));
        pagerList.add(new BindCardListPager(context, 3));
        vp_pager_content.setAdapter(new MyPagerAdapter());
        //给ViewPager设置页面变化的监听器
        vp_pager_content.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //当页面被选中时调用此方法
            @Override
            public void onPageSelected(int position) {
                pagerList.get(position).initData();
                initVisibility(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    class MyPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(pagerList.get(position).getRootView());
            return pagerList.get(position).getRootView();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

    }

    private void initVisibility(int identify) {
        switch (identify) {
            case 0:
                tv_1.setBackgroundResource(R.drawable.bindcard_title_1);
                tv_2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_3.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_1.setTextColor(getResources().getColor(R.color.white));
                tv_2.setTextColor(getResources().getColor(R.color.black));
                tv_3.setTextColor(getResources().getColor(R.color.black));
                break;
            case 1:
                tv_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_2.setBackgroundColor(getResources().getColor(R.color.title_bg));
                tv_3.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_2.setTextColor(getResources().getColor(R.color.white));
                tv_1.setTextColor(getResources().getColor(R.color.black));
                tv_3.setTextColor(getResources().getColor(R.color.black));
                break;
            case 2:
                tv_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tv_3.setBackgroundResource(R.drawable.bindcard_title_3);
                tv_3.setTextColor(getResources().getColor(R.color.white));
                tv_2.setTextColor(getResources().getColor(R.color.black));
                tv_1.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }


}

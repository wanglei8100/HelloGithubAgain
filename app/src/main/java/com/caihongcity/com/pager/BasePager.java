package com.caihongcity.com.pager;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2015/9/15 0015.
 */
public abstract class BasePager {
    public Context context;
    public View view;
    public BasePager(Context context) {
        this.context = context;
        view = initView();
    }

    /**
     * 获取view对象的方法
     *
     * @return
     */
    public View getRootView() {
        return view;
    }

    /**
     * 加载布局的方法
     *
     * @return View对象
     */
    public abstract View initView();

    /**
     * 填充数据的方法
     */
    public abstract void initData();
}

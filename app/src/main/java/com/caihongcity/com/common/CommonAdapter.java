package com.caihongcity.com.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int mLayoutResId;

    public CommonAdapter(Context context, List<T> datas, int layoutResId) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        mInflater = LayoutInflater.from(context);
    }

    public void setListData(List<T> datas) {
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder holder = CommonViewHolder.get(mContext, convertView, position, parent, mLayoutResId);
        convert(holder, getItem(position));
        return holder.getConvertView();
    }

    /**
     * 让使用者去实现的方法，使用者只需要实现这一个方法即可，
     * 如有对item中的某个控件单独设置事件可用holder.getView()获取指定控件进行设置
     *
     * @param holder listview控件的容器
     * @param t      listview的item实体数据
     */
    public abstract void convert(CommonViewHolder holder, T t);

}

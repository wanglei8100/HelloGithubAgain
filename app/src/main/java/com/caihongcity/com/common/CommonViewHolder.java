package com.caihongcity.com.common;


import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.lidroid.xutils.BitmapUtils;

public class CommonViewHolder {
    private SparseArray<View> mViews;
    private int position;
    private View mConvertView;
    private Context mContext;
    private BitmapUtils bitmapUtils;

    public CommonViewHolder(Context mContext, int position, ViewGroup parent,
                            int layoutId) {
        this.mViews = new SparseArray<View>();
        this.position = position;
        this.mContext = mContext;
        mConvertView = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        mConvertView.setTag(this);
        initBitmap();
    }

    public static CommonViewHolder get(Context mContext, View convertView,
                                       int position, ViewGroup parent, int layoutId) {
        if (convertView == null) {
            return new CommonViewHolder(mContext, position, parent, layoutId);
        } else {
            CommonViewHolder holder = (CommonViewHolder) convertView.getTag();
            holder.position = position;
            return holder;
        }
    }

    public int getPosition() {
        return position;
    }

    public View getConvertView() {
        return mConvertView;
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置值
     *
     * @param resId 资源id
     * @param text  要显示的值
     * @return CommonViewHolder 用于链式编程
     */
    public CommonViewHolder setText(int resId, String text) {
        TextView textView = getView(resId);
        if (!TextUtils.isEmpty(text)){
            textView.setText(text);
        }else {
            textView.setVisibility(View.GONE);
        }

        return this;
    }
    /**
     * 为TextView设置值
     *
     * @param resId 资源id
     * @param text  要显示的值
     * @return CommonViewHolder 用于链式编程
     */
    public CommonViewHolder setTextHint(int resId, String text) {
        TextView textView = getView(resId);
        if (!TextUtils.isEmpty(text)){
            textView.setHint(text);
        }else {
            textView.setVisibility(View.GONE);
        }

        return this;
    }

    /**
     * 为imageview设置图片——使用第三方jar（universal-image-loader）
     *
     * @param resId
     * @param url   本地或是网络路径
     * @return CommonViewHolder 用于链式编程
     */
    public CommonViewHolder setImageURI(int resId, String url) {
        if (TextUtils.isEmpty(url)) return this;
        final ImageView imageView = getView(resId);
        bitmapUtils.display(imageView, url);
        return this;
    }

    /**
     * 为imageview设置图片
     *
     * @param resId
     * @param bm    Bitmap对象
     * @return
     */
    public CommonViewHolder setImageBitmap(int resId, Bitmap bm) {
        ImageView imageView = getView(resId);
        imageView.setImageBitmap(bm);
        return this;
    }


    /**
     * 为imageview设置图片
     *
     * @param resId
     * @param imgId 图片id
     * @return
     */
    public CommonViewHolder setImageBitmap(int resId, int imgId) {
        ImageView imageView = getView(resId);
        imageView.setImageResource(imgId);
        return this;
    }
    public CommonViewHolder setCheckBox(int resId, Boolean isChecked) {
        CheckBox checkBox = getView(resId);
        checkBox.setChecked(isChecked);
        return this;
    }
    public CommonViewHolder setVisibility(int resId, Boolean isVisibility) {
        View view = getView(resId);
        view.setVisibility(isVisibility==true?View.VISIBLE:View.GONE);
        return this;
    }

    private void initBitmap() {
        bitmapUtils = new BitmapUtils(mContext, CommonUtils.getDiskCacheDir(mContext, "ImageCache").getPath());
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher);
         bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        bitmapUtils.configDefaultImageLoadAnimation(animation);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
        bitmapUtils.configMemoryCacheEnabled(true);
        bitmapUtils.configDiskCacheEnabled(true);

    }

}

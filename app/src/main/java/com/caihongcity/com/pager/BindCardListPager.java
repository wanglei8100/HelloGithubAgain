package com.caihongcity.com.pager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.caihongcity.com.R;
import com.caihongcity.com.activity.ImproveActivity_;
import com.caihongcity.com.activity.MyApplication;
import com.caihongcity.com.common.CommonAdapter;
import com.caihongcity.com.common.CommonViewHolder;
import com.caihongcity.com.model.BindCard;
import com.caihongcity.com.model.CardImg;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.NetUtils;
import com.caihongcity.com.utils.StorageCustomerInfo02Util;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/10/29 0029.
 */
public class BindCardListPager extends BasePager {
    private static final String TAG = "BindCardListPager";
    private final int identify;
    private MyAdapter mAdapter;
    private FrameLayout fl_nodata;

    public BindCardListPager(Context context, int identify) {
        super(context);
        this.identify = identify;
    }

    PullToRefreshListView pull_to_refresh_listview;
    ArrayList<BindCard> datas;

    @Override
    public View initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.bind_card_list_pager, null);
        pull_to_refresh_listview = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
        fl_nodata = (FrameLayout) view.findViewById(R.id.fl_nodata);

        initData();
        return view;
    }

    @Override
    public void initData() {
        datas = new ArrayList<BindCard>();
        pull_to_refresh_listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                pull_to_refresh_listview.getLoadingLayoutProxy().setLastUpdatedLabel("上次刷新时间:" + CommonUtils.getTime("HH:mm:ss"));
                requestDatas(true, 1);
            }
        });
        pull_to_refresh_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(context, ImproveActivity_.class);
                BindCard bindCard = datas.get(i - 1);
                List<CardImg> images = bindCard.getImages();
                if (images.size() != 5) {
                    for (int j = 0; j < 5; j++) {
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_10A", "");
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_10B", "");
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_10D", "");
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_10E", "");
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_10F", "");
                    }

                } else {
                    for (int j = 0; j < images.size(); j++) {
                        String imageUrl = images.get(j).getImageUrl();
                        String type = images.get(j).getType();
                        StorageCustomerInfo02Util.putInfo(context, "infoImageUrl_" + type, imageUrl);
                    }

                }


                intent.putExtra("bindCard", bindCard);
                context.startActivity(intent);
            }
        });

        requestDatas(true, 1);
    }

    private void requestDatas(boolean b, int i) {
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        String customerNum = StorageCustomerInfoUtil.getInfo("customerNum", context);
        requestData.put(0, "0700");
        requestData.put(3, "190932");
        requestData.put(42, customerNum);
        requestData.put(59, Constant.VERSION);
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        LogUtil.i("login", url);
        NetUtils.sendStringRequest(context, url, TAG, new NetUtils.RequestCallBack() {
            @Override
            public void loading() {
            }

            @Override
            public void successful(String response) {
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("39")) {
                            String code = (String) obj.get("39");
                            if ("00".equals(code)) {//成功
                                String customerInfosString = obj.getString("57");
                                if (!TextUtils.isEmpty(customerInfosString)) {
                                    List<BindCard> bindCard = com.alibaba.fastjson.JSONArray.parseArray(customerInfosString, BindCard.class);
                                    datas.clear();
                                    for (int j = 0; j < bindCard.size(); j++) {
                                        BindCard bindCard1 = bindCard.get(j);
                                        switch (identify) {
                                            case 1:
                                                if ("审核通过".equals(bindCard1.getIncreaseLimitStatus())) {
                                                    datas.add(bindCard1);
                                                }
                                                break;
                                            case 2:
                                                if ("重新审核".equals(bindCard1.getIncreaseLimitStatus()) || "等待审核".equals(bindCard1.getIncreaseLimitStatus())) {
                                                    datas.add(bindCard1);
                                                }
                                                break;
                                            case 3:
                                                if ("审核拒绝".equals(bindCard1.getIncreaseLimitStatus())) {
                                                    datas.add(bindCard1);
                                                }
                                                break;
                                        }
                                    }
                                    if (datas.size() > 0) {
                                        fl_nodata.setVisibility(View.GONE);
                                    } else {
                                        fl_nodata.setVisibility(View.VISIBLE);
                                    }
                                    if (mAdapter == null) {
                                        mAdapter = new MyAdapter(context, datas, R.layout.bind_item);
                                        pull_to_refresh_listview.setAdapter(mAdapter);
                                    } else {
                                        mAdapter.setListData(datas);
                                        mAdapter.notifyDataSetChanged();
                                    }

                                }
                            } else {//失败
                                String resultValue = MyApplication.getErrorHint(code);
                                if(!TextUtils.isEmpty(resultValue)) {
                                    ViewUtils.makeToast(context, resultValue, 1000);
                                }else{
                                    ViewUtils.makeToast(context,"未知错误，错误码："+code,1500);
                                }
                            }
                        } else {
                            ViewUtils.makeToast(context, context.getString(R.string.server_error), 1000);
                        }
                    } catch (JSONException e) {
                        ViewUtils.makeToast(context, context.getString(R.string.server_error), 1000);
                        e.printStackTrace();
                    }
                } else {
                    ViewUtils.makeToast(context, context.getString(R.string.server_error), 1000);
                }
                pull_to_refresh_listview.onRefreshComplete();
            }

            @Override
            public void errored(String response) {
                pull_to_refresh_listview.onRefreshComplete();
            }
        });
    }

    class MyAdapter extends CommonAdapter<BindCard> {

        public MyAdapter(Context context, List<BindCard> datas, int layoutResId) {
            super(context, datas, layoutResId);
        }

        @Override
        public void convert(CommonViewHolder holder, BindCard bean) {
            LinearLayout bind_item = holder.getView(R.id.bind_item);
            switch (identify) {
                case 1:
                    bind_item.setBackgroundResource(R.drawable.bind_item);
                    holder.setText(R.id.tv_tixian_money, bean.getSingleLimit());
                    break;
                case 2:
                    bind_item.setBackgroundResource(R.drawable.bind_item);
                    holder.setVisibility(R.id.ll_tixian_money,false);
                    break;
                case 3:
                    bind_item.setBackgroundResource(R.drawable.bind_item_gray);
                    holder.setVisibility(R.id.ll_tixian_money, false);
                    break;
            }

            holder.setText(R.id.tv_bank_name, bean.getBankName()).setText(R.id.tv_bank_type, "信用卡").setText(R.id.tv_bank_card_number, CommonUtils.translateShortNumber(bean.getBankAccount(), 6, 4));
        }
    }
}

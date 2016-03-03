package com.caihongcity.com.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.common.CommonAdapter;
import com.caihongcity.com.common.CommonViewHolder;
import com.caihongcity.com.db.NotificationData;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.NetUtils;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/10/29 0029.
 */
@EActivity(R.layout.activity_notification_list)
public class NotificationListActivity extends BaseActivity{
    private static final String TAG = "NotificationListActivity";
    @ViewById
    PullToRefreshListView pull_to_refresh_listview;
    @ViewById
     FrameLayout fl_nodata;
    @ViewById
    TextView tv_title_des;
    MyAdapter mAdapter;
    ArrayList<NotificationData> datas;
    @AfterViews
    void initView() {
        tv_title_des.setText("公告列表");
        datas = new ArrayList<NotificationData>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pull_to_refresh_listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                pull_to_refresh_listview.getLoadingLayoutProxy().setLastUpdatedLabel("上次刷新时间:"+CommonUtils.getTime("HH:mm:ss"));
                requestDatas(true, 1);
            }
        });
        pull_to_refresh_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NotificationData notification = datas.get(i-1);
                Intent intent = new Intent(context,NotificationDetailActivity_.class);
                intent.putExtra("title",notification.getNotificationTitle());
                intent.putExtra("id",notification.getNotificationId());
                intent.putExtra("content",notification.getNotificationContent());
                startActivity(intent);
                ViewUtils.overridePendingTransitionCome(context);
            }
        });
        requestDatas(true, 1);
    }

    private void requestDatas(boolean b, int i) {
        HashMap<Integer,String> requestData = new HashMap<Integer,String>();
        requestData.put(0,"0700");
        requestData.put(3,"190103");
        requestData.put(42, StorageCustomerInfoUtil.getInfo("customerNum", this));
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
                                JSONArray jsonArray = new JSONArray(obj.getString("60"));
                               String loginPhoneNum = StorageAppInfoUtil.getInfo("phoneNum", context);
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    JSONObject object = (JSONObject) jsonArray.get(j);
                                    String notificationId = object.getString("id");
                                    List<NotificationData> notificationDatas = DataSupport.where("notificationId = ?", notificationId).find(NotificationData.class);
                                    NotificationData notificationData;
                                    if (notificationDatas.size() <= 0) {
                                        notificationData = new NotificationData();
                                        String updateDateStr = object.getString("updateDateStr").substring(5, 10);
                                        notificationData.setNotificationDate(updateDateStr.equals(CommonUtils.getTime("MM-dd")) ? "今天" : updateDateStr);
                                        notificationData.setNotificationId(notificationId);
                                        notificationData.setNotificationTitle(object.getString("title"));
                                        notificationData.setNotificationContent(object.getString("content"));
                                        notificationData.setUserPhoneNumer(loginPhoneNum);
                                        notificationData.save();
                                    }else {
                                        notificationData = notificationDatas.get(0);
                                        String updateDateStr = object.getString("updateDateStr").substring(5, 10);
                                        notificationData.setNotificationDate(updateDateStr.equals(CommonUtils.getTime("MM-dd")) ? "今天" : updateDateStr);
                                        notificationData.setUserPhoneNumer(loginPhoneNum);
                                        notificationData.updateAll("notificationId = ?", notificationId);
                                    }
                                }
                                datas.clear();
                                datas.addAll(DataSupport.select("*").where("userPhoneNumer = ?", loginPhoneNum).order("notificationDate desc").find(NotificationData.class));
//                                Collections.reverse(datas);
                                if (datas.size() > 0) {
                                    fl_nodata.setVisibility(View.GONE);
                                } else {
                                    fl_nodata.setVisibility(View.VISIBLE);
                                }
                                if (mAdapter == null) {
                                    mAdapter = new MyAdapter(context, datas, R.layout.notification_list_item);
                                    pull_to_refresh_listview.setAdapter(mAdapter);
                                } else {

                                    mAdapter.setListData(datas);
                                    mAdapter.notifyDataSetChanged();
                                }
                            } else {//失败
                                ViewUtils.makeToast(context, MyApplication.getErrorHint(code), 1000);
                            }
                        } else {
                            ViewUtils.makeToast(context, getString(R.string.server_error), 1000);
                        }
                    } catch (JSONException e) {
                        ViewUtils.makeToast(context, getString(R.string.server_error), 1000);
                        e.printStackTrace();
                    }
                } else {
                    ViewUtils.makeToast(context, getString(R.string.server_error), 1000);
                }
                pull_to_refresh_listview.onRefreshComplete();
            }

            @Override
            public void errored(String response) {
                pull_to_refresh_listview.onRefreshComplete();
            }
        });
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
    class MyAdapter extends CommonAdapter<NotificationData>{

        public MyAdapter(Context context, List<NotificationData> datas, int layoutResId) {
            super(context, datas, layoutResId);
        }

        @Override
        public void convert(CommonViewHolder holder, NotificationData bean) {
            holder.setText(R.id.tv_title,bean.getNotificationTitle()).setText(R.id.tv_content,bean.getNotificationContent()).setText(R.id.tv_date, bean.getNotificationDate());
            ImageView iv_type = holder.getView(R.id.iv_type);
            iv_type.setBackgroundResource(bean.isNotificationIsRead() ? R.drawable.msg_normal : R.drawable.msg_new);
        }
    }
}

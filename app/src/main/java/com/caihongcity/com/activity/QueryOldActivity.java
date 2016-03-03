package com.caihongcity.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.MyAsyncTask;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author yuanjigong 流水查询
 */
public class QueryOldActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener, OnScrollListener {
    private static final String TAG = "QueryActivity";
    /**
     * 名 域名定义 属性 格式 类型 请求 响应 备注 0 消息类型 n4 BCD 0700 0700 MSG-TYPE-ID 3 交易处理码 N6
     * BCD M M 190978 39 应答码 an2 ASCII M 42 商户编号 N15 ASCII M 商户编号 57 订单列表 JSON M
     * 订单列表（交易时间，交易类型，交易状态，交易金额） 60 自定义域 n…011 LLLVAR BCD M M 60.1 交易类型码 n2 BCD
     * M M 填“00” 60.2 当前页数 n6 BCD M M 60.3 每页数量 n3 BCD M M
     */

    private ArrayList<QueryModel> queryModels = null;
    private ListView listView = null;
    QueryAdapter queryAdapter = null;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int id = msg.what;
            switch (id) {
                case 0:
                    if (queryModels.size() == 0) {
                        listView.setVisibility(View.GONE);
                        ll_notrade.setVisibility(View.VISIBLE);
                    } else {
                        queryAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }

    };
    private int pagesize;
    private LinearLayout ll_notrade;
    private boolean isLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runningwater);

        listView = (ListView) findViewById(R.id.liushui);
        ll_notrade = (LinearLayout) findViewById(R.id.ll_notrade);
        findViewById(R.id.ll_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_title_des)).setText("交易列表");
//        pagesize = 1;
//        queryHistoryTrade(pagesize + "");
//        queryAdapter = new QueryAdapter();
//        listView.setAdapter(queryAdapter);
//        listView.setOnItemClickListener(this);
//        listView.setOnScrollListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        queryModels = new ArrayList<QueryModel>();
        isLast = false;
        pagesize = 1;
        queryHistoryTrade(pagesize + "");
        queryAdapter = new QueryAdapter();
        listView.setAdapter(queryAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
    }

    public void queryHistoryTrade(String page) {
        if (isLast) {
            ViewUtils.makeToast(QueryOldActivity.this, "没有更多交易信息了", 1500);
            return;
        }
        //检查网络状态
        if (CommonUtils.getConnectedType(QueryOldActivity.this) == -1) {
            ViewUtils.makeToast(QueryOldActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        if (pagesize < 10) {
            page = "0" + page;
        }
        String customerNo = StorageCustomerInfoUtil
                .getInfo("customerNum", this);
        HashMap<Integer,String> requestData = new HashMap<Integer,String>();
        requestData.put(0,"0700");
        requestData.put(3,"190978");
        requestData.put(42,customerNo);
        requestData.put(59, Constant.VERSION);
        requestData.put(60,"220000" + page + "010");
        requestData.put(64, Constant.getMacData(requestData));
        String url = Constant.getUrl(requestData);
        final String finalPage = page;
        MyAsyncTask myAsyncTask = new MyAsyncTask(new MyAsyncTask.LoadResourceCall() {
            @Override
            public void isLoadingContent() {
                loadingDialog.show();
            }

            @Override
            public void isLoadedContent(String content) {
                loadingDialog.dismiss();
                LogUtil.syso("content==" + content);
                if (StringUtil.isEmpty(content)) {
                    ViewUtils.showChoseDialog(QueryOldActivity.this, true, getString(R.string.server_error), View.VISIBLE, new ViewUtils.OnChoseDialogClickCallback() {
                        @Override
                        public void clickOk() {
                            queryHistoryTrade(finalPage);
                        }

                        @Override
                        public void clickCancel() {
                            ViewUtils.overridePendingTransitionBack(QueryOldActivity.this);
                        }
                    });
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.responseCodeMap
                            .get(result);
                    if ("00".equals(result)) {
                        String tradeHistory = (String) obj.get("57");
                        JSONArray jArray = new JSONArray(tradeHistory);
                        int len = jArray.length();
                        if (len < 10) isLast = true;
                        for (int i = 0; i < len; i++) {
                            QueryModel queryModel = new QueryModel();
                            JSONObject jobj = (JSONObject) jArray.get(i);
                            String tradeMoney = jobj.getString("trxAmt");
                            String tradeStatus = jobj.getString("status");
                            String tradeTime = jobj.getString("completeTime");
                            String tradeType = jobj.getString("tradeType");
                            String cardNo = jobj.getString("cardNo");
                            String orderNo = jobj.getString("orderNo");
                            String bankName = jobj.getString("bankName");
                            String imageUrl = jobj.getString("imageUrl");// 小票url地址
                            String signUrl = jobj.getString("signUrl");
                            String acqAuthNo = jobj.getString("acqAuthNo");
                            String terminalBatchNo = jobj
                                    .getString("terminalBatchNo");
                            String termianlVoucherNo = jobj
                                    .getString("termianlVoucherNo");
                            if (jobj.has("strRate")) {
                                String strRate = jobj.getString("strRate");
                                queryModel.setFeeRate(strRate);
                            }
                            if (jobj.has("settleCycle")) {
                                String settleCycle = jobj.getString("settleCycle");
                                queryModel.setSettleCycle(settleCycle);
                            }
                            if (jobj.has("payStatus")) {
                                String payStatus = jobj.getString("payStatus");
                                queryModel.setPayStatus(payStatus);
                            }
                            if (jobj.has("payResMsg")) {
                                String payResMsg = jobj.getString("payResMsg");
                                queryModel.setPayResMsg(payResMsg);
                            }
                            if (jobj.has("maxFee")) {
                                String maxFee = jobj.getString("maxFee");
                                queryModel.setMaxFee(maxFee);
                            }
                            queryModel.setTradeMoney(tradeMoney);
                            queryModel.setTradeStatus(tradeStatus);
                            queryModel.setTradeTime(tradeTime);
                            queryModel.setTradeType(tradeType);
                            queryModel.setBankName(bankName);
                            queryModel.setCardNo(cardNo);
                            queryModel.setOrderNo(orderNo);
                            queryModel.setImageUrl(imageUrl);
                            queryModel.setSignUrl(signUrl);
                            queryModel.setAcqAuthNo(acqAuthNo);
                            queryModel.setTermianlVoucherNo(termianlVoucherNo);
                            queryModel.setTerminalBatchNo(terminalBatchNo);
                            queryModels.add(queryModel);
                        }
                        LogUtil.syso("tradeHistory===" + tradeHistory);
                        Message msg = Message.obtain();
                        msg.what = 0;
                        handler.handleMessage(msg);
                    } else {
                        ViewUtils.makeToast(QueryOldActivity.this, resultValue, 1500);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        myAsyncTask.execute(url);
        LogUtil.d(TAG, "url==" + url);
    }

    class QueryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return queryModels.size();
        }

        @Override
        public Object getItem(int position) {
            return queryModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HoldView holdView;
            if (convertView == null) {
                holdView = new HoldView();
                View view = getLayoutInflater().inflate(
                        R.layout.running_water_item, null);
                holdView.tradeTime = (TextView) view
                        .findViewById(R.id.tradetime);
                holdView.tradeType = (TextView) view
                        .findViewById(R.id.tradetype);
                holdView.tradeStatus = (TextView) view
                        .findViewById(R.id.tradestatus);
                holdView.tradeMoney = (TextView) view
                        .findViewById(R.id.trademoney);
                convertView = view;
                convertView.setTag(holdView);

            } else {
                holdView = (HoldView) convertView.getTag();
            }

            QueryModel queryModel = queryModels.get(position);
            holdView.tradeTime.setText(queryModel.getTradeTime());
            String tradeTypeDes = queryModel.getTradeType();
            if ("消费撤销".equals(tradeTypeDes)) {
                holdView.tradeMoney.setTextColor(getResources().getColor(
                        R.color.red));
            } else {
                holdView.tradeMoney.setTextColor(getResources().getColor(
                        R.color.black));
            }
            holdView.tradeType.setText(tradeTypeDes);
            holdView.tradeStatus.setText(queryModel.getTradeStatus());
            String tradeMoney = queryModel.getTradeMoney();
            if (tradeMoney.contains("-")) {
                tradeMoney = tradeMoney.replace("-", "");
            }
            holdView.tradeMoney.setText(CommonUtils.format(tradeMoney));

            return convertView;
        }

    }

    class HoldView {
        TextView tradeTime;
        TextView tradeType;
        TextView tradeStatus;
        TextView tradeMoney;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(this);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        QueryModel queryModel = queryModels.get(position);
        Intent intent = new Intent();
        intent.putExtra("queryModel", queryModel);
        intent.setClass(this, TradeDetailActivity.class);
        startActivity(intent);
        ViewUtils.overridePendingTransitionCome(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 当不滚动时
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                /*
                 * adapter.count += 10; adapter.notifyDataSetChanged(); int
				 * currentPage=adapter.count/10;
				 */
                pagesize++;
                loadingDialog = ViewUtils.createLoadingDialog(this, getString(R.string.loading_wait), false);
                queryHistoryTrade(pagesize + "");

            }
//			LogUtil.i(TAG, "firstVisibleItem == " + view.getLastVisiblePosition());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
		/*LogUtil.i(TAG, "firstVisibleItem == " + firstVisibleItem
				+ "  visibleItemCount == " + visibleItemCount
				+ "  totalItemCount == " + totalItemCount);*/
    }}

package com.caihongcity.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

public class RealTimeActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "QueryActivity";
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
                        ll_notrade.setVisibility(View.VISIBLE);
                        if (listView.getFooterViewsCount() == 0) {
                            listView.addFooterView(ll_notrade);
                        }
                    } else {
                        ll_notrade.setVisibility(View.GONE);
                        if (listView.getFooterViewsCount() != 0) {
                            listView.removeFooterView(ll_notrade);
                        }
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
    private LinearLayout ll_consume_type;
    LinearLayout  ll_right;
    TextView title_left;
    private TextView tv_top, tv_right, tv_left;
    private boolean isLast;
    private final String RIGHT = "right";
    private String identity;
    private View view_white_mark_left, view_white_mark_right;
    private View view_guide_line_supermarket, view_guide_line_baihuo, view_guide_line_canyin, view_guide_line_pifa;
    private RelativeLayout rl_supermarket, rl_canyin, rl_baihuo, rl_pifa;
    private final String LEFT_SUPERMARKET = "LEFT_SUPERMARKET";
    private final String LEFT_BAIHUO = "LEFT_BAIHUO";
    private final String LEFT_CANYIN = "LEFT_CANYIN";
    private final String LEFT_PIFA = "LEFT_PIFA";
    private View viewRight;
    private View viewLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time);
        listView = (ListView) findViewById(R.id.liushui);
        //        LinearLayout listHader = (LinearLayout) getLayoutInflater().inflate(R.layout.trade_list_title, null, false);
        TextView total_money = (TextView) findViewById(R.id.total_money);
        TextView title_left = (TextView) findViewById(R.id.title_left);
        TextView title_right = (TextView) findViewById(R.id.title_right);
        title_left.setText("今日可提现笔数");
        total_money.setText("今日交易总额");
        title_right.setText("今日提现剩余总额");
//        LinearLayout listHader = (LinearLayout) getLayoutInflater().inflate(R.layout.trade_list_title_realtime, null, false);
        ll_notrade = (LinearLayout) getLayoutInflater().inflate(R.layout.notrade_layout, null, false);
        title_left = (TextView) findViewById(R.id.title_left);
        title_left.setOnClickListener(this);
        ll_right = (LinearLayout) findViewById(R.id.ll_right);
        ll_right.setOnClickListener(this);
        tv_top = (TextView) findViewById(R.id.tv_top);
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_left = (TextView) findViewById(R.id.tv_left);
        view_white_mark_left = (View) findViewById(R.id.view_white_mark_left);
        view_white_mark_right = (View) findViewById(R.id.view_white_mark_right);
        view_guide_line_supermarket = (View) findViewById(R.id.view_guide_line_supermarket);
        view_guide_line_baihuo = (View) findViewById(R.id.view_guide_line_baihuo);
        view_guide_line_canyin = (View) findViewById(R.id.view_guide_line_canyin);
        view_guide_line_pifa = (View) findViewById(R.id.view_guide_line_pifa);
        ll_consume_type = (LinearLayout) findViewById(R.id.ll_consume_type);
        ll_consume_type.setVisibility(View.GONE);
//        rl_supermarket = (RelativeLayout) findViewById(R.id.rl_supermarket);
//        rl_supermarket.setVisibility(View.GONE);
//        rl_canyin = (RelativeLayout) findViewById(R.id.rl_canyin);
//        rl_canyin.setVisibility(View.GONE);
//        rl_baihuo = (RelativeLayout) findViewById(R.id.rl_baihuo);
//        rl_baihuo.setVisibility(View.GONE);
//        rl_pifa = (RelativeLayout) findViewById(R.id.rl_pifa);
//        rl_pifa.setVisibility(View.GONE);
        viewLeft = findViewById(R.id.tipView_left);
        viewRight = findViewById(R.id.tipView_right);
//        rl_supermarket.setOnClickListener(this);
//        rl_canyin.setOnClickListener(this);
//        rl_baihuo.setOnClickListener(this);
//        rl_pifa.setOnClickListener(this);
//        viewLeft.setOnClickListener(this);
//        viewRight.setOnClickListener(this);
        view_white_mark_left.setOnClickListener(this);
        view_white_mark_right.setOnClickListener(this);
        tv_left.setOnClickListener(this);
        tv_right.setOnClickListener(this);

//        listView.addHeaderView(listHader);
        identity = LEFT_SUPERMARKET;


        initQuery(identity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");

    }

    private void initQuery(String identify) {
        if (RIGHT.equals(identify)) {
            view_white_mark_right.setBackgroundResource(R.drawable.tradelist_guide_click);
            view_white_mark_left.setBackgroundResource(R.drawable.tradelist_guide_normal);
            ll_consume_type.setVisibility(View.GONE);
        } else {
            view_white_mark_right.setBackgroundResource(R.drawable.tradelist_guide_normal);
            view_white_mark_left.setBackgroundResource(R.drawable.tradelist_guide_click);
//            ll_consume_type.setVisibility(View.VISIBLE);
            if (identify.equals(LEFT_SUPERMARKET)) {
                view_guide_line_supermarket.setVisibility(View.VISIBLE);
                view_guide_line_canyin.setVisibility(View.GONE);
                view_guide_line_pifa.setVisibility(View.GONE);
                view_guide_line_baihuo.setVisibility(View.GONE);
            } else if (identify.equals(LEFT_CANYIN)) {
                view_guide_line_supermarket.setVisibility(View.GONE);
                view_guide_line_canyin.setVisibility(View.VISIBLE);
                view_guide_line_pifa.setVisibility(View.GONE);
                view_guide_line_baihuo.setVisibility(View.GONE);
            } else if (identify.equals(LEFT_PIFA)) {
                view_guide_line_supermarket.setVisibility(View.GONE);
                view_guide_line_canyin.setVisibility(View.GONE);
                view_guide_line_pifa.setVisibility(View.VISIBLE);
                view_guide_line_baihuo.setVisibility(View.GONE);
            } else if (identify.equals(LEFT_BAIHUO)) {
                view_guide_line_supermarket.setVisibility(View.GONE);
                view_guide_line_canyin.setVisibility(View.GONE);
                view_guide_line_pifa.setVisibility(View.GONE);
                view_guide_line_baihuo.setVisibility(View.VISIBLE);
            }
        }
        queryModels = new ArrayList<QueryModel>();
        isLast = false;
        pagesize = 1;
        queryHistoryTrade(pagesize + "", identify);
        queryAdapter = new QueryAdapter();
        listView.setAdapter(queryAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
    }

    public void queryHistoryTrade(String page, String identify) {
        if (isLast) {
            ViewUtils.makeToast(RealTimeActivity.this, "没有更多交易信息了", 1500);
            return;
        }
        //检查网络状态
        if (CommonUtils.getConnectedType(RealTimeActivity.this) == -1) {
            ViewUtils.makeToast(RealTimeActivity.this, getString(R.string.nonetwork), 1500);
            return;
        }
        if (pagesize < 10) {
            page = "0" + page;
        }
        String customerNo = StorageCustomerInfoUtil
                .getInfo("customerNum", this);
        HashMap<Integer, String> requestData = new HashMap<Integer, String>();
        requestData.put(0, "0700");
        requestData.put(3, "190978");
        requestData.put(9, "00000000");
        requestData.put(42, customerNo);
        requestData.put(59, Constant.VERSION);
        requestData.put(60, (identify.equals(LEFT_SUPERMARKET) ? "03" : "04") + "0000" + page + "010");
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
                    ViewUtils.makeToast(RealTimeActivity.this, getString(R.string.server_error), 1500);
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(content);
                    String result = (String) obj.get("39");
                    String resultValue = MyApplication.responseCodeMap
                            .get(result);
                    if ("00".equals(result)) {
                        String tradeHistory = (String) obj.get("57");
                        JSONArray jArray01 = new JSONArray(tradeHistory);
                        JSONObject obj01 = (JSONObject) jArray01.get(0);
                        if (obj01.has("todayAmount")) {
                            String todayAmount = obj01.getString("todayAmount");
                            tv_top.setText(CommonUtils.format(todayAmount));
                        }
                        if (obj01.has("todayWithDraw")) {
                            String todayWithDraw = obj01.getString("todayWithDraw");
                            tv_left.setText(todayWithDraw);
                        }
                        if (obj01.has("todayWithDrawSurplus")) {
                            String todayWithDrawSurplus = obj01.getString("todayWithDrawSurplus");
                            tv_right.setText(CommonUtils.format(todayWithDrawSurplus));
                        }
                        JSONArray jArray = (JSONArray) obj01.get("appPayment");
                        int len = jArray.length();
                        if (len < 10) isLast = true;
                        for (int i = 0; i < len; i++) {
                            QueryModel queryModel = new QueryModel();
                            JSONObject jobj = (JSONObject) jArray.get(i);
                            String tradeMoney = jobj.getString("trxAmt");
                            String tradeStatus = jobj.getString("statusName");
                            String tradeTime = jobj.getString("completeTimeString");
                            String tradeType = jobj.getString("tradeTypeName");
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
                                String strRate = jobj.getString("rate");
                                queryModel.setFeeRate(strRate);
                            }
                            if (jobj.has("settleCycle")) {
                                String settleCycle = jobj.getString("settleCycle");
                                queryModel.setSettleCycle(settleCycle);
                            }
                            if (jobj.has("status")) {
                                String status = jobj.getString("status");
                                queryModel.setPayStatus(status);
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
                        ViewUtils.makeToast(RealTimeActivity.this, resultValue, 1500);
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
            LogUtil.d("getView", "position==" + position);
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
                holdView.tv_yuefen = (TextView) view
                        .findViewById(R.id.tv_yuefen);
                holdView.ll_yuefen = (LinearLayout) view
                        .findViewById(R.id.ll_yuefen);
                convertView = view;
                convertView.setTag(holdView);

            } else {
                holdView = (HoldView) convertView.getTag();
            }

            QueryModel queryModel = queryModels.get(position);
            holdView.tradeTime.setText(queryModel.getTradeTime().substring(0, 10));
            String tradeTypeDes = queryModel.getTradeType();
            if ("消费撤销".equals(tradeTypeDes)) {
                holdView.tradeType.setTextColor(getResources().getColor(
                        R.color.red));

            } else {
                holdView.tradeType.setTextColor(getResources().getColor(
                        R.color.black));
            }
            holdView.tradeType.setText(tradeTypeDes);
            holdView.tradeStatus.setText(queryModel.getTradeStatus());
            String tradeMoney = queryModel.getTradeMoney();
            if (tradeMoney.contains("-")) {
                tradeMoney = tradeMoney.replace("-", "");
            }
            holdView.tradeMoney.setText(CommonUtils.format(tradeMoney));
            if (position == 0) {
                holdView.ll_yuefen.setVisibility(View.VISIBLE);
                holdView.tv_yuefen.setText(queryModel.getTradeTime().substring(5, 7).replace("-", "") + "月");
            } else if (position > 0) {
                if (!queryModels.get(position).getTradeTime().substring(5, 7).equals(queryModels.get(position - 1).getTradeTime().substring(5, 7))) {
                    holdView.ll_yuefen.setVisibility(View.VISIBLE);
                    holdView.tv_yuefen.setText(queryModel.getTradeTime().substring(5, 7).replace("-", "") + "月");
                } else {
                    holdView.ll_yuefen.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

    }

    class HoldView {
        TextView tradeTime;
        TextView tradeType;
        TextView tradeStatus;
        TextView tradeMoney;
        LinearLayout ll_yuefen;
        TextView tv_yuefen;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.title_left:
            case R.id.tv_left:
                identity = LEFT_SUPERMARKET;
                initQuery(identity);
                break;
            case R.id.ll_right:
                identity = RIGHT;
                initQuery(identity);
                break;
            case R.id.rl_supermarket:
                identity = LEFT_SUPERMARKET;
                initQuery(identity);
                break;
            case R.id.rl_baihuo:
                identity = LEFT_BAIHUO;
                initQuery(identity);
                break;
            case R.id.rl_canyin:
                identity = LEFT_CANYIN;
                initQuery(identity);
                break;
            case R.id.rl_pifa:
                identity = LEFT_PIFA;
                initQuery(identity);
                break;
            case R.id.tipView_right:
            case R.id.view_white_mark_right:
                ViewUtils.makeToast(this, "hehe2", 1000);
                break;
            case R.id.tipView_left:
            case R.id.view_white_mark_left:
                ViewUtils.makeToast(this,"hehe",1000);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        if ( ll_notrade.getVisibility() == View.VISIBLE) return;
        QueryModel queryModel = queryModels.get(position );
        Intent intent = new Intent();
        intent.putExtra("queryModel", queryModel);
        intent.putExtra("where_from", "RealTimeActivity");
        intent.setClass(this, TradeDetailActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 当不滚动时
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                /*
                 * adapter.count += 10; adapter.notifyDataSetChanged(); int
				 * currentPage=adapter.count/10;
				 */
                pagesize++;
                loadingDialog = ViewUtils.createLoadingDialog(this, getString(R.string.loading_wait), true);
                queryHistoryTrade(pagesize + "", identity);

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
    }
}

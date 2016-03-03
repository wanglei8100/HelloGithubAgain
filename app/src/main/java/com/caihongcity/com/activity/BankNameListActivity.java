package com.caihongcity.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.ViewUtils;

import java.util.ArrayList;

public class BankNameListActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
private ArrayList<String> list;
private String[] datas = new String[]{"北京银行","光大银行","广发银行","建设银行","交通银行","民生银行","农业银行","平安银行","浦发银行",
		"邮政储蓄银行","招商银行","中国工商银行","中国银行","中信银行","上海银行","杭州银行"};
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bank_name_list);
	findViewById(R.id.ll_back).setOnClickListener(this);
	((TextView)findViewById(R.id.tv_title_des)).setText("银行列表");
	ListView lv_banklist = (ListView) findViewById(R.id.lv_banklist);
	
	lv_banklist.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,datas));
	lv_banklist.setOnItemClickListener(this);
}


@Override
public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	String selectBankname = datas[position];
	 Intent intent=new Intent();  
     intent.putExtra("selectBankname", selectBankname);  
     setResult(RESULT_OK, intent);  
     finish();
}

@Override
public void onClick(View v) {
	if (CommonUtils.isFastDoubleClick()) {  
        return;  
    }
switch (v.getId()) {
case R.id.ll_back:
	ViewUtils.overridePendingTransitionBack(this);
	break;

default:
	break;
}	
}

}

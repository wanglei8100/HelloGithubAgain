package com.caihongcity.com.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.ViewUtils;
/**
 * 
 * @author yuanjigong
 * 交易结果界面
 */
public class TradeStatusSuccessActivity extends BaseActivity implements OnClickListener{

	private ImageView iv_sign_name;
	private String content;
	private TextView money;
	private TextView tradeType;
	private TextView cardNo;
	private TextView consumetradetime_pro;
	private String moneyStr,cardNumber,tradeTypeStr;
	private Button confirm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tradestatus_success);
		findViewById(R.id.ll_back).setOnClickListener(this);
		((TextView)findViewById(R.id.tv_title_des)).setText("交易结果");
		iv_sign_name = (ImageView) findViewById(R.id.iv_sign_name);
		content = getIntent().getStringExtra("content");
		money = (TextView) findViewById(R.id.money);
		tradeType = (TextView) findViewById(R.id.tradetype_pro);
		cardNo = (TextView) findViewById(R.id.cardnumber_pro);
		moneyStr = getIntent().getStringExtra("money");
		cardNumber = getIntent().getStringExtra("cardNo");
		tradeTypeStr = getIntent().getStringExtra("tradeType");
		money.setText(moneyStr);
		if(Constant.CONSUME.equals(tradeTypeStr)){
			tradeType.setText(getString(R.string.consume));
		}else{
			tradeType.setText(getString(R.string.canceltrade));
		}
		cardNo.setText(cardNumber);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String tradeTime = sdf.format(new Date());
		consumetradetime_pro = (TextView) findViewById(R.id.consumetradetime_pro);
		consumetradetime_pro.setText(tradeTime);
		confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(this);
		
		//设置签名图片
		String signname_path = StorageCustomerInfoUtil.getInfo( "signname",this);
		Bitmap bitmap=BitmapFactory.decodeFile(signname_path);
//		Bitmap loadImageFromUrl = ImageUtils.getLoacalBitmapByAssets(this, signname_path);
		iv_sign_name.setImageBitmap(bitmap);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.ll_back:
			ViewUtils.overridePendingTransitionBack(this);
			break;
		case R.id.confirm:
			ViewUtils.overridePendingTransitionBack(this);
			break;

		default:
			break;
		}
	}
}

package com.caihongcity.com.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.BitmapManage;
import com.caihongcity.com.utils.CommonUtils;
import com.caihongcity.com.utils.Constant;
import com.caihongcity.com.utils.ImageUtils;
import com.caihongcity.com.utils.LogUtil;
import com.caihongcity.com.utils.StorageCustomerInfoUtil;
import com.caihongcity.com.utils.StringUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.caihongcity.com.utils.ViewUtils.OnChoseDialogClickCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author yuanjigong 签名界面
 */
public class SignNameActivity extends BaseActivity implements OnClickListener {
	private LinearLayout signname_cacel_layout;
	private LinearLayout  confirmtotrade;
	private String newName = "image.jpg";
	FrameLayout framelayout_signname = null;
	LayoutParams p;
	private boolean isSign = false;
	static final int BACKGROUND_COLOR = Color.WHITE;
	static final int BRUSH_COLOR = Color.BLACK;
	private static final String TAG = "SignNameActivity";
	PaintView mView;
	FrameLayout frameLayout = null;
	private Bitmap mSignBitmap = null;
	private String signPath = null;
	private String tradeType;
	private String money;
	private String cardNo, voucherNo;
	private TextView tradetype_pro, cardnumber_pro, moneyText;
	private String voucherNo37;
	private TextView customername;
	private int requestNumber = 3;
private Handler handler =   new Handler(){
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case 1:
			ViewUtils.showChoseDialog(SignNameActivity.this, true, msg.obj.toString(), View.GONE,new OnChoseDialogClickCallback() {
				
				@Override
				public void clickOk() {
					if (requestNumber<=0) {
						ViewUtils.overridePendingTransitionBack(SignNameActivity.this);
					} else {
						sendSignName();
					}
				}
				
				@Override
				public void clickCancel() {
					
				}
			});
			break;

		default:
			break;
		}
	};
};
private String feeRate,topFeeRate;
private TextView trade_rate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.signname_layout);
		tradetype_pro = (TextView) findViewById(R.id.tradetype_pro);
		cardnumber_pro = (TextView) findViewById(R.id.tradecardnumber);
		customername = (TextView) findViewById(R.id.customername);
		moneyText = (TextView) findViewById(R.id.money);
		trade_rate = (TextView) findViewById(R.id.trade_rate);
		findViewById(R.id.ll_back).setVisibility(View.GONE);
		((TextView) findViewById(R.id.tv_title_des)).setText("签名");
		tradeType = getIntent().getStringExtra("tradeType");
		money = getIntent().getStringExtra("money");
		cardNo = getIntent().getStringExtra("cardNo");
		voucherNo = getIntent().getStringExtra("voucherNo");
		voucherNo37 = getIntent().getStringExtra("voucherNo37");
		feeRate = getIntent().getStringExtra("feeRate");
		topFeeRate = getIntent().getStringExtra("topFeeRate");
		trade_rate.setText(feeRate+"-"+topFeeRate);
		String customerName = StorageCustomerInfoUtil.getInfo("customerName",
				this);
		customername.setText(customerName);
		if (Constant.CONSUME.equals(tradeType)) {
			tradetype_pro.setText(getString(R.string.consume));
		} else {
			tradetype_pro.setText(getString(R.string.tradecancel));
		}
//		String maskedPANValue = cardNo.replace(
//				cardNo.subSequence(6, cardNo.length() - 4), "****");
		cardnumber_pro.setText(cardNo);
		moneyText.setText(money);
		signname_cacel_layout = (LinearLayout) findViewById(R.id.signname_cacel_layout);
		signname_cacel_layout.setOnClickListener(this);
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		p = this.getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) ((display.getHeight())); // 高度设置为屏幕的高度
		p.width = (int) ((display.getWidth())); // 宽度设置为屏幕的高度
		getWindow().setAttributes(p); // 设置生效

		frameLayout = (FrameLayout) findViewById(R.id.signname_view);
		mView = new PaintView(this);
		LogUtil.syso("(int)p.width==" + (int) p.width + " p.height=="
				+ p.height);
		frameLayout.addView(mView, (int) (p.width), (int) (p.height * 0.4));
		mView.requestFocus();
		mView.setPadding(5, 10, 0, 0);
		LinearLayout btnClear = (LinearLayout) findViewById(R.id.clear_layout);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CommonUtils.isFastDoubleClick()) {
					return;
				}
				mView.clear();
				isSign = false;
			}
		});
		confirmtotrade = (LinearLayout) findViewById(R.id.confirmtotrade);
		changeButtonStatus(true);
		confirmtotrade.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonUtils.isFastDoubleClick()) {
					return;
				}
				try {
					if (isSign != true) {
						ViewUtils.makeToast(SignNameActivity.this, "您没有签名！",
								1500);
						return;
					}
					Bitmap bit = mView.getCachebBitmap();
					mSignBitmap = BitmapManage.zoomImage(bit, 60, 30);
					signPath = createFile();

					sendSignName();

				} catch (Exception e) {

					LogUtil.syso("error==" + e.getCause());
					ViewUtils
							.makeToast(SignNameActivity.this, "内存卡没有就绪！", 1500);
					return;
				}
			}
		});
	}
	private void sendSignName() {
		// 检查网络状态
		if (CommonUtils.getConnectedType(SignNameActivity.this) == -1) {
			ViewUtils.makeToast(SignNameActivity.this,
					getString(R.string.nonetwork), 1500);
			return;
		}
		loadingDialog.show();
		changeButtonStatus(false);//设置按钮不可点击
		UploadFileAAsyncTask asyncTask_pic = new UploadFileAAsyncTask();
		asyncTask_pic.execute(signPath);

		requestNumber--;
	}

	class UploadFileAAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String str = params[0];
			File file = new File(str);
			String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
					SignNameActivity.this);
			String data = "0=0700" + "&3=190968&11=" + voucherNo + "&37="
					+ voucherNo37 + "&42=" + customerNum + "&59="
					+ Constant.VERSION;
			String macData = "0700" + "190968" + voucherNo + voucherNo37
					+ customerNum + Constant.VERSION;
			String url = Constant.UPLOADIMAGE + data + "&64="
					+ CommonUtils.Md5(macData + Constant.mainKey);
			String result = ImageUtils.uploadFile(file, url);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {// 处理UI
			super.onPostExecute(result);
			loadingDialog.dismiss();
			changeButtonStatus(true);//设置按钮可点击
			try {
				if (StringUtil.isEmpty(result)) {
					ViewUtils.makeToast(SignNameActivity.this,
							getString(R.string.server_error), 1500);
					return;
				}
				JSONObject jsonResult = new JSONObject(result);

				String strCode = jsonResult.getString("39");
				String resultValue = MyApplication.getErrorHint(result);
				if ("00".equals(strCode)) {// 上传成功
					String imageUrl = jsonResult.getString("11");
					// ViewUtils.makeToast(SignNameActivity.this, "成功", 1000);
					Intent intent = new Intent(SignNameActivity.this,
							TradeStatusActivity.class);
					intent.putExtra("imageUrl", imageUrl);
					startActivity(intent);
					finish();
					ViewUtils
							.overridePendingTransitionCome(SignNameActivity.this);
				} else {// 上传失败
					handler.sendMessage(handler.obtainMessage(1, getString(R.string.server_error)));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}


	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		int id = v.getId();
		switch (id) {
		case R.id.signname_cacel_layout:
			ViewUtils.overridePendingTransitionBack(this);
			break;

		default:
			break;
		}

	}
	private Bitmap cachebBitmap;
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cachebBitmap!=null){
			cachebBitmap.recycle();
			cachebBitmap=null;
		}
	}
	class PaintView extends View {

		private Paint paint;
		private Canvas cacheCanvas;

		private Path path;

		public Bitmap getCachebBitmap() {

			return cachebBitmap;

		}

		public PaintView(Context context) {

			super(context);
			init();

		}

		private void init() {

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			path = new Path();
			cachebBitmap = Bitmap.createBitmap(p.width, (int) (p.height*0.4),
					Config.RGB_565);
			LogUtil.syso("width==" + p.width + " height==" + p.height * 0.4);
			cacheCanvas = new Canvas(cachebBitmap);
			cacheCanvas.drawColor(Color.WHITE);

		}

		public void clear() {

			if (cacheCanvas != null) {

//				paint.setColor(BACKGROUND_COLOR);
//				cacheCanvas.drawPaint(paint);
				paint.setColor(Color.BLACK);
				cacheCanvas.drawColor(Color.WHITE);
				invalidate();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawBitmap(cachebBitmap, 0, 0, null);
			canvas.drawPath(path, paint);

		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {

			int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
			int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;

			if (curW >= w && curH >= h) {

				return;

			}
			if (curW < w) {

				curW = w;
			}
			if (curH < h) {

				curH = h;
			}

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Config.RGB_565);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);

			if (cachebBitmap != null) {

				newCanvas.drawBitmap(cachebBitmap, 0, 0, null);

			}

			cachebBitmap = newBitmap;
			cacheCanvas = newCanvas;
		}

		private float cur_x, cur_y;

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				cur_x = x;
				cur_y = y;
				path.moveTo(cur_x, cur_y);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				path.quadTo(cur_x, cur_y, x, y);
				cur_x = x;
				cur_y = y;
				isSign = true;
				break;
			}
			case MotionEvent.ACTION_UP: {
				cacheCanvas.drawPath(path, paint);
				path.reset();
				break;
			}
			}
			invalidate();
			return true;
		}
	}

	private String createFile() {

		ByteArrayOutputStream baos = null;
		String _path = null;
		try {

			String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
					this);

			// if(!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			// ViewUtils.makeToast(SignNameAndConfirmActivity.this, "内存卡没有就绪！",
			// 1500);
			// return "";
			// }

			String sdCardPath = Environment.getExternalStorageDirectory()
					.getPath();
			String[] direList = new File(sdCardPath).list();
			LogUtil.syso("direList length=" + direList.length);

			String restoreDir = sdCardPath + "/" + this.getPackageName()
					+ "/myImage/";
			/*
			 * for(int i=0;i<direList.length;i++){ LogUtil.syso("====");
			 * LogUtil.syso("==="+sdCardPath+"\\"+direList[i]); File file = new
			 * File(sdCardPath+"//"+direList[i]);
			 * LogUtil.syso("=="+sdCardPath+"//"+direList[i]);
			 * if(file.isDirectory()&!file.isHidden()){ restoreDir =
			 * sdCardPath+"/"+direList[i]+"/myImage/"; break; } }
			 */
			File ff = new File(restoreDir);
			ff.mkdirs();
			_path = restoreDir + customerNum + "_signname.jpg";
			LogUtil.syso("_path==" + _path);
			StorageCustomerInfoUtil.putInfo(this, "signname", _path);
			baos = new ByteArrayOutputStream();
			mSignBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
			byte[] photoBytes = baos.toByteArray();
			File file = new File(_path);
			LogUtil.syso("file=" + file);
			if (photoBytes != null) {
				new FileOutputStream(new File(_path)).write(photoBytes);
			}
		} catch (IOException e) {
			LogUtil.syso("create signname error==" + e.getMessage());
			e.printStackTrace();

		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _path;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
public void changeButtonStatus(Boolean isClickable){
	if (isClickable) {
		confirmtotrade.setBackgroundResource(R.drawable.button_click_selector);
		confirmtotrade.setClickable(true);
	} else {
		confirmtotrade.setBackgroundResource(R.color.gray_light);
		confirmtotrade.setClickable(false);
	}
}

}

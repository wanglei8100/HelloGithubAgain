package com.caihongcity.com.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.itron.cswiper4.CSwiper;
import com.itron.cswiper4.CSwiperStateChangedListener;
import com.itron.cswiper4.DecodeResult;
import com.caihongcity.com.utils.LogUtil;

public class GetKsnService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private CSwiper cSwiperController;
	private CSwiperListener cSwiperListener;
	@Override
	public void onCreate() {
		super.onCreate();
		new Thread(){

			public void run() {
				
				
				cSwiperListener = new CSwiperListener();
				cSwiperController = CSwiper.GetInstance(GetKsnService.this, cSwiperListener);
//				String ksn = cSwiperListener.getKSN(cSwiperController);
				/*if (TextUtils.isEmpty(ksn)) {
					ksn = 123456789
				}*/
				String ksn = cSwiperController.getKSN();
				LogUtil.e("ksn:"+ksn);
				
			};
		}.start();
	}

	class CSwiperListener implements CSwiperStateChangedListener {

		@Override
		public void EmvOperationWaitiing() {
			LogUtil.e("IC卡插入，请勿拔出");
		}

		@Override
		public void onCardSwipeDetected() {
			LogUtil.e("CSwiperListener 用户已刷卡");
		}

		@Override
		public void onDecodeCompleted(String formatID, String ksn,
				String encTracks, int track1Length, int track2Length,
				int track3Length, String randomNumber, String maskedPAN,
				String pan, String expiryDate, String cardHolderName,
				String mac, int cardType, byte[] cardSeriNo, byte[] ic55Data) {
			LogUtil.e("CSwiperListener 刷卡返回数据");
		}

		@Override
		public void onDecodeError(DecodeResult paramDecodeResult) {
			LogUtil.e("CSwiperListener"+paramDecodeResult);
		}

		@Override
		public void onDecodingStart() {
			LogUtil.e("CSwiperListener 开始解码");
		}

		@Override
		public void onDevicePlugged() {
			LogUtil.e("CSwiperListener 刷卡器插入手机");
		}

		@Override
		public void onDeviceUnplugged() {
			LogUtil.e("CSwiperListener 未检测到刷卡设备");
		}

		@Override
		public void onError(int errcode, String paramString) {
			LogUtil.e("CSwiperListener"+ errcode +" : "+paramString);
		}

		@Override
		public void onICResponse(int result, byte[] resuiltScript, byte[] data) {
			LogUtil.e("onICResponse "+result+" : "+resuiltScript.toString()+" : "+data.toString());
		}

		@Override
		public void onInterrupted() {
			LogUtil.e("CSwiperListener 用户中断操作");
		}

		@Override
		public void onNoDeviceDetected() {
			LogUtil.e("CSwiperListener 未检测到刷卡设备");
		}

		@Override
		public void onTimeout() {
			LogUtil.e("CSwiperListener 操作超时");
		}

		@Override
		public void onWaitingForCardSwipe() {
			LogUtil.e("CSwiperListener 找到刷卡设备等待刷卡");
		}

		@Override
		public void onWaitingForDevice() {
			LogUtil.e("CSwiperListener 查找设备中...");
		}
	}
}

package com.caihongcity.com.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class ZoomImageView extends ImageView implements OnGlobalLayoutListener,
		OnScaleGestureListener, OnTouchListener {

	private boolean mOnce;// 图片初始化操作标识，只需要做一次
	private float initScale;// 初始化缩放比例
	private float middleScale;// 双击放大的比例
	private float maxScale;// 最大放大比例
	private Matrix matrix;
	private ScaleGestureDetector mScaleGestureDetector;// 捕获多点触控所需要类

	// --------------4 自由移动
	private int mLastPointerCount;// 上次多点触控时手指的数量
	private float mLastX;
	private float mLastY;
	private int mTouchSlope = 10;// 触发移动操作的参考值
	private boolean isCanDrag;
	
	//---------------5 双击放大缩小
	private GestureDetector mGestureDetector;
	private boolean isAutoScale;
	private OnSingleClickListner mOnSingleClickListner;

	public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		matrix = new Matrix();
		setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		setOnTouchListener(this);// 自己处理触控事件的监听
//		mTouchSlope = ViewConfiguration.get(context).getScaledTouchSlop();
		
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
			/* 
			 * 单击事件的处理
			 */
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (mOnSingleClickListner!=null) {
					mOnSingleClickListner.onSingleClickCallback();
					return true;
				} 
				return super.onSingleTapConfirmed(e);
			}
		/**
		 * 双击事件的处理
		 */
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (isAutoScale) {
					return true;
				}
				
				float x = e.getX();
				float y = e.getY();
				if (getScale()<middleScale) {
					/*matrix.postScale(middleScale/getScale(), middleScale/getScale(), x, y);
					setImageMatrix(matrix);*/	
					postDelayed(new AutoScaleRnnable(middleScale, x, y), 20); 
					isAutoScale = true;
				} else {
					/*matrix.postScale(initScale/getScale(), initScale/getScale(), x, y);
					setImageMatrix(matrix);*/
					postDelayed(new AutoScaleRnnable(initScale, x, y), 20); 
					isAutoScale = true;
				}
				
				return true;
			}
		});
	}
	/**
	 * 单击事件的接口
	 * @author wangshan
	 *
	 */
	public interface OnSingleClickListner{
		abstract void onSingleClickCallback();
	}
	/**
	 * 单击事件的回调
	 * @param l
	 */
	public void setOnSingleClickListner(OnSingleClickListner l){
		this.mOnSingleClickListner = l;
		
	}
	/**
	 * 自动放大和缩小
	 * @author wangshan
	 *
	 */
	private class AutoScaleRnnable implements Runnable{
		
		private float mTargetScale;
		private float x;
		private float y;
		private final float BIGGER = 1.07f;
		private final float SMALLER = 0.93f;
		private float tempScale;
		
		public AutoScaleRnnable(float mTargetScale, float x, float y) {
			this.mTargetScale = mTargetScale;
			this.x = x;
			this.y = y;
			if (getScale()<mTargetScale) {
				tempScale = BIGGER;
			}
			if (getScale()> mTargetScale) {
				tempScale = SMALLER;
			}
		}

		@Override
		public void run() {
			matrix.postScale(tempScale, tempScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(matrix);
			float currentScale = getScale();
			if ((tempScale>1.0f&&currentScale<mTargetScale)||(tempScale<1.0f&&currentScale>mTargetScale)) {
				postDelayed(this, 20);
			}else {
				float scale = mTargetScale/currentScale;
				matrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(matrix);
				isAutoScale = false;
			}
		}
		
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomImageView(Context context) {
		this(context, null);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	// 1
	// 全局的布局完成后会调用，进行图片初始化加载的缩放,可在onAttachedToWindow中注册和在onDetachedFromWindow中移除
	@Override
	public void onGlobalLayout() {
		if (!mOnce) {
			// 获取控件的宽高
			int width = getWidth();
			int height = getHeight();
			// 获取图片的宽高
			Drawable d = getDrawable();
			if (d == null)
				return;
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();
			// 进行图片缩放比例的计算
			float scale = 1.0f;
			// 如果图片的宽度大于控件的宽度且图片的高度小于控件的高度，让图片进行缩小
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;
			}
			// 如果图片的宽度小于控件的宽度且图片的高度大于控件的高度，让图片进行缩小
			if (dw < width && dh > height) {
				scale = height * 1.0f / dh;
			}
			// 如果图片的宽度大于控件的宽度且图片的高度大于控件的高度，让图片进行缩小
			// 如果图片的宽度小于控件的宽度且图片的高度小于控件的高度，让图片进行放大
			if ((dw >= width && dh >= height) || (dw <= width && dh <= height)) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			}
			// 得到初始化时的缩放比例
			initScale = scale;
			middleScale = initScale * 2;
			maxScale = initScale * 4;
			// 将图片移动到当前控件的中心
			int dx = width / 2 - dw / 2;
			int dy = height / 2 - dh / 2;
			// 使用matrix进行缩放和平移,先平移再缩放
			matrix.postTranslate(dx, dy);
			matrix.postScale(initScale, initScale, width / 2, height / 2);
			setImageMatrix(matrix);
			mOnce = true;
		}
	}

	// 2 多点触控的缩放
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();// 当前图片的缩放值
		float scaleFactor = detector.getScaleFactor();// 想要缩放的值
		if (getDrawable() == null)
			return true;
		// 缩放范围的控制在 initScale和maxScale之间

		if ((scale < maxScale && scaleFactor > 1.0f)
				|| (scale > initScale && scaleFactor < 1.0f)) {
			if (scale * scaleFactor < initScale) {
				scaleFactor = initScale / scale;
			}
			if (scale * scaleFactor > maxScale) {
				scaleFactor = maxScale / scale;
			}
			// 缩放
			matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(),
					detector.getFocusY());
			// 缩放时要不断检测边界
			checkBorderAndCenterWhenScale();
			setImageMatrix(matrix);
		}
		return true;
	}

	/**
	 * 获得图片放大缩小后的宽高及l,t,r,b
	 * 
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix tempMatrix = matrix;
		RectF rectF = new RectF();
		Drawable drawable = getDrawable();
		if (drawable != null) {
			rectF.set(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			tempMatrix.mapRect(rectF);
		}
		return rectF;
	}

	/**
	 * 在缩放时进行边界和位置的控制
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;
		int width = getWidth();
		int height = getHeight();
		if (rect.width() >= width) {// 水平方向的控制
			if (rect.left > 0) {
				deltaX = -rect.left;
			}
			if (rect.right < width) {
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height) {// 垂直方向的控制
			if (rect.top > 0) {
				deltaY = -rect.top;
			}
			if (rect.bottom < height) {
				deltaY = height - rect.bottom;
			}
		}
		// 如果宽度或高度小于控件的宽高，让其居中
		if (rect.width() < width) {
			deltaX = width / 2f - rect.right + rect.width() / 2f;
		}
		if (rect.height() < height) {
			deltaY = height / 2f - rect.bottom + rect.height() / 2f;
		}
		matrix.postTranslate(deltaX, deltaY);
	}

	/**
	 * 获取当前图片的缩放值
	 * 
	 * @return
	 */
	private float getScale() {
		float[] values = new float[9];
		matrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//首先把双击事件交给mGestureDetector去处理
		mGestureDetector.onTouchEvent(event);
		// 让触控事件交给mScaleGestureDetector去处理
		mScaleGestureDetector.onTouchEvent(event);

		// 多点触控的中心点坐标
		float x = 0;
		float y = 0;
		// 得到多点触控的数量
		int pointerCount = event.getPointerCount();
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x /= pointerCount;
		y /= pointerCount;

		if (mLastPointerCount != pointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		mLastPointerCount = pointerCount;

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = x - mLastX;
			float dy = y - mLastY;
			if (!isCanDrag) {
				isCanDrag = isMoveAction(dx, dy);
			}
			if (isCanDrag) {
				RectF rectF = getMatrixRectF();
				if (getDrawable() != null) {

					if (rectF.width() < getWidth()) {
						dx = 0;
					}
					if (rectF.height() < getHeight()) {
						dy = 0;
					}

					matrix.postTranslate(dx, dy);
//					checkBorderWhenTranslate();
					checkBorderAndCenterWhenScale();
					setImageMatrix(matrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mLastPointerCount = 0;
			break;
		}
		return true;
	}

	/**
	 * 当自由移动时检测边界
	 */
	private void checkBorderWhenTranslate() {
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;
		int width = getWidth();
		int height = getHeight();
		if (rect.width() >= width) {// 水平方向的控制
			if (rect.left > 0) {
				deltaX = -rect.left;
			}
			if (rect.right < width) {
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height) {// 垂直方向的控制
			if (rect.top > 0) {
				deltaY = -rect.top;
			}
			if (rect.bottom < height) {
				deltaY = height - rect.bottom;
			}
		}
		
		matrix.postTranslate(deltaX, deltaY);
	}

	/**
	 * 判断是否足以触发move
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isMoveAction(float dx, float dy) {
		return mTouchSlope < Math.sqrt(dx * dx + dy * dy);
	}

}

package com.caihongcity.com.utils;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;


/**
 * 
 * @author hkrt
 * 处理图片
 */
public class BitmapManage {
	
	//计算图片的缩放值
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	             final int heightRatio = Math.round((float) height/ (float) reqHeight);
	             final int widthRatio = Math.round((float) width / (float) reqWidth);
	             inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	        return inSampleSize;
	}

	
	// 根据路径获得图片并压缩，返回bitmap用于显示
	public static Bitmap getSmallBitmap(String filePath,int reqWidth, int reqHeight) {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filePath, options);

	        // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	        // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;

	    return BitmapFactory.decodeFile(filePath, options);
	    }

	
	//把bitmap转换成String
	public static String bitmapToString(String filePath,int reqWidth, int reqHeight) {

	        Bitmap bm = getSmallBitmap(filePath,reqWidth,reqHeight);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
	        byte[] b = baos.toByteArray();
	        return Base64.encode(b);
	    }

	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,  
               double newHeight) {  
		
       // 获取这个图片的宽和高  
       float width = bgimage.getWidth();  
       float height = bgimage.getHeight();  
       // 创建操作图片用的matrix对象  
       Matrix matrix = new Matrix();  
       // 计算宽高缩放率  
       float scaleWidth = ((float) newWidth) / width;  
       float scaleHeight = ((float) newHeight) / height;  
       LogUtil.syso("scaleWidth="+scaleWidth);
       LogUtil.syso("scaleHeight="+scaleHeight);
       // 缩放图片动作  
       matrix.postScale(0.5f, 0.4f);  
       Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,  
                       (int) height, matrix, true);  
       
       Canvas canvas = new Canvas(bitmap);//用bm创建一个画布 可以往bm中画入东西了
       canvas.drawBitmap(bgimage,matrix,null);

       return bitmap;  
	}  
	
	
	 /*
     * Drawable转化为Bitmap 
    */
	public  Bitmap drawableToBitmap(Drawable drawable) {  
	   
	    int width = drawable.getIntrinsicWidth();  
	    int height = drawable.getIntrinsicHeight();  
	    Bitmap bitmap = Bitmap.createBitmap(width, height,drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);  
	    Canvas canvas = new Canvas(bitmap);  
	    drawable.setBounds(0,0,width,height);  
	    drawable.draw(canvas);  
	    
	    return bitmap;  
	 } 
	/*
	 * 
	 * 当我们需要圆角的时候，调用这个方法，第一个参数是传入需要转化成圆角的图片，
	 * 第二个参数是圆角的度数，数值越大，圆角越大 
	 * 
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) { 
		
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888); 
			Canvas canvas = new Canvas(output); 
			final int color = 0xff424242; 
			final Paint paint = new Paint(); 
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
			final RectF rectF = new RectF(rect); 
			final float roundPx = pixels; 
			paint.setAntiAlias(true); 
			canvas.drawARGB(0, 0, 0, 0); 
			paint.setColor(color); 
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
			canvas.drawBitmap(bitmap, rect, rect, paint); 
			return output; 
		
		} 



}

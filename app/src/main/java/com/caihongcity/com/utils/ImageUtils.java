package com.caihongcity.com.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;


public class ImageUtils {
		public static Bitmap loadImageFromUrl(String url, int sc)
    {

        URL m;
        InputStream i = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream out = null;

        if (url == null)
            return null;
        try
        {
            m = new URL(url);
            i = (InputStream) m.getContent();
            bis = new BufferedInputStream(i, 1024 * 4);
            out = new ByteArrayOutputStream();
            int len = 0;
byte[] isBuffer = new byte[1024*5];
            while ((len = bis.read(isBuffer)) != -1)
            {
                out.write(isBuffer, 0, len);
            }
            out.close();
            bis.close();
        } catch (MalformedURLException e1)
        {
            e1.printStackTrace();
            return null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        if (out == null)
            return null;
        byte[] data = out.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inJustDecodeBounds = false;
        int be = (int) (options.outHeight / (float) sc);
        if (be <= 0)
        {
            be = 1;
        } else if (be > 3)
        {
            be = 3;
        }
        options.inSampleSize = be;
        Bitmap bmp =null;
        try
        {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options); //返回缩略图
        } catch (OutOfMemoryError e)
        {
            System.gc();
            bmp =null;
        }
        return bmp;
    } 
	/** 
	    * 加载本地图片 
	    * @param url 
	    * @return 
	    */  
	    public static Bitmap getLoacalBitmapByAssets(Context c, String url)  
	    {  
	        Bitmap bitmap = null;  
	        InputStream in = null;  
	        try  
	        {  
	            in = c.getResources().getAssets().open(url);  
	            bitmap = BitmapFactory.decodeStream(in);  
	        }  
	        catch (IOException e)  
	        {  
	            e.printStackTrace();  
	        }  
	        finally  
	        {
				try
				{
					if (null != in)
					{
						in.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
	        }  
	        return bitmap;  
	    }  
	  
	    /** 
	    * 从服务器取图片 
	    * @param url 
	    * @return 
	    */  
	    public static Bitmap getHttpBitmap(String url)  
	    {  
	        URL myFileUrl = null;
	        Bitmap bitmap = null;
	        InputStream in = null;  
	        try  
	        {  
	            myFileUrl = new URL(url);  
	            HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();  
	            conn.setConnectTimeout(1000*60);
	            conn.setDoInput(true);  
	            conn.connect();  
	            in = conn.getInputStream();  
	            bitmap = BitmapFactory.decodeStream(in);  
	        }  
	        catch (IOException e)  
	        {  
	            e.printStackTrace();  
	        }  
	        finally  
	        {
				try
				{
					if (null != in)
					{
						in.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
	        return bitmap;  
	    }


	    
	    private static final String TAG = "uploadFile";


	    private static final int TIME_OUT = 90 * 1000; // 超时时间


	    private static final String CHARSET = "UTF-8"; // 设置编码


	    /**
	     * Android上传文件到服务端
	     * 
	     * @param file 需要上传的文件
	     * @param RequestURL 请求的rul
	     * @return 返回响应的内容
	     */
	    public static String uploadFile(File file, String RequestURL) {
			LogUtil.d("uploadFile","RequestURL:"+RequestURL);
	        String result = null;
	        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
	        String PREFIX = "--", LINE_END = "\r\n";
	        String CONTENT_TYPE = "multipart/form-data"; // 内容类型


	        try {
	            URL url = new URL(RequestURL);//http://192.168.1.200:9002/hatchet-posp-proxy/uploadImage.app?0=0700&3=190968&11=204240&42=220558015061077&59=CHDS-1.0.0&64=b28188838455c0da2becd01ce3311589
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setReadTimeout(TIME_OUT);
	            conn.setConnectTimeout(TIME_OUT);
	            conn.setDoInput(true); // 允许输入流
	            conn.setDoOutput(true); // 允许输出流
	            conn.setUseCaches(false); // 不允许使用缓存
	            conn.setRequestMethod("POST"); // 请求方式
	            conn.setRequestProperty("Charset", CHARSET); // 设置编码
	            conn.setRequestProperty("connection", "keep-alive");
	            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
				conn.connect();

	            if (file != null) {
	                /**
	                 * 当文件不为空，把文件包装并且上传
	                 */
	                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
	                StringBuffer sb = new StringBuffer();
	                sb.append(PREFIX);
	                sb.append(BOUNDARY);
	                sb.append(LINE_END);
	                /**
	                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
	                 * filename是文件的名字，包含后缀名的 比如:abc.png
	                 */


	                sb.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
	                        + file.getName() + "\"" + LINE_END);
	                sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
	                sb.append(LINE_END);
	                dos.write(sb.toString().getBytes());
	                InputStream is = new FileInputStream(file);
	                byte[] bytes = new byte[1024*10];
	                int len = 0;
	                while ((len = is.read(bytes)) != -1) {
	                    dos.write(bytes, 0, len);
	                }
	                is.close();
	                dos.write(LINE_END.getBytes());
	                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
	                dos.write(end_data);
	                dos.flush();
	                /**
	                 * 获取响应码 200=成功 当响应成功，获取响应的流
	                 */
	                int res = conn.getResponseCode();
	                LogUtil.i(TAG, "response code:" + res);
	                // if(res==200)
	                // {
	                LogUtil.e(TAG, "request success");
	               /* InputStream input = conn.getInputStream();
	                StringBuffer sb1 = new StringBuffer();
	                int ss;
	                while ((ss = input.read()) != -1) {
	                    sb1.append((char) ss);
	                }
	                result = sb1.toString();
//	                result = URLEncoder.encode(sb1.toString(), HTTP.UTF_8);
	                LogUtils.e(TAG, "result : " + result);*/
	                // }
	                // else{
	                // LogUtils.e(TAG, "request error");
	                // }
	                
	                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	                StringBuffer sb1 = new StringBuffer();
	                String ss;
	                while ((ss = reader.readLine()) != null) {
	                    sb1.append(ss);
	                }
	                result = sb1.toString();
	                LogUtil.e(TAG, "result : " + result);//{"0":"0700","3":"190968","64":"B28188838455C0DA2BECD01CE3311589","59":"CHDS-1.0.0","42":"220558015061077","11":"204240"}
	            }
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return result;
	    }


	    /**
	     * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	     * 
	     * @param url Service net address
	     * @param params text content
	     * @param files pictures
	     * @return String result of Service response
	     * @throws IOException
	     */
	    public static String post(String url, Map<String, String> params, Map<String, File> files)
	            throws IOException {
	        String BOUNDARY = java.util.UUID.randomUUID().toString();
	        String PREFIX = "--", LINEND = "\r\n";
	        String MULTIPART_FROM_DATA = "multipart/form-data";
	        String CHARSET = "UTF-8";


	        URL uri = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
	        conn.setReadTimeout(10 * 1000); // 缓存的最长时间
	        conn.setDoInput(true);// 允许输入
	        conn.setDoOutput(true);// 允许输出
	        conn.setUseCaches(false); // 不允许使用缓存
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("connection", "keep-alive");
	        conn.setRequestProperty("Charsert", "UTF-8");
	        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

	        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

	        // 首先组拼文本类型的参数
	        StringBuilder sb = new StringBuilder();
	        if (params!=null) {
	        	for (Map.Entry<String, String> entry : params.entrySet()) {
	        		sb.append(PREFIX);
	        		sb.append(BOUNDARY);
	        		sb.append(LINEND);
	        		sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
	        		sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
	        		sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
	        		sb.append(LINEND);
	        		sb.append(entry.getValue());
	        		sb.append(LINEND);
	        	}
	        	outStream.write(sb.toString().getBytes());
			}
	        // 发送文件数据
	        if (files != null)
	            for (Map.Entry<String, File> file : files.entrySet()) {
	                StringBuilder sb1 = new StringBuilder();
	                sb1.append(PREFIX);
	                sb1.append(BOUNDARY);
	                sb1.append(LINEND);
	                sb1.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
	                        + file.getValue().getName() + "\"" + LINEND);
	                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
	                sb1.append(LINEND);
	                outStream.write(sb1.toString().getBytes());


	                InputStream is = new FileInputStream(file.getValue());
	                byte[] buffer = new byte[1024];
	                int len = 0;
	                while ((len = is.read(buffer)) != -1) {
	                    outStream.write(buffer, 0, len);
	                }


	                is.close();
	                outStream.write(LINEND.getBytes());
	            }


	        // 请求结束标志
	        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
	        outStream.write(end_data);
	        outStream.flush();
	        // 得到响应码
	        int res = conn.getResponseCode();
	        InputStream in = conn.getInputStream();
	        StringBuilder sb2 = new StringBuilder();
	        if (res == 200) {
	            int ch;
	            while ((ch = in.read()) != -1) {
	                sb2.append((char) ch);
	            }
	        }
	        outStream.close();
	        conn.disconnect();
	        return sb2.toString();
	    }

	public static String uploadFile2Sever(File file,String url) {
		HttpPost request = new HttpPost(url);
		HttpClient httpClient = new DefaultHttpClient();
		FileEntity entity = new FileEntity(file, "binary/octet-stream");
		HttpResponse response;
		request.setEntity(entity);
		entity.setContentEncoding("binary/octet-stream");
		try {
			response = httpClient.execute(request);
			System.out.println(response.toString());
			return response.toString();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {

		}
		return "";
	}

	/**
	 * TODO 缩放图片
	 *
	 * @param imagePath
	 * @param scale
	 *            压缩比率 可以为空,那么是默认是200,越大压缩比越小
	 * @return 一个缩放好的bitmap
	 * 此压缩方法存在的弊端是：不能精确压缩图片的大小，只能整数倍压缩，因为压缩参数为int型
	 */
	public static Bitmap getZoomBitmap(String imagePath, Float scale) {
		if (!new File(imagePath).exists())
			return null;
		if (scale == null) {
			scale = 600f;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeFile(imagePath, options);
		int beH = (int) ((options.outHeight / (float) scale)+0.5);
		int beW = (int) ((options.outWidth / (float) scale)+0.5);
		int be = 1;
		be = (beH >= beW) ? beH : beW;
		if (be <= 1) {
			be = 1;
		}
		options.inSampleSize = be;// be=2.表示压缩为原来的1/2,以此类推
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(imagePath, options);
		return bm;
	}

	/**
	 * 根据指定宽高压缩BitMap
	 * @param bm
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap scaleImg(Bitmap bm, int newWidth, int newHeight) {

// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
// 设置想要的大小
		int newWidth1 = newWidth;
		int newHeight1 = newHeight;
// 计算缩放比例
		float scaleWidth = ((float) newWidth1) / width;
		float scaleHeight = ((float) newHeight1) / height;
// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
				true);
		return newbm;

	}

	/**
	 *
	 * 根据指定宽高压缩本地资源图片
	 * @param res
	 * @param resId 资源id
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap scaleImage(Resources res, int resId,
									int reqWidth, int reqHeight){
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = inSampleSize;
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 根据指定宽高压缩本地sd卡图片
	 * @param filePath 图片路径
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap scaleImage(String filePath,
									int reqWidth, int reqHeight){
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = inSampleSize;
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * 根据指定宽高压缩网络图片
	 * @param fileUrl 图片的网络地址
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap scaleImageFromUrl(String fileUrl,
									int reqWidth, int reqHeight){
		try {
			URL myFileUrl = new URL(fileUrl);
			HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
			conn.setConnectTimeout(1000 * 60);
			conn.setDoInput(true);
			conn.connect();
			InputStream fileStream = conn.getInputStream();
			// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(fileStream, new Rect(0,0,0,0),options);
			// 源图片的高度和宽度
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;
			if (height > reqHeight || width > reqWidth) {
                // 计算出实际宽高和目标宽高的比率
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
                // 一定都会大于等于目标的宽和高。
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
			// 调用上面定义的方法计算inSampleSize值
			options.inSampleSize = inSampleSize;
			// 使用获取到的inSampleSize值再次解析图片
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(fileStream, new Rect(0, 0, 0, 0), options);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return  null;
	}

}

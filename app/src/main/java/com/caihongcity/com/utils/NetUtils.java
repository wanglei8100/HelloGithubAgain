package com.caihongcity.com.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caihongcity.com.R;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2015/9/8 0008.
 */
public class NetUtils {
    public static RequestQueue newRequestQueue;
    static Dialog loading;
    public static void sendStringRequest(final Context context, final String url, final String tag,final RequestCallBack callback){
        LogUtil.d(tag,url);
        //检查网络状态
        if (CommonUtils.getConnectedType(context) == -1) {
            ViewUtils.makeToast(context, "没有可用的网络", 1500);
            return;
        }
        newRequestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                LogUtil.d(tag,"onResponse==="+s);
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.successful(s);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ViewUtils.makeToast(context, context.getString(R.string.server_error),1500);
                LogUtil.e(tag, "onErrorResponse===" + volleyError.toString());
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.errored(volleyError.getMessage());
            }
        });
        stringRequest.setTag(tag);
        callback.loading();

        if (loading == null) {
            loading = ViewUtils.createLoadingDialog(context, "请稍后...", true);
        }
        loading.show();
        newRequestQueue.add(stringRequest);
    }
    public static void sendStringRequest_Post(final Context context, final String url, final Map<String, String> map ,final String tag,final RequestCallBack callback){
        LogUtil.d(tag,url);
        //检查网络状态
        if (CommonUtils.getConnectedType(context) == -1) {
            ViewUtils.makeToast(context, "没有可用的网络", 1500);
            return;
        }
        newRequestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                LogUtil.d(tag,"onResponse==="+s);
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.successful(s);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ViewUtils.makeToast(context, context.getString(R.string.server_error),1500);
                LogUtil.e(tag, "onErrorResponse==="+volleyError.toString());
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.errored(volleyError.getMessage());
            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                LogUtil.d(tag,"getParams==="+map.toString());
                return map;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        callback.loading();

        if (loading == null) {
            loading = ViewUtils.createLoadingDialog(context, "请稍后...", true);
        }
        loading.show();
        newRequestQueue.add(stringRequest);
    }
    public static void sendJsonRequest_Post(final Context context, final String url, JSONObject jsonObject ,final String tag,final RequestCallBack callback){
        LogUtil.d(tag,url);
        //检查网络状态
        if (CommonUtils.getConnectedType(context) == -1) {
            ViewUtils.makeToast(context, "没有可用的网络", 1500);
            return;
        }
        newRequestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest stringRequest = new JsonObjectRequest( url,jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                LogUtil.d(tag,"onResponse==="+String.valueOf(jsonObject));
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.successful(String.valueOf(jsonObject));
            }

        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ViewUtils.makeToast(context, context.getString(R.string.server_error),1500);
                LogUtil.e(tag, "onErrorResponse==="+volleyError.toString());
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.errored(volleyError.getMessage());
            }

        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        callback.loading();

        if (loading == null) {
            loading = ViewUtils.createLoadingDialog(context, "请稍后...", true);
        }
        loading.show();
        newRequestQueue.add(stringRequest);
    }
    public static void sendImageRequest(final Context context, final String url, final String tag,final RequestImageCallBack callback){
        LogUtil.d(tag,url);
        //检查网络状态
        if (CommonUtils.getConnectedType(context) == -1) {
            ViewUtils.makeToast(context, "没有可用的网络", 1500);
            return;
        }
        newRequestQueue = Volley.newRequestQueue(context);
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                LogUtil.d(tag,"onResponse===Success");
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.successful(bitmap);
            }
        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ViewUtils.makeToast(context, context.getString(R.string.server_error),1500);
                LogUtil.e(tag, "onErrorResponse==="+volleyError.toString());
                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                callback.errored(volleyError.getMessage());
            }
        });
        imageRequest.setTag(tag);
        if (loading == null) {
            loading = ViewUtils.createLoadingDialog(context, "请稍后...", true);
        }
        loading.show();
        newRequestQueue.add(imageRequest);
    }
    public interface RequestCallBack{
        void loading();
        void successful(String response);
        void errored(String response);
    }
    public interface RequestImageCallBack{
        void successful(Bitmap response);
        void errored(String response);
    }

}

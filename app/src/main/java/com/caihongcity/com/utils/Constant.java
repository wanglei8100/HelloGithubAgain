package com.caihongcity.com.utils;

import android.text.TextUtils;

import java.util.HashMap;

public class Constant {

    //  public static final String BASE_URL = "http://118.186.1.34";
//    public static final String BASE_URL = "http://192.168.1.200:9002";
    public static final String BASE_URL = "http://120.132.39.35:9000";//生产
//    public static final String BASE_URL = "http://118.186.1.34:8081";//

    public static final String REQUEST_API = BASE_URL + "/hatchet-posp-proxy/request.app?";
//    public static final String REQUEST_API = BASE_URL + "/hytx-posp-proxy/request.app?";
    public static final String UPLOADIMAGE = BASE_URL + "/hatchet-posp-proxy/uploadImage.app?";
    public static final String mainKey = "21E4ACD4CD5D4619B063F40C5A454F7D";
    public static String URL = "";
    public static String CONSUME = "consume";
    public static String QUERYBALANCE = "querybalance";
    public static String CANCEL = "canceltrade";
    public static String VERSION = "CHDS-A-1.9.1";
    public static String AGENCY_CODE44 = BASE_URL.equals("http://120.132.39.35:9000") ? "2800055" : "100602183";//44域 机构编码 生成环境(任妮刷  2800056  彩虹都市 2800055 上海可付 2800075 )  机构编码 测试环境 100602183
//    public static String AGENCY_CODE44 = BASE_URL.equals("http://118.186.1.34:8081") ? "2800055" : "100602183";//44域 机构编码 生成环境(任妮刷  2800056  彩虹都市 2800055 上海可付 2800075 )  机构编码 测试环境 100602183
    public static String DOWNLOAD_APK = "http://www.ychpay.com/android/CaiHongCity.apk";
    public static String SUPERMAKET_FEERATE = "0.49";//超市费率和余额查询费率
    public static String getUrl(HashMap<Integer, String> parameterMap) {
        StringBuffer urlBuffer = new StringBuffer();
        if (!TextUtils.isEmpty(parameterMap.get(65))) {//特殊的请求链接 如上传图片
            urlBuffer.append(parameterMap.get(65));
        } else {
            urlBuffer.append(REQUEST_API);
        }
        for (int i = 0; i <= 64; i++) {
            if (!TextUtils.isEmpty(parameterMap.get(i)))
                if (i == 0) {
                    urlBuffer.append(i + "=" + parameterMap.get(i));
                } else {
                    urlBuffer.append("&" + i + "=" + parameterMap.get(i));
                }
        }
        LogUtil.i("requestUrl", "requestUrl==" + urlBuffer.toString());
        return urlBuffer.toString();
    }


    public static String getUrl2(HashMap<String, String> parameterMap) {
        StringBuffer urlBuffer = new StringBuffer();
        if (!TextUtils.isEmpty(parameterMap.get(65))) {//特殊的请求链接 如上传图片
            urlBuffer.append(parameterMap.get(65));
        } else {
            urlBuffer.append(REQUEST_API);
        }
        for (int i = 0; i <= 64; i++) {
            if (!TextUtils.isEmpty(parameterMap.get(i)))
                if (i == 0) {
                    urlBuffer.append(i + "=" + parameterMap.get(i));
                } else {
                    urlBuffer.append("&" + i + "=" + parameterMap.get(i));
                }
        }
        LogUtil.i("requestUrl", "requestUrl==" + urlBuffer.toString());
        return urlBuffer.toString();
    }

    public static String getMacData(HashMap<Integer, String> parameterMap) {
        StringBuffer urlBuffer = new StringBuffer();
        for (int i = 0; i <= 63; i++) {
            if (!TextUtils.isEmpty(parameterMap.get(i))) urlBuffer.append(parameterMap.get(i));
        }
        LogUtil.i("macData", "macData==" + urlBuffer.toString());
        return CommonUtils.Md5(urlBuffer.toString() + mainKey);
    }

}

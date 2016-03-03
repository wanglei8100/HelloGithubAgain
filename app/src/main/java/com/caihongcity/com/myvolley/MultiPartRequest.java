package com.caihongcity.com.myvolley;

import java.io.File;
import java.util.Map;

public interface MultiPartRequest {
  
    public void addFileUpload(String param,File file);
      
    public void addStringUpload(String param,String content);   
      
    public Map<String,File> getFileUploads();  
      
    public Map<String,String> getStringUploads();

        /*
        //使用volley进行图片上传  示例
        File file = new File(signPath);
        final Map<String,File>  files =  new HashMap<String, File>();
        files.put(imageType,file);
        final String customerNum = StorageCustomerInfoUtil.getInfo("customerNum",
                CustomerPicInfoActivity.this);
        String data = "0=0700" + "&3=190948&9=" + imageType + "&42=" + customerNum + "&59="
                + Constant.VERSION;
        String macData = "0700" + "190948" + imageType
                + customerNum + Constant.VERSION;
        String url = Constant.UPLOADIMAGE + data + "&64="
                + CommonUtils.Md5(macData + Constant.mainKey);
        RequestQueue mSingleQueue = Volley.newRequestQueue(this, new MultiPartStack());


        MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(
                Request.Method.PUT, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();
                try {
                    if (StringUtil.isEmpty(result)) {
                        ViewUtils.makeToast(CustomerPicInfoActivity.this,
                                getString(R.string.server_error), 1500);
                        return;
                    }
                    JSONObject jsonResult = new JSONObject(result);

                    String strCode = jsonResult.getString("39");
                    String resultValue = MyApplication.getErrorHint(result);
                    if ("00".equals(strCode)) {// 上传成功
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.upload_sucess), 1000);

                    } else {// 上传失败
                        ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.server_error), 1000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                loadingDialog.dismiss();
                ViewUtils.makeToast(CustomerPicInfoActivity.this, getString(R.string.server_error), 1000);
            }
        }) {

            @Override
            public Map<String, File> getFileUploads() {
                return files;
            }

        };

        mSingleQueue.add(multiPartRequest);*/
} 
package com.caihongcity.com.utils;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyStore;

public class MyAsyncTask extends AsyncTask<String, Void, String> {

	HttpClient httpClient;
	private HttpParams httpParams;
	
	private LoadResourceCall loadResourceCall;
	
	public MyAsyncTask(LoadResourceCall loadResourceCall) {
		super();
		this.loadResourceCall = loadResourceCall;
	}

	public MyAsyncTask() {
	}

	public interface LoadResourceCall{
		
		public void isLoadedContent(String content);
		public void isLoadingContent();
		
	}
	
	
	
	@Override
	protected String doInBackground(String... params) {
		String url = params[0];
		httpClient = getNewHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 90*1000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 90*1000);

		String content =null;
//		HttpPost httpPost = new HttpPost(url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				content = EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	@Override
	protected void onPostExecute(String result) {
		LogUtil.v("result==", result+"==");
		loadResourceCall.isLoadedContent(result);
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		loadResourceCall.isLoadingContent();
		super.onPreExecute();
		LogUtil.v("doing==", "doing==");
	}
	
	public HttpClient getHttpClient() {

        this.httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, 90 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 90 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        HttpClientParams.setRedirecting(httpParams, true);
        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);
        httpClient = new DefaultHttpClient(httpParams);
        return httpClient;
    }
	
	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	

}

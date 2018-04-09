package com.xk.httpclient;

import com.xk.httpclient.common.util.PropertiesUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 异步http工具类, 注意事项： 在全部回调完成后，请执行close（），否则线程不会关闭
 * 
 * @author hxk
 * @version Id: HttpAsyncUtil.java, v 0.1 2017/3/22 20:29 hxk Exp $$
 */
public class HttpAsyncUtil {

	private final static int SOCKET_TIMEOUT = Integer.valueOf(PropertiesUtil.getProperty("application.properties","SOCKET_TIMEOUT"));;// 单位ms 从服务器读取数据的timeout
	private final static int CONNECTION_TIMEOUT = Integer.valueOf(PropertiesUtil.getProperty("application.properties","CONNECTION_TIMEOUT"));;// 单位ms 和服务器建立连接的timeout
	private final static int CONNECTION_REQUEST_TIMEOUT = Integer.valueOf(PropertiesUtil.getProperty("application.properties","CONNECTION_REQUEST_TIMEOUT"));;// 单位ms
																// 从连接池获取连接的timeout
	private final static boolean KEEP_ALIVE = false;
	private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECTION_TIMEOUT).setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).build();

	private CloseableHttpAsyncClient httpclient;
	private CloseableHttpAsyncClient httpsclient;
	private static HttpAsyncUtil instance;

	private static ReentrantLock startLock = new ReentrantLock();
	
	public HttpAsyncUtil() {
		httpStart();
		httpsStart();
	}

	private void httpStart()
	{
		if (httpclient == null) {
			httpclient = HttpAsyncClients.createDefault();
		}
		if (httpclient.isRunning()) {
			return;
		}
		httpclient.start();
	}

	public void close()
	{
		try {
			if (httpclient != null && httpclient.isRunning()) {
				httpclient.close();
			}

			if (httpsclient != null && httpsclient.isRunning()) {
				httpsclient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void httpsStart()
	{
		if (httpsclient == null) {
			try {
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException
					{
						return true;
					}
				}).build();
				httpsclient = HttpAsyncClients.custom().setSSLContext(sslContext).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (httpsclient.isRunning()) {
			return;
		}
		httpsclient.start();
	}

	public static HttpAsyncUtil getInstance()
	{
		if (instance == null) {
			instance = new HttpAsyncUtil();
		}
		return instance;
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 */
	public void sendHttpPost(String httpUrl, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		sendHttpPost(httpPost, callback);
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 * @param params
	 *            请求参数
	 * @param charset
	 *            字符编码
	 * @param contentType
	 *            contentType跟请求参数要对应如：params是json根式，contentType为application/
	 *            json
	 */
	public void sendHttpPost(String httpUrl, String params, String charset, String contentType, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		// 设置参数
		StringEntity stringEntity = new StringEntity(params, charset);
		stringEntity.setContentType(contentType);
		httpPost.setEntity(stringEntity);
		sendHttpPost(httpPost, callback);
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 * @param maps
	 *            参数
	 */
	public void sendHttpPost(String httpUrl, Map<String, String> maps, FutureCallback<HttpResponse> callback) throws UnsupportedEncodingException, IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		// 创建参数队列
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : maps.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, maps.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		sendHttpPost(httpPost, callback);
	}

	/**
	 * 发送 post请求（带文件）
	 * 
	 * @param httpUrl
	 *            地址
	 * @param maps
	 *            参数
	 * @param fileLists
	 *            附件
	 */
	public void sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
		for (String key : maps.keySet()) {
			meBuilder.addPart(key, new StringBody(maps.get(key), ContentType.TEXT_PLAIN));
		}
		for (File file : fileLists) {
			FileBody fileBody = new FileBody(file);
			meBuilder.addPart("files", fileBody);
		}
		HttpEntity reqEntity = meBuilder.build();
		httpPost.setEntity(reqEntity);
		sendHttpPost(httpPost, callback);
	}

	/**
	 * 发送 get请求
	 * 
	 * @param httpUrl
	 */
	public void sendHttpGet(String httpUrl, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		sendHttpGet(httpGet, callback);
	}

	public void sendHttpsGet(String url, FutureCallback<HttpResponse> callback)
	{
		HttpGet httpGet = new HttpGet(url);
		sendHttpsGet(httpGet, callback);
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 */
	public void sendHttpsPost(String httpUrl, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		sendHttpsPost(httpPost, callback);
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 * @param params
	 *            请求参数
	 * @param charset
	 *            字符编码
	 * @param contentType
	 *            contentType跟请求参数要对应如：params是json根式，contentType为application/
	 *            json
	 */
	public void sendHttpsPost(String httpUrl, String params, String charset, String contentType, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		// 设置参数
		StringEntity stringEntity = new StringEntity(params, charset);
		stringEntity.setContentType(contentType);
		httpPost.setEntity(stringEntity);
		sendHttpsPost(httpPost, callback);
	}

	/**
	 * 发送 post请求
	 * 
	 * @param httpUrl
	 *            地址
	 * @param maps
	 *            参数
	 */
	public void sendHttpsPost(String httpUrl, Map<String, String> maps, FutureCallback<HttpResponse> callback) throws UnsupportedEncodingException, IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		// 创建参数队列
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : maps.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, maps.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		sendHttpsPost(httpPost, callback);
	}

	/**
	 * 发送 post请求（带文件）
	 * 
	 * @param httpUrl
	 *            地址
	 * @param maps
	 *            参数
	 * @param fileLists
	 *            附件
	 */
	public void sendHttpsPost(String httpUrl, Map<String, String> maps, List<File> fileLists, FutureCallback<HttpResponse> callback) throws IOException, ClientProtocolException
	{
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
		for (String key : maps.keySet()) {
			meBuilder.addPart(key, new StringBody(maps.get(key), ContentType.TEXT_PLAIN));
		}
		for (File file : fileLists) {
			FileBody fileBody = new FileBody(file);
			meBuilder.addPart("files", fileBody);
		}
		HttpEntity reqEntity = meBuilder.build();
		httpPost.setEntity(reqEntity);
		sendHttpsPost(httpPost, callback);
	}

	public void sendHttpPost(HttpPost httpPost, FutureCallback<HttpResponse> callback)
	{
		httpPost.setConfig(requestConfig);
		//httpStart();
		httpclient.execute(httpPost, callback);
	}

	public void sendHttpGet(HttpGet httpGet, FutureCallback<HttpResponse> callback)
	{
		httpGet.setConfig(requestConfig);
		//httpStart();
		httpclient.execute(httpGet, callback);
	}

	public void sendHttpsGet(HttpGet httpGet, FutureCallback<HttpResponse> callback)
	{
		httpGet.setConfig(requestConfig);
		//httpsStart();
		httpsclient.execute(httpGet, callback);
	}

	public void sendHttpsPost(HttpPost httpPost, FutureCallback<HttpResponse> callback)
	{
		httpPost.setConfig(requestConfig);
		//httpsStart();
		httpsclient.execute(httpPost, callback);
	}

	public static void main(String[] argv) throws Exception
	{
		// 利用计数器阻塞主线程，在全部回调结束后再close
		final CountDownLatch countDownLatch = new CountDownLatch(4);

		// 测试回调
		FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse>() {
			public void completed(final HttpResponse response)
			{
				// 计数器减
				countDownLatch.countDown();
				System.out.println(" callback thread id is : " + Thread.currentThread().getId());
				try {
//					String content = EntityUtils.toString(response.getEntity(), "UTF-8");
					System.out.println(" response content is : " + response.getStatusLine());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void failed(final Exception ex)
			{
				// 计数器减
				countDownLatch.countDown();
				System.out.println(" callback thread id is : " + Thread.currentThread().getId());
			}

			public void cancelled()
			{
				// 计数器减
				countDownLatch.countDown();
				System.out.println(" callback thread id is : " + Thread.currentThread().getId());
			}
		};

		final HttpGet httpGet = new HttpGet("http://cn.bing.com/");
		final HttpGet httpsGet = new HttpGet("https://www.baidu.com/");
		final HttpPost httpPost = new HttpPost("http://cn.bing.com/");
		final HttpPost httpsPost = new HttpPost("https://www.baidu.com/");

		HttpAsyncUtil util = HttpAsyncUtil.getInstance();

		util.sendHttpGet(httpGet, callback);
		util.sendHttpPost(httpPost, callback);

		util.sendHttpsGet(httpsGet, callback);
		util.sendHttpsPost(httpsPost, callback);

		// 阻塞主线程
		countDownLatch.await();
		util.close();
	}

}

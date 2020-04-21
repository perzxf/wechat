package com.zcf.utils;

import com.google.gson.Gson;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 可以发送Http请求的工具类（可以发送Https请求）
 */
public class HttpClient {

	// 请求的地址
	private String url;
	// 请求的参数
	private Map<String, String> param;
	private int statusCode;
	// 请求后，获取到的响应内容
	private String content;
	private String xmlParam;
	private boolean isHttps;

	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public String getXmlParam() {
		return xmlParam;
	}

	public void setXmlParam(String xmlParam) {
		this.xmlParam = xmlParam;
	}

	public HttpClient(String url, Map<String, String> param) {
		this.url = url;
		this.param = param;
	}

	public HttpClient(String url) {
		this.url = url;
	}

	public void setParameter(Map<String, String> map) {
		param = map;
	}

	public void addParameter(String key, String value) {
		if (param == null)
			param = new HashMap<String, String>();
		param.put(key, value);
	}

	/**
	 * 发送post请求
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void post() throws ClientProtocolException, IOException {
		HttpPost http = new HttpPost(url);
		setEntity(http);
		execute(http);
	}

	/**
	 * 发送put请求
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void put() throws ClientProtocolException, IOException {
		HttpPut http = new HttpPut(url);
		setEntity(http);
		execute(http);
	}

	/**
	 * 发送get请求
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void get() throws ClientProtocolException, IOException {
		if (param != null) {
			StringBuilder url = new StringBuilder(this.url);
			boolean isFirst = true;
			for (String key : param.keySet()) {
				if (isFirst)
					url.append("?");
				else
					url.append("&");
				url.append(key).append("=").append(param.get(key));
			}
			this.url = url.toString();
		}
		HttpGet http = new HttpGet(url);
		execute(http);
	}

	/**
	 * set http post,put param
	 */
	private void setEntity(HttpEntityEnclosingRequestBase http) {
		if (param != null) {
			List<NameValuePair> nvps = new LinkedList<NameValuePair>();
			for (String key : param.keySet())
				nvps.add(new BasicNameValuePair(key, param.get(key))); // 参数
			http.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8)); // 设置参数
		}
		if (xmlParam != null) {
			http.setEntity(new StringEntity(xmlParam, Consts.UTF_8));
		}
	}

	/**
	 * 真正发送请求的方法
	 * @param http
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void execute(HttpUriRequest http) throws ClientProtocolException,
			IOException {
		CloseableHttpClient httpClient = null;
		try {
			if (isHttps) {
				SSLContext sslContext = new SSLContextBuilder()
						.loadTrustMaterial(null, new TrustStrategy() {
							// 信任所有
							public boolean isTrusted(X509Certificate[] chain,
									String authType)
									throws CertificateException {
								return true;
							}
						}).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						sslContext);
				httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
						.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			CloseableHttpResponse response = httpClient.execute(http);
			try {
				if (response != null) {
					if (response.getStatusLine() != null)
						statusCode = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();
					// 响应内容
					content = EntityUtils.toString(entity, Consts.UTF_8);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.close();
		}
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getContent() throws ParseException, IOException {
		return content;
	}

	/**
	 * httpClient-Post请求
	 * @param url 请求地址
	 * @param params post参数
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> httpClientPost(String url, String params) throws Exception {
		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		client.getParams().setContentCharset("UTF-8");
		PostMethod httpPost = new PostMethod(url);
		try {
			RequestEntity requestEntity = new ByteArrayRequestEntity(params.getBytes("utf-8"));
			httpPost.setRequestEntity(requestEntity);
			client.executeMethod(httpPost);
			String response = httpPost.getResponseBodyAsString();
			Map<String, Object> map = new Gson().fromJson(response, Map.class);
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			httpPost.releaseConnection();
		}
	}

	/**
	 * httpClient-Get请求
	 * @param url 请求地址
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> httpClientGet(String url) throws Exception {
		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		client.getParams().setContentCharset("UTF-8");
		GetMethod httpGet = new GetMethod(url);
		try {
			client.executeMethod(httpGet);
			String response = httpGet.getResponseBodyAsString();
			Map<String, Object> map = new Gson().fromJson(response, Map.class);
			return map;
		} catch (Exception e) {
			throw e;
		} finally {
			httpGet.releaseConnection();
		}
	}
}

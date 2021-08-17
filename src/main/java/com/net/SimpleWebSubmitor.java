package com.net;

//import java.net.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

//带会话状态的、可多次使用的快速网络请求提交器
public class SimpleWebSubmitor {
	public static final String POST = "post";
	public static final String GET = "get";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";
	private static final String ACCEPT_LANGUAGE = "zh-cn,zh;en-us";
	//	private static final String ACCEPT_ENCODING = "gzip,deflate";
	private static final long DEFAULT_CONNECT_TIMEOUT = 6000;

	private String urlString = "http://127.0.0.1/";
	private String charsetName = "UTF-8";
	private Map<String, String> formData = new HashMap<String, String>();
	private Set<String> resultCode = new HashSet<String>();
	private String submitMethod = POST;
	private String responseContent;
	private HttpClient httpClient;
	HttpContext localContext;

//	private long timeout;

	// 设置提交器的链接超时时间
	// public void setConnectionTime(int timeout)
	// {
	// httpClient.getParams().setIntParameter( HttpConnectionParams.SO_TIMEOUT,
	// timeout); // 超时设置
	// httpClient.getParams().setIntParameter(
	// HttpConnectionParams.CONNECTION_TIMEOUT, timeout);// 连接超时
	// }

	// 客户端长连接策略
	private static ConnectionKeepAliveStrategy keepAliveStrategy=new
			ConnectionKeepAliveStrategy(){

				@Override
				public long getKeepAliveDuration(HttpResponse response, HttpContext
						context)
				{
					return 10;
				}};

	// 自动重定向策略对象
//	 private static DefaultRedirectStrategy redirectStrategy=new
//	 DefaultRedirectStrategy(){
//	 public boolean isRedirected(HttpRequest request, HttpResponse response,
//	 HttpContext context)
//	 {
//	 boolean isRedirect = false;
//	 try
//	 {
//	 isRedirect = super.isRedirected(request, response, context);
//	 } catch (Exception e)
//	 {
//	 e.printStackTrace();
//	 }
//	 if (!isRedirect)
//	 {
//	 int responseCode = response.getStatusLine().getStatusCode();
//	 if (responseCode == 301 || responseCode == 302)
//	 {
//	 return true;
//	 }
//	 }
//	 return isRedirect;
//	 }};

	public SimpleWebSubmitor(String urlString) {
		this.urlString = urlString;
		try {
			httpClient = new DefaultHttpClient();
			System.out.println("建立了一个快速提交器。");
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		// force strict cookie policy per default
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BEST_MATCH);
		setConnectionTimeout(DEFAULT_CONNECT_TIMEOUT);
		// Create a local instance of cookie store
		CookieStore cookieStore = new BasicCookieStore();
		// Create local HTTP context
		localContext = new BasicHttpContext();
		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		cookieStore = (CookieStore) localContext
				.getAttribute(ClientContext.COOKIE_STORE);
		System.out.println("新建的提交器中Cookie内容为：" + cookieStore.getCookies());

		// 另一种处理Post或Get重定向问题的方式：设置重定向策略
		AbstractHttpClient client = (DefaultHttpClient) httpClient;
//		client.setRedirectStrategy(redirectStrategy);
		client.setKeepAliveStrategy(keepAliveStrategy);
	}

	// 设置连接超时时间
	public void setConnectionTimeout(long timeout) {
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, (int) timeout);
	}

	// 设置读取响应超时时间
	public void setSoTimeout(long timeout) {
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				(int) timeout);
	}

	public SimpleWebSubmitor clone() {
		SimpleWebSubmitor clonedSubmitor = new SimpleWebSubmitor(urlString);
		clonedSubmitor.charsetName = charsetName;
		clonedSubmitor.submitMethod = submitMethod;
		clonedSubmitor.urlString = urlString;
		Set<String> keys = formData.keySet();
		for (String key : keys) {
			String value = formData.get(key);
			clonedSubmitor.formData.put(key, value);
		}
		for (String each : resultCode)
			clonedSubmitor.resultCode.add(each);
		clonedSubmitor.localContext = localContext;
		return clonedSubmitor;
	}

	// 设置新的访问url
	public void setUrl(String urlString) {
		this.urlString = urlString;
	}

	// 清除全部结果校验码
	public void clearAllResultCode() {
		resultCode.clear();
	}

	// 清除全部表单数据
	public void clearAllFormData() {
		formData.clear();
	}

	public void addFormData(String name, String value) {
		formData.put(name, value);
	}

	public void addResultCode(String code) {
		resultCode.add(code);

	}

	public void addResultCode(Set<String> codeSet) {
		for (String each : codeSet)
			resultCode.add(each);
	}

	public void setCharset(String charsetName) {
		this.charsetName = charsetName;
	}

	public void setSubmitMethod(String submitMethod) {
		this.submitMethod = submitMethod;
	}

	public String submitForText() {
		String resultPage = "";
		try {
			HttpEntity entity = getHttpResponseEntity();
			resultPage = EntityUtils.toString(entity, charsetName);
			System.out.println(resultPage);
			checkResultPage(resultPage, resultCode);
			return resultPage;
		} catch (Exception e) {
			throw new RuntimeException("提交表单时出错：" + e.getMessage()
					+ "//获得的响应信息：//" + resultPage);
		}
	}

	public byte[] submitForData() {
		try {
			HttpEntity entity = getHttpResponseEntity();
			byte[] data = EntityUtils.toByteArray(entity);
			if (data != null)
				System.out.println("数据长度：" + data.length);
			if (data == null)
				throw new RuntimeException("数据获取为null！");
			responseContent="获取图像成功，图像大小为："+data.length+"字节。";
			return data;
		} catch (Exception e) {
			responseContent="获取图像时出错：" + e.getMessage();
			throw new RuntimeException(responseContent);
		}
	}

	public String getResponseContent() {
		return responseContent;
	}

	// 设置http请求头常规参数
	private void setNormalParamsForHead(HttpPost httpPost) {
		httpPost.addHeader("user-agent", USER_AGENT);
		// httpPost.addHeader("accept", ACCEPT);
		httpPost.addHeader("accept-language", ACCEPT_LANGUAGE);
		// httpPost.addHeader("accept-encoding", ACCEPT_ENCODING);
	}

	// 设置http请求头常规参数
	private void setNormalParamsForHead(HttpGet httpGet) {
		httpGet.addHeader("user-agent", USER_AGENT);
		// httpGet.addHeader("accept", ACCEPT);
		httpGet.addHeader("accept-language", ACCEPT_LANGUAGE);
		// httpGet.addHeader("accept-encoding", ACCEPT_ENCODING);
	}

//	private void printHeaders(Header[] headers) {
//		System.out.println("------request headers-------");
//		for (Header each : headers) {
//			System.out.println(each.getName() + ":" + each.getValue());
//		}
//		System.out.println("----------------------------");
//	}

	// 获得响应实体
	private HttpEntity getHttpResponseEntity() throws Exception {
		HttpResponse response;
		if (submitMethod.equals(POST)) {
			HttpPost httpPost = buildHttpPost();
			setNormalParamsForHead(httpPost);
			response = httpClient.execute(httpPost, localContext);
			// printHeaders(httpPost.getAllHeaders());

			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println("status Code:" + statusCode);
		} else {
			HttpGet httpGet = buildHttpGet();
			setNormalParamsForHead(httpGet);
			response = httpClient.execute(httpGet, localContext);
			// printHeaders(httpGet.getAllHeaders());
			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println("status Code:" + statusCode);
		}
		CookieStore cookieStore = (CookieStore) localContext
				.getAttribute(ClientContext.COOKIE_STORE);
		System.out.println("Cookies:" + cookieStore.getCookies());
		HttpEntity entity = response.getEntity();
		return entity;
	}

	// 构建HttpPost对象
	private HttpPost buildHttpPost() throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		Set<String> keys = formData.keySet();
		for (String key : keys) {
			String value = formData.get(key);
			formparams.add(new BasicNameValuePair(key, value));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
				charsetName);
		HttpPost httppost = new HttpPost(urlString);
		httppost.setEntity(entity);
		return httppost;
	}

	// 构建HttpGet对象
	private HttpGet buildHttpGet() {
		HttpGet httpGet = new HttpGet(urlString);
		return httpGet;
	}

	private void checkResultPage(String resultPage, Set<String> resultCode) {
		for (String code : resultCode) {
			if (resultPage.indexOf(code) == -1)
				throw new RuntimeException("结果页面不符合此特征码：" + code);
		}

	}

	// 清除Http连接的状态信息
	public void cleanStatus() {
		localContext.removeAttribute(ClientContext.COOKIE_STORE);
		// CookieStore cookieStore = new BasicCookieStore();
		// localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		System.out.println("清除了一个提交器的状态！");
		// cookieStore=(CookieStore)localContext.getAttribute(ClientContext.COOKIE_STORE);
		// System.out.println("清除后的提交器中Cookie内容为："+cookieStore.getCookies());
	}

	// 关闭提交器的连接
	public void closeConnection() {
		localContext = null;
		httpClient.getConnectionManager().shutdown();
		System.out.println("关闭了一个提交器的连接！");
	}

}

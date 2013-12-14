package com.brsanthu.googleanalytics.http;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;
import com.brsanthu.googleanalytics.GoogleAnalyticsParameter;
import com.brsanthu.googleanalytics.GoogleAnalyticsRequest;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
import com.brsanthu.googleanalytics.HttpStack;
import com.brsanthu.googleanalytics.RequestProvider;

public class ApacheHttpClient extends HttpStack {

	private CloseableHttpClient httpClient;
	private ThreadPoolExecutor executor;

	@Override
	public void setConfig(GoogleAnalyticsConfig config) {
		super.setConfig(config);
		httpClient = createHttpClient(config);
	}
	
	private CloseableHttpClient createHttpClient(GoogleAnalyticsConfig config) {
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(getDefaultMaxPerRoute(config));

		HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager);

		if (GoogleAnalytics.isNotEmpty(config.getProxyHost())) {
			builder.setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()));

			if (GoogleAnalytics.isNotEmpty(config.getProxyUserName())) {
				BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(config.getProxyHost(), config.getProxyPort()), new UsernamePasswordCredentials(config.getProxyUserName(), config.getProxyPassword()));
				builder.setDefaultCredentialsProvider(credentialsProvider);
			}
		}

		return builder.build();
	}

	@Override
	public GoogleAnalyticsResponse post(GoogleAnalyticsRequest request) {

		if (httpClient == null) {
			throw new RuntimeException("Config is not set, call 'setConfig' first");
		}

		Map<GoogleAnalyticsParameter, String> params = request.getParameters();
		
		HttpPost httpPost = new HttpPost(config.getUrl());
		List<NameValuePair> postParms = new ArrayList<NameValuePair>();
		for (GoogleAnalyticsParameter key : params.keySet()) {
			postParms.add(new BasicNameValuePair(key.getParameterName(), params.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(postParms, GoogleAnalytics.UTF8));

		httpPost.setHeader("User-Agent", request.getUserAgent());

		GoogleAnalyticsResponse result = new GoogleAnalyticsResponse();

		try {
			CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpPost);
			result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
			EntityUtils.consumeQuietly(httpResponse.getEntity());
		} catch (IOException e) {
			result.setStatusCode(500);
		}

		return result;
	}

	@Override
	public void close() {
		if (httpClient == null) {
			throw new RuntimeException("Config is not set, call 'setConfig' first");
		}
		try {
			httpClient.close();
		} catch (IOException e) {
		}
	}

	@Override
	public Future<GoogleAnalyticsResponse> postAsync(final RequestProvider requestProvider) {
		if (!config.isEnabled()) {
			return null;
		}

		Future<GoogleAnalyticsResponse> future = getExecutor().submit(new Callable<GoogleAnalyticsResponse>() {
			public GoogleAnalyticsResponse call() throws Exception {
				try {
					@SuppressWarnings("rawtypes")
					GoogleAnalyticsRequest request = requestProvider.getRequest();
					if (request != null) {
						return post(request);
					}
				} catch (Exception e) {
					logger.warn("Request Provider (" + requestProvider + ") thrown exception " + e.toString() + " and hence nothing is posted to GA.");
				}

				return null;
			}
		});
		return future;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	public Future<GoogleAnalyticsResponse> postAsync(final GoogleAnalyticsRequest request) {
		if (!config.isEnabled()) {
			return null;
		}

		Future<GoogleAnalyticsResponse> future = getExecutor().submit(new Callable<GoogleAnalyticsResponse>() {
			public GoogleAnalyticsResponse call() throws Exception {
				return post(request);
			}
		});
		return future;
	}

	protected ThreadPoolExecutor getExecutor() {
		if (executor == null) {
			executor = createExecutor(config);
		}
		return executor;
	}

	protected synchronized ThreadPoolExecutor createExecutor(GoogleAnalyticsConfig config) {
		return new ThreadPoolExecutor(0, config.getMaxThreads(), 5, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(), createThreadFactory());
	}

	protected ThreadFactory createThreadFactory() {
		return new LocalThreadFactory(config.getThreadNameFormat());
	}

}

class LocalThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private String threadNameFormat = null;

    public LocalThreadFactory(String threadNameFormat) {
    	this.threadNameFormat = threadNameFormat;
	}

	public Thread newThread(Runnable r) {
        Thread thread = new Thread(Thread.currentThread().getThreadGroup(), r, MessageFormat.format(threadNameFormat, threadNumber.getAndIncrement()), 0);
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}

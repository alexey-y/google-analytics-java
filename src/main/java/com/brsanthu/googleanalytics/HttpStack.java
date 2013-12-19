package com.brsanthu.googleanalytics;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;


public abstract class HttpStack {
	
	protected GoogleAnalyticsConfig config;
	protected Logger logger;

	public void setConfig(GoogleAnalyticsConfig config)
	{
		this.config = config;
	}
	
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public abstract GoogleAnalyticsResponse post(GoogleAnalyticsRequest request);
	
	public abstract Future<GoogleAnalyticsResponse> postAsync(RequestProvider requestProvider);
	
	public abstract void close();
	
	protected int getDefaultMaxPerRoute(GoogleAnalyticsConfig config) {
		return Math.max(config.getMaxThreads(), 1);
	}
}

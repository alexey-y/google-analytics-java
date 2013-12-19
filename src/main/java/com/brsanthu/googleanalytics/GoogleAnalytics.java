/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brsanthu.googleanalytics;

import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import com.brsanthu.googleanalytics.requests.DefaultRequest;

/**
 * This is the main class of this library that accepts the requests from clients and
 * sends the events to Google Analytics (GA).
 *
 * Clients needs to instantiate this object with {@link GoogleAnalyticsConfig} and {@link DefaultRequest}.
 * Configuration contains sensible defaults so one could just initialize using one of the convenience constructors.
 *
 * This object is ThreadSafe and it is intended that clients create one instance of this for each GA Tracker Id
 * and reuse each time an event needs to be posted.
 *
 * This object contains resources which needs to be shutdown/disposed. So {@link #close()} method is called
 * to release all resources. Once close method is called, this instance cannot be reused so create new instance
 * if required.
 */
public class GoogleAnalytics {

	public static final Charset UTF8 = Charset.forName("UTF-8");

	private GoogleAnalyticsConfig config;
	private DefaultRequest defaultRequest;
	
	private HttpStack httpClient;
	
	private GoogleAnalyticsStats stats = new GoogleAnalyticsStats();
	
	private final String trackingId;

	private Logger logger = Logger.getLogger(this.getClass());

	public GoogleAnalytics(String trackingId, GoogleAnalyticsConfig config) {
		this.trackingId = trackingId;
		this.config = config;
	}

	public GoogleAnalyticsConfig getConfig() {
		return config;
	}
	
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public void setHttpClient(HttpStack httpClient) {
		this.httpClient = httpClient;
		httpClient.setConfig(config);
		if (logger!=null)
		{
			httpClient.setLogger(logger);
		}
	}

	public DefaultRequest getDefaultRequest() {
		return defaultRequest;
	}

	public void setDefaultRequest(DefaultRequest request) {
		this.defaultRequest = request;
	}

	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GoogleAnalyticsResponse post(GoogleAnalyticsRequest request) {
		
		if (httpClient==null)
		{
			throw new RuntimeException("Config is not set, call 'setHttpClient' first");
		}
		
		request.trackingId(trackingId);
		
		if (!config.isEnabled()) {
			return new GoogleAnalyticsResponse();
		}

		GoogleAnalyticsResponse response = null;
		try {
			//Combine request with default parms.
			
			if (defaultRequest!=null)
			{
				request.updateWithDefault(defaultRequest);
			}

			logger.info("Sending " + request);

			response = doHttpPost(request);

			if (config.isGatherStats()) {
				gatherStats(request);
			}

		} catch (Exception e) {
			if (e instanceof UnknownHostException) {
				logger.error("Coudln't connect to Google Analytics. Internet may not be available. " + e.toString());
			} else {
				logger.error("Exception while sending the Google Analytics tracker request " + request, e);
			}
		} 

		return response;
	}

	private GoogleAnalyticsResponse doHttpPost(GoogleAnalyticsRequest request) {
		return httpClient.post(request);
	}

	private void gatherStats(@SuppressWarnings("rawtypes") GoogleAnalyticsRequest request) {
		String hitType = request.hitType();

		if ("pageView".equalsIgnoreCase(hitType)) {
			stats.pageViewHit();

		} else if ("appView".equalsIgnoreCase(hitType)) {
			stats.appViewHit();

		} else if ("event".equalsIgnoreCase(hitType)) {
			stats.eventHit();

		} else if ("item".equalsIgnoreCase(hitType)) {
			stats.itemHit();

		} else if ("transaction".equalsIgnoreCase(hitType)) {
			stats.transactionHit();

		} else if ("social".equalsIgnoreCase(hitType)) {
			stats.socialHit();

		} else if ("timing".equalsIgnoreCase(hitType)) {
			stats.timingHit();
		}
	}

	public static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}

	public static boolean isEmpty(String value) {
    	return value == null || value.trim().length() == 0;
    }

	public void close() {
		httpClient.close();
	}

//	protected DefaultRequest deriveSystemParameters(GoogleAnalyticsConfig config, DefaultRequest request) {
//		try {
//			if (isEmpty(config.getUserAgent())) {
//				config.setUserAgent(getUserAgentString());
//			}
//
//			if (isEmpty(request.userLanguage())) {
//			    String region = System.getProperty("user.region");
//			    if (isEmpty(region)) {
//			        region = System.getProperty("user.country");
//			    }
//			    request.userLanguage(System.getProperty("user.language") + "-" + region);
//			}
//
//			if (isEmpty(request.documentEncoding())) {
//				request.documentEncoding(System.getProperty("file.encoding"));
//			}
//
//			Toolkit toolkit = Toolkit.getDefaultToolkit();
//
//			if (isEmpty(request.screenResolution())) {
//				Dimension screenSize = toolkit.getScreenSize();
//				request.screenResolution(((int) screenSize.getWidth()) + "x" + ((int) screenSize.getHeight()) + ", " + toolkit.getScreenResolution() + " dpi");
//			}
//
//			if (isEmpty(request.screenColors())) {
//				GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
//				GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
//
//				StringBuilder sb = new StringBuilder();
//				for (GraphicsDevice graphicsDevice : graphicsDevices) {
//					if (sb.length() != 0) {
//						sb.append(", ");
//					}
//					sb.append(graphicsDevice.getDisplayMode().getBitDepth());
//				}
//				request.screenColors(sb.toString());
//			}
//		} catch (Exception e) {
//			logger.warn("Exception while deriving the System properties for request " + request, e);
//		}
//
//		return request;
//	}


	public GoogleAnalyticsStats getStats() {
		return stats;
	}

	public void resetStats() {
		stats = new GoogleAnalyticsStats();
	}
}


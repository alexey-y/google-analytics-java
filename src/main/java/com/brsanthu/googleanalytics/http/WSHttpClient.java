package com.brsanthu.googleanalytics.http;

import java.util.Map;
import java.util.concurrent.Future;

import org.jboss.netty.handler.codec.http.QueryStringEncoder;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import com.brsanthu.googleanalytics.GoogleAnalyticsParameter;
import com.brsanthu.googleanalytics.GoogleAnalyticsRequest;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
import com.brsanthu.googleanalytics.HttpStack;
import com.brsanthu.googleanalytics.RequestProvider;

public class WSHttpClient extends HttpStack {

	@Override
	public GoogleAnalyticsResponse post(GoogleAnalyticsRequest gaRequest) {
		
		Map<GoogleAnalyticsParameter, String> params = gaRequest.getParameters();
		
		WSRequest request = WS.url(config.getUrl());
		request.setHeader("User-Agent", gaRequest.getUserAgent());
		request.mimeType("application/x-www-form-urlencoded");
		
		QueryStringEncoder encoder = new QueryStringEncoder("/");
		for (GoogleAnalyticsParameter key : params.keySet()) {
			encoder.addParam(key.getParameterName(), params.get(key));
		}
		String body = encoder.toString().substring(2);
		request.body(body);
		
		GoogleAnalyticsResponse result = new GoogleAnalyticsResponse();

		try {
			
			HttpResponse httpResponse = request.post();
			result.setStatusCode(httpResponse.getStatus());
		} catch (Throwable e) {
			result.setStatusCode(500);
		}
		
		return result;
	}

	@Override
	public Future<GoogleAnalyticsResponse> postAsync(RequestProvider requestProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
	}

}

//package com.brsanthu.googleanalytics.http;
//
//import java.util.Map;
//import java.util.concurrent.Future;
//
//import play.libs.WS;
//import play.libs.WS.WSRequest;
//
//import com.brsanthu.googleanalytics.GoogleAnalyticsParameter;
//import com.brsanthu.googleanalytics.GoogleAnalyticsRequest;
//import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
//import com.brsanthu.googleanalytics.HttpStack;
//import com.brsanthu.googleanalytics.RequestProvider;
//
//public class WSHttpClient extends HttpStack {
//
//	@Override
//	public GoogleAnalyticsResponse post(GoogleAnalyticsRequest gaRequest) {
//		
//		Map<GoogleAnalyticsParameter, String> params = gaRequest.getParameters();
//		
//		WSRequest request = WS.url(config.getUrl());
//		request.setHeader("User-Agent", gaRequest.getUserAgent());
//		
//		for (GoogleAnalyticsParameter key : params.keySet()) {
//			request.setParameter(key.getParameterName(), params.get(key));
//		}
//		request.body(new UrlEncodedFormEntity(postParms, GoogleAnalytics.UTF8));
//		
//		GoogleAnalyticsResponse result = new GoogleAnalyticsResponse();
//
//		try {
//			CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpPost);
//			result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
//			EntityUtils.consumeQuietly(httpResponse.getEntity());
//		} catch (IOException e) {
//			result.setStatusCode(500);
//		}
//	}
//
//	@Override
//	public Future<GoogleAnalyticsResponse> postAsync(RequestProvider requestProvider) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void close() {
//		// TODO Auto-generated method stub
//		
//	}
//
//}

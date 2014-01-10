package com.brsanthu.googleanalytics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brsanthu.googleanalytics.requests.AppViewHit;
import com.brsanthu.googleanalytics.requests.EventHit;
import com.brsanthu.googleanalytics.requests.ExceptionHit;
import com.brsanthu.googleanalytics.requests.ItemHit;
import com.brsanthu.googleanalytics.requests.PageViewHit;
import com.brsanthu.googleanalytics.requests.SocialHit;
import com.brsanthu.googleanalytics.requests.TimingHit;
import com.brsanthu.googleanalytics.requests.TransactionHit;

public class HitTypesTest {

	@Test
	public void testHitTypes() throws Exception {
		assertEquals("item", new ItemHit().hitType());
		assertEquals("appview", new AppViewHit().hitType());
		assertEquals("event", new EventHit().hitType());
		assertEquals("exception", new ExceptionHit().hitType());
		assertEquals("pageview", new PageViewHit().hitType());
		assertEquals("social", new SocialHit().hitType());
		assertEquals("timing", new TimingHit().hitType());
		assertEquals("transaction", new TransactionHit().hitType());
	}
}

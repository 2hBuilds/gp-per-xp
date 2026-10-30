package com.gpperxp;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TradeFeed}: the one fixed request, the parse of the wiki's latest prices, the rule for when the next request is
 * due, and what a good answer does. The OkHttp client is a mock, so nothing here reaches the network.
 */
public class TradeFeedTest
{
	@Test
	public void requestIsFixed()
	{
		final Request request = TradeFeed.request();
		assertEquals("GET", request.method());
		assertEquals(TradeFeed.URL, request.url().toString());
		assertNull(request.url().query());
		assertEquals(TradeFeed.USER_AGENT, request.header("User-Agent"));
		assertNull(request.body());
	}

	@Test
	public void parseKeepsOnlyWantedIds()
	{
		final String json = "{\"data\":{"
			+ "\"10\":{\"high\":100,\"highTime\":1,\"low\":90,\"lowTime\":2},"
			+ "\"11\":{\"high\":50,\"highTime\":3,\"low\":40,\"lowTime\":4},"
			+ "\"12\":{\"high\":7,\"highTime\":5,\"low\":6,\"lowTime\":6}}}";
		final Map<Integer, Print> prints = TradeFeed.parse(new Gson(), new StringReader(json),
			new HashSet<>(Arrays.asList(10, 12)));
		assertEquals(new HashSet<>(Arrays.asList(10, 12)), prints.keySet());
		assertEquals(Long.valueOf(100L), prints.get(10).high);
		assertEquals(Long.valueOf(6L), prints.get(12).low);
	}

	@Test
	public void parseKeepsNulls()
	{
		final String json = "{\"data\":{\"10\":{\"high\":null,\"highTime\":8,\"low\":4,\"lowTime\":9}}}";
		final Print print = TradeFeed.parse(new Gson(), new StringReader(json), Collections.singleton(10)).get(10);
		assertNull(print.high);
		assertEquals(Long.valueOf(8L), print.highTime);
		assertEquals(Long.valueOf(4L), print.low);
		assertEquals(Long.valueOf(9L), print.lowTime);
	}

	@Test
	public void parseEmptyAnswers()
	{
		assertTrue(TradeFeed.parse(new Gson(), new StringReader("{}"), Collections.singleton(10)).isEmpty());
		assertTrue(TradeFeed.parse(new Gson(), new StringReader("{\"data\":{}}"), Collections.singleton(10)).isEmpty());
	}

	@Test(expected = JsonParseException.class)
	public void badJsonThrows()
	{
		TradeFeed.parse(new Gson(), new StringReader("not json{"), Collections.singleton(10));
	}

	@Test
	public void dueRules()
	{
		final OkHttpClient http = Mockito.mock(OkHttpClient.class);
		final Call call = Mockito.mock(Call.class);
		when(http.newCall(any(Request.class))).thenReturn(call);
		final TradeFeed feed = new TradeFeed(http, new Gson());
		final List<Map<Integer, Print>> loaded = new ArrayList<>();
		assertTrue(feed.due(1000L));
		feed.fetch(1000L, Collections.singleton(10), loaded::add);
		assertFalse(feed.due(1000L));
		final ArgumentCaptor<Callback> callback = ArgumentCaptor.forClass(Callback.class);
		verify(call).enqueue(callback.capture());
		callback.getValue().onFailure(call, new IOException("offline"));
		assertFalse(feed.due(1000L + 299_999L));
		assertTrue(feed.due(1000L + 300_000L));
		assertTrue(loaded.isEmpty());
	}

	@Test
	public void successCallsBack() throws IOException
	{
		final OkHttpClient http = Mockito.mock(OkHttpClient.class);
		final Call call = Mockito.mock(Call.class);
		when(http.newCall(any(Request.class))).thenReturn(call);
		final TradeFeed feed = new TradeFeed(http, new Gson());
		final List<Map<Integer, Print>> loaded = new ArrayList<>();
		feed.fetch(1000L, Collections.singleton(10), loaded::add);
		final ArgumentCaptor<Callback> callback = ArgumentCaptor.forClass(Callback.class);
		verify(call).enqueue(callback.capture());
		final Response response = new Response.Builder()
			.request(TradeFeed.request())
			.protocol(Protocol.HTTP_1_1)
			.code(200)
			.message("OK")
			.body(ResponseBody.create(MediaType.parse("application/json"),
				"{\"data\":{\"10\":{\"high\":5,\"highTime\":9,\"low\":4,\"lowTime\":9}}}"))
			.build();
		callback.getValue().onResponse(call, response);
		assertEquals(1, loaded.size());
		assertEquals(Long.valueOf(5L), loaded.get(0).get(10).high);
		assertEquals(Long.valueOf(4L), loaded.get(0).get(10).low);
	}
}

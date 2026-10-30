package com.gpperxp;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads the wiki's public latest-price list, one request at a time and at most once every five minutes. */
final class TradeFeed
{
	private static final Logger log = LoggerFactory.getLogger(TradeFeed.class);

	static final String URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
	static final String USER_AGENT = "2h-xp-and-gp (RuneLite plugin; github.com/2hBuilds)";
	private static final long MIN_GAP_MILLIS = 300_000L;

	private final OkHttpClient http;
	private final Gson gson;
	private volatile boolean inFlight;
	private volatile boolean everStarted;
	private volatile long lastStartMillis;

	TradeFeed(OkHttpClient http, Gson gson)
	{
		this.http = http;
		this.gson = gson;
	}

	/** A plain GET of the list: no query, no body, no player data. */
	static Request request()
	{
		return new Request.Builder().url(URL).header("User-Agent", USER_AGENT).get().build();
	}

	/** Keeps only the wanted ids of {@code {"data":{"<id>":{high, highTime, low, lowTime}}}}. */
	static Map<Integer, Print> parse(Gson gson, Reader reader, Set<Integer> wanted)
	{
		final Map<Integer, Print> out = new HashMap<>();
		final JsonElement root = new JsonParser().parse(reader);
		if (!root.isJsonObject() || !root.getAsJsonObject().has("data") || !root.getAsJsonObject().get("data").isJsonObject())
		{
			return out;
		}
		final JsonObject data = root.getAsJsonObject().getAsJsonObject("data");
		for (Map.Entry<String, JsonElement> entry : data.entrySet())
		{
			final int id;
			try
			{
				id = Integer.parseInt(entry.getKey());
			}
			catch (NumberFormatException e)
			{
				continue;
			}
			if (wanted.contains(id) && entry.getValue().isJsonObject())
			{
				out.put(id, gson.fromJson(entry.getValue(), Print.class));
			}
		}
		return out;
	}

	/** True when no request is in flight and five minutes have passed since the last one started. */
	boolean due(long nowMillis)
	{
		return !inFlight && (!everStarted || nowMillis - lastStartMillis >= MIN_GAP_MILLIS);
	}

	/** Starts one request; {@code onLoaded} runs on the OkHttp thread with the wanted prints. */
	void fetch(long nowMillis, Set<Integer> wanted, Consumer<Map<Integer, Print>> onLoaded)
	{
		inFlight = true;
		everStarted = true;
		lastStartMillis = nowMillis;
		http.newCall(request()).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				inFlight = false;
				log.debug("Trade prices not read: {}", e.toString());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (ResponseBody body = response.body())
				{
					if (!response.isSuccessful() || body == null)
					{
						log.debug("Trade prices not read: HTTP {}", response.code());
						inFlight = false;
						return;
					}
					final Map<Integer, Print> prints = parse(gson, body.charStream(), wanted);
					inFlight = false;
					onLoaded.accept(prints);
				}
				catch (JsonParseException e)
				{
					inFlight = false;
					log.debug("Trade prices not read: {}", e.toString());
				}
			}
		});
	}
}

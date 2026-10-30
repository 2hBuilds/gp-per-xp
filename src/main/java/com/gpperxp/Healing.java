package com.gpperxp;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The foods and potions read from the healing file. */
public final class Healing
{
	private static final String RESOURCE = "/com/gpperxp/healing.json";

	private final String updated;
	private final List<HealingItem> all;

	/** The file's shape, for Gson. */
	private static class Data
	{
		String updated;
		List<HealingItem> items;
	}

	private Healing(String updated, List<HealingItem> all)
	{
		this.updated = updated;
		this.all = Collections.unmodifiableList(all);
	}

	/** No items at all: what the plugin shows when the file is not there. */
	public static Healing empty()
	{
		return new Healing(null, new ArrayList<>());
	}

	/** Choice: a null or nameless entry in the file is skipped. */
	public static Healing load(Gson gson, Reader reader)
	{
		final Data data = gson.fromJson(reader, Data.class);
		if (data == null)
		{
			return empty();
		}
		final List<HealingItem> items = new ArrayList<>();
		if (data.items != null)
		{
			for (HealingItem i : data.items)
			{
				if (i != null && i.name != null)
				{
					items.add(i);
				}
			}
		}
		return new Healing(data.updated, items);
	}

	/** Choice: a missing or unreadable file gives the empty list, so that the plugin starts without it. */
	public static Healing loadBundled(Gson gson)
	{
		try (InputStream in = Healing.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				return empty();
			}
			return load(gson, new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		catch (IOException | RuntimeException e)
		{
			return empty();
		}
	}

	public List<HealingItem> all()
	{
		return all;
	}

	/** Null when the file has no date. */
	public String updated()
	{
		return updated;
	}
}

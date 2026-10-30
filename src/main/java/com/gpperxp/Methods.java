package com.gpperxp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/** The training methods read from the methods file. */
public class Methods
{
	private static final String RESOURCE = "/com/gpperxp/methods.json";

	private final String updated;
	private final List<TrainingMethod> all;

	private Methods(String updated, List<TrainingMethod> all)
	{
		this.updated = updated;
		this.all = Collections.unmodifiableList(all);
	}

	/**
	 * Reads the compact file: {@code updated}, {@code base} and {@code pages} (the source URLs), {@code items} (id to
	 * name), the tables {@code skills}, {@code anchors}, {@code notes}, {@code outs} and {@code reqs}, and
	 * {@code methods} with short keys (see {@code tools/merge_data.py}). In a row, a table field holds either the value
	 * itself or its index in the table; an item is either {@code [id, qty]} or the bare id (quantity 1); a flag is
	 * true, or any number but 0.
	 * Choice: a null row in the file is skipped; a row with a {@code skill} key is read in the long shape (field
	 * names as in {@link TrainingMethod}), so a small hand-written file works too.
	 */
	public static Methods load(Gson gson, Reader reader)
	{
		final JsonObject data = gson.fromJson(reader, JsonObject.class);
		final List<TrainingMethod> rows = new ArrayList<>();
		if (data == null)
		{
			return new Methods(null, rows);
		}
		final Tables tables = new Tables(data);
		final JsonElement methods = data.get("methods");
		if (methods != null && methods.isJsonArray())
		{
			for (JsonElement e : methods.getAsJsonArray())
			{
				if (e == null || !e.isJsonObject())
				{
					continue;
				}
				final JsonObject row = e.getAsJsonObject();
				rows.add(row.has("skill") ? longRow(gson, row) : compactRow(row, tables));
			}
		}
		final JsonElement updated = data.get("updated");
		return new Methods(updated == null || updated.isJsonNull() ? null : updated.getAsString(), rows);
	}

	private static TrainingMethod longRow(Gson gson, JsonObject json)
	{
		final TrainingMethod m = gson.fromJson(json, TrainingMethod.class);
		return new TrainingMethod(m.skill, m.name, m.level, m.xpPerAction, listOrEmpty(m.inputs),
			listOrEmpty(m.outputs), m.actionsPerHour, m.source, m.note)
			.withV2(m.rateLow, m.rateHigh, m.rateKind, m.rateLevel, m.requires, m.outputsNote)
			.withRateUnit(m.rateUnit)
			.withCostsNotStated(m.costsNotStated)
			.withRecommended(m.recommended);
	}

	private static TrainingMethod compactRow(JsonObject row, Tables t)
	{
		final String anchor = str(t.pick(t.anchors, row.get("a")));
		final String source = t.page(row.get("p")) + (anchor == null ? "" : anchor);
		return new TrainingMethod(str(t.pick(t.skills, row.get("s"))), str(row.get("n")), integer(row.get("l")),
			number(row.get("x")), t.items(row.get("in")), t.items(row.get("out")), number(row.get("r")), source,
			str(t.pick(t.notes, row.get("nt"))))
			.withV2(number(row.get("lo")), number(row.get("hi")), str(row.get("k")), integer(row.get("rl")),
				t.gates(row.get("q")), str(t.pick(t.outs, row.get("on"))))
			.withRateUnit(str(row.get("u")))
			.withCostsNotStated(number(row.get("c")) != 0 || flag(row.get("c")))
			.withRecommended(number(row.get("rec")) != 0 || flag(row.get("rec")));
	}

	/** The shared tables of the compact file. */
	private static class Tables
	{
		private final String base;
		private final JsonArray pages;
		private final JsonObject names;
		private final JsonArray skills;
		private final JsonArray anchors;
		private final JsonArray notes;
		private final JsonArray outs;
		private final JsonArray reqs;

		Tables(JsonObject data)
		{
			base = str(data.get("base")) == null ? "" : str(data.get("base"));
			pages = array(data.get("pages"));
			final JsonElement items = data.get("items");
			names = items != null && items.isJsonObject() ? items.getAsJsonObject() : new JsonObject();
			skills = array(data.get("skills"));
			anchors = array(data.get("anchors"));
			notes = array(data.get("notes"));
			outs = array(data.get("outs"));
			reqs = array(data.get("reqs"));
		}

		/** The page URL: a page with "://" is whole, any other follows the base. */
		String page(JsonElement index)
		{
			final String page = str(pick(pages, index));
			if (page == null)
			{
				return "";
			}
			return page.contains("://") ? page : base + page;
		}

		/** The value of a table field: a number is an index into the table, anything else is the value. */
		JsonElement pick(JsonArray table, JsonElement value)
		{
			if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber())
			{
				final int i = value.getAsInt();
				return i >= 0 && i < table.size() ? table.get(i) : null;
			}
			return value;
		}

		List<ItemQty> items(JsonElement list)
		{
			final List<ItemQty> out = new ArrayList<>();
			for (JsonElement e : array(list))
			{
				final int id = e.isJsonArray() ? e.getAsJsonArray().get(0).getAsInt() : e.getAsInt();
				final double qty = e.isJsonArray() ? e.getAsJsonArray().get(1).getAsDouble() : 1;
				final JsonElement name = names.get(Integer.toString(id));
				out.add(new ItemQty(id, str(name), qty));
			}
			return Collections.unmodifiableList(out);
		}

		List<Gate> gates(JsonElement value)
		{
			final List<Gate> out = new ArrayList<>();
			for (JsonElement e : array(pick(reqs, value)))
			{
				final JsonObject g = e.getAsJsonObject();
				out.add(new Gate(str(g.get("s")), integer(g.get("l")), str(g.get("qu")), str(g.get("d")),
					str(g.get("t"))));
			}
			return out;
		}
	}

	private static JsonArray array(JsonElement e)
	{
		return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
	}

	private static String str(JsonElement e)
	{
		return e == null || e.isJsonNull() ? null : e.getAsString();
	}

	private static double number(JsonElement e)
	{
		return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsDouble() : 0;
	}

	private static int integer(JsonElement e)
	{
		return (int) number(e);
	}

	private static boolean flag(JsonElement e)
	{
		return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() && e.getAsBoolean();
	}

	public static Methods loadBundled(Gson gson)
	{
		try (InputStream in = Methods.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				throw new IllegalStateException("Missing resource " + RESOURCE);
			}
			return load(gson, new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Could not read " + RESOURCE, e);
		}
	}

	private static List<ItemQty> listOrEmpty(List<ItemQty> list)
	{
		return list == null ? Collections.<ItemQty>emptyList() : Collections.unmodifiableList(new ArrayList<>(list));
	}

	public List<TrainingMethod> all()
	{
		return all;
	}

	public List<TrainingMethod> forSkill(String skill)
	{
		final List<TrainingMethod> out = new ArrayList<>();
		for (TrainingMethod m : all)
		{
			if (m.skill != null && m.skill.equals(skill))
			{
				out.add(m);
			}
		}
		return Collections.unmodifiableList(out);
	}

	public List<String> skills()
	{
		final LinkedHashSet<String> set = new LinkedHashSet<>();
		for (TrainingMethod m : all)
		{
			set.add(m.skill);
		}
		return Collections.unmodifiableList(new ArrayList<>(set));
	}

	/** Null when the file has no date. */
	public String updated()
	{
		return updated;
	}
}

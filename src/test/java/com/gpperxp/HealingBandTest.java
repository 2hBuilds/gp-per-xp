package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Items stated per Hitpoints band show only for the player's max HP. */
public class HealingBandTest
{
	private static final String JSON = "{\"updated\":\"2026-10-30\",\"items\":["
		+ "{\"name\":\"Angler A\",\"itemId\":1,\"healsFlat\":1,\"healsPercent\":10,\"hpMin\":10,\"hpMax\":19},"
		+ "{\"name\":\"Angler B\",\"itemId\":1,\"healsFlat\":13,\"healsPercent\":10,\"hpMin\":93,\"hpMax\":99}"
		+ "]}";
	private static final String UNBANDED = "{\"updated\":\"2026-10-30\",\"items\":["
		+ "{\"name\":\"Plain\",\"itemId\":1,\"heals\":5}]}";

	private static List<HealLine> build(String json, int maxHp)
	{
		return HealModel.build(Healing.load(new Gson(), new StringReader(json)).all(), id -> 100L, maxHp,
			HealSort.CHEAPEST_GP_HP, false);
	}

	private static HealLine named(List<HealLine> lines, String name)
	{
		return lines.stream().filter(l -> l.name.equals(name)).findFirst().get();
	}

	@Test
	public void maxHp99KeepsBandB()
	{
		final List<HealLine> lines = build(JSON, 99);
		assertEquals(1, lines.size());
		assertEquals("22", named(lines, "Angler B").hp);
	}

	@Test
	public void maxHp15KeepsBandA()
	{
		final List<HealLine> lines = build(JSON, 15);
		assertEquals(1, lines.size());
		assertEquals("2", named(lines, "Angler A").hp);
	}

	@Test
	public void unbandedItemAlwaysShows()
	{
		assertEquals(1, build(UNBANDED, 15).size());
		assertEquals(1, build(UNBANDED, 99).size());
	}
}

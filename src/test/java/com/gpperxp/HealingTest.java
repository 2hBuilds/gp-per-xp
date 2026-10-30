package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntToLongFunction;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the healing chart under Hitpoints: heal arithmetic, the sort
 * orders, the hover card and the reading of the bundled file.
 */
public class HealingTest
{
	private static final String FIXTURE = "{\"updated\":\"2026-10-30\",\"items\":["
		+ "{\"name\":\"Shark\",\"itemId\":1,\"item\":\"Shark\",\"heals\":20,\"portions\":1,\"source\":\"https://oldschool.runescape.wiki/w/Shark\",\"note\":null},"
		+ "{\"name\":\"Karambwan\",\"itemId\":2,\"item\":\"Karambwan\",\"heals\":18,\"portions\":1,\"source\":\"https://oldschool.runescape.wiki/w/Karambwan\",\"note\":null},"
		+ "{\"name\":\"Brew\",\"itemId\":3,\"item\":\"Brew\",\"healsFlat\":2,\"healsPercent\":15,\"portions\":4,\"source\":\"https://oldschool.runescape.wiki/w/Saradomin_brew\",\"note\":\"Lowers Attack, Strength, Defence, Magic and Ranged\"},"
		+ "{\"name\":\"Pizza\",\"itemId\":4,\"item\":\"Pizza\",\"heals\":7,\"portions\":2,\"source\":\"https://oldschool.runescape.wiki/w/Pizza\",\"note\":null},"
		+ "{\"name\":\"Mystery\",\"itemId\":9,\"item\":\"Mystery\",\"heals\":5,\"portions\":1,\"source\":\"https://oldschool.runescape.wiki/w/Mystery\",\"note\":null}"
		+ "]}";

	private static final String TWO_ITEMS = "{\"updated\":\"2026-10-30\",\"items\":["
		+ "{\"name\":\"Shark\",\"itemId\":1,\"item\":\"Shark\",\"heals\":20,\"source\":\"https://oldschool.runescape.wiki/w/Shark\",\"note\":null},"
		+ "{\"name\":\"Range\",\"itemId\":2,\"item\":\"Range\",\"healsLow\":10,\"healsHigh\":22,\"source\":\"https://oldschool.runescape.wiki/w/Range\",\"note\":null}"
		+ "]}";

	/** Prices: 1 -> 800, 2 -> 400, 3 -> 6000, 4 -> 200; 9 is unpriced (0). */
	private static final IntToLongFunction PRICE = id ->
	{
		switch (id)
		{
			case 1:
				return 800L;
			case 2:
				return 400L;
			case 3:
				return 6000L;
			case 4:
				return 200L;
			default:
				return 0L;
		}
	};

	private static List<HealingItem> fixtures() throws Exception
	{
		return Healing.load(new Gson(), new StringReader(FIXTURE)).all();
	}

	private static HealingItem named(List<HealingItem> items, String name)
	{
		return items.stream().filter(i -> i.name.equals(name)).findFirst().get();
	}

	private static <T> List<String> names(List<T> lines, Function<T, String> name)
	{
		return lines.stream().map(name).collect(Collectors.toList());
	}

	/** T1 */
	@Test
	public void healsPerPortionAndPerItem() throws Exception
	{
		List<HealingItem> items = fixtures();
		assertEquals(20, named(items, "Shark").healsPerPortion(99));
		assertEquals(20, named(items, "Shark").healsPerItem(99));
		assertEquals(7, named(items, "Pizza").healsPerPortion(99));
		assertEquals(14, named(items, "Pizza").healsPerItem(99));
		assertEquals(16, named(items, "Brew").healsPerPortion(99));
		assertEquals(64, named(items, "Brew").healsPerItem(99));
		assertEquals(3, named(items, "Brew").healsPerPortion(10));
		assertEquals(12, named(items, "Brew").healsPerItem(10));
	}

	/** T2 */
	@Test
	public void cheapestGpPerHpOrderAndTexts() throws Exception
	{
		var lines = HealModel.build(fixtures(), PRICE, 99, HealSort.CHEAPEST_GP_HP, false);
		assertEquals(Arrays.asList("Pizza", "Karambwan", "Shark", "Brew", "Mystery"),
			names(lines, l -> l.name));

		var pizza = lines.get(0);
		assertEquals("14.3", pizza.gpHp);
		assertEquals("14", pizza.hp);
		assertEquals("200", pizza.price);

		var karambwan = lines.get(1);
		assertEquals("22.2", karambwan.gpHp);
		assertEquals("18", karambwan.hp);
		assertEquals("400", karambwan.price);

		var shark = lines.get(2);
		assertEquals("40.0", shark.gpHp);
		assertEquals("20", shark.hp);
		assertEquals("800", shark.price);

		var brew = lines.get(3);
		assertEquals("93.8", brew.gpHp);
		assertEquals("64", brew.hp);
		assertEquals("6k", brew.price);

		var mystery = lines.get(4);
		assertEquals("?", mystery.gpHp);
		assertTrue(mystery.unpriced);
	}

	/** T3 */
	@Test
	public void reversedAndOtherSortOrders() throws Exception
	{
		var reversed = HealModel.build(fixtures(), PRICE, 99, HealSort.CHEAPEST_GP_HP, true);
		assertEquals(Arrays.asList("Brew", "Shark", "Karambwan", "Pizza", "Mystery"),
			names(reversed, l -> l.name));

		var mostHp = HealModel.build(fixtures(), PRICE, 99, HealSort.MOST_HP, false);
		assertEquals(Arrays.asList("Brew", "Shark", "Karambwan", "Pizza", "Mystery"),
			names(mostHp, l -> l.name));

		var cheapestPrice = HealModel.build(fixtures(), PRICE, 99, HealSort.CHEAPEST_PRICE, false);
		assertEquals(Arrays.asList("Pizza", "Karambwan", "Shark", "Brew", "Mystery"),
			names(cheapestPrice, l -> l.name));
	}

	/** T4 */
	@Test
	public void lowMaxHpChangesBrew() throws Exception
	{
		var lines = HealModel.build(fixtures(), PRICE, 10, HealSort.CHEAPEST_GP_HP, false);
		var brew = lines.stream().filter(l -> l.name.equals("Brew")).findFirst().get();
		assertEquals("500.0", brew.gpHp);
		assertEquals("12", brew.hp);
	}

	/** T5 */
	@Test
	public void hoverCardFacts() throws Exception
	{
		List<HealingItem> items = fixtures();

		var pizza = HealCard.of(named(items, "Pizza"), PRICE, 99);
		assertEquals("Pizza", pizza.title);
		assertArrayEquals(new String[] {"Heals per portion", "7"}, pizza.facts.get(0));
		assertArrayEquals(new String[] {"Portions", "2"}, pizza.facts.get(1));
		assertArrayEquals(new String[] {"Heals per item", "14"}, pizza.facts.get(2));
		assertArrayEquals(new String[] {"Price", "200"}, pizza.facts.get(3));
		assertArrayEquals(new String[] {"GP per HP", "14.3"}, pizza.facts.get(4));

		var brew = HealCard.of(named(items, "Brew"), PRICE, 99);
		assertArrayEquals(new String[] {"Heals per portion", "16 (2 + 15% of max HP)"}, brew.facts.get(0));
		assertArrayEquals(new String[] {"Portions", "4"}, brew.facts.get(1));
		assertArrayEquals(new String[] {"Heals per item", "64"}, brew.facts.get(2));
	}

	/** T6 */
	@Test
	public void loadReadsFileAndRangeShowsLowEnd() throws Exception
	{
		var healing = Healing.load(new Gson(), new StringReader(TWO_ITEMS));
		assertEquals("2026-10-30", healing.updated());

		var shark = healing.all().get(0);
		assertEquals("Shark", shark.name);
		assertEquals(1, shark.itemId);
		assertEquals(20, shark.heals);
		assertEquals(1, shark.portions);
		assertEquals("https://oldschool.runescape.wiki/w/Shark", shark.source);

		var lines = HealModel.build(healing.all(), PRICE, 99, HealSort.CHEAPEST_GP_HP, false);
		var range = lines.stream().filter(l -> l.name.equals("Range")).findFirst().get();
		assertEquals("10-22", range.hp);
		assertEquals("40.0", range.gpHp);
	}
}

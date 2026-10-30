package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link CardModel}'s needs and youGet lines. Player xp 1154, target level 20. Fixture prices: 10 -> 100. */
public class CardModelRequiresTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		map.put(99, 0L);
		return map;
	}

	@Test
	public void needsAndYouGetWhenGiven()
	{
		final List<Gate> requires = Arrays.asList(
			new Gate("Magic", 90, null, null, null),
			new Gate(null, 0, "Dragon Slayer II", null, null),
			new Gate(null, 0, null, "Ardougne hard", null),
			new Gate(null, 0, null, null, "Smiths' Uniform"));
		final TrainingMethod m = new TrainingMethod("Test", "Gated", 5, 10.0,
			Collections.singletonList(new ItemQty(10, 1.0)), NONE, 1000, "src", null)
			.withV2(0, 0, null, 0, requires, " supply crates ");
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
		assertEquals("Magic 90, Dragon Slayer II, Ardougne hard diary, Smiths' Uniform", model.needs);
		assertEquals("supply crates", model.youGet);
	}

	@Test
	public void needsAndYouGetEmptyWhenNotGiven()
	{
		final TrainingMethod m = new TrainingMethod("Test", "Plain", 5, 10.0,
			Collections.singletonList(new ItemQty(10, 1.0)), NONE, 1000, "src", null);
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
		assertEquals("", model.needs);
		assertEquals("", model.youGet);
	}
}

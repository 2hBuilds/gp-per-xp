package com.gpperxp;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link CardPainter#paint}: the height of the card with the footer line and with the shopping sections. The fixture
 * is "Test potion" with a trip (xp 1154, target level 20, 133 actions) unless stated. Fixture prices: 10 -> 100,
 * 11 -> 10, 12 -> 500, 99 -> 0.
 */
public class CardPainterFooterTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		map.put(99, 0L);
		return map;
	}

	private static CardModel model(int currentXp)
	{
		final TrainingMethod m = new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, currentXp, 20);
	}

	@Test
	public void footerAddsHeight()
	{
		final CardModel model = model(1154);
		final BufferedImage withoutFooter = CardPainter.paint(model, id -> null);
		final BufferedImage withFooter = CardPainter.paint(model.withFooter("Wiki data of 2026-10-29"), id -> null);
		assertEquals(230, withoutFooter.getWidth());
		assertEquals(230, withFooter.getWidth());
		assertTrue(withFooter.getHeight() > withoutFooter.getHeight());
	}

	@Test
	public void shoppingAddsHeight()
	{
		final BufferedImage withShopping = CardPainter.paint(model(1154), id -> null);
		final BufferedImage withoutShopping = CardPainter.paint(model(-1), id -> null);
		assertTrue(withShopping.getHeight() > withoutShopping.getHeight());
	}
}

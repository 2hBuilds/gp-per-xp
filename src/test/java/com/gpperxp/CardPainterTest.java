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
 * {@link CardPainter#paint}: the size and the two colours of the card. The fixture is the method "Test potion" with
 * a trip (xp 1154, target level 0) unless stated; fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 99 -> 0.
 */
public class CardPainterTest
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
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, currentXp, 0);
	}

	@Test
	public void paintsACard()
	{
		final BufferedImage image = CardPainter.paint(model(1154), id -> null);
		assertEquals(230, image.getWidth());
		assertTrue(image.getHeight() >= 100);
		assertEquals(0x766852, image.getRGB(0, 0) & 0xFFFFFF);
		assertEquals(0x463D32, image.getRGB(4, 4) & 0xFFFFFF);
	}

	@Test
	public void iconDoesNotChangeTheSize()
	{
		final CardModel model = model(1154);
		final BufferedImage withoutIcon = CardPainter.paint(model, id -> null);
		final BufferedImage withIcon = CardPainter.paint(model,
			id -> new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB));
		assertEquals(withoutIcon.getWidth(), withIcon.getWidth());
		assertEquals(withoutIcon.getHeight(), withIcon.getHeight());
	}

	@Test
	public void tripAddsHeight()
	{
		final BufferedImage withTrip = CardPainter.paint(model(1154), id -> null);
		final BufferedImage withoutTrip = CardPainter.paint(model(-1), id -> null);
		assertTrue(withTrip.getHeight() > withoutTrip.getHeight());
	}
}

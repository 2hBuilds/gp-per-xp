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
 * {@link CardModel#note}: the method's note as it is, or "" when the note is null or blank. The fixture is the method
 * "Test potion" (fixture prices: 10 -> 100, 11 -> 10, 12 -> 500), built with tax off, xp 1154 and target level 20.
 * {@link CardPainter#paint} draws the note, so a card with one is taller.
 */
public class CardNoteTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		return map;
	}

	/** The fixture method, Herblore level 3 with Herb and Vial making a Potion, carrying the given note. */
	private static TrainingMethod method(String note)
	{
		return new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", note);
	}

	private static CardModel model(String note)
	{
		return CardModel.of(Costs.row(method(note), PRICE, false), PRICE, false, 1154, 20);
	}

	@Test
	public void noteIsCarried()
	{
		assertEquals("Rate: wiki 357,000 xp/h / 142.5 xp", model("Rate: wiki 357,000 xp/h / 142.5 xp").note);
	}

	@Test
	public void nullNoteIsEmpty()
	{
		assertEquals("", model(null).note);
	}

	@Test
	public void blankNoteIsEmpty()
	{
		assertEquals("", model("   ").note);
	}

	@Test
	public void painterGrowsWithNote()
	{
		final String note = "Wiki rate assumes herbs bought in bulk, "
			+ "vials reused between trips, one potion per action.";
		assertEquals(90, note.length());
		final BufferedImage plain = CardPainter.paint(model(null), id -> null);
		final BufferedImage noted = CardPainter.paint(model(note), id -> null);
		assertEquals(230, plain.getWidth());
		assertEquals(230, noted.getWidth());
		assertTrue(noted.getHeight() > plain.getHeight());
	}
}

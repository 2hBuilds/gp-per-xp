package com.gpperxp;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import net.runelite.client.ui.FontManager;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Card titles wrap onto more lines instead of being cut with "...". */
public class CardTitleWrapTest
{
	private static final String LONG = "Carnivorous chinchompa, tick manipulation, 5 traps (rate at 80)";
	/** The height of the card with a short title, measured before the change. */
	private static final int SHORT_HEIGHT = 104;
	private static final int INNER = 230 - 16;

	private static BufferedImage card(String title)
	{
		final CardModel model = CardModel.healing(title, Arrays.<String[]>asList(new String[] {"Price", "100"}), 379, "", "");
		return CardPainter.paint(model, id -> null);
	}

	private static FontMetrics bold()
	{
		final BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Font font = FontManager.getRunescapeBoldFont();
		return scratch.createGraphics().getFontMetrics(font);
	}

	@Test
	public void sixtyCharacterTitleIsTaller()
	{
		assertEquals(60, LONG.length() - 3);
		assertTrue(card(LONG).getHeight() > card("Lobster").getHeight());
	}

	@Test
	public void shortTitleKeepsToday()
	{
		assertEquals(SHORT_HEIGHT, card("Lobster").getHeight());
	}

	@Test
	public void wrappedLinesJoinToTheFullTitleAndFit()
	{
		final FontMetrics fm = bold();
		final List<String> lines = CardPainter.wrapAll(LONG, fm, INNER);
		assertTrue(lines.size() > 1);
		assertEquals(LONG, String.join(" ", lines));
		for (String line : lines)
		{
			assertFalse(line.endsWith("..."));
			assertTrue(fm.stringWidth(line) <= INNER);
		}
	}

	@Test
	public void shortTitleIsOneLine()
	{
		assertEquals(Arrays.asList("Lobster"), CardPainter.wrapAll("Lobster", bold(), INNER));
	}

	@Test
	public void aWordWiderThanTheCardIsSplitNotCut()
	{
		final String word = "Supercalifragilisticexpialidocious_Supercalifragilisticexpialidocious";
		final List<String> lines = CardPainter.wrapAll(word, bold(), INNER);
		assertEquals(word, String.join("", lines));
	}
}

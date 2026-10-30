package com.gpperxp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** {@link NavIcon}, in memory: the size, the coin exactly as {@link CoinGlyph} draws it, the picture clear of it, the picture pinned. */
public class NavIconTest
{
	/** The coin's centre and the farthest its jittered rim reaches from it, with most of a pixel of margin. */
	private static final double COIN_CENTRE = 4.5;
	private static final double CLEAR_DISTANCE = 5.5;

	/** Coin pixels fainter than this (the rim's antialiasing, alpha 3 at 8,8) may sit under the picture. */
	private static final int MIN_COIN_ALPHA = 32;

	/** {@link Arrays#hashCode(int[])} of the icon's 256 ARGB values: coin plus balance scale. */
	private static final int PICTURE_CHECKSUM = 220536880;

	private static BufferedImage coinAlone()
	{
		final BufferedImage img = new BufferedImage(NavIcon.SIZE, NavIcon.SIZE, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		try
		{
			CoinGlyph.paint(g);
		}
		finally
		{
			g.dispose();
		}
		return img;
	}

	@Test
	public void theIconIsSixteenBySixteenArgb()
	{
		final BufferedImage icon = NavIcon.create();
		assertEquals(16, icon.getWidth());
		assertEquals(16, icon.getHeight());
		assertEquals(BufferedImage.TYPE_INT_ARGB, icon.getType());
	}

	@Test
	public void theCoinIsDrawnPixelForPixelAsTheCoinGlyphDrawsIt()
	{
		final BufferedImage icon = NavIcon.create();
		final BufferedImage coin = coinAlone();
		int coinPixels = 0;
		for (int y = 0; y < NavIcon.SIZE; y++)
		{
			for (int x = 0; x < NavIcon.SIZE; x++)
			{
				if ((coin.getRGB(x, y) >>> 24) >= MIN_COIN_ALPHA)
				{
					coinPixels++;
					assertEquals("coin pixel " + x + "," + y, coin.getRGB(x, y), icon.getRGB(x, y));
				}
			}
		}
		assertTrue("the coin drew something", coinPixels > 0);
	}

	@Test
	public void thePictureLiesClearOfTheCoinsCircle()
	{
		final BufferedImage icon = NavIcon.create();
		final BufferedImage coin = coinAlone();
		int pictureOnly = 0;
		for (int y = 0; y < NavIcon.SIZE; y++)
		{
			for (int x = 0; x < NavIcon.SIZE; x++)
			{
				if (icon.getRGB(x, y) != coin.getRGB(x, y))
				{
					pictureOnly++;
					final double dx = x + 0.5 - COIN_CENTRE;
					final double dy = y + 0.5 - COIN_CENTRE;
					assertTrue("picture pixel " + x + "," + y + " is too near the coin",
						Math.sqrt(dx * dx + dy * dy) >= CLEAR_DISTANCE);
				}
			}
		}
		assertTrue("the picture drew something", pictureOnly > 0);
	}

	@Test
	public void theBalanceScalePictureIsPinnedByAChecksumOfAllSixteenBySixteenArgbValues()
	{
		final BufferedImage icon = NavIcon.create();
		final int[] argb = icon.getRGB(0, 0, NavIcon.SIZE, NavIcon.SIZE, null, 0, NavIcon.SIZE);
		assertEquals(PICTURE_CHECKSUM, Arrays.hashCode(argb));
	}
}

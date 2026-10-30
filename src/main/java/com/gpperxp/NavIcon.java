package com.gpperxp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The sidebar button's icon, 16 x 16 px: the 2hBuilds coin top-left ({@link CoinGlyph}) and the plugin's own
 * picture clear of the coin, bottom-right: a balance scale, a gold pan against a blue pan (gp against xp). Drawn
 * in code from a pixel map, so it needs no image file.
 */
public final class NavIcon
{
	/** The sidebar icon size. */
	public static final int SIZE = 16;

	/** The scale's pixel map, one character per pixel, '.' empty; its top-left pixel sits at (PICTURE_X, PICTURE_Y). */
	private static final String[] PICTURE = {
		"....O....",
		".OOOOOOO.",
		".S..O..S.",
		".S..O..S.",
		"GGG.O.UUU",
		".G..O..U.",
		"....O....",
		"...OOO...",
		"..OOOOO..",
	};
	private static final int PICTURE_X = 7;
	private static final int PICTURE_Y = 7;

	/** The colour of a map character: orange beam and stand, steel chains, gold pan, blue pan. */
	private static Color colourOf(final char c)
	{
		switch (c)
		{
			case 'O':
				return new Color(220, 138, 0);
			case 'S':
				return new Color(178, 183, 192);
			case 'G':
				return new Color(196, 156, 58);
			case 'U':
				return new Color(80, 155, 245);
			default:
				return null;
		}
	}

	private NavIcon()
	{
	}

	/** A new 16 x 16 ARGB image of the icon. */
	public static BufferedImage create()
	{
		final BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		try
		{
			CoinGlyph.paint(g);
			for (int j = 0; j < PICTURE.length; j++)
			{
				for (int i = 0; i < PICTURE[j].length(); i++)
				{
					final Color colour = colourOf(PICTURE[j].charAt(i));
					if (colour != null)
					{
						img.setRGB(PICTURE_X + i, PICTURE_Y + j, colour.getRGB());
					}
				}
			}
		}
		finally
		{
			g.dispose();
		}
		return img;
	}
}

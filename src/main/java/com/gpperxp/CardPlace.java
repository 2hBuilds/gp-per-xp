package com.gpperxp;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/** Where the card window goes. Pure. */
final class CardPlace
{
	private CardPlace()
	{
	}

	/** Left of the row, level with it, held inside the screen (at its left and top edges when it does not fit). */
	static Point besideRow(Rectangle row, Dimension card, Rectangle screen, int gap)
	{
		final int x = row.x - gap - card.width;
		final int y = row.y + row.height / 2 - card.height / 2;
		return new Point(
			Math.max(screen.x, Math.min(x, screen.x + screen.width - card.width)),
			Math.max(screen.y, Math.min(y, screen.y + screen.height - card.height)));
	}
}

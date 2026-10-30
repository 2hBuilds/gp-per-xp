package com.gpperxp;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link CardPlace#besideRow}: the card sits left of the row, level with it, held on the screen. */
public class CardPlaceTest
{
	private static final Dimension CARD = new Dimension(200, 100);
	private static final Rectangle SCREEN = new Rectangle(0, 0, 1920, 1080);
	private static final int GAP = 8;

	@Test
	public void normal()
	{
		final Point p = CardPlace.besideRow(new Rectangle(900, 300, 225, 40), CARD, SCREEN, GAP);
		assertEquals(692, p.x);
		assertEquals(270, p.y);
	}

	@Test
	public void clampsLeft()
	{
		final Point p = CardPlace.besideRow(new Rectangle(100, 300, 225, 40), CARD, SCREEN, GAP);
		assertEquals(0, p.x);
		assertEquals(270, p.y);
	}

	@Test
	public void clampsTop()
	{
		final Point p = CardPlace.besideRow(new Rectangle(900, 10, 225, 40), CARD, SCREEN, GAP);
		assertEquals(692, p.x);
		assertEquals(0, p.y);
	}

	@Test
	public void clampsBottom()
	{
		final Point p = CardPlace.besideRow(new Rectangle(900, 1050, 225, 40), CARD, SCREEN, GAP);
		assertEquals(692, p.x);
		assertEquals(980, p.y);
	}
}

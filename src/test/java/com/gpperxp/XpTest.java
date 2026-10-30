package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link Xp}: the OSRS experience table, the level for an xp total, and the xp target for a level. */
public class XpTest
{
	@Test
	public void forLevelTable()
	{
		assertEquals(0, Xp.forLevel(1));
		assertEquals(83, Xp.forLevel(2));
		assertEquals(174, Xp.forLevel(3));
		assertEquals(1154, Xp.forLevel(10));
		assertEquals(101333, Xp.forLevel(50));
		assertEquals(737627, Xp.forLevel(70));
		assertEquals(1986068, Xp.forLevel(80));
		assertEquals(6517253, Xp.forLevel(92));
		assertEquals(11805606, Xp.forLevel(98));
		assertEquals(13034431, Xp.forLevel(99));
	}

	@Test
	public void forLevelClamps()
	{
		assertEquals(0, Xp.forLevel(0));
		assertEquals(0, Xp.forLevel(-4));
		assertEquals(13034431, Xp.forLevel(100));
	}

	@Test
	public void levelForBoundaries()
	{
		assertEquals(1, Xp.levelFor(-5));
		assertEquals(1, Xp.levelFor(0));
		assertEquals(1, Xp.levelFor(82));
		assertEquals(2, Xp.levelFor(83));
		assertEquals(2, Xp.levelFor(173));
		assertEquals(3, Xp.levelFor(174));
		assertEquals(98, Xp.levelFor(13034430));
		assertEquals(99, Xp.levelFor(13034431));
		assertEquals(99, Xp.levelFor(200000000));
	}

	@Test
	public void targetXpCases()
	{
		assertEquals(174, Xp.targetXp(100, 0));
		assertEquals(101333, Xp.targetXp(100, 50));
		assertEquals(13034431, Xp.targetXp(13034431, 0));
		assertEquals(13034431, Xp.targetXp(13034430, 0));
		assertEquals(83, Xp.targetXp(0, 0));
		assertEquals(174, Xp.targetXp(83, 0));
		assertEquals(0, Xp.targetXp(100, 1));
		assertEquals(13034431, Xp.targetXp(100, 120));
	}
}

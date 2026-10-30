package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link SortState}: picking the current sort again reverses it, picking another sort starts in its normal order, and
 * the labels of the six states.
 */
public class SortStateTest
{
	@Test
	public void pickSameReverses()
	{
		final SortState first = new SortState(Sort.CHEAPEST_GP_XP, false).pick(Sort.CHEAPEST_GP_XP);
		assertEquals(Sort.CHEAPEST_GP_XP, first.sort);
		assertTrue(first.reversed);
		final SortState second = first.pick(Sort.CHEAPEST_GP_XP);
		assertEquals(Sort.CHEAPEST_GP_XP, second.sort);
		assertFalse(second.reversed);
	}

	@Test
	public void pickOtherResets()
	{
		final SortState state = new SortState(Sort.CHEAPEST_GP_XP, true).pick(Sort.FASTEST_XP_H);
		assertEquals(Sort.FASTEST_XP_H, state.sort);
		assertFalse(state.reversed);
	}

	@Test
	public void labels()
	{
		assertEquals("Cheapest gp/xp", new SortState(Sort.CHEAPEST_GP_XP, false).label());
		assertEquals("Dearest gp/xp", new SortState(Sort.CHEAPEST_GP_XP, true).label());
		assertEquals("Fastest xp/h", new SortState(Sort.FASTEST_XP_H, false).label());
		assertEquals("Slowest xp/h", new SortState(Sort.FASTEST_XP_H, true).label());
		assertEquals("Best gp/h", new SortState(Sort.BEST_GP_H, false).label());
		assertEquals("Worst gp/h", new SortState(Sort.BEST_GP_H, true).label());
	}
}

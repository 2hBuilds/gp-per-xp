package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link RefreshGate}: at most one panel refresh waits at a time.
 */
public class RefreshGateTest
{
	@Test
	public void firstRequestSchedules()
	{
		assertTrue(new RefreshGate().request());
	}

	@Test
	public void secondRequestWhilePendingDoesNot()
	{
		final RefreshGate gate = new RefreshGate();
		gate.request();
		assertFalse(gate.request());
	}

	@Test
	public void afterDoneItSchedulesAgain()
	{
		final RefreshGate gate = new RefreshGate();
		gate.request();
		gate.done();
		assertTrue(gate.request());
	}
}

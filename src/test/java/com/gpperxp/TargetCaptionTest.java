package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link TargetCaption}: the grey note beside the target box. Xp -1 is not logged in; xp 1154 is level 10.
 */
public class TargetCaptionTest
{
	private static final int LEVEL_TEN_XP = 1154;
	/** The xp that reaches level 90. */
	private static final int LEVEL_90_XP = 5346332;
	/** The xp that reaches level 99. */
	private static final int LEVEL_99_XP = 13034431;

	@Test
	public void notLoggedIn()
	{
		assertEquals("0 = next level", TargetCaption.of(-1, 0));
	}

	@Test
	public void nextLevel()
	{
		assertEquals("to level 11", TargetCaption.of(LEVEL_TEN_XP, 0));
	}

	@Test
	public void targetAhead()
	{
		assertEquals("to level 20", TargetCaption.of(LEVEL_TEN_XP, 20));
	}

	@Test
	public void targetBehind()
	{
		assertEquals("level 5 reached", TargetCaption.of(LEVEL_TEN_XP, 5));
	}

	@Test
	public void targetEqualsLevel()
	{
		assertEquals("level 10 reached", TargetCaption.of(LEVEL_TEN_XP, 10));
	}

	@Test
	public void maxedNext()
	{
		assertEquals("level 99 reached", TargetCaption.of(LEVEL_99_XP, 0));
	}

	@Test
	public void ninetyToNinetyTwo()
	{
		assertEquals("to level 92", TargetCaption.of(LEVEL_90_XP, 92));
	}
}

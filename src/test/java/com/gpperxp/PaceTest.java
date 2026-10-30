package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link Pace}: the pace setting scales a method's hourly rate and leaves everything else alone. The fixture is the
 * method "Test potion" (Herblore level 3, 25 xp per action, 1200 actions per hour) unless stated.
 */
public class PaceTest
{
	/** Herb and Vial make a Potion. */
	private static TrainingMethod m1()
	{
		return method("Test potion", 1200);
	}

	private static TrainingMethod method(String name, double actionsPerHour)
	{
		return new TrainingMethod("Herblore", name, 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), actionsPerHour, "src", null);
	}

	@Test
	public void hundredIsTheSameInstance()
	{
		final TrainingMethod m = m1();
		assertSame(m, Pace.apply(m, 100));
	}

	@Test
	public void fiftyHalvesTheRate()
	{
		final TrainingMethod m = m1();
		final TrainingMethod paced = Pace.apply(m, 50);
		assertEquals(600.0, paced.actionsPerHour, 1e-9);
		assertEquals(m.skill, paced.skill);
		assertEquals(m.name, paced.name);
		assertEquals(m.level, paced.level);
		assertEquals(m.xpPerAction, paced.xpPerAction, 1e-9);
		assertEquals(m.inputs, paced.inputs);
		assertEquals(m.outputs, paced.outputs);
		assertEquals(m.source, paced.source);
		assertEquals(m.note, paced.note);
	}

	@Test
	public void unratedStaysTheSame()
	{
		final TrainingMethod unrated = method("Test potion", 0);
		assertSame(unrated, Pace.apply(unrated, 50));
	}

	@Test
	public void applyAllKeepsOrderAndSize()
	{
		final List<TrainingMethod> methods = Arrays.asList(method("Alpha", 1000), method("Beta", 0),
			method("Gamma", 400));
		final List<TrainingMethod> paced = Pace.applyAll(methods, 50);
		assertEquals(3, paced.size());
		assertEquals("Alpha", paced.get(0).name);
		assertEquals("Beta", paced.get(1).name);
		assertEquals("Gamma", paced.get(2).name);
		assertEquals(500.0, paced.get(0).actionsPerHour, 1e-9);
		assertEquals(0.0, paced.get(1).actionsPerHour, 1e-9);
		assertEquals(200.0, paced.get(2).actionsPerHour, 1e-9);
	}

	@Test
	public void clampKeepsTenToHundred()
	{
		assertEquals(10, Pace.clamp(5));
		assertEquals(100, Pace.clamp(150));
		assertEquals(55, Pace.clamp(55));
	}
}

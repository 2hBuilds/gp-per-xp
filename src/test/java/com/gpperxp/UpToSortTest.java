package com.gpperxp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** An "up to" row sorts by its stated figure and counts in Top picks (a Wintertodt-like row against a stated 300k xp/h). */
public class UpToSortTest
{
	private static final IntToLongFunction PRICE = id -> 0L;
	private static final List<ItemQty> NONE = Collections.emptyList();

	private static TrainingMethod wintertodtLike()
	{
		return new TrainingMethod("Test", "Wintertodt like", 50, 1.0, NONE, NONE, 0, "src", null)
			.withV2(0, 320000, null, 0, null, "supply crates")
			.withRateUnit("xp");
	}

	private static TrainingMethod stated()
	{
		return new TrainingMethod("Test", "Stated", 50, 1.0, NONE, NONE, 300000, "src", null)
			.withRateUnit("xp");
	}

	private static List<TrainingMethod> methods()
	{
		return Arrays.asList(stated(), wintertodtLike());
	}

	@Test
	public void upToRowSortsFirstInFastest()
	{
		final List<Row> rows = new ArrayList<>();
		for (TrainingMethod m : methods())
		{
			rows.add(Costs.row(m, PRICE, false));
		}
		final List<Row> sorted = Sort.arrange(rows, Sort.FASTEST_XP_H, Sort.atLevel(60), true, true, false);
		assertEquals("Wintertodt like", sorted.get(0).method.name);
	}

	@Test
	public void upToRowIsInTopPicksFastest()
	{
		final TopPicks top = TopPicks.build(methods(), PRICE, false, -1, Standing.ALL, true);
		assertEquals("Wintertodt like", top.fastest.get(0).row.method.name);
		assertEquals("<320k", top.fastest.get(0).xpH);
		assertTrue(top.fastest.size() == 2);
	}
}

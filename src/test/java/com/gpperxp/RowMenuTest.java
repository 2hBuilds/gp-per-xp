package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntToLongFunction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link RowMenu}: the one-item menu a right-click opens on a row. The row is the fixture method "Test potion",
 * Herblore level 3, with source "src".
 */
public class RowMenuTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(12, 500L);
		return map;
	}

	/** One Herb makes one Potion. */
	private static Row potionRow()
	{
		return Costs.row(new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Collections.singletonList(new ItemQty(10, "Herb", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null), PRICE, false);
	}

	@Test
	public void hasOneItem()
	{
		final JPopupMenu menu = RowMenu.build(potionRow(), source -> { });
		assertEquals(1, menu.getComponentCount());
		final JMenuItem item = (JMenuItem) menu.getComponent(0);
		assertEquals("Open wiki page", item.getText());
	}

	@Test
	public void clickBrowsesTheSource()
	{
		final Row row = potionRow();
		final List<String> browsed = new ArrayList<>();
		final Consumer<String> browse = browsed::add;
		final JMenuItem item = (JMenuItem) RowMenu.build(row, browse).getComponent(0);
		item.doClick();
		assertEquals(Collections.singletonList(row.method.source), browsed);
	}
}

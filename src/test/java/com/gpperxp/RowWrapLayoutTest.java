package com.gpperxp;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A wrapped name pushes the values line below its last line in the live panel: the real {@link GpPerXpPanel} at the
 * sidebar's width, laid out the way Swing does, never a row that is too short for its name.
 */
public class RowWrapLayoutTest
{
	private static final String LONG = "Extended antifire (4-dose, 1-tick)";
	private static final String SIXTY = "Wine of zamorak fermenting in a very long named barrel batch";
	private static final String SHORT = "Bones";
	private static final String JSON = "{\"updated\":\"2026-10-29\",\"methods\":["
		+ method(SHORT, 1, 10, 1000, "{\"itemId\":2,\"qty\":1}")
		+ "," + method(LONG, 1, 25, 1000, "{\"itemId\":1,\"qty\":1}")
		+ "," + method(SIXTY, 1, 100, 500, "{\"itemId\":3,\"qty\":1}")
		+ variants()
		+ "]}";

	/** The name of the row the live panel overlapped, and a dozen longer by one letter each (one sits at the boundary). */
	private static final String GEM = "Gem rocks, 3-tick (rate at 70)";

	private static String variants()
	{
		final StringBuilder out = new StringBuilder(",").append(method(GEM, 1, 10, 1000, "{\"itemId\":2,\"qty\":1}"));
		for (int i = 1; i <= 14; i++)
		{
			out.append(',').append(method(GEM + "xxxxxxxxxxxxxx".substring(0, i), 1, 10, 1000, "{\"itemId\":2,\"qty\":1}"));
		}
		return out.toString();
	}

	private static String method(String name, int level, double xp, int actions, String in)
	{
		return "{\"skill\":\"Prayer\",\"name\":\"" + name + "\",\"level\":" + level + ",\"xpPerAction\":" + xp
			+ ",\"inputs\":[" + in + "],\"outputs\":[],\"actionsPerHour\":" + actions + "}";
	}

	/** Settings held in fields. */
	private static final class Fields implements PanelSettings
	{
		private int target;
		private boolean profit = true;
		private boolean above = true;
		private boolean bands;
		private boolean tax = true;
		private Sort sort = Sort.CHEAPEST_GP_XP;
		private boolean sortReversed;
		private String skill = "";
		private int pace = 100;
		private PriceSource priceSource = PriceSource.GUIDE;

		@Override
		public boolean otherBands()
		{
			return bands;
		}

		@Override
		public void setOtherBands(boolean on)
		{
			bands = on;
		}

		@Override
		public int pace()
		{
			return pace;
		}

		@Override
		public void setPace(int percent)
		{
			pace = percent;
		}

		@Override
		public int targetLevel()
		{
			return target;
		}

		@Override
		public void setTargetLevel(int level)
		{
			target = level;
		}

		@Override
		public boolean includeProfit()
		{
			return profit;
		}

		@Override
		public void setIncludeProfit(boolean on)
		{
			profit = on;
		}

		@Override
		public boolean showAbove()
		{
			return above;
		}

		@Override
		public void setShowAbove(boolean on)
		{
			above = on;
		}

		@Override
		public boolean takeOffTax()
		{
			return tax;
		}

		@Override
		public void setTakeOffTax(boolean on)
		{
			tax = on;
		}

		@Override
		public Sort sort()
		{
			return sort;
		}

		@Override
		public void setSort(Sort s)
		{
			sort = s;
		}

		@Override
		public boolean sortReversed()
		{
			return sortReversed;
		}

		@Override
		public void setSortReversed(boolean on)
		{
			sortReversed = on;
		}

		@Override
		public PriceSource priceSource()
		{
			return priceSource;
		}

		@Override
		public void setPriceSource(PriceSource s)
		{
			priceSource = s;
		}

		private String view = "list";
		private HealSort healSort = HealSort.CHEAPEST_GP_HP;
		private boolean healSortReversed;

		@Override
		public String view()
		{
			return view;
		}

		@Override
		public void setView(String next)
		{
			view = next;
		}

		@Override
		public HealSort healSort()
		{
			return healSort;
		}

		@Override
		public void setHealSort(HealSort sort)
		{
			healSort = sort;
		}

		@Override
		public boolean healSortReversed()
		{
			return healSortReversed;
		}

		@Override
		public void setHealSortReversed(boolean on)
		{
			healSortReversed = on;
		}

		@Override
		public String skill()
		{
			return skill;
		}

		@Override
		public void setSkill(String s)
		{
			skill = s;
		}
	}

	private static final class Quiet implements CardViewer
	{
		@Override
		public void show(Row row, Rectangle rowOnScreen)
		{
		}

		@Override
		public void hide()
		{
		}
	}

	private GpPerXpPanel panel;

	@Before
	public void setUp() throws Exception
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(JSON));
		final Map<Integer, Long> prices = new HashMap<>();
		prices.put(1, 100L);
		prices.put(2, 10L);
		prices.put(3, 500L);
		final GpPerXpPanel[] out = new GpPerXpPanel[1];
		SwingUtilities.invokeAndWait(() -> out[0] = new GpPerXpPanel(() -> { }, methods, Healing.empty(), () -> 99,
			id -> prices.getOrDefault(id, 0L), new Fields(), skill -> 1154, new Quiet(), url -> { }));
		panel = out[0];
		SwingUtilities.invokeAndWait(() ->
		{
			panel.showSkill("Prayer");
			panel.setSize(225, 700);
			layoutPlain(panel);
		});
	}

	private static void layoutPlain(Container c)
	{
		c.doLayout();
		for (Component child : c.getComponents())
		{
			if (child instanceof Container)
			{
				layoutPlain((Container) child);
			}
		}
	}

	private static void layoutAll(Container c)
	{
		// Invalidated first, as a real resize does, so that the layouts drop what they remember of the old width.
		c.invalidate();
		c.doLayout();
		for (Component child : c.getComponents())
		{
			if (child instanceof Container)
			{
				layoutAll((Container) child);
			}
		}
	}

	private static void collect(Container c, List<ListRow> into)
	{
		for (Component child : c.getComponents())
		{
			if (child instanceof ListRow)
			{
				into.add((ListRow) child);
			}
			if (child instanceof Container)
			{
				collect((Container) child, into);
			}
		}
	}

	private ListRow row(String name)
	{
		final List<ListRow> rows = new ArrayList<>();
		collect(panel, rows);
		for (ListRow row : rows)
		{
			if (name.equals(row.getName()))
			{
				return row;
			}
		}
		throw new AssertionError("no row " + name);
	}

	private void check(String name, int width) throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			// Resized the way the sidebar does: the list gets the new width, then the layout asks the rows.
			panel.setSize(width, 700);
			layoutAll(panel);
			final ListRow shortRow = row(SHORT);
			final ListRow longRow = row(name);
			assertTrue("the wrapped row is taller than a short one", longRow.getHeight() > shortRow.getHeight());
			final BufferedImage image = new BufferedImage(longRow.getWidth(), longRow.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
			longRow.paint(image.getGraphics());
			assertTrue("the values line (" + longRow.valuesBaseline() + ") is below the last name line ("
				+ longRow.lastNameBaseline() + ")", longRow.valuesBaseline() > longRow.lastNameBaseline());
			assertTrue("the values line is inside the row", longRow.valuesBaseline() <= longRow.getHeight());
		});
	}

	@Test
	public void aWrappedNameGrowsTheRow() throws Exception
	{
		check(LONG, 160);
	}

	@Test
	public void aSixtyCharacterNameGrowsTheRow() throws Exception
	{
		check(SIXTY, 225);
	}

	@Test
	public void aRowTooShortForItsNameStillPaintsTheValuesBelowTheName() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			final ListRow longRow = row(LONG);
			longRow.setSize(longRow.getWidth(), 40);
			assertTrue(longRow.valuesBaseline() > longRow.lastNameBaseline());
		});
	}

	/** Laid out once, top down, with no invalidation (a panel painted straight after it was sized). */
	@Test
	public void everyRowIsAsTallAsItsNameNeedsAtTheBoundary() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			final List<ListRow> rows = new ArrayList<>();
			collect(panel, rows);
			for (ListRow row : rows)
			{
				assertEquals(row.getName(), row.getPreferredSize().height, row.getHeight());
				assertTrue(row.getName(), row.valuesBaseline() > row.lastNameBaseline());
			}
		});
	}
}

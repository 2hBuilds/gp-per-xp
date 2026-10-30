package com.gpperxp;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The jump from Top picks to the List and the search box's clear control, driven headlessly like
 * {@link PanelBehaviourTest}: a left click on a Top picks row shows the whole list with an empty search and the
 * clicked method's row marked; the "x" at the box's right end and Escape empty the box.
 */
public class PanelJumpTest
{
	private static final int LEVEL_TEN_XP = 1154;
	private static final String JSON = "{\"updated\":\"2026-10-29\",\"methods\":["
		+ method("Bones", 1, 10, 1000, "{\"itemId\":2,\"qty\":1}")
		+ "," + method("Big bones", 1, 25, 1000, "{\"itemId\":1,\"qty\":1}")
		+ "," + method("Wine", 1, 100, 500, "{\"itemId\":3,\"qty\":1}")
		+ "]}";
	private static final int CLEAR_X = 95;

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
		public void show(Row row, java.awt.Rectangle rowOnScreen)
		{
		}

		@Override
		public void hide()
		{
		}
	}

	private Fields settings;
	private GpPerXpPanel panel;

	@Before
	public void setUp() throws Exception
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(JSON));
		settings = new Fields();
		final Map<Integer, Long> prices = new HashMap<>();
		prices.put(1, 100L);
		prices.put(2, 10L);
		prices.put(3, 500L);
		final GpPerXpPanel[] out = new GpPerXpPanel[1];
		SwingUtilities.invokeAndWait(() -> out[0] = new GpPerXpPanel(() -> { }, methods, Healing.empty(), () -> 99,
			id -> prices.getOrDefault(id, 0L), settings, skill -> LEVEL_TEN_XP, new Quiet(), url -> { }));
		panel = out[0];
		edt(() -> panel.showSkill("Prayer"));
	}

	private static void edt(Runnable r) throws Exception
	{
		SwingUtilities.invokeAndWait(r);
	}

	private static <T extends Component> void collect(Container c, Class<T> type, List<T> into)
	{
		for (Component child : c.getComponents())
		{
			if (type.isInstance(child))
			{
				into.add(type.cast(child));
			}
			if (child instanceof Container)
			{
				collect((Container) child, type, into);
			}
		}
	}

	private List<ListRow> rows()
	{
		final List<ListRow> out = new ArrayList<>();
		collect(panel, ListRow.class, out);
		return out;
	}

	private List<String> names()
	{
		final List<String> out = new ArrayList<>();
		for (ListRow row : rows())
		{
			out.add(row.getName());
		}
		return out;
	}

	private SearchBox searchBox()
	{
		final List<SearchBox> boxes = new ArrayList<>();
		collect(panel, SearchBox.class, boxes);
		assertEquals(1, boxes.size());
		return boxes.get(0);
	}

	private void click(Component c, int x) throws Exception
	{
		edt(() -> c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, 0, MouseEvent.BUTTON1_DOWN_MASK, x, 5, 1,
			false, MouseEvent.BUTTON1)));
	}

	private void typeBones() throws Exception
	{
		edt(() ->
		{
			searchBox().setSize(100, 20);
			searchBox().setText("bones");
		});
		assertEquals(2, rows().size());
	}

	@Test
	public void topPicksClickShowsTheWholeListAndMarksTheRow() throws Exception
	{
		edt(() ->
		{
			searchBox().setText("wine");
			settings.setView(GpPerXpPanel.VIEW_TOP);
			panel.refresh();
		});
		final List<ListRow> picks = rows();
		assertFalse(picks.isEmpty());
		final ListRow clicked = picks.get(picks.size() - 1);
		final String name = clicked.getName();
		click(clicked, 5);
		assertEquals(GpPerXpPanel.VIEW_LIST, settings.view());
		assertEquals("", searchBox().getText());
		assertEquals(3, rows().size());
		int marked = 0;
		for (ListRow row : rows())
		{
			if (Boolean.TRUE.equals(row.getClientProperty(ListRow.MARKED)))
			{
				marked++;
				assertEquals(name, row.getName());
			}
		}
		assertEquals(1, marked);
	}

	@Test
	public void clearControlShowsEveryRowAgain() throws Exception
	{
		typeBones();
		click(searchBox(), CLEAR_X);
		assertEquals("", searchBox().getText());
		assertEquals(3, rows().size());
	}

	@Test
	public void escapeClearsTheBox() throws Exception
	{
		typeBones();
		final Object key = searchBox().getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		final Action clear = searchBox().getActionMap().get(key);
		edt(() -> clear.actionPerformed(new ActionEvent(searchBox(), ActionEvent.ACTION_PERFORMED, "")));
		assertEquals("", searchBox().getText());
		assertEquals(3, rows().size());
	}

	@Test
	public void clickLeftOfTheClearControlKeepsTheText() throws Exception
	{
		typeBones();
		click(searchBox(), 10);
		assertTrue(searchBox().getText().length() > 0);
	}
}

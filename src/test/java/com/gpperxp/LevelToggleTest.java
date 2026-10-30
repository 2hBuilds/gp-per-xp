package com.gpperxp;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Lot V: the "My speed" label and the "My level / All levels" row, driven headlessly as {@link PanelBehaviourTest}
 * drives the panel. Prayer has three methods the player (level 10) can do and one at level 60 above them.
 */
public class LevelToggleTest
{
	private static final int LEVEL_TEN_XP = 1154;
	private static final String SPEED_TIP = "Lower it if you play slower than the wiki's rates. "
		+ "xp/h, gp/h and time to target change with it.";
	private static final String JSON = "{\"updated\":\"2026-10-29\",\"methods\":["
		+ method("Prayer", "Cheap", 1, 10, "{\"itemId\":2,\"qty\":1}")
		+ "," + method("Prayer", "Mid", 1, 25, "{\"itemId\":1,\"qty\":1}")
		+ "," + method("Prayer", "Dear", 1, 100, "{\"itemId\":3,\"qty\":1}")
		+ "," + method("Prayer", "High", 60, 50, "{\"itemId\":2,\"qty\":1}")
		+ "]}";

	private static String method(String skill, String name, int level, double xp, String in)
	{
		return "{\"skill\":\"" + skill + "\",\"name\":\"" + name + "\",\"level\":" + level + ",\"xpPerAction\":" + xp
			+ ",\"inputs\":[" + in + "],\"outputs\":[],\"actionsPerHour\":1000}";
	}

	/** Settings held in fields; showAbove starts off as the real default does. */
	private static final class Fields implements PanelSettings
	{
		private boolean above;
		private String view = "list";
		private String skill = "";
		private Sort sort = Sort.CHEAPEST_GP_XP;
		private boolean sortReversed;
		private int target;
		private int pace = 100;
		private boolean profit = true;
		private boolean bands;
		private boolean tax = true;
		private PriceSource source = PriceSource.GUIDE;
		private HealSort healSort = HealSort.CHEAPEST_GP_HP;
		private boolean healReversed;

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
		public PriceSource priceSource()
		{
			return source;
		}

		@Override
		public void setPriceSource(PriceSource s)
		{
			source = s;
		}

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
			return healReversed;
		}

		@Override
		public void setHealSortReversed(boolean on)
		{
			healReversed = on;
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

	private Fields settings;
	private int playerXp;
	private GpPerXpPanel panel;

	@Before
	public void setUp() throws Exception
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(JSON));
		settings = new Fields();
		playerXp = LEVEL_TEN_XP;
		final GpPerXpPanel[] out = new GpPerXpPanel[1];
		SwingUtilities.invokeAndWait(() -> out[0] = new GpPerXpPanel(() -> { }, methods, Healing.empty(), () -> 99,
			id -> id == 1 ? 100L : id == 2 ? 10L : 500L, settings, skill -> playerXp,
			new CardViewer()
			{
				@Override
				public void show(Row row, java.awt.Rectangle rowOnScreen)
				{
				}

				@Override
				public void hide()
				{
				}
			}, url -> { }));
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

	private <T extends Component> List<T> all(Class<T> type)
	{
		final List<T> out = new ArrayList<>();
		collect(panel, type, out);
		return out;
	}

	private List<String> names()
	{
		final List<String> out = new ArrayList<>();
		for (ListRow row : all(ListRow.class))
		{
			out.add(row.getName());
		}
		return out;
	}

	private JLabel labelWithText(String text)
	{
		for (JLabel label : all(JLabel.class))
		{
			if (text.equals(label.getText()))
			{
				return label;
			}
		}
		return null;
	}

	private JButton buttonWithText(String text)
	{
		for (JButton button : all(JButton.class))
		{
			if (text.equals(button.getText()))
			{
				return button;
			}
		}
		throw new AssertionError("no button " + text);
	}

	private void press(String text) throws Exception
	{
		final JButton button = buttonWithText(text);
		edt(button::doClick);
	}

	@Test
	public void t1PaceRowSaysMySpeed() throws Exception
	{
		final JLabel label = labelWithText("My speed");
		assertNotNull(label);
		assertEquals(SPEED_TIP, label.getToolTipText());
		assertNotNull(labelWithText("% of wiki"));
		assertEquals(SPEED_TIP, labelWithText("% of wiki").getToolTipText());
		assertEquals(null, labelWithText("Pace"));
	}

	@Test
	public void t2AllLevelsAddsTheGreyedLines() throws Exception
	{
		assertEquals(3, names().size());
		assertFalse(names().contains("High"));
		press("All levels");
		assertTrue(settings.showAbove());
		final List<String> shown = names();
		assertEquals(4, shown.size());
		assertEquals("High", shown.get(3));
		press("My level");
		assertFalse(settings.showAbove());
		assertEquals(3, names().size());
		assertFalse(names().contains("High"));
	}

	private static boolean visibleUp(Component c)
	{
		for (Component p = c; p != null; p = p.getParent())
		{
			if (!p.isVisible())
			{
				return false;
			}
		}
		return true;
	}

	@Test
	public void t3RowIsInTheListViewOnly() throws Exception
	{
		assertTrue(visibleUp(buttonWithText("My level")));
		assertTrue(visibleUp(buttonWithText("All levels")));
		edt(() ->
		{
			settings.setView("top");
			panel.refresh();
		});
		assertFalse(visibleUp(buttonWithText("My level")));
		edt(() ->
		{
			settings.setView("list");
			panel.refresh();
		});
		assertTrue(visibleUp(buttonWithText("All levels")));
	}

	@Test
	public void t4MenuHasNoShowAboveItem() throws Exception
	{
		Assume.assumeFalse("a menu needs a display", GraphicsEnvironment.isHeadless());
		final JFrame[] frame = new JFrame[1];
		edt(() ->
		{
			frame[0] = new JFrame();
			frame[0].add(panel);
			frame[0].setSize(400, 800);
			frame[0].setVisible(true);
		});
		try
		{
			final JLabel icon = all(JLabel.class).stream()
				.filter(l -> SupportLinks.SETTINGS_TIP.equals(l.getToolTipText())).findFirst().get();
			edt(() -> icon.dispatchEvent(new MouseEvent(icon, MouseEvent.MOUSE_PRESSED, 0,
				MouseEvent.BUTTON1_DOWN_MASK, 2, 2, 1, false, MouseEvent.BUTTON1)));
			final List<JMenuItem> items = new ArrayList<>();
			edt(() ->
			{
				for (Window w : Window.getWindows())
				{
					collect(w, JMenuItem.class, items);
				}
			});
			assertFalse(items.isEmpty());
			for (JMenuItem item : items)
			{
				assertFalse("Show methods above my level".equals(item.getText()));
			}
		}
		finally
		{
			edt(() -> frame[0].dispose());
		}
	}

	@Test
	public void t5CaptionAsksForALoginWhenXpIsUnknown() throws Exception
	{
		assertFalse(captionShown());
		playerXp = -1;
		edt(panel::refresh);
		assertTrue(captionShown());
		playerXp = LEVEL_TEN_XP;
		edt(panel::refresh);
		assertFalse(captionShown());
	}

	private boolean captionShown()
	{
		final JLabel label = labelWithText("Log in to see your levels");
		return label != null && label.isVisible();
	}

	@Test
	public void t6ShowAboveDefaultsToFalse()
	{
		final GpPerXpConfig config = new GpPerXpConfig()
		{
		};
		assertFalse(config.showAbove());
	}
}

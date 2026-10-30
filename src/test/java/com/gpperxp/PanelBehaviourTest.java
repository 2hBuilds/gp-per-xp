package com.gpperxp;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * What the panel's controls do, driven headlessly: the picker, the sort boxes, the target box, the hover and the
 * settings. The panel is found by walking its components; a list row's name and texts are read from its name and
 * its TEXTS client property. Player xp 1154 is level 10 unless stated. Prices: 1 -> 100, 2 -> 10, 3 -> 500, 4 -> 300;
 * 99 is unpriced.
 *
 * <p>Prayer by gp/xp: Profit (makes money), Cheap 1, Mid 4, Dear 5, then Unknown (no price), then High (level 60,
 * above the player; at 0.2 gp/xp it is the cheapest of all once the player can do it).
 */
public class PanelBehaviourTest
{
	private static final int LEVEL_TEN_XP = 1154;
	private static final String JSON = "{\"updated\":\"2026-10-29\",\"methods\":["
		+ method("Prayer", "Cheap", 1, 10, 1000, "{\"itemId\":2,\"qty\":1}", "")
		+ "," + method("Prayer", "Dear", 1, 100, 500, "{\"itemId\":3,\"qty\":1}", "")
		+ "," + method("Prayer", "Mid", 1, 25, 1000, "{\"itemId\":1,\"qty\":1}", "")
		+ "," + method("Prayer", "Profit", 1, 10, 800, "{\"itemId\":2,\"qty\":1}", "{\"itemId\":4,\"qty\":2}")
		+ "," + method("Prayer", "Unknown", 1, 10, 1000, "{\"itemId\":99,\"qty\":1}", "")
		+ "," + method("Prayer", "High", 60, 50, 1000, "{\"itemId\":2,\"qty\":1}", "")
		+ "," + method("Herblore", "Potion", 3, 25, 3000, "{\"itemId\":1,\"qty\":1}", "")
		+ "]}";
	private static final String[] CHEAPEST = {"Profit", "Cheap", "Mid", "Dear", "Unknown", "High"};
	private static final String[] FASTEST = {"Dear", "Mid", "Cheap", "Profit", "Unknown", "High"};
	private static final String[] DEAREST_FIRST = {"Dear", "Mid", "Cheap", "Profit", "Unknown", "High"};
	private static final String[] CHEAPEST_ALL_DOABLE = {"Profit", "High", "Cheap", "Mid", "Dear", "Unknown"};
	private static final String ABOVE = "above your level";

	private static String method(String skill, String name, int level, double xp, int actions, String in, String out)
	{
		return "{\"skill\":\"" + skill + "\",\"name\":\"" + name + "\",\"level\":" + level + ",\"xpPerAction\":" + xp
			+ ",\"inputs\":[" + in + "],\"outputs\":[" + out + "],\"actionsPerHour\":" + actions + "}";
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

	/** Records what the rows ask of the card viewer. */
	private static final class Recorder implements CardViewer
	{
		private final List<Row> shown = new ArrayList<>();
		private final List<Rectangle> where = new ArrayList<>();
		private int hides;

		@Override
		public void show(Row row, Rectangle rowOnScreen)
		{
			shown.add(row);
			where.add(rowOnScreen);
		}

		@Override
		public void hide()
		{
			hides++;
		}
	}

	private Methods methods;
	private Fields settings;
	private Recorder viewer;
	private Map<Integer, Long> prices;
	private int playerXp;
	private GpPerXpPanel panel;

	@Before
	public void setUp() throws Exception
	{
		methods = Methods.load(new Gson(), new StringReader(JSON));
		settings = new Fields();
		viewer = new Recorder();
		prices = new HashMap<>();
		givePrices();
		playerXp = LEVEL_TEN_XP;
		panel = build();
		// The picker starts on Herblore (alphabetical); most tests drive Prayer.
		edt(() -> panel.showSkill("Prayer"));
	}

	private void givePrices()
	{
		prices.put(1, 100L);
		prices.put(2, 10L);
		prices.put(3, 500L);
		prices.put(4, 300L);
	}

	private GpPerXpPanel build() throws Exception
	{
		final GpPerXpPanel[] out = new GpPerXpPanel[1];
		SwingUtilities.invokeAndWait(() -> out[0] = new GpPerXpPanel(() -> { }, methods,
			Healing.empty(), () -> 99, id -> prices.getOrDefault(id, 0L), settings, skill -> playerXp, viewer, url -> { }));
		return out[0];
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

	@SuppressWarnings("unchecked")
	private JComboBox<String> picker()
	{
		final List<JComboBox> boxes = all(JComboBox.class);
		assertEquals(1, boxes.size());
		return boxes.get(0);
	}

	private JTextField targetBox()
	{
		final List<JTextField> boxes = all(JTextField.class);
		assertFalse(boxes.isEmpty());
		return boxes.get(0);
	}

	/** The pace box: the second text box, under the target box. */
	private JTextField paceBox()
	{
		final List<JTextField> boxes = all(JTextField.class);
		assertTrue(boxes.size() > 1);
		return boxes.get(1);
	}

	/** The three sort boxes in Sort order: gp/xp, xp/h, gp/h. */
	private List<JToggleButton> sortBoxes()
	{
		final List<JToggleButton> boxes = all(JToggleButton.class);
		assertEquals(3, boxes.size());
		return boxes;
	}

	private List<ListRow> rows()
	{
		return all(ListRow.class);
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

	private String text(String name)
	{
		for (ListRow row : rows())
		{
			if (name.equals(row.getName()))
			{
				return (String) row.getClientProperty(ListRow.TEXTS);
			}
		}
		throw new AssertionError("no row " + name + " in " + names());
	}

	private void assertNames(String... expected)
	{
		assertEquals(Arrays.asList(expected), names());
	}

	private void refresh() throws Exception
	{
		edt(panel::refresh);
	}

	private void click(JToggleButton box) throws Exception
	{
		edt(box::doClick);
	}

	private static void type(JTextField box, String text) throws Exception
	{
		edt(() -> box.setText(text));
	}

	private Row rowOf(String name)
	{
		for (TrainingMethod m : methods.forSkill("Prayer"))
		{
			if (m.name.equals(name))
			{
				return Costs.row(m, id -> prices.getOrDefault(id, 0L), settings.takeOffTax());
			}
		}
		throw new AssertionError(name);
	}

	@Test
	public void startsOnFirstSkillAndListsItsRows() throws Exception
	{
		settings.skill = "";
		panel = build();
		assertEquals("Herblore", picker().getSelectedItem());
		assertEquals(Sort.CHEAPEST_GP_XP, settings.sort());
		assertNames("Potion");
	}

	@Test
	public void skillPickerSwitchesList() throws Exception
	{
		edt(() -> picker().setSelectedItem("Herblore"));
		assertNames("Potion");
		edt(() -> picker().setSelectedItem("Prayer"));
		assertNames(CHEAPEST);
	}

	@Test
	public void showSkillSelectsSkill() throws Exception
	{
		edt(() -> panel.showSkill("Herblore"));
		assertEquals("Herblore", picker().getSelectedItem());
		assertNames("Potion");
	}

	@Test
	public void clickingEachSortBoxSorts() throws Exception
	{
		click(sortBoxes().get(1));
		assertEquals(Sort.FASTEST_XP_H, settings.sort());
		assertFalse(settings.sortReversed());
		assertNames(FASTEST);
		click(sortBoxes().get(2));
		assertEquals(Sort.BEST_GP_H, settings.sort());
		assertFalse(settings.sortReversed());
		final List<String> expected = new ArrayList<>();
		for (ListLine line : ListModel.build(methods.forSkill("Prayer"), id -> prices.getOrDefault(id, 0L), true,
			Sort.BEST_GP_H, LEVEL_TEN_XP, true, true, false, Standing.ALL, true))
		{
			expected.add(line.name);
		}
		assertEquals(expected, names());
		click(sortBoxes().get(0));
		assertEquals(Sort.CHEAPEST_GP_XP, settings.sort());
		assertNames(CHEAPEST);
	}

	@Test
	public void clickingLitSortBoxTwiceReverses() throws Exception
	{
		final JToggleButton lit = sortBoxes().get(0);
		assertTrue(lit.getText().endsWith("▼"));
		click(lit);
		assertTrue(settings.sortReversed());
		assertNames(DEAREST_FIRST);
		assertTrue(lit.getText().endsWith("▲"));
		click(lit);
		assertFalse(settings.sortReversed());
		assertNames(CHEAPEST);
		assertTrue(lit.getText().endsWith("▼"));
		final String other = sortBoxes().get(1).getText();
		assertFalse(other.contains("▼") || other.contains("▲"));
	}

	@Test
	public void targetBoxStoresValidNumbers() throws Exception
	{
		type(targetBox(), "92");
		assertEquals(92, settings.targetLevel());
		type(targetBox(), "0");
		assertEquals(0, settings.targetLevel());
		type(targetBox(), "92");
		type(targetBox(), "100");
		assertEquals(92, settings.targetLevel());
		type(targetBox(), "abc");
		assertEquals(92, settings.targetLevel());
		type(targetBox(), "");
		assertEquals(92, settings.targetLevel());
	}

	@Test
	public void paceBoxStoresValidNumbers() throws Exception
	{
		type(paceBox(), "80");
		assertEquals(80, settings.pace());
		type(paceBox(), "5");
		assertEquals(80, settings.pace());
		type(paceBox(), "101");
		assertEquals(80, settings.pace());
		type(paceBox(), "abc");
		assertEquals(80, settings.pace());
		type(paceBox(), "");
		assertEquals(80, settings.pace());
	}

	/** The "gp/xp" part of a row's texts. */
	private static String gpXp(String texts)
	{
		return texts.substring(texts.indexOf(", ") + 2, texts.indexOf(" gp/xp"));
	}

	@Test
	public void paceScalesTheList() throws Exception
	{
		methods = Methods.load(new Gson(), new StringReader("{\"updated\":\"2026-10-29\",\"methods\":["
			+ method("Prayer", "Rated", 1, 10, 1000, "{\"itemId\":2,\"qty\":1}", "")
			+ "," + method("Prayer", "Unrated", 1, 10, 0, "{\"itemId\":2,\"qty\":1}", "") + "]}"));
		panel = build();
		edt(() -> panel.showSkill("Prayer"));
		final String rated = text("Rated");
		final String unrated = text("Unrated");
		assertTrue(rated, rated.contains("10k xp/h"));
		assertTrue(rated, rated.contains("gp/xp"));
		type(paceBox(), "50");
		assertTrue(text("Rated").contains("5k xp/h"));
		assertEquals(gpXp(rated), gpXp(text("Rated")));
		assertEquals(unrated, text("Unrated"));
	}

	@Test
	public void targetLevelDrivesTheCard() throws Exception
	{
		playerXp = Xp.forLevel(90);
		refresh();
		type(targetBox(), "92");
		final CardModel card = CardModel.of(rowOf("Mid"), id -> prices.getOrDefault(id, 0L), settings.takeOffTax(),
			playerXp, settings.targetLevel());
		assertNotNull(card.trip);
		assertEquals("To level 92", card.trip.heading);
		type(targetBox(), "0");
		assertEquals("To level 91", CardModel.of(rowOf("Mid"), id -> prices.getOrDefault(id, 0L), true, playerXp,
			settings.targetLevel()).trip.heading);
	}

	@Test
	public void priceSourceRowsStoreTheChoice() throws Exception
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
			edt(() -> icon.dispatchEvent(new MouseEvent(icon, MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1_DOWN_MASK,
				2, 2, 1, false, MouseEvent.BUTTON1)));
			final List<JRadioButtonMenuItem> radios = new ArrayList<>();
			edt(() ->
			{
				for (java.awt.Window w : java.awt.Window.getWindows())
				{
					collect(w, JRadioButtonMenuItem.class, radios);
				}
			});
			JRadioButtonMenuItem guide = null;
			JRadioButtonMenuItem live = null;
			for (JRadioButtonMenuItem r : radios)
			{
				guide = "Guide price".equals(r.getText()) ? r : guide;
				live = "Live trade prices".equals(r.getText()) ? r : live;
			}
			assertNotNull(guide);
			assertNotNull(live);
			assertTrue(guide.isSelected());
			final JRadioButtonMenuItem pick = live;
			edt(pick::doClick);
			assertEquals(PriceSource.TRADES, settings.priceSource());
		}
		finally
		{
			edt(() -> frame[0].dispose());
		}
	}

	@Test
	public void hoverShowsAndHides() throws Exception
	{
		Assume.assumeFalse("a row has no screen position without a display", GraphicsEnvironment.isHeadless());
		final JFrame[] frame = new JFrame[1];
		edt(() ->
		{
			frame[0] = new JFrame();
			frame[0].add(panel);
			frame[0].pack();
			frame[0].setVisible(true);
		});
		try
		{
			final ListRow first = rows().get(0);
			final int hidesBefore = viewer.hides;
			edt(() -> first.dispatchEvent(new MouseEvent(first, MouseEvent.MOUSE_ENTERED, 0, 0, 5, 5, 0, false)));
			assertEquals(1, viewer.shown.size());
			assertSame(rowOf("Profit").method, viewer.shown.get(0).method);
			assertEquals(first.getSize(), viewer.where.get(0).getSize());
			edt(() -> first.dispatchEvent(new MouseEvent(first, MouseEvent.MOUSE_EXITED, 0, 0, 5, 5, 0, false)));
			assertEquals(hidesBefore + 1, viewer.hides);
		}
		finally
		{
			edt(() -> frame[0].dispose());
		}
	}

	@Test
	public void hoverLeavingHidesWithoutADisplay() throws Exception
	{
		final ListRow first = rows().get(0);
		final int hidesBefore = viewer.hides;
		edt(() -> first.dispatchEvent(new MouseEvent(first, MouseEvent.MOUSE_EXITED, 0, 0, 5, 5, 0, false)));
		assertEquals(hidesBefore + 1, viewer.hides);
		// A row that is not on screen has no place to put a card, so entering it shows nothing.
		edt(() -> first.dispatchEvent(new MouseEvent(first, MouseEvent.MOUSE_ENTERED, 0, 0, 5, 5, 0, false)));
		assertTrue(viewer.shown.isEmpty());
	}

	@Test
	public void settingsSwitchesChangeTheList() throws Exception
	{
		settings.setIncludeProfit(false);
		refresh();
		assertFalse(names().contains("Profit"));
		assertTrue(names().contains("High"));
		settings.setShowAbove(false);
		refresh();
		assertNames("Cheap", "Mid", "Dear", "Unknown");
		settings.setIncludeProfit(true);
		settings.setShowAbove(true);
		refresh();
		final String taxedProfit = text("Profit");
		final String cheap = text("Cheap");
		settings.setTakeOffTax(false);
		refresh();
		assertFalse(taxedProfit.equals(text("Profit")));
		assertEquals(cheap, text("Cheap"));
	}

	@Test
	public void refreshAfterPricesLoad() throws Exception
	{
		prices.clear();
		refresh();
		assertEquals(0, rows().size());
		givePrices();
		refresh();
		assertFalse(text("Cheap"), text("Cheap").contains("?"));
		assertFalse(text("Dear"), text("Dear").contains("?"));
		assertTrue(text("Unknown").contains("?"));
		assertNames(CHEAPEST);
	}

	private JTextField searchBox()
	{
		final List<JTextField> boxes = all(JTextField.class);
		assertEquals(3, boxes.size());
		return boxes.get(2);
	}

	private String caption()
	{
		for (JLabel label : all(JLabel.class))
		{
			final String text = label.getText();
			if (text != null && (text.startsWith("to level") || text.endsWith("reached") || text.startsWith("0 =")))
			{
				return text;
			}
		}
		throw new AssertionError("no caption");
	}

	@Test
	public void rememberedSkillIsSelected() throws Exception
	{
		settings.skill = "Herblore";
		panel = build();
		assertEquals("Herblore", picker().getSelectedItem());
		assertNames("Potion");
	}

	@Test
	public void pickingASkillIsRemembered() throws Exception
	{
		edt(() -> picker().setSelectedItem("Herblore"));
		edt(() -> picker().setSelectedItem("Prayer"));
		assertEquals("Prayer", settings.skill());
	}

	@Test
	public void searchNarrowsTheList() throws Exception
	{
		type(searchBox(), "ig");
		assertNames("High");
		type(searchBox(), "");
		assertNames(CHEAPEST);
	}

	@Test
	public void waitingForPricesMessage() throws Exception
	{
		prices.clear();
		refresh();
		assertEquals(0, rows().size());
		boolean found = false;
		for (JLabel label : all(JLabel.class))
		{
			found |= GpPerXpPanel.WAITING_TEXT.equals(label.getText());
		}
		assertTrue(found);
		givePrices();
		refresh();
		assertNames(CHEAPEST);
	}

	@Test
	public void targetCaptionFollowsTheTarget() throws Exception
	{
		playerXp = Xp.forLevel(90);
		refresh();
		assertEquals("to level 91", caption());
		type(targetBox(), "92");
		assertEquals("to level 92", caption());
	}

	@Test
	public void notLoggedInGreysNothing() throws Exception
	{
		assertTrue(text("High").contains(ABOVE));
		playerXp = -1;
		refresh();
		assertNames(CHEAPEST_ALL_DOABLE);
		for (String name : names())
		{
			assertFalse(text(name), text(name).contains(ABOVE));
		}
	}
}

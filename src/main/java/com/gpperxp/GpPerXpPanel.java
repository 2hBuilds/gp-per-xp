package com.gpperxp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * The sidebar panel, top to bottom: the title row with the support pair and the settings icon, a skill picker, the
 * target level box, the three sort boxes, then the list of methods ({@link ListView}). The settings icon opens the
 * menu (STANDARD A4 and A5): header, a rule, three switches, a rule, the sort as radio rows, a rule, the OK row.
 *
 * <p>Choice: the panel is not wrapped ({@code super(false)}), as the chat plugin's, so that it lays out its own
 * scrolling and only the list scrolls.
 * <p>Choice: the target level is stored and shown but does not change the list in this lot.
 * <p>Choice: a click on the lit sort box reverses the order; its arrow shows which way (down: best first).
 */
public class GpPerXpPanel extends PluginPanel
{
	/** The plugin's GitHub page, what the menu header's fourth mark opens. */
	static final String GITHUB_URL = "https://github.com/2hBuilds/gp-per-xp";

	/** The menu's rows. */
	static final String RESET_TEXT = "Reset to default";
	static final String OK_TEXT = "OK";

	/** What the list says while no price has loaded. */
	static final String WAITING_TEXT = "Waiting for prices...";

	private static final String[] SORT_WORDS = {"gp/xp", "xp/h", "gp/h"};
	private static final String ARROW_DOWN = " ▼";
	private static final String ARROW_UP = " ▲";
	private static final int PADDING = 8;
	private static final int GAP = 6;
	private static final int ICON_SIZE = 12;
	private static final int MAX_LEVEL = 99;
	private static final String HITPOINTS = "Hitpoints";
	/** The values of the {@code view} setting. */
	static final String VIEW_LIST = "list";
	static final String VIEW_TOP = "top";
	static final String VIEW_HEALING = "healing";
	/** What the Top picks view says when no method qualifies. */
	static final String NO_PICKS_TEXT = "No top picks for you yet.";
	private static final String[] TOP_HEADINGS = {"Fastest xp", "Best gp/h", "Cheapest per xp", "Wiki suggests"};
	/** A press on the settings icon this soon after the menu closed is the press that closed it, not a new open. */
	private static final long REOPEN_GUARD_NANOS = 250_000_000L;

	private final Runnable resetToDefault;
	private final Methods methods;
	private final Healing healing;
	private final IntSupplier maxHp;
	private final IntToLongFunction price;
	private final PanelSettings settings;
	private final Player player;
	private final SupportLinks support;
	private final ListView list;
	private final JComboBox<String> picker = new JComboBox<>();
	private final JTextField targetBox = new JTextField(3);
	private final JLabel targetCaption = label("0 = next level", plainFont(), ColorScheme.LIGHT_GRAY_COLOR);
	private final PaceBox paceBox;
	private final JTextField searchBox = new SearchBox(this::refresh);
	private final JButton listToggle = new JButton("List");
	private final JButton topToggle = new JButton("Top picks");
	private final JButton healingToggle = new JButton("Healing");
	private final JPanel toggles = new JPanel(new GridLayout(1, 0, 4, 0));
	private final JButton myLevelToggle = new JButton("My level");
	private final JButton allLevelsToggle = new JButton("All levels");
	private final JPanel levelRow = new JPanel(new GridLayout(1, 2, 4, 0));
	private final JLabel loginCaption = label("Log in to see your levels", plainFont(), ColorScheme.LIGHT_GRAY_COLOR);
	private final JPanel targetRow = new JPanel();
	private final JPanel sortRow = new JPanel(new GridLayout(1, 3, 4, 0));
	private final JToggleButton[] sortButtons = new JToggleButton[Sort.values().length];
	private final JCheckBoxMenuItem profitCheck;
	private final JCheckBoxMenuItem bandsCheck;
	private final JCheckBoxMenuItem taxCheck;
	private final JRadioButtonMenuItem[] sortRadios = new JRadioButtonMenuItem[Sort.values().length];
	private final JRadioButtonMenuItem[] priceRadios = new JRadioButtonMenuItem[PriceSource.values().length];
	private final JPopupMenu menu;
	private long closedAtNanos = System.nanoTime() - 2 * REOPEN_GUARD_NANOS;
	private boolean syncing;

	/**
	 * @param resetToDefault what "Reset to default" does: puts the plugin's settings back to their defaults
	 * @param methods the bundled training methods
	 * @param healing the foods and potions of the healing list, empty for none (the toggles stay hidden)
	 * @param maxHp the player's Hitpoints level, for heals that are a percent of max HP
	 * @param price the price of an item, 0 or less when unknown
	 * @param settings the settings, read and written
	 * @param player the player's xp
	 * @param viewer what a hovered row asks for
	 * @param browse opens an address in the browser
	 */
	public GpPerXpPanel(Runnable resetToDefault, Methods methods, Healing healing, IntSupplier maxHp,
		IntToLongFunction price, PanelSettings settings, Player player, CardViewer viewer, Consumer<String> browse)
	{
		super(false);
		this.resetToDefault = resetToDefault;
		this.methods = methods;
		this.healing = healing;
		this.maxHp = maxHp;
		this.price = price;
		this.settings = settings;
		this.player = player;
		this.support = new SupportLinks("2h XP and GP", Version.CURRENT, GITHUB_URL, browse, this::closeMenu);
		this.list = new ListView(viewer, browse);
		this.profitCheck = check("Include profit methods", on -> changed(() -> settings.setIncludeProfit(on)));
		this.bandsCheck = check("Show other level bands", on -> changed(() -> settings.setOtherBands(on)));
		this.taxCheck = check("Take off the GE tax", on -> changed(() -> settings.setTakeOffTax(on)));
		this.paceBox = new PaceBox(plainFont(), GAP, percent -> changed(() -> settings.setPace(percent)));
		this.menu = buildMenu();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
		add(controls(), BorderLayout.NORTH);
		final JScrollPane scroll = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		scroll.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		addHierarchyListener(e ->
		{
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing())
			{
				syncControls();
				refresh();
			}
		});
		syncControls();
		refresh();
	}

	/** The title row, the picker, the target row, the sort boxes and the search box. */
	private JPanel controls()
	{
		final JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		box.setOpaque(false);
		box.setBorder(new EmptyBorder(0, 0, GAP, 0));

		final JPanel title = new JPanel(new BorderLayout());
		title.setOpaque(false);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JPanel words = new JPanel();
		words.setLayout(new BoxLayout(words, BoxLayout.X_AXIS));
		words.setOpaque(false);
		words.add(label("2h", boldFont(), ColorScheme.BRAND_ORANGE));
		words.add(label(" XP and GP", boldFont(), Color.WHITE));
		title.add(words, BorderLayout.WEST);
		title.add(support.supportPair(settingsIcon()), BorderLayout.EAST);
		box.add(title);
		box.add(Box.createVerticalStrut(GAP));

		for (String skill : skillsInOrder())
		{
			picker.addItem(skill);
		}
		// Choice: the remembered skill is selected before the listener is added, so the start-up builds the list once.
		picker.setSelectedItem(settings.skill());
		if (picker.getSelectedItem() == null && picker.getItemCount() > 0)
		{
			picker.setSelectedIndex(0);
		}
		picker.setFont(plainFont());
		picker.setFocusable(false);
		picker.addActionListener(e ->
		{
			settings.setSkill((String) picker.getSelectedItem());
			refresh();
		});
		picker.setAlignmentX(Component.LEFT_ALIGNMENT);
		picker.setMaximumSize(new Dimension(Integer.MAX_VALUE, picker.getPreferredSize().height));
		box.add(picker);
		box.add(Box.createVerticalStrut(GAP));

		toggles.setOpaque(false);
		toggles.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (final JButton mode : new JButton[] {listToggle, topToggle, healingToggle})
		{
			mode.setMargin(new Insets(2, 2, 2, 2));
			mode.setFont(plainFont());
			mode.setFocusable(false);
			mode.addActionListener(e -> changed(() -> settings.setView(mode == healingToggle ? VIEW_HEALING
				: mode == topToggle ? VIEW_TOP : VIEW_LIST)));
		}
		toggles.add(listToggle);
		toggles.add(topToggle);
		toggles.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggles.getPreferredSize().height));
		box.add(toggles);
		box.add(Box.createVerticalStrut(GAP));

		levelRow.setOpaque(false);
		levelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (final JButton level : new JButton[] {myLevelToggle, allLevelsToggle})
		{
			level.setMargin(new Insets(2, 2, 2, 2));
			level.setFont(plainFont());
			level.setFocusable(false);
			level.addActionListener(e -> changed(() -> settings.setShowAbove(level == allLevelsToggle)));
			levelRow.add(level);
		}
		levelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, levelRow.getPreferredSize().height));
		box.add(levelRow);
		box.add(Box.createVerticalStrut(GAP));

		final JPanel target = targetRow;
		target.setLayout(new BoxLayout(target, BoxLayout.X_AXIS));
		target.setOpaque(false);
		target.setAlignmentX(Component.LEFT_ALIGNMENT);
		target.add(label("Target", plainFont(), Color.WHITE));
		target.add(Box.createHorizontalStrut(GAP));
		targetBox.setFont(plainFont());
		targetBox.setMaximumSize(targetBox.getPreferredSize());
		targetBox.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				targetTyped();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				targetTyped();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				targetTyped();
			}
		});
		target.add(targetBox);
		target.add(Box.createHorizontalStrut(GAP));
		target.add(targetCaption);
		target.add(Box.createHorizontalGlue());
		box.add(target);
		box.add(Box.createVerticalStrut(GAP));
		box.add(paceBox);
		box.add(Box.createVerticalStrut(GAP));

		final JPanel sorts = sortRow;
		sorts.setOpaque(false);
		sorts.setAlignmentX(Component.LEFT_ALIGNMENT);
		final ButtonGroup group = new ButtonGroup();
		for (final Sort sort : Sort.values())
		{
			final JToggleButton button = new JToggleButton(SORT_WORDS[sort.ordinal()]);
			button.setMargin(new Insets(2, 2, 2, 2));
			button.setFont(plainFont());
			button.setFocusable(false);
			button.addActionListener(e -> pickSort(sort));
			sortButtons[sort.ordinal()] = button;
			group.add(button);
			sorts.add(button);
		}
		box.add(sorts);
		box.add(Box.createVerticalStrut(GAP));

		searchBox.setFont(plainFont());
		searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchBox.getPreferredSize().height));
		box.add(searchBox);
		loginCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		loginCaption.setVisible(false);
		box.add(loginCaption);
		return box;
	}

	/** True when Hitpoints is picked and has a healing file. */
	private boolean healingAvailable()
	{
		return HITPOINTS.equals(picker.getSelectedItem()) && !healing.all().isEmpty();
	}

	/** True when Hitpoints is picked and the healing list is on and has items. */
	private boolean healingMode()
	{
		return healingAvailable() && VIEW_HEALING.equals(settings.view());
	}

	/** True when the Top picks view is on (and the healing list is not). */
	private boolean topMode()
	{
		return VIEW_TOP.equals(settings.view()) && !healingMode();
	}

	/**
	 * Shows the view toggles (Healing joins them for Hitpoints when there is a healing file) and hides the boxes the
	 * view has no use for.
	 */
	private void syncMode()
	{
		final boolean healingOn = healingMode();
		final boolean topOn = topMode();
		final boolean hasHealing = healingAvailable();
		if (hasHealing != (healingToggle.getParent() == toggles))
		{
			if (hasHealing)
			{
				toggles.add(healingToggle);
			}
			else
			{
				toggles.remove(healingToggle);
			}
			toggles.revalidate();
		}
		for (JButton b : new JButton[] {listToggle, topToggle, healingToggle})
		{
			final boolean lit = b == healingToggle ? healingOn : b == topToggle ? topOn : !healingOn && !topOn;
			b.setBackground(lit ? ColorScheme.BRAND_ORANGE : ColorScheme.DARK_GRAY_COLOR);
			b.setForeground(lit ? Color.BLACK : Color.WHITE);
		}
		final boolean allLevels = settings.showAbove();
		for (JButton b : new JButton[] {myLevelToggle, allLevelsToggle})
		{
			final boolean lit = (b == allLevelsToggle) == allLevels;
			b.setBackground(lit ? ColorScheme.BRAND_ORANGE : ColorScheme.DARK_GRAY_COLOR);
			b.setForeground(lit ? Color.BLACK : Color.WHITE);
		}
		levelRow.setVisible(!healingOn && !topOn);
		// Choice: the caption shows only in the List view, where the levels decide what is listed.
		final Object picked = picker.getSelectedItem();
		loginCaption.setVisible(!healingOn && !topOn && picked != null && player.xp((String) picked) < 0);
		targetRow.setVisible(!healingOn);
		paceBox.setVisible(!healingOn);
		searchBox.setVisible(!topOn);
		sortRow.setVisible(!topOn);
	}

	/** The skills that have rows, in alphabetical order. */
	private List<String> skillsInOrder()
	{
		final List<String> out = new ArrayList<>();
		for (String skill : methods.skills())
		{
			if (skill != null)
			{
				out.add(skill);
			}
		}
		Collections.sort(out);
		return out;
	}

	/** Selects the skill in the picker (which rebuilds the list). */
	void showSkill(String skillName)
	{
		if (skillName == null)
		{
			return;
		}
		final boolean same = skillName.equals(picker.getSelectedItem());
		picker.setSelectedItem(skillName);
		settings.setSkill(skillName);
		if (same)
		{
			refresh();
		}
	}

	/** Builds the list again from the skill, the sort, the settings, the player's xp and the prices. */
	public void refresh()
	{
		final String skill = (String) picker.getSelectedItem();
		if (skill == null)
		{
			return;
		}
		syncMode();
		syncSortWords();
		if (healingMode())
		{
			final List<HealLine> heal = HealModel.build(healing.all(), price, maxHp.getAsInt(), settings.healSort(),
				settings.healSortReversed());
			if (!heal.isEmpty() && heal.stream().allMatch(line -> line.unpriced))
			{
				list.setMessage(WAITING_TEXT);
				return;
			}
			list.setHealLines(HealModel.filter(heal, searchBox.getText()));
			return;
		}
		final int xp = player.xp(skill);
		targetCaption.setText(TargetCaption.of(xp, settings.targetLevel()));
		if (topMode())
		{
			showTopPicks(Pace.applyAll(methods.forSkill(skill), settings.pace()), xp);
			return;
		}
		final List<ListLine> lines = ListModel.build(Pace.applyAll(methods.forSkill(skill), settings.pace()), price, settings.takeOffTax(),
			settings.sort(), xp, settings.includeProfit(), settings.showAbove(), settings.sortReversed(), player,
			settings.otherBands());
		if (!lines.isEmpty() && lines.stream().allMatch(line -> line.unpriced))
		{
			list.setMessage(WAITING_TEXT);
			return;
		}
		list.setLines(ListFilter.apply(lines, searchBox.getText()));
	}

	/** Fills the list with the four headed sections of the Top picks view. */
	private void showTopPicks(List<TrainingMethod> paced, int xp)
	{
		final TopPicks top = TopPicks.build(paced, price, settings.takeOffTax(), xp, player, settings.includeProfit());
		final List<List<ListLine>> sections = new ArrayList<>();
		sections.add(top.fastest);
		sections.add(top.bestGp);
		sections.add(top.cheapest);
		sections.add(top.suggested);
		boolean any = false;
		for (List<ListLine> section : sections)
		{
			any |= !section.isEmpty();
		}
		if (!any)
		{
			boolean priced = false;
			for (TrainingMethod m : paced)
			{
				priced |= Costs.priced(m, price);
			}
			list.setMessage(paced.isEmpty() || priced ? NO_PICKS_TEXT : WAITING_TEXT);
			return;
		}
		list.setSections(Arrays.asList(TOP_HEADINGS), sections, this::pickRow);
	}

	/** A left click on a Top picks row: back to the full List with the search cleared, that method's row marked. */
	private void pickRow(ListLine line)
	{
		settings.setView(VIEW_LIST);
		searchBox.setText("");
		syncControls();
		refresh();
		list.jumpTo(line.row.method);
	}

	/** Runs a settings write, then brings the controls and the list in line with it. */
	private void changed(Runnable write)
	{
		if (syncing)
		{
			return;
		}
		write.run();
		syncControls();
		refresh();
	}

	/** A sort box or row was chosen: the lit one reverses, another starts in its normal order. */
	private void pickSort(Sort picked)
	{
		if (healingMode())
		{
			final HealSort heal = HealSort.values()[picked.ordinal()];
			final boolean flip = heal == settings.healSort() && !settings.healSortReversed();
			changed(() ->
			{
				settings.setHealSort(heal);
				settings.setHealSortReversed(flip);
			});
			return;
		}
		final SortState next = new SortState(settings.sort(), settings.sortReversed()).pick(picked);
		changed(() ->
		{
			settings.setSort(next.sort);
			settings.setSortReversed(next.reversed);
		});
	}

	/** The target box was typed in: a whole number 0..99 is stored; anything else is left alone. */
	private void targetTyped()
	{
		if (syncing)
		{
			return;
		}
		try
		{
			final int level = Integer.parseInt(targetBox.getText().trim());
			if (level >= 0 && level <= MAX_LEVEL)
			{
				settings.setTargetLevel(level);
				targetCaption.setText(TargetCaption.of(player.xp((String) picker.getSelectedItem()), level));
			}
		}
		catch (NumberFormatException e)
		{
			// Choice: half-typed or blank text keeps the stored level.
		}
	}

	/** Sets the target box, the sort boxes and the menu's controls from the settings. */
	private void syncControls()
	{
		syncing = true;
		try
		{
			final String shown = Integer.toString(settings.targetLevel());
			if (!shown.equals(targetBox.getText().trim()))
			{
				targetBox.setText(shown);
			}
			paceBox.show(settings.pace());
			syncSortWords();
			for (PriceSource p : PriceSource.values())
			{
				if (priceRadios[p.ordinal()] != null)
				{
					priceRadios[p.ordinal()].setSelected(p == settings.priceSource());
				}
			}
			profitCheck.setSelected(settings.includeProfit());
			bandsCheck.setSelected(settings.otherBands());
			taxCheck.setSelected(settings.takeOffTax());
		}
		finally
		{
			syncing = false;
		}
	}

	/** Sets the sort boxes and the menu's sort rows from the settings, in the words of the list on show. */
	private void syncSortWords()
	{
		final boolean heal = healingMode();
		final int litIndex = heal ? settings.healSort().ordinal() : settings.sort().ordinal();
		final boolean reversed = heal ? settings.healSortReversed() : settings.sortReversed();
		final SortState state = new SortState(settings.sort(), settings.sortReversed());
		for (int i = 0; i < sortButtons.length; i++)
		{
			final boolean lit = i == litIndex;
			final String word = heal ? HealSort.values()[i].word : SORT_WORDS[i];
			final JToggleButton button = sortButtons[i];
			button.setText(word + (lit ? (reversed ? ARROW_UP : ARROW_DOWN) : ""));
			button.setSelected(lit);
			button.setBackground(lit ? ColorScheme.BRAND_ORANGE : ColorScheme.DARK_GRAY_COLOR);
			button.setForeground(lit ? Color.BLACK : Color.WHITE);
			if (sortRadios[i] != null)
			{
				final String normal = heal ? HealSort.values()[i].normal : SortState.normalLabel(Sort.values()[i]);
				final String back = heal ? HealSort.values()[i].reversed : state.label();
				sortRadios[i].setText(lit && reversed ? back : normal);
				sortRadios[i].setSelected(lit);
			}
		}
	}

	/** The settings icon: grey at rest, orange under the mouse; a press opens the menu under it. */
	private JLabel settingsIcon()
	{
		final JLabel[] self = new JLabel[1];
		self[0] = SupportLinks.namedIconButton(GearGlyph.icon(ICON_SIZE, ColorScheme.LIGHT_GRAY_COLOR),
			GearGlyph.icon(ICON_SIZE, ColorScheme.BRAND_ORANGE), SupportLinks.SETTINGS_TIP, () -> openMenu(self[0]));
		self[0].setBorder(SupportLinks.iconBorder());
		return self[0];
	}

	/** Opens the menu under {@code anchor}; the press that closed it a moment ago opens nothing, so the icon toggles. */
	private void openMenu(JLabel anchor)
	{
		if (!anchor.isShowing() || System.nanoTime() - closedAtNanos < REOPEN_GUARD_NANOS)
		{
			return;
		}
		menu.show(anchor, 0, anchor.getHeight());
	}

	/** Takes the menu down: OK, a link press, or the reset. */
	private void closeMenu()
	{
		menu.setVisible(false);
	}

	/** The settings menu: the header, a rule, the switches, a rule, the sort rows, a rule, then the OK row. */
	private JPopupMenu buildMenu()
	{
		final JPopupMenu popup = new JPopupMenu();
		popup.setBorder(new EmptyBorder(5, 5, 5, 5));
		popup.add(support.header());
		popup.addSeparator();
		popup.add(profitCheck);
		popup.add(bandsCheck);
		popup.add(taxCheck);
		popup.addSeparator();
		popup.add(label("Sort by", plainFont(), ColorScheme.LIGHT_GRAY_COLOR));
		final ButtonGroup group = new ButtonGroup();
		for (final Sort sort : Sort.values())
		{
			final JRadioButtonMenuItem item = radioItem(SortState.normalLabel(sort));
			item.addActionListener(e -> pickSort(sort));
			sortRadios[sort.ordinal()] = item;
			group.add(item);
			popup.add(item);
		}
		popup.addSeparator();
		popup.add(label("Prices", plainFont(), ColorScheme.LIGHT_GRAY_COLOR));
		final ButtonGroup priceGroup = new ButtonGroup();
		final String[] priceWords = {"Guide price", "Live trade prices"};
		for (final PriceSource source : PriceSource.values())
		{
			final JRadioButtonMenuItem item = radioItem(priceWords[source.ordinal()]);
			item.addActionListener(e -> changed(() -> settings.setPriceSource(source)));
			priceRadios[source.ordinal()] = item;
			priceGroup.add(item);
			popup.add(item);
		}
		popup.addSeparator();
		popup.add(okRow());
		popup.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				syncControls();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
				closedAtNanos = System.nanoTime();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
				closedAtNanos = System.nanoTime();
			}
		});
		return popup;
	}

	/** The menu's last row: "Reset to default" at its left, "OK" at its right. */
	private JPanel okRow()
	{
		final JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setOpaque(false);
		row.setBorder(new EmptyBorder(2, 6, 2, 6));
		final JButton reset = button(RESET_TEXT);
		reset.addActionListener(e ->
		{
			resetToDefault.run();
			syncControls();
			refresh();
		});
		final JButton ok = button(OK_TEXT);
		ok.addActionListener(e -> closeMenu());
		row.add(reset);
		row.add(Box.createHorizontalGlue());
		row.add(ok);
		return row;
	}

	/**
	 * A switch: a check in the menu's 12 px type that takes the mouse release over, so that a click never closes the
	 * menu (the player flips several in one visit).
	 */
	private static JCheckBoxMenuItem check(String text, Consumer<Boolean> write)
	{
		final JCheckBoxMenuItem check = new JCheckBoxMenuItem(text)
		{
			@Override
			protected void processMouseEvent(MouseEvent e)
			{
				if (!takeRelease(this, e))
				{
					super.processMouseEvent(e);
				}
			}
		};
		check.setFont(plainFont());
		check.setForeground(Color.WHITE);
		check.addActionListener(e -> write.accept(check.isSelected()));
		return check;
	}

	private static JRadioButtonMenuItem radioItem(String text)
	{
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(text)
		{
			@Override
			protected void processMouseEvent(MouseEvent e)
			{
				if (!takeRelease(this, e))
				{
					super.processMouseEvent(e);
				}
			}
		};
		item.setFont(plainFont());
		item.setForeground(Color.WHITE);
		return item;
	}

	/** Clicks the item on a left release inside it and consumes the event; true when it did. */
	private static boolean takeRelease(JMenuItem item, MouseEvent e)
	{
		if (e.getID() != MouseEvent.MOUSE_RELEASED || !SwingUtilities.isLeftMouseButton(e) || !item.contains(e.getPoint()))
		{
			return false;
		}
		item.doClick(0);
		item.getModel().setArmed(true);
		e.consume();
		return true;
	}

	private static JLabel label(String text, Font font, Color colour)
	{
		final JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(colour);
		return label;
	}

	private static Font plainFont()
	{
		return FontManager.getDefaultFont().deriveFont((float) SupportLinks.MENU_FONT_SIZE);
	}

	private static Font boldFont()
	{
		return FontManager.getDefaultBoldFont().deriveFont(14f);
	}

	/** A small button in the menu's 12 px type, no wider than its word. */
	private static JButton button(String text)
	{
		final JButton button = new JButton(text);
		button.setFont(plainFont());
		button.setFocusable(false);
		button.setMaximumSize(button.getPreferredSize());
		return button;
	}
}

package com.gpperxp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** The list of rows: rebuilt whole, as wide as its scroll pane, and it tells the {@link CardViewer} about the hovered row. */
final class ListView extends JPanel implements Scrollable
{
	private static final int ROW_GAP = 2;
	private static final int HEADING_GAP = 8;
	private static final int SCROLL_STEP = 16;
	private static final String[] HEAL_UNITS = {" gp/hp", " hp", " gp"};

	private final CardViewer viewer;
	private static final int MARK_MS = 2000;

	private final Consumer<String> browse;
	private final List<ListRow> methodRows = new ArrayList<>();
	private final List<TrainingMethod> rowMethods = new ArrayList<>();
	private Timer markTimer;
	private ListRow marked;

	ListView(CardViewer viewer, Consumer<String> browse)
	{
		this.viewer = viewer;
		this.browse = browse;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
	}

	/** Drops the remembered rows and any mark; every rebuild starts here. */
	private void forgetRows()
	{
		unmark();
		methodRows.clear();
		rowMethods.clear();
	}

	private void unmark()
	{
		if (markTimer != null)
		{
			markTimer.stop();
			markTimer = null;
		}
		if (marked != null)
		{
			marked.setMarked(false);
			marked = null;
		}
	}

	/**
	 * Marks the row of {@code method} for about two seconds (or until the mouse moves over the list) and scrolls it into
	 * view. Choice: the row is found by the method object, or else by skill, name and rate level, because a pace below
	 * 100 builds new method objects; no such row (a hidden band) does nothing. Call on the EDT.
	 */
	void jumpTo(TrainingMethod method)
	{
		unmark();
		for (int i = 0; i < methodRows.size(); i++)
		{
			final TrainingMethod m = rowMethods.get(i);
			if (m == method || (m.skill.equals(method.skill) && m.name.equals(method.name)
				&& m.rateLevel == method.rateLevel && m.level == method.level))
			{
				final ListRow row = methodRows.get(i);
				marked = row;
				row.setMarked(true);
				markTimer = new Timer(MARK_MS, e -> unmark());
				markTimer.setRepeats(false);
				markTimer.start();
				SwingUtilities.invokeLater(() ->
				{
					validate();
					scrollRectToVisible(row.getBounds());
				});
				return;
			}
		}
	}

	/** Replaces the rows with one per line. */
	void setLines(List<ListLine> lines)
	{
		viewer.hide();
		forgetRows();
		removeAll();
		for (ListLine line : lines)
		{
			final ListRow row = new ListRow(line);
			methodRows.add(row);
			rowMethods.add(line.row.method);
			addRow(row, rect -> viewer.show(line.row, rect), () -> RowMenu.build(line.row, browse), null);
		}
		revalidate();
		repaint();
	}

	/**
	 * Replaces the rows with headed sections of lines; a section with no lines is left out, and a left click on a row
	 * tells {@code onPick}.
	 */
	void setSections(List<String> headings, List<List<ListLine>> sections, Consumer<ListLine> onPick)
	{
		viewer.hide();
		forgetRows();
		removeAll();
		for (int i = 0; i < sections.size(); i++)
		{
			if (sections.get(i).isEmpty())
			{
				continue;
			}
			if (getComponentCount() > 0)
			{
				add(Box.createVerticalStrut(HEADING_GAP));
			}
			final JLabel heading = new JLabel(headings.get(i));
			heading.setFont(FontManager.getDefaultBoldFont().deriveFont(12f));
			heading.setForeground(ColorScheme.BRAND_ORANGE);
			final JPanel head = new JPanel(new BorderLayout());
			head.setOpaque(false);
			head.setBorder(new EmptyBorder(0, 0, ROW_GAP, 0));
			head.add(heading, BorderLayout.WEST);
			head.setMaximumSize(new Dimension(Integer.MAX_VALUE, head.getPreferredSize().height));
			add(head);
			boolean first = true;
			for (ListLine line : sections.get(i))
			{
				if (!first)
				{
					add(Box.createVerticalStrut(ROW_GAP));
				}
				first = false;
				addRow(new ListRow(line), rect -> viewer.show(line.row, rect), () -> RowMenu.build(line.row, browse),
					() -> onPick.accept(line));
			}
		}
		revalidate();
		repaint();
	}

	/** Replaces the rows with one per food or potion; the row's units are gp/hp, hp and gp. */
	void setHealLines(List<HealLine> lines)
	{
		viewer.hide();
		forgetRows();
		removeAll();
		for (HealLine heal : lines)
		{
			final ListLine line = new ListLine(null, heal.name, "", heal.gpHp, heal.hp, heal.price, Money.NEUTRAL, false,
				heal.unpriced);
			addRow(new ListRow(line, HEAL_UNITS), rect -> viewer.showHealing(heal.item, rect),
				() -> RowMenu.build(heal.item.source, browse), null);
		}
		revalidate();
		repaint();
	}

	/**
	 * Adds one row with its hover, its right-click menu and, when {@code click} is not null, its left click. A row
	 * with a click sits in a section, which spaces its own rows.
	 */
	private void addRow(ListRow row, Consumer<Rectangle> hover, Supplier<JPopupMenu> menu, Runnable click)
	{
		final MouseAdapter listener = new MouseAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				unmark();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				row.setHot(true);
				if (row.isShowing())
				{
					hover.accept(new Rectangle(row.getLocationOnScreen(), row.getSize()));
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				row.setHot(false);
				viewer.hide();
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				offerMenu(e, row, menu);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				offerMenu(e, row, menu);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (click != null && SwingUtilities.isLeftMouseButton(e))
				{
					click.run();
				}
			}
		};
		row.addMouseListener(listener);
		row.addMouseMotionListener(listener);
		if (click == null && getComponentCount() > 0)
		{
			add(Box.createVerticalStrut(ROW_GAP));
		}
		add(row);
	}

	/** Replaces the rows with one grey line of words. */
	void setMessage(String text)
	{
		viewer.hide();
		forgetRows();
		removeAll();
		final JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		add(label);
		revalidate();
		repaint();
	}

	/** A popup-trigger press or release on a row opens its menu at the mouse. */
	private void offerMenu(MouseEvent e, ListRow row, Supplier<JPopupMenu> menu)
	{
		if (e.isPopupTrigger())
		{
			menu.get().show(row, e.getX(), e.getY());
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return SCROLL_STEP;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return Math.max(SCROLL_STEP, visibleRect.height - SCROLL_STEP);
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
}

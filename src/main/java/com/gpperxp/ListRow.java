package com.gpperxp;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * One list row: the method's name and its level at the right, then three columns, gp/xp, xp/h and gp/h. A name that
 * does not fit its line goes on a second line (cut with "..." after that) and the row grows by one line.
 */
final class ListRow extends JPanel
{
	/** The green of a method that makes money (rise and fall are the only meanings of green). */
	static final Color PROFIT_GREEN = new Color(0, 200, 83);
	/** The red of a method that costs money. */
	static final Color SPEND_RED = new Color(224, 98, 88);
	private static final Color GROUND = ColorScheme.DARK_GRAY_COLOR;
	private static final Color GROUND_HOT = ColorScheme.DARK_GRAY_HOVER_COLOR;
	private static final Color GREYED = Color.GRAY;
	private static final int HEIGHT = 40;
	private static final int PAD = 6;
	private static final int FONT_SIZE = 12;
	private static final String GAP = "...";
	/** Choice: the width a row is measured at before it has a size (the sidebar's width). */
	private static final int DEFAULT_WIDTH = 225;

	/** The client property that holds the texts the row paints, after its name. */
	static final String TEXTS = "texts";

	private static final String[] TRAINING_UNITS = {" gp/xp", " xp/h", " gp/h"};

	private final ListLine line;
	private final String[] units;
	/** The client property that is TRUE while the row is marked by a jump (a test reads it back). */
	static final String MARKED = "marked";

	private boolean hot;
	private boolean marked;

	ListRow(ListLine line)
	{
		this(line, TRAINING_UNITS);
	}

	/** @param units the three columns' units, each with its leading space */
	ListRow(ListLine line, String[] units)
	{
		this.line = line;
		this.units = units;
		setOpaque(true);
		setFont(FontManager.getDefaultFont().deriveFont((float) FONT_SIZE));
		// The row's name and the texts it paints, kept on the component so that a test can read them back.
		setName(line.name);
		putClientProperty(TEXTS, (line.level.isEmpty() ? "" : line.level + ", ") + line.gpXp + units[0] + ", "
			+ line.xpH + units[1] + ", " + line.gpH + units[2] + (line.greyed ? ", above your level" : ""));
	}

	void setHot(boolean hot)
	{
		this.hot = hot;
		repaint();
	}

	/** Marks the row after a jump: it is painted with the hover background until unmarked. */
	void setMarked(boolean marked)
	{
		this.marked = marked;
		putClientProperty(MARKED, marked);
		repaint();
	}

	/**
	 * The width the row is measured at: the list's (its parent's), which is the width the row will be laid out at and is
	 * already new when the sidebar is resized; else its own, else the sidebar's. Choice: not its own first, because that is
	 * the old width while the layout asks for the new height.
	 */
	private int layoutWidth()
	{
		final Container parent = getParent();
		if (parent != null && parent.getWidth() > 0)
		{
			return parent.getWidth();
		}
		return getWidth() > 0 ? getWidth() : DEFAULT_WIDTH;
	}

	/** The name's lines at {@code width}. */
	private List<String> nameParts(FontMetrics fm, int width)
	{
		return splitName(line.name, fm, firstWidth(fm, width), width - 2 * PAD);
	}

	/** The row's height for its name's line count at {@code width}; a pure function of the width. */
	private int heightFor(int width)
	{
		final FontMetrics fm = getFontMetrics(getFont());
		return HEIGHT + (nameParts(fm, width).size() - 1) * fm.getHeight();
	}

	private int firstWidth(FontMetrics fm, int width)
	{
		return width - 2 * PAD - fm.stringWidth(line.level) - PAD;
	}

	/** The baseline of the name's last line at the row's width; the painter draws it there. */
	int lastNameBaseline()
	{
		final FontMetrics fm = getFontMetrics(getFont());
		return PAD + fm.getAscent() + (nameParts(fm, layoutWidth()).size() - 1) * fm.getHeight();
	}

	/**
	 * The baseline of the values line: the bottom of a one-line row, moved down by the name's extra lines, so it is
	 * below the name whatever height the row was given.
	 */
	int valuesBaseline()
	{
		final FontMetrics fm = getFontMetrics(getFont());
		return heightFor(layoutWidth()) - PAD - fm.getDescent();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(super.getPreferredSize().width, heightFor(layoutWidth()));
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, heightFor(layoutWidth()));
	}

	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		// The height asked for was measured at an earlier width; if it does not fit the name at this width, lay out again.
		if (width > 0 && height != heightFor(width))
		{
			revalidate();
		}
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		final Graphics2D g2 = (Graphics2D) g.create();
		try
		{
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(hot || marked ? GROUND_HOT : GROUND);
			g2.fillRect(0, 0, getWidth(), getHeight());
			final Font font = getFont();
			g2.setFont(font);
			final FontMetrics fm = g2.getFontMetrics();
			final int first = PAD + fm.getAscent();
			final int second = valuesBaseline();
			final List<String> parts = nameParts(fm, layoutWidth());
			final int firstName = lastNameBaseline() - (parts.size() - 1) * fm.getHeight();
			final int inner = getWidth() - 2 * PAD;

			final Color quiet = line.greyed ? GREYED : ColorScheme.LIGHT_GRAY_COLOR;
			g2.setColor(quiet);
			g2.drawString(line.level, getWidth() - PAD - fm.stringWidth(line.level), first);
			g2.setColor(line.greyed ? GREYED : Color.WHITE);
			for (int i = 0; i < parts.size(); i++)
			{
				g2.drawString(parts.get(i), PAD, firstName + i * fm.getHeight());
			}

			final int column = inner / 3;
			drawColumn(g2, line.gpXp, units[0], PAD, second, column, fm, true);
			drawColumn(g2, line.xpH, units[1], PAD + column, second, column, fm, false);
			drawColumn(g2, line.gpH, units[2], PAD + 2 * column, second, column, fm, true);
		}
		finally
		{
			g2.dispose();
		}
	}

	/**
	 * The name as one or two lines: the words that fit {@code firstWidth} on the first, the rest on the second, cut with
	 * "..." when it does not fit {@code fullWidth}. Choice: a first word wider than the first line is broken inside the
	 * word, so the first line is never empty.
	 */
	static List<String> splitName(String name, FontMetrics fm, int firstWidth, int fullWidth)
	{
		final List<String> parts = new ArrayList<>();
		if (fm.stringWidth(name) <= firstWidth)
		{
			parts.add(name);
			return parts;
		}
		int end = 0;
		int space = name.indexOf(' ');
		while (space > 0 && fm.stringWidth(name.substring(0, space)) <= firstWidth)
		{
			end = space;
			space = name.indexOf(' ', space + 1);
		}
		if (end == 0)
		{
			end = name.length();
			while (end > 1 && fm.stringWidth(name.substring(0, end)) > firstWidth)
			{
				end--;
			}
			parts.add(name.substring(0, end));
			parts.add(fit(name.substring(end), fm, fullWidth));
			return parts;
		}
		parts.add(name.substring(0, end));
		parts.add(fit(name.substring(end + 1), fm, fullWidth));
		return parts;
	}

	/** Choice: the unit follows the text only when both fit the column; "per run", "free", "-" and "?" never get one. */
	private void drawColumn(Graphics2D g2, String value, String unit, int x, int y, int width, FontMetrics fm,
		boolean gp)
	{
		final boolean bare = value.equals("per run") || value.equals("free") || value.equals("-") || value.equals("?");
		final String text = !bare && fm.stringWidth(value + unit) <= width - 6 ? value + unit : value;
		g2.setColor(line.greyed || line.unpriced ? GREYED : gp ? gpColour(line.money, value) : ColorScheme.LIGHT_GRAY_COLOR);
		g2.drawString(fit(text, fm, width - 2), x, y);
	}

	/**
	 * Choice: earning gp is green, spending red, and "free" and "-" the greyed-row grey; xp/h is never coloured.
	 * Shared with the card.
	 */
	static Color gpColour(Money money, String value)
	{
		if (value.equals("free") || value.equals("-"))
		{
			return GREYED;
		}
		return money == Money.EARN ? PROFIT_GREEN : money == Money.SPEND ? SPEND_RED : ColorScheme.LIGHT_GRAY_COLOR;
	}

	/** {@code text}, or its head and "..." when it is wider than {@code width}. */
	static String fit(String text, FontMetrics fm, int width)
	{
		if (fm.stringWidth(text) <= width)
		{
			return text;
		}
		int end = text.length();
		while (end > 0 && fm.stringWidth(text.substring(0, end) + GAP) > width)
		{
			end--;
		}
		return text.substring(0, end) + GAP;
	}
}

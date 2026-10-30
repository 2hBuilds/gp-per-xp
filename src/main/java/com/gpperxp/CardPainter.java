package com.gpperxp;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import net.runelite.client.ui.FontManager;

/** Paints a {@link CardModel} as the hover card: plain brown ground, 1 px border, grey labels, white values. */
// Choice: public so the local picture test (another package) can call it.
public final class CardPainter
{
	private static final Color GROUND = new Color(70, 61, 50);
	private static final Color BORDER = new Color(118, 104, 82);
	private static final Color LABEL = new Color(165, 165, 165);
	private static final int WIDTH = 230;
	private static final int PAD = 8;
	private static final int ICON_WIDTH = 24;
	private static final int ICON_GAP = 4;
	private static final int ITEM_LINE = 24;
	private static final int SECTION_GAP = 6;
	private static final int GAP_BETWEEN = 8;
	private static final int MIN_HEIGHT = 100;
	private static final int NOTE_LINES = 4;

	private CardPainter()
	{
	}

	public static BufferedImage paint(CardModel model, IntFunction<BufferedImage> icon)
	{
		// Measure on a scratch image, then draw on one of the right height.
		final BufferedImage scratch = new BufferedImage(WIDTH, 1, BufferedImage.TYPE_INT_ARGB);
		final int height = draw(scratch, model, icon, false);
		final BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_ARGB);
		draw(image, model, icon, true);
		return image;
	}

	/** Lays the card out and, when {@code real}, draws it; returns the height. */
	private static int draw(BufferedImage image, CardModel model, IntFunction<BufferedImage> icon, boolean real)
	{
		final Graphics2D g = image.createGraphics();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final Font plain = FontManager.getRunescapeSmallFont();
			final Font bold = FontManager.getRunescapeBoldFont();
			final int inner = WIDTH - 2 * PAD;
			if (real)
			{
				g.setColor(GROUND);
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
				g.setColor(BORDER);
				g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
			}
			g.setFont(plain);
			final FontMetrics fm = g.getFontMetrics();
			g.setFont(bold);
			final FontMetrics bm = g.getFontMetrics();
			int y = PAD;

			g.setFont(bold);
			for (String part : wrapAll(model.title, bm, inner))
			{
				y = line(g, part, y, bm, Color.WHITE, real);
			}
			g.setFont(plain);
			for (String part : wrapAll(model.subtitle, fm, inner))
			{
				y = line(g, part, y, fm, LABEL, real);
			}
			y += SECTION_GAP;

			for (String[] fact : model.facts)
			{
				final int baseline = y + fm.getAscent();
				text(g, fact[0], PAD, baseline, LABEL, real);
				// Choice: a fact whose label and value do not fit on one line puts the value on the next line, at the right.
				final boolean split = fm.stringWidth(fact[0]) + 8 + fm.stringWidth(fact[1]) > inner;
				if (split)
				{
					y += fm.getHeight();
				}
				text(g, fact[1], WIDTH - PAD - fm.stringWidth(fact[1]), y + fm.getAscent(), factColour(fact[0], fact[1]),
					real);
				y += fm.getHeight();
			}

			y = items(g, fm, model.usesHeading, model.uses, icon, y + SECTION_GAP, real);
			if (!model.makes.isEmpty())
			{
				y = items(g, fm, "Makes", model.makes, icon, y + SECTION_GAP, real);
				if (!model.makesNote.isEmpty())
				{
					y = line(g, model.makesNote, y, fm, LABEL, real);
				}
			}

			final CardTrip trip = model.trip;
			if (trip != null)
			{
				y += SECTION_GAP;
				g.setFont(bold);
				y = line(g, trip.heading, y, bm, Color.WHITE, real);
				g.setFont(plain);
				y = line(g, trip.xp, y, fm, Color.WHITE, real);
				y = line(g, trip.actions, y, fm, Color.WHITE, real);
				y = line(g, trip.cost, y, fm, tripColour(trip), real);
				y = line(g, trip.time, y, fm, Color.WHITE, real);
				if (!model.buy.isEmpty())
				{
					y = items(g, fm, "To buy", model.buy, icon, y + SECTION_GAP, real);
				}
				if (!model.get.isEmpty())
				{
					y = items(g, fm, "You get", model.get, icon, y + SECTION_GAP, real);
				}
			}
			if (!model.needs.isEmpty())
			{
				y += SECTION_GAP;
				for (String part : wrap("Needs: " + model.needs, fm, inner))
				{
					y = line(g, part, y, fm, LABEL, real);
				}
			}
			if (!model.youGet.isEmpty())
			{
				y += SECTION_GAP;
				for (String part : wrap("You get: " + model.youGet, fm, inner))
				{
					y = line(g, part, y, fm, LABEL, real);
				}
			}
			if (!model.note.isEmpty())
			{
				y += SECTION_GAP;
				for (String part : wrap(model.note, fm, inner))
				{
					y = line(g, part, y, fm, LABEL, real);
				}
			}
			if (!model.footer.isEmpty())
			{
				y += SECTION_GAP;
				y = line(g, model.footer, y, fm, LABEL, real);
			}
			// Choice: a card is never shorter than 100 px, so a bare card still reads as a card.
			return Math.max(MIN_HEIGHT, y + PAD);
		}
		finally
		{
			g.dispose();
		}
	}

	/**
	 * The text broken at spaces onto as many lines as it needs to fit {@code width}; never cut, never "...".
	 */
	// Choice: a single word wider than a line is split by characters so that nothing is lost.
	static List<String> wrapAll(String text, FontMetrics fm, int width)
	{
		final List<String> lines = new ArrayList<>();
		String current = "";
		for (String word : text.trim().split("\\s+"))
		{
			if (!current.isEmpty() && fm.stringWidth(current + " " + word) <= width)
			{
				current = current + " " + word;
				continue;
			}
			if (!current.isEmpty())
			{
				lines.add(current);
			}
			String rest = word;
			while (fm.stringWidth(rest) > width && rest.length() > 1)
			{
				int end = rest.length() - 1;
				while (end > 1 && fm.stringWidth(rest.substring(0, end)) > width)
				{
					end--;
				}
				lines.add(rest.substring(0, end));
				rest = rest.substring(end);
			}
			current = rest;
		}
		lines.add(current);
		return lines;
	}

	/** The text broken at spaces to fit {@code width}, at most {@link #NOTE_LINES} lines; the last ends "..." when cut. */
	// Choice: a word longer than a line is cut by ListRow.fit.
	private static List<String> wrap(String text, FontMetrics fm, int width)
	{
		final List<String> lines = new ArrayList<>();
		final String[] words = text.split("\\s+");
		StringBuilder current = new StringBuilder();
		int i = 0;
		while (i < words.length && lines.size() < NOTE_LINES)
		{
			final String tryText = current.length() == 0 ? words[i] : current + " " + words[i];
			if (current.length() == 0 || fm.stringWidth(tryText) <= width)
			{
				current = new StringBuilder(tryText);
				i++;
			}
			else
			{
				lines.add(ListRow.fit(current.toString(), fm, width));
				current = new StringBuilder();
			}
		}
		if (lines.size() < NOTE_LINES)
		{
			lines.add(ListRow.fit(current.toString(), fm, width));
		}
		else if (i < words.length)
		{
			final int last = NOTE_LINES - 1;
			lines.set(last, ListRow.fit(lines.get(last) + " " + String.join(" ", Arrays.asList(words).subList(i, words.length)),
				fm, width));
		}
		return lines;
	}

	/** One text line at the left margin starting at {@code y}; returns the y under it. */
	private static int line(Graphics2D g, String s, int y, FontMetrics fm, Color color, boolean real)
	{
		text(g, s, PAD, y + fm.getAscent(), color, real);
		return y + fm.getHeight();
	}

	private static void text(Graphics2D g, String s, int x, int baseline, Color color, boolean real)
	{
		if (real)
		{
			g.setColor(color);
			g.drawString(s, x, baseline);
		}
	}

	/** A grey heading and one line per item: picture, "qty x name", price at the right; returns the y under them. */
	private static int items(Graphics2D g, FontMetrics fm, String heading, List<CardItem> items,
		IntFunction<BufferedImage> icon, int y, boolean real)
	{
		int top = line(g, heading, y, fm, LABEL, real);
		final int textX = PAD + ICON_WIDTH + ICON_GAP;
		for (CardItem item : items)
		{
			final int baseline = top + (ITEM_LINE + fm.getAscent() - fm.getDescent()) / 2;
			if (real)
			{
				final BufferedImage picture = icon.apply(item.itemId);
				if (picture != null)
				{
					final int h = Math.max(1, picture.getHeight() * ICON_WIDTH / Math.max(1, picture.getWidth()));
					g.drawImage(picture, PAD, top + (ITEM_LINE - h) / 2, ICON_WIDTH, h, null);
				}
			}
			final int priceWidth = fm.stringWidth(item.price);
			text(g, item.price, WIDTH - PAD - priceWidth, baseline, Color.WHITE, real);
			text(g, ListRow.fit(item.text, fm, WIDTH - PAD - priceWidth - GAP_BETWEEN - textX), textX, baseline,
				Color.WHITE, real);
			top += ITEM_LINE;
		}
		return top;
	}

	/**
	 * The colour of a fact value. Choice: the gp facts of a method follow the list: "+" green, "-" red, "free" and
	 * "-" grey; the healing "GP per HP" and every other fact stay white.
	 */
	static Color factColour(String label, String value)
	{
		if (!label.startsWith("GP") || label.equals("GP per HP"))
		{
			return Color.WHITE;
		}
		final String figure = value.replaceFirst("^(about|at least|up to) ", "");
		final Color colour = ListRow.gpColour(Money.of(figure), value);
		return colour == ListRow.PROFIT_GREEN || colour == ListRow.SPEND_RED || value.equals("free") || value.equals("-")
			? colour : Color.WHITE;
	}

	/** The colour of the trip cost line: green for a gain, red for a cost, grey for "free", white otherwise. */
	static Color tripColour(CardTrip trip)
	{
		return trip.cost.equals("free") ? ListRow.gpColour(Money.NEUTRAL, "free")
			: trip.money == Money.NEUTRAL ? Color.WHITE : ListRow.gpColour(trip.money, trip.cost);
	}
}

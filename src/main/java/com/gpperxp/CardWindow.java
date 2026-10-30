package com.gpperxp;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import net.runelite.client.util.AsyncBufferedImage;

/** The small window beside the sidebar that shows a row's card; never takes the keyboard. Event thread only. */
final class CardWindow implements CardViewer
{
	private static final int GAP = 8;

	private final Supplier<Window> owner;
	private final Function<Row, CardContext> context;
	private final Supplier<HealContext> healContext;
	/** Item ids already waiting for their picture, for the row showing. */
	private final Set<Integer> waiting = new HashSet<>();
	private JWindow window;
	private JLabel label;
	private Row showing;
	private HealingItem showingItem;
	private Rectangle showingRect;

	CardWindow(Supplier<Window> owner, Function<Row, CardContext> context)
	{
		this(owner, context, null);
	}

	CardWindow(Supplier<Window> owner, Function<Row, CardContext> context, Supplier<HealContext> healContext)
	{
		this.owner = owner;
		this.context = context;
		this.healContext = healContext;
	}

	@Override
	public void show(Row row, Rectangle rowOnScreen)
	{
		waiting.clear();
		showing = row;
		showingItem = null;
		showingRect = rowOnScreen;
		render();
	}

	@Override
	public void showHealing(HealingItem item, Rectangle rowOnScreen)
	{
		if (healContext == null)
		{
			return;
		}
		waiting.clear();
		showing = null;
		showingItem = item;
		showingRect = rowOnScreen;
		render();
	}

	@Override
	public void hide()
	{
		showing = null;
		showingItem = null;
		showingRect = null;
		if (window != null)
		{
			window.setVisible(false);
		}
	}

	void dispose()
	{
		hide();
		if (window != null)
		{
			window.dispose();
			window = null;
			label = null;
		}
	}

	private void render()
	{
		final Row row = showing;
		final HealingItem item = showingItem;
		final Rectangle rect = showingRect;
		if ((row == null && item == null) || rect == null)
		{
			return;
		}
		final BufferedImage image;
		if (row != null)
		{
			final CardContext ctx = context.apply(row);
			final CardModel paced = CardModel.of(row, ctx.price, ctx.tax, ctx.currentXp, ctx.targetLevel)
				.withPace(ctx.pace);
			final CardModel model = ctx.dataDate == null ? paced : paced.withFooter("Wiki data of " + ctx.dataDate + "; prices: " + ctx.priceSource);
			image = CardPainter.paint(model, id -> pictureOf(ctx.icon, id));
		}
		else
		{
			final HealContext ctx = healContext.get();
			final String footer = ctx.dataDate == null ? "" : "Wiki data of " + ctx.dataDate + "; prices: " + ctx.priceSource;
			image = CardPainter.paint(HealCard.of(item, ctx.price, ctx.maxHp).toModel(footer), id -> pictureOf(ctx.icon, id));
		}
		if (window == null)
		{
			final Window parent = owner.get();
			window = parent == null ? new JWindow() : new JWindow(parent);
			window.setFocusableWindowState(false);
			label = new JLabel();
			window.getContentPane().add(label);
		}
		final Dimension size = new Dimension(image.getWidth(), image.getHeight());
		label.setIcon(new ImageIcon(image));
		window.setSize(size);
		window.setLocation(CardPlace.besideRow(rect, size, screenOf(rect), GAP));
		window.setVisible(true);
	}

	/** The item's picture; when it is still loading, draws the card again once it has, if the row is still showing. */
	private BufferedImage pictureOf(IntFunction<BufferedImage> icon, int id)
	{
		final BufferedImage picture = icon.apply(id);
		if (picture instanceof AsyncBufferedImage && waiting.add(id))
		{
			final Row row = showing;
			final HealingItem item = showingItem;
			((AsyncBufferedImage) picture).onLoaded(() -> SwingUtilities.invokeLater(() ->
			{
				if (showing == row && showingItem == item)
				{
					render();
				}
			}));
		}
		return picture;
	}

	/** The usable area (the screen less its task bar and the like) of the screen the middle of {@code row} is on. */
	private static Rectangle screenOf(Rectangle row)
	{
		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration found = environment.getDefaultScreenDevice().getDefaultConfiguration();
		for (final GraphicsDevice device : environment.getScreenDevices())
		{
			final GraphicsConfiguration candidate = device.getDefaultConfiguration();
			if (candidate.getBounds().contains(row.x + row.width / 2, row.y + row.height / 2))
			{
				found = candidate;
				break;
			}
		}
		final Rectangle bounds = found.getBounds();
		final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(found);
		return new Rectangle(bounds.x + insets.left, bounds.y + insets.top,
			bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
	}
}

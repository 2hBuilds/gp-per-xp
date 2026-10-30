package com.gpperxp;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;

/**
 * A text box that paints a grey hint while empty, tells one listener when its text changes, and, while it holds text,
 * paints a small "x" at its right end. Choice: a click on the right end of the box, or Escape, clears it; the "x" is
 * painted, not a child component, so the box stays one text field.
 */
final class SearchBox extends JTextField
{
	private static final String HINT = "Search";
	private static final String CLEAR = "x";
	private static final int CLEAR_WIDTH = 20;
	private static final String CLEAR_KEY = "clear-search";

	SearchBox(Runnable onChange)
	{
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLEAR_KEY);
		getActionMap().put(CLEAR_KEY, new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setText("");
			}
		});
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!getText().isEmpty() && e.getX() >= getWidth() - CLEAR_WIDTH)
				{
					setText("");
				}
			}
		});
		getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				onChange.run();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (getText().isEmpty())
		{
			g.setColor(ColorScheme.MEDIUM_GRAY_COLOR);
			g.setFont(getFont());
			g.drawString(HINT, getInsets().left, (getHeight() + g.getFontMetrics().getAscent()
				- g.getFontMetrics().getDescent()) / 2);
		}
		else
		{
			g.setColor(ColorScheme.LIGHT_GRAY_COLOR);
			g.setFont(getFont());
			final int width = g.getFontMetrics().stringWidth(CLEAR);
			g.drawString(CLEAR, getWidth() - CLEAR_WIDTH / 2 - width / 2, (getHeight() + g.getFontMetrics().getAscent()
				- g.getFontMetrics().getDescent()) / 2);
		}
	}
}

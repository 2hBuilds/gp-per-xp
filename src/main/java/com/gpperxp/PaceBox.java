package com.gpperxp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;

/** The "My speed" row: a number box and its caption; tells one listener each whole number from 10 to 100 typed. */
final class PaceBox extends JPanel
{
	/** What hovering the label, the box or the caption says. */
	static final String TIP = "Lower it if you play slower than the wiki's rates. "
		+ "xp/h, gp/h and time to target change with it.";

	private final JTextField box = new JTextField(3);
	private final IntConsumer onValid;
	private boolean setting;

	PaceBox(Font font, int gap, IntConsumer onValid)
	{
		this.onValid = onValid;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		final JLabel name = label("My speed", font, Color.WHITE);
		name.setToolTipText(TIP);
		add(name);
		add(Box.createHorizontalStrut(gap));
		box.setFont(font);
		box.setMaximumSize(box.getPreferredSize());
		box.setToolTipText(TIP);
		box.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				typed();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				typed();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				typed();
			}
		});
		add(box);
		add(Box.createHorizontalStrut(gap));
		final JLabel caption = label("% of wiki", font, ColorScheme.LIGHT_GRAY_COLOR);
		caption.setToolTipText(TIP);
		add(caption);
		add(Box.createHorizontalGlue());
	}

	/** Shows {@code percent} without telling the listener. */
	void show(int percent)
	{
		final String shown = Integer.toString(percent);
		if (!shown.equals(box.getText().trim()))
		{
			setting = true;
			try
			{
				box.setText(shown);
			}
			finally
			{
				setting = false;
			}
		}
	}

	private void typed()
	{
		if (setting)
		{
			return;
		}
		try
		{
			final int percent = Integer.parseInt(box.getText().trim());
			if (Pace.clamp(percent) == percent)
			{
				onValid.accept(percent);
			}
		}
		catch (NumberFormatException e)
		{
			// Choice: half-typed or blank text keeps the stored pace.
		}
	}

	private static JLabel label(String text, Font font, Color colour)
	{
		final JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(colour);
		return label;
	}
}

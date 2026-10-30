package com.gpperxp;

import java.awt.Rectangle;

/** What a hovered list row asks for: its card shown beside the sidebar, and taken down when the mouse leaves. */
public interface CardViewer
{
	/** Shows the card of {@code row}, level with the row whose bounds on screen are {@code rowOnScreen}. */
	void show(Row row, Rectangle rowOnScreen);

	/** Shows the card of a food or potion, level with the row at {@code rowOnScreen}; nothing by default. */
	default void showHealing(HealingItem item, Rectangle rowOnScreen)
	{
	}

	/** Takes the card down. */
	void hide();
}

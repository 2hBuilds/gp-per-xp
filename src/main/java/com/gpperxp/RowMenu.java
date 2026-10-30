package com.gpperxp;

import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/** The menu a right-click on a list row opens: one item, the method's wiki page. */
final class RowMenu
{
	static final String OPEN_WIKI_TEXT = "Open wiki page";

	private RowMenu()
	{
	}

	/** @param browse takes the method's source address */
	static JPopupMenu build(Row row, Consumer<String> browse)
	{
		return build(row.method.source, browse);
	}

	/** The same menu for any address, the healing list's items too. */
	static JPopupMenu build(String source, Consumer<String> browse)
	{
		final JPopupMenu popup = new JPopupMenu();
		final JMenuItem item = new JMenuItem(OPEN_WIKI_TEXT);
		item.addActionListener(e -> browse.accept(source));
		popup.add(item);
		return popup;
	}
}

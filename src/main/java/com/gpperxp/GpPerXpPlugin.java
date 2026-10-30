package com.gpperxp;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.LinkBrowser;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2h XP and GP: the plugin. It adds the sidebar button and its panel at start-up and takes them away at shut-down,
 * keeps the player's xp for the panel, and opens the panel on a skill from the Skills tab.
 */
@PluginDescriptor(
	name = "2h XP and GP",
	internalName = "gp-per-xp",
	description = "Right-click a skill to see the cheapest, fastest and most profitable ways to train it: gp/xp, xp/h, gp/h and cost to your goal. (v" + Version.CURRENT + ")",
	tags = {"xp", "gp", "cost", "training", "skills", "calculator", "planner", "profit", "money", "rates", "xp/h", "gp/h", "efficiency", "cheapest", "fastest", "99", "level", "goal", "prices", "ge", "wiki", "healing", "food"}
)
public class GpPerXpPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(GpPerXpPlugin.class);

	/** The sidebar button's place. */
	private static final int NAV_PRIORITY = 7;

	/** Game ticks between price reads (100 ticks is about a minute). */
	private static final int PRICE_TICKS = 100;

	/** The least time between two panel refreshes caused by xp drops. */
	private static final int REFRESH_DELAY_MS = 750;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private GpPerXpConfig config;

	@Inject
	private OkHttpClient okHttpClient;

	/** The player's xp per skill; read on the event thread, written on the client thread. */
	private final Map<Skill, Integer> xp = new EnumMap<>(Skill.class);

	/** Quest name to done, for the quests the methods ask for; written on the client thread. */
	private final Map<String, Boolean> quests = new ConcurrentHashMap<>();
	/** The names of the quests the methods ask for. */
	private Set<String> questNames = new HashSet<>();
	/** True from login until the quests have been read once. */
	private boolean questsPending;

	private NavigationButton navButton;
	private GpPerXpPanel panel;
	private Methods methods;
	private Healing healing = Healing.empty();
	private CardWindow cardWindow;
	private PriceBook priceBook;
	private TradeFeed tradeFeed;
	private HierarchyListener showingListener;
	private Set<Integer> priceIds;
	private final RefreshGate gate = new RefreshGate();
	private Timer refreshTimer;
	private int ticks;

	@Provides
	GpPerXpConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GpPerXpConfig.class);
	}

	@Override
	protected void startUp()
	{
		methods = Methods.loadBundled(gson);
		// Choice: the item manager answers only on the client thread, so prices are read there into a book.
		final PriceBook book = new PriceBook(itemManager::getItemPrice);
		book.setUseTrades(config.priceSource() == PriceSource.TRADES);
		priceBook = book;
		tradeFeed = new TradeFeed(okHttpClient, gson);
		healing = Healing.loadBundled(gson);
		priceIds = PriceBook.idsOf(methods);
		for (HealingItem h : healing.all())
		{
			priceIds.add(h.itemId);
		}
		questNames = new HashSet<>();
		for (TrainingMethod m : methods.all())
		{
			for (Gate g : m.requires)
			{
				if (g.quest != null)
				{
					questNames.add(g.quest);
				}
			}
		}
		cardWindow = new CardWindow(() -> panel == null ? null : SwingUtilities.getWindowAncestor(panel),
			row -> new CardContext(book, config.takeOffTax(), xpOf(row.method.skill),
				config.targetLevel(), id -> itemManager.getImage(id), Pace.clamp(config.pace()), methods.updated(),
				config.priceSource() == PriceSource.TRADES ? "live trades" : "guide"),
			() -> new HealContext(book, id -> itemManager.getImage(id), maxHp(), healing.updated(),
				config.priceSource() == PriceSource.TRADES ? "live trades" : "guide"));
		panel = new GpPerXpPanel(this::resetSettings, methods, healing, this::maxHp, book, new ConfigSettings(),
			new SidebarPlayer(), cardWindow, LinkBrowser::browse);
		navButton = NavigationButton.builder()
			.tooltip("2h XP and GP")
			.icon(NavIcon.create())
			.priority(NAV_PRIORITY)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		showingListener = e ->
		{
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && panel != null && panel.isShowing())
			{
				fetchTrades(false);
			}
		};
		panel.addHierarchyListener(showingListener);
		refreshPrices();
		fetchTrades(false);
		log.info("2h XP and GP {} started on RuneLite {}; {} methods, table of {}", Version.CURRENT,
			RuneLiteProperties.getVersion(), methods.all().size(), methods.updated());
	}

	@Override
	protected void shutDown()
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		if (panel != null && showingListener != null)
		{
			panel.removeHierarchyListener(showingListener);
		}
		showingListener = null;
		panel = null;
		tradeFeed = null;
		if (refreshTimer != null)
		{
			refreshTimer.stop();
			refreshTimer = null;
		}
		gate.done();
		if (cardWindow != null)
		{
			cardWindow.dispose();
			cardWindow = null;
		}
		methods = null;
		healing = Healing.empty();
		priceBook = null;
		priceIds = null;
		questNames = new HashSet<>();
		quests.clear();
		synchronized (xp)
		{
			xp.clear();
		}
	}

	/** Puts the settings back to their defaults. */
	private void resetSettings()
	{
		for (String key : new String[] {"targetLevel", "includeProfit", "showAbove", "otherBands", "takeOffTax", "sort", "sortReversed", "skill", "pace", "priceSource", "view", "healSort", "healSortReversed"})
		{
			configManager.unsetConfiguration(GpPerXpConfig.GROUP, key);
		}
		if (priceBook != null)
		{
			priceBook.setUseTrades(false);
		}
	}

	/** The player's Hitpoints level, 99 when it is not known (logged out). */
	private int maxHp()
	{
		final int xpNow = xpOf("Hitpoints");
		return xpNow < 0 ? 99 : Xp.levelFor(xpNow);
	}

	/** The player's xp in the skill, or -1 when unknown. */
	private int xpOf(String skillName)
	{
		synchronized (xp)
		{
			for (Map.Entry<Skill, Integer> entry : xp.entrySet())
			{
				if (entry.getKey().getName().equals(skillName))
				{
					return entry.getValue();
				}
			}
		}
		return -1;
	}

	/** Reads every skill's xp on login; forgets them on the login screen and while hopping. */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		final GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			synchronized (xp)
			{
				for (Skill skill : Skill.values())
				{
					if (skill != Skill.OVERALL)
					{
						xp.put(skill, client.getSkillExperience(skill));
					}
				}
			}
			questsPending = true;
			refreshPanel();
			refreshPrices();
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			synchronized (xp)
			{
				xp.clear();
			}
			questsPending = false;
			quests.clear();
			refreshPanel();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		synchronized (xp)
		{
			xp.put(event.getSkill(), event.getXp());
		}
		refreshPanel();
	}

	/** About once a minute the prices are read again; the panel redraws only when one changed. */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		final boolean priceTick = ++ticks % PRICE_TICKS == 0;
		if (questsPending || priceTick)
		{
			questsPending = false;
			readQuests();
		}
		if (priceTick)
		{
			refreshPrices();
			if (panel != null && panel.isShowing())
			{
				fetchTrades(false);
			}
		}
	}

	/** Reads the state of the quests the methods ask for; client thread only. Redraws the panel when one changed. */
	private void readQuests()
	{
		if (questNames.isEmpty())
		{
			return;
		}
		boolean changed = false;
		for (Quest quest : Quest.values())
		{
			if (!questNames.contains(quest.getName()))
			{
				continue;
			}
			final Boolean done = quest.getState(client) == QuestState.FINISHED;
			if (!done.equals(quests.put(quest.getName(), done)))
			{
				changed = true;
			}
		}
		if (changed)
		{
			refreshPanel();
		}
	}

	/** The panel's view of the player: the xp per skill, and the quests read on the client thread. */
	private final class SidebarPlayer implements Player
	{
		@Override
		public int xp(String skillName)
		{
			return xpOf(skillName);
		}

		@Override
		public boolean questDone(String quest)
		{
			final Boolean done = quests.get(quest);
			return done == null || done;
		}
	}

	/** Reads the wiki's trade prices when the player chose them; at most every five minutes unless forced. */
	private void fetchTrades(boolean force)
	{
		if (config.priceSource() != PriceSource.TRADES)
		{
			return;
		}
		final TradeFeed feed = tradeFeed;
		final PriceBook book = priceBook;
		final Set<Integer> ids = priceIds;
		if (feed == null || book == null || ids == null)
		{
			return;
		}
		final long now = System.currentTimeMillis();
		if (!force && !feed.due(now))
		{
			return;
		}
		feed.fetch(now, ids, prints ->
		{
			book.setPrints(prints);
			refreshPanel();
		});
	}

	/** Reads the prices on the client thread and redraws the panel when one changed. */
	private void refreshPrices()
	{
		final PriceBook book = priceBook;
		final Set<Integer> ids = priceIds;
		if (book == null || ids == null)
		{
			return;
		}
		clientThread.invoke(() ->
		{
			if (book.refresh(ids))
			{
				refreshPanel();
			}
		});
	}

	/** Asks for a panel redraw: at most one per {@link #REFRESH_DELAY_MS}, and none while the panel is closed. */
	private void refreshPanel()
	{
		if (!gate.request())
		{
			return;
		}
		SwingUtilities.invokeLater(() ->
		{
			if (panel == null)
			{
				gate.done();
				return;
			}
			final Timer timer = new Timer(REFRESH_DELAY_MS, e ->
			{
				gate.done();
				if (panel != null && panel.isShowing())
				{
					panel.refresh();
				}
			});
			timer.setRepeats(false);
			refreshTimer = timer;
			timer.start();
		});
	}

	/** The panel's settings, backed by the config. */
	private final class ConfigSettings implements PanelSettings
	{
		@Override
		public int targetLevel()
		{
			return config.targetLevel();
		}

		@Override
		public void setTargetLevel(int level)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "targetLevel", level);
		}

		@Override
		public boolean includeProfit()
		{
			return config.includeProfit();
		}

		@Override
		public void setIncludeProfit(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "includeProfit", on);
		}

		@Override
		public boolean showAbove()
		{
			return config.showAbove();
		}

		@Override
		public void setShowAbove(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "showAbove", on);
		}

		@Override
		public boolean otherBands()
		{
			return config.otherBands();
		}

		@Override
		public void setOtherBands(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "otherBands", on);
		}

		@Override
		public boolean takeOffTax()
		{
			return config.takeOffTax();
		}

		@Override
		public void setTakeOffTax(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "takeOffTax", on);
		}

		@Override
		public Sort sort()
		{
			return config.sort();
		}

		@Override
		public void setSort(Sort sort)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "sort", sort);
		}

		@Override
		public boolean sortReversed()
		{
			return config.sortReversed();
		}

		@Override
		public void setSortReversed(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "sortReversed", on);
		}

		@Override
		public int pace()
		{
			return Pace.clamp(config.pace());
		}

		@Override
		public void setPace(int percent)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "pace", Pace.clamp(percent));
		}

		@Override
		public PriceSource priceSource()
		{
			return config.priceSource();
		}

		@Override
		public void setPriceSource(PriceSource source)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "priceSource", source);
			final PriceBook book = priceBook;
			if (book != null)
			{
				book.setUseTrades(source == PriceSource.TRADES);
			}
			fetchTrades(true);
		}

		@Override
		public String view()
		{
			return config.view();
		}

		@Override
		public void setView(String view)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "view", view);
		}

		@Override
		public HealSort healSort()
		{
			return config.healSort();
		}

		@Override
		public void setHealSort(HealSort sort)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "healSort", sort);
		}

		@Override
		public boolean healSortReversed()
		{
			return config.healSortReversed();
		}

		@Override
		public void setHealSortReversed(boolean on)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "healSortReversed", on);
		}

		@Override
		public String skill()
		{
			return config.skill();
		}

		@Override
		public void setSkill(String skill)
		{
			configManager.setConfiguration(GpPerXpConfig.GROUP, "skill", skill);
		}
	}

	/** Adds "GP/XP <skill>" after the game's entries on a Skills tab skill, as the Wiki plugin does (Choice). */
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (methods == null || event.getType() != MenuAction.CC_OP.getId()
			|| WidgetUtil.componentToInterface(event.getActionParam1()) != InterfaceID.STATS)
		{
			return;
		}
		final Skill skill = SkillMenu.skillOf(event.getOption());
		if (skill == null || methods.forSkill(skill.getName()).isEmpty())
		{
			return;
		}
		client.getMenu().createMenuEntry(-1)
			.setOption("GP/XP")
			.setTarget(skill.getName())
			.setType(MenuAction.RUNELITE)
			.onClick(e -> open(skill));
	}

	/** Shows the skill in the panel and opens the sidebar on it. */
	private void open(Skill skill)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null && navButton != null)
			{
				panel.showSkill(skill.getName());
				clientToolbar.openPanel(navButton);
			}
		});
	}
}

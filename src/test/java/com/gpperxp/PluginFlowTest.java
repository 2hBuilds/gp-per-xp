package com.gpperxp;

import com.google.gson.Gson;
import com.google.inject.Guice;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The real plugin class, started with mocked RuneLite services. The item manager mock throws, as RuneLite's does, when
 * it is asked for a price anywhere but the client thread, so a start-up that reads prices on the Swing thread fails here.
 */
public class PluginFlowTest
{
	private static final String CLIENT_THREAD = "client-thread-fake";

	private ExecutorService clientExecutor;
	private volatile Thread clientThreadObject;
	private final AtomicReference<Throwable> priceFailure = new AtomicReference<>();

	private ClientToolbar clientToolbar;
	private Client client;
	private ItemManager itemManager;
	private Menu menu;
	private MenuEntry entry;
	private GpPerXpPlugin plugin;
	private OkHttpClient http;
	private GpPerXpConfig config;

	@Before
	public void setUp() throws Exception
	{
		clientExecutor = Executors.newSingleThreadExecutor(r ->
		{
			final Thread thread = new Thread(r, CLIENT_THREAD);
			thread.setDaemon(true);
			clientThreadObject = thread;
			return thread;
		});

		clientToolbar = Mockito.mock(ClientToolbar.class);
		client = Mockito.mock(Client.class);
		menu = Mockito.mock(Menu.class);
		entry = Mockito.mock(MenuEntry.class, Mockito.RETURNS_SELF);
		when(client.getMenu()).thenReturn(menu);
		when(client.getIntStack()).thenReturn(new int[] {2});
		when(menu.createMenuEntry(anyInt())).thenReturn(entry);

		final ClientThread clientThread = Mockito.mock(ClientThread.class);
		Mockito.doAnswer(call ->
		{
			runOnClientThread(call.getArgument(0));
			return null;
		}).when(clientThread).invoke(any(Runnable.class));
		Mockito.doAnswer(call ->
		{
			runOnClientThread(call.getArgument(0));
			return null;
		}).when(clientThread).invokeLater(any(Runnable.class));

		itemManager = Mockito.mock(ItemManager.class);
		when(itemManager.getItemPrice(anyInt())).thenAnswer(call ->
		{
			if (SwingUtilities.isEventDispatchThread() || Thread.currentThread() != clientThreadObject)
			{
				final AssertionError error = new AssertionError("must be called on client thread");
				priceFailure.compareAndSet(null, error);
				throw error;
			}
			return 100L;
		});

		final ConfigManager configManager = Mockito.mock(ConfigManager.class);
		config = Mockito.mock(GpPerXpConfig.class, Mockito.CALLS_REAL_METHODS);
		final Gson gson = new Gson();
		http = Mockito.mock(OkHttpClient.class);
		when(http.newCall(any(Request.class))).thenReturn(Mockito.mock(Call.class));

		plugin = new GpPerXpPlugin();
		Guice.createInjector(binder ->
		{
			binder.bind(ClientToolbar.class).toInstance(clientToolbar);
			binder.bind(ConfigManager.class).toInstance(configManager);
			binder.bind(Gson.class).toInstance(gson);
			binder.bind(Client.class).toInstance(client);
			binder.bind(ClientThread.class).toInstance(clientThread);
			binder.bind(ItemManager.class).toInstance(itemManager);
			binder.bind(GpPerXpConfig.class).toInstance(config);
			binder.bind(OkHttpClient.class).toInstance(http);
		}).injectMembers(plugin);
	}

	@After
	public void tearDown() throws Exception
	{
		clientExecutor.shutdownNow();
		clientExecutor.awaitTermination(5, TimeUnit.SECONDS);
	}

	private void runOnClientThread(Runnable task) throws Exception
	{
		clientExecutor.submit(task).get(30, TimeUnit.SECONDS);
	}

	private void drainClientThread() throws Exception
	{
		runOnClientThread(() ->
		{
		});
	}

	private void drainEventThread() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
		});
	}

	private void startOnTheEventThread() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				plugin.startUp();
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		});
	}

	private MenuEntryAdded added(int type, int param1, String option)
	{
		final MenuEntryAdded event = Mockito.mock(MenuEntryAdded.class);
		when(event.getType()).thenReturn(type);
		when(event.getActionParam1()).thenReturn(param1);
		when(event.getOption()).thenReturn(option);
		return event;
	}

	@Test
	public void startUpOnTheEventThreadDoesNotCrash() throws Exception
	{
		startOnTheEventThread();
		verify(clientToolbar, times(1)).addNavigation(any(NavigationButton.class));

		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				plugin.shutDown();
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		});
		verify(clientToolbar, times(1)).removeNavigation(any(NavigationButton.class));
	}

	@Test
	public void noNetworkWithTheGuidePrice() throws Exception
	{
		startOnTheEventThread();
		for (int i = 0; i < 120; i++)
		{
			plugin.onGameTick(new GameTick());
		}
		drainClientThread();
		drainEventThread();
		verify(http, never()).newCall(any(Request.class));
	}

	@Test
	public void liveSourceFetchesTheFixedUrl() throws Exception
	{
		Mockito.doReturn(PriceSource.TRADES).when(config).priceSource();
		startOnTheEventThread();
		drainClientThread();
		drainEventThread();
		final ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
		verify(http, times(1)).newCall(request.capture());
		org.junit.Assert.assertEquals(TradeFeed.URL, request.getValue().url().toString());
	}

	@Test
	public void pricesAreReadOnTheClientThread() throws Exception
	{
		startOnTheEventThread();
		drainClientThread();
		drainEventThread();
		verify(itemManager, atLeastOnce()).getItemPrice(anyInt());
		assertNull("a price was read off the client thread", priceFailure.get());
	}

	@Test
	public void menuEntryAddedForASkillWithRows() throws Exception
	{
		startOnTheEventThread();
		plugin.onMenuEntryAdded(added(MenuAction.CC_OP.getId(), InterfaceID.Stats.PRAYER, "View Prayer guide"));

		verify(menu, times(1)).createMenuEntry(-1);
		verify(entry).setOption("GP/XP");
		verify(entry).setTarget("Prayer");
		verify(entry).setType(MenuAction.RUNELITE);
	}

	@Test
	public void menuEntryClickOpensThePanel() throws Exception
	{
		startOnTheEventThread();
		final ArgumentCaptor<NavigationButton> button = ArgumentCaptor.forClass(NavigationButton.class);
		verify(clientToolbar).addNavigation(button.capture());

		plugin.onMenuEntryAdded(added(MenuAction.CC_OP.getId(), InterfaceID.Stats.PRAYER, "View Prayer guide"));
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Consumer<MenuEntry>> click = ArgumentCaptor.forClass(Consumer.class);
		verify(entry).onClick(click.capture());
		click.getValue().accept(entry);
		drainEventThread();

		verify(clientToolbar).openPanel(button.getValue());
	}

	@Test
	public void noMenuEntryForOtherSkillsOrOptions() throws Exception
	{
		startOnTheEventThread();
		plugin.onMenuEntryAdded(added(MenuAction.CC_OP.getId(), InterfaceID.Stats.ATTACK, "View Nothing guide"));
		plugin.onMenuEntryAdded(added(MenuAction.CC_OP.getId(), InterfaceID.Stats.PRAYER, "Examine"));
		plugin.onMenuEntryAdded(added(MenuAction.WALK.getId(), InterfaceID.Stats.PRAYER, "View Prayer guide"));
		plugin.onMenuEntryAdded(added(MenuAction.CC_OP.getId(), InterfaceID.INVENTORY << 16, "View Prayer guide"));

		verify(menu, never()).createMenuEntry(anyInt());
	}

	@Test
	public void loginReadsXpAndLogoutForgets() throws Exception
	{
		when(client.getSkillExperience(Skill.PRAYER)).thenReturn(5_346_332);
		startOnTheEventThread();

		final GameStateChanged login = new GameStateChanged();
		login.setGameState(GameState.LOGGED_IN);
		plugin.onGameStateChanged(login);
		verify(client, atLeastOnce()).getSkillExperience(Skill.PRAYER);
		drainEventThread();

		final GameStateChanged logout = new GameStateChanged();
		logout.setGameState(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(logout);
		drainClientThread();
		drainEventThread();
		assertNull("a price was read off the client thread", priceFailure.get());
	}

	@Test
	public void priceTickRefreshesOnTheClientThread() throws Exception
	{
		startOnTheEventThread();
		drainClientThread();
		final int before = Mockito.mockingDetails(itemManager).getInvocations().size();

		for (int i = 0; i < 100; i++)
		{
			plugin.onGameTick(new GameTick());
		}
		drainClientThread();

		final int after = Mockito.mockingDetails(itemManager).getInvocations().size();
		assertTrue("the tick did not read prices again", after > before);
		assertNull("a price was read off the client thread", priceFailure.get());
	}
}

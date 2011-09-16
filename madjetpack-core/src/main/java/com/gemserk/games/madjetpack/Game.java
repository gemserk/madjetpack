package com.gemserk.games.madjetpack;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.EventListenerReflectionRegistrator;
import com.gemserk.commons.gdx.GameTransitions.ScreenTransition;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.GameTransitions.TransitionScreen;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.madjetpack.gamestates.MainMenuGameState;
import com.gemserk.games.madjetpack.gamestates.SplashGameState;
import com.gemserk.games.madjetpack.preferences.GamePreferences;
import com.gemserk.games.madjetpack.resources.GameResources;
import com.gemserk.games.madjetpack.transitions.FadeInTransition;
import com.gemserk.games.madjetpack.transitions.FadeOutTransition;
import com.gemserk.util.ScreenshotSaver;

public class Game extends com.gemserk.commons.gdx.Game {

	private static boolean showFps = false;
	private static boolean showBox2dDebug = false;

	public static void setShowFps(boolean showFps) {
		Game.showFps = showFps;
	}

	public static boolean isShowFps() {
		return showFps;
	}

	public static boolean isShowBox2dDebug() {
		return showBox2dDebug;
	}

	public static void setShowBox2dDebug(boolean showBox2dDebug) {
		Game.showBox2dDebug = showBox2dDebug;
	}

	private Screen splashScreen;
	private Screen mainMenuScreen;

	private CustomResourceManager<String> resourceManager;
	private BitmapFont fpsFont;
	private SpriteBatch spriteBatch;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private EventManager eventManager;

	/**
	 * Used to store global information about the game and to send data between GameStates and Screens.
	 */
	private Parameters gameData;

	public Screen getSplashScreen() {
		return splashScreen;
	}

	public Screen getMainMenuScreen() {
		return mainMenuScreen;
	}

	public Parameters getGameData() {
		return gameData;
	}

	public CustomResourceManager<String> getResourceManager() {
		return resourceManager;
	}

	/**
	 * Used to communicate between gamestates.
	 */
	public EventManager getEventManager() {
		return eventManager;
	}

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		Converters.register(Float.class, Converters.floatValue());

		gameData = new ParametersWrapper();

		try {
			Properties properties = new Properties();
			properties.load(Gdx.files.classpath("version.properties").read());
			getGameData().put("version", properties.getProperty("version"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ExecutorService executorService = Executors.newCachedThreadPool();
		Preferences preferences = Gdx.app.getPreferences("gemserk-vampirerunner");

		GamePreferences gamePreferences = new GamePreferences();

		eventManager = new EventManagerImpl();
		resourceManager = new CustomResourceManager<String>();

		GameResources.load(resourceManager);

		// fpsFont = resourceManager.getResourceValue("FpsFont");
		fpsFont = new BitmapFont();
		spriteBatch = new SpriteBatch();

		MainMenuGameState mainMenuGameState = new MainMenuGameState(this);
		mainMenuGameState.setResourceManager(resourceManager);
		mainMenuGameState.setGamePreferences(gamePreferences);

		SplashGameState splashGameState = new SplashGameState(this);
		splashGameState.setResourceManager(resourceManager);

		splashScreen = new ScreenImpl(splashGameState);
		mainMenuScreen = new ScreenImpl(mainMenuGameState);

		EventListenerReflectionRegistrator registrator = new EventListenerReflectionRegistrator(eventManager);

		registrator.registerEventListeners(this);

		setScreen(splashScreen);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKey("grabScreenshot", Keys.NUM_9);
				monitorKey("toggleBox2dDebug", Keys.NUM_8);
				monitorKey("toggleFps", Keys.NUM_7);
				monitorKey("restartScreen", Keys.NUM_1);
			}
		};

		Gdx.graphics.getGL10().glClearColor(0, 0, 0, 1);
	}

	@Override
	public void render() {

		GlobalTime.setDelta(Gdx.graphics.getDeltaTime());

		inputDevicesMonitor.update();

		super.render();

		spriteBatch.begin();
		if (showFps)
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
		spriteBatch.end();

		if (inputDevicesMonitor.getButton("grabScreenshot").isReleased()) {
			try {
				ScreenshotSaver.saveScreenshot("superflyingthing");
			} catch (IOException e) {
				Gdx.app.log("SuperFlyingThing", "Can't save screenshot");
			}
		}

		if (inputDevicesMonitor.getButton("restartScreen").isReleased()) 
			getScreen().restart();

		if (inputDevicesMonitor.getButton("toggleFps").isReleased())
			setShowFps(!isShowFps());

		if (inputDevicesMonitor.getButton("toggleBox2dDebug").isReleased())
			setShowBox2dDebug(!isShowBox2dDebug());

		eventManager.process();
	}

	private boolean withTransition = false;

	public class TransitionBuilder {

		private final Screen screen;
		private final Game game;

		float leaveTime;
		float enterTime;
		boolean shouldDisposeCurrentScreen;

		TransitionHandler leaveTransitionHandler = new TransitionHandler();

		public TransitionBuilder leaveTime(float leaveTime) {
			this.leaveTime = leaveTime;
			return this;
		}

		public TransitionBuilder enterTime(float enterTime) {
			this.enterTime = enterTime;
			return this;
		}

		public TransitionBuilder leaveTime(int leaveTime) {
			return leaveTime((float) leaveTime * 0.001f);
		}

		public TransitionBuilder enterTime(int enterTime) {
			return enterTime((float) enterTime * 0.001f);
		}

		public TransitionBuilder disposeCurrent() {
			this.shouldDisposeCurrentScreen = true;
			return this;
		}

		public TransitionBuilder disposeCurrent(boolean disposeCurrent) {
			this.shouldDisposeCurrentScreen = disposeCurrent;
			return this;
		}

		public TransitionBuilder leaveTransitionHandler(TransitionHandler transitionHandler) {
			this.leaveTransitionHandler = transitionHandler;
			return this;
		}

		public TransitionBuilder parameter(String key, Object value) {
			screen.getParameters().put(key, value);
			return this;
		}

		public TransitionBuilder(final Game game, final Screen screen) {
			this.game = game;
			this.screen = screen;
			this.leaveTransitionHandler = new TransitionHandler();
			this.leaveTime = 0.25f;
			this.enterTime = 0.25f;
		}

		public void start() {
			if (withTransition)
				return;
			withTransition = true;
			final Screen currentScreen = game.getScreen();
			game.setScreen(new TransitionScreen(new ScreenTransition( //
					new FadeOutTransition(resourceManager, currentScreen, leaveTime, leaveTransitionHandler), //
					new FadeInTransition(resourceManager, screen, enterTime, new TransitionHandler() {
						public void onEnd() {
							withTransition = false;
							// disposes current transition screen, not previous screen.
							game.setScreen(screen, true);
							if (shouldDisposeCurrentScreen)
								currentScreen.dispose();
						};
					}))) {
				@Override
				public void resume() {
					super.resume();
					Gdx.input.setCatchBackKey(true);
				}
			});
		}

	}

	public TransitionBuilder transition(Screen screen) {
		return new TransitionBuilder(this, screen);
	}

	@Override
	public void dispose() {
		super.dispose();
		resourceManager.unloadAll();
		spriteBatch.dispose();
	}

}
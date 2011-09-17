package com.gemserk.games.madjetpack.gamestates;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.games.madjetpack.Game;
import com.gemserk.games.madjetpack.scenes.NormalModeSceneTemplate;
import com.gemserk.resources.ResourceManager;

public class PlayGameState extends GameStateImpl {

	private final Game game;

	private ResourceManager<String> resourceManager;

	private WorldWrapper scene;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	public PlayGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		scene = new WorldWrapper(new World());
		
		NormalModeSceneTemplate normalModeSceneTemplate = new NormalModeSceneTemplate();
		normalModeSceneTemplate.setResourceManager(resourceManager);
		
		normalModeSceneTemplate.apply(scene);
	}
	
	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		scene.render();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		scene.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		scene.dispose();
	}
}

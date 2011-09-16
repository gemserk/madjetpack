package com.gemserk.games.madjetpack.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizer;
import com.gemserk.commons.gdx.GameTransitions;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.resources.ResourceManager;

public class FadeOutTransition extends GameTransitions.LeaveTransition {

	private float alpha = 0f;
	private Sprite whiteRectangle;
	ResourceManager<String> resourceManager;
	SpriteBatch spriteBatch;
	private final float time;
	private Synchronizer synchronizer;

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public FadeOutTransition(ResourceManager<String> resourceManager, Screen screen, float time) {
		super(screen, time);
		this.resourceManager = resourceManager;
		this.time = time;
	}

	public FadeOutTransition(ResourceManager<String> resourceManager, Screen screen, float time, TransitionHandler transitionHandler) {
		super(screen, time, transitionHandler);
		this.resourceManager = resourceManager;
		this.time = time;
	}

	@Override
	public void init() {
		super.init();
		whiteRectangle = resourceManager.getResourceValue("WhiteRectangleSprite");
		spriteBatch = new SpriteBatch();
		synchronizer = new Synchronizer();
		synchronizer.transition(this, "alpha", Transitions.transitionBuilder(alpha).end(1f).time(time));
	}

	@Override
	public void postRender(float delta) {
		whiteRectangle.setPosition(0, 0);
		whiteRectangle.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		whiteRectangle.setColor(0f, 0f, 0f, alpha);
		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void internalUpdate(float delta) {
		super.internalUpdate(delta);
		synchronizer.synchronize(delta);
	}
}
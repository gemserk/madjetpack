package com.gemserk.games.madjetpack.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Rectangle;

public class BoundsComponent extends Component {
	
	private Rectangle bounds;
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public BoundsComponent(Rectangle bounds) {
		this.bounds = bounds;
	}

}
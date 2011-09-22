package com.gemserk.games.madjetpack.components;

import com.artemis.Entity;

public class GameComponents {
	
	public static final Class<ShipPartComponent> shipPartComponentClass = ShipPartComponent.class;
	
	public static ShipPartComponent getShipComponent(Entity e) {
		return e.getComponent(shipPartComponentClass);
	}

}

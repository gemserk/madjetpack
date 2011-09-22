package com.gemserk.games.madjetpack.components;

import com.artemis.Entity;

public class GameComponents {
	
	public static final Class<ShipPartComponent> shipPartComponentClass = ShipPartComponent.class;
	public static final Class<WorldWrapTeleportComponent> worldWrapTeleportComponentClass = WorldWrapTeleportComponent.class;
	
	public static ShipPartComponent getShipComponent(Entity e) {
		return e.getComponent(shipPartComponentClass);
	}
	
	public static WorldWrapTeleportComponent getWorldWrapTeleportComponent(Entity e) {
		return e.getComponent(worldWrapTeleportComponentClass);
	}	

}

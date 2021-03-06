package com.gemserk.games.madjetpack.components;

import com.artemis.Entity;

public class GameComponents {
	
	public static final Class<CameraComponent> cameraComponentClass = CameraComponent.class;
	public static final Class<ShipPartComponent> shipPartComponentClass = ShipPartComponent.class;
	public static final Class<WorldWrapTeleportComponent> worldWrapTeleportComponentClass = WorldWrapTeleportComponent.class;
	public static final Class<BoundsComponent> boundsComponentClass = BoundsComponent.class;
	public static final Class<ControllerComponent> controllerComponentClass = ControllerComponent.class;
	
	public static ShipPartComponent shipPartComponent(Entity e) {
		return e.getComponent(shipPartComponentClass);
	}
	
	public static WorldWrapTeleportComponent worldWrapTeleportComponent(Entity e) {
		return e.getComponent(worldWrapTeleportComponentClass);
	}	
	
	public static BoundsComponent boundsComponent(Entity e) {
		return e.getComponent(boundsComponentClass);
	}	
	
	public static ControllerComponent controllerComponent(Entity e) {
		return e.getComponent(controllerComponentClass);
	}	
	
	public static CameraComponent cameraComponent(Entity e) {
		return e.getComponent(cameraComponentClass);
	}

}

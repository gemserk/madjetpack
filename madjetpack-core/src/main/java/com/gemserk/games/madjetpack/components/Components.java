package com.gemserk.games.madjetpack.components;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;

public class Components {
	
	public static final Class<CameraComponent> cameraComponentClass = CameraComponent.class;
	public static final Class<SpatialComponent> spatialComponentClass = SpatialComponent.class;
	public static final Class<ScriptComponent> scriptComponentClass = ScriptComponent.class;
	public static final Class<PhysicsComponent> physicsComponentClass = PhysicsComponent.class;
	
	public static CameraComponent cameraComponent(Entity e) {
		return e.getComponent(cameraComponentClass);
	}
	
	public static SpatialComponent spatialComponent(Entity e) {
		return e.getComponent(spatialComponentClass);
	}
	
	public static ScriptComponent scriptComponent(Entity e) {
		return e.getComponent(scriptComponentClass);
	}
	
	public static PhysicsComponent physicsComponent(Entity e) {
		return e.getComponent(physicsComponentClass);
	}


}

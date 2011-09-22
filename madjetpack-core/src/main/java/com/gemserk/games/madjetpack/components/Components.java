package com.gemserk.games.madjetpack.components;

import com.artemis.Entity;

public class Components {
	
	public static final Class<CameraComponent> cameraComponentClass = CameraComponent.class;
	
	public static CameraComponent cameraComponent(Entity e) {
		return e.getComponent(cameraComponentClass);
	}

}

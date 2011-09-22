package com.gemserk.games.madjetpack.components;

import com.artemis.Component;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class CameraComponent extends Component {

	private Libgdx2dCamera libgdx2dCamera;
	private Camera camera;

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setLibgdx2dCamera(Libgdx2dCamera libgdx2dCamera) {
		this.libgdx2dCamera = libgdx2dCamera;
	}

	public Libgdx2dCamera getLibgdx2dCamera() {
		return libgdx2dCamera;
	}

	public CameraComponent() {

	}

	public CameraComponent(Camera camera, Libgdx2dCamera libgdx2dCamera) {
		setLibgdx2dCamera(libgdx2dCamera);
		setCamera(camera);
	}

}
package com.gemserk.games.madjetpack.components;

import com.artemis.Component;
import com.gemserk.commons.artemis.templates.EntityTemplate;

public class WeaponComponent extends Component {

	private float fireRate;
	private float reloadTime;
	private float bulletDuration;
	private EntityTemplate bulletTemplate;

	public void setFireRate(int fireRate) {
		this.fireRate = fireRate;
	}

	public float getReloadTime() {
		return reloadTime;
	}

	public float getFireRate() {
		return fireRate;
	}

	public float getBulletDuration() {
		return bulletDuration;
	}

	public void setReloadTime(float reloadTime) {
		this.reloadTime = reloadTime;
	}

	public EntityTemplate getBulletTemplate() {
		return bulletTemplate;
	}

	public WeaponComponent(float fireRate, float bulletDuration, float currentReloadTime, EntityTemplate bulletTemplate) {
		this.fireRate = fireRate;
		this.bulletDuration = bulletDuration;
		this.reloadTime = currentReloadTime;
		this.bulletTemplate = bulletTemplate;
	}
	
	public WeaponComponent(float fireRate, float bulletDuration, EntityTemplate bulletTemplate) {
		this(fireRate, bulletDuration, 0f, bulletTemplate);
	}

}
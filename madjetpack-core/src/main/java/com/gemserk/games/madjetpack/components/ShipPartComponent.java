package com.gemserk.games.madjetpack.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.physics.box2d.Joint;

public class ShipPartComponent extends Component {

	private Entity part;
	private Joint joint;
	
	public Entity getPart() {
		return part;
	}
	
	public void setPart(Entity part) {
		this.part = part;
	}
	
	public void setJoint(Joint joint) {
		this.joint = joint;
	}
	
	public Joint getJoint() {
		return joint;
	}
	
	public ShipPartComponent() {
		
	}

	public ShipPartComponent(Entity part) {
		this.part = part;
	}

}
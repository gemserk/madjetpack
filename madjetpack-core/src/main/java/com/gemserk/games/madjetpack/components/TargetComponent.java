package com.gemserk.games.madjetpack.components;

import com.artemis.Component;

public class TargetComponent extends Component {
	
	private final String entityTag;
	
	public String getEntityTag() {
		return entityTag;
	}

	public TargetComponent(String entityTag) {
		this.entityTag = entityTag;
	}

}
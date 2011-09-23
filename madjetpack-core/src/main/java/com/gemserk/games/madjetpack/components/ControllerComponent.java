package com.gemserk.games.madjetpack.components;

import com.artemis.Component;
import com.gemserk.games.madjetpack.scripts.controllers.CharacterController;

public class ControllerComponent extends Component {

	public CharacterController characterController;

	public ControllerComponent(CharacterController characterController) {
		this.characterController = characterController;
	}

}
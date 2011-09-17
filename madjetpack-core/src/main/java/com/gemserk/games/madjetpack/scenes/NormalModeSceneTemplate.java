package com.gemserk.games.madjetpack.scenes;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.madjetpack.GameInformation;
import com.gemserk.games.madjetpack.components.RenderScriptComponent;
import com.gemserk.games.madjetpack.systems.RenderScriptSystem;
import com.gemserk.resources.ResourceManager;

public class NormalModeSceneTemplate {

	static class CollisionBits {

		public static final short ALL = 0xFF;
		public static final short NONE = 0x00;

		public static final short Character = 0x01;
		public static final short Platform = 0x02;

	}

	static class Box2dRenderDebugScript extends ScriptJavaImpl {

		private final Box2DDebugRenderer box2dDebugRenderer = new Box2DDebugRenderer();
		private final Libgdx2dCamera camera;
		private final com.badlogic.gdx.physics.box2d.World physicsWorld;

		public Box2dRenderDebugScript(Libgdx2dCamera camera, com.badlogic.gdx.physics.box2d.World physicsWorld) {
			this.camera = camera;
			this.physicsWorld = physicsWorld;
		}

		@Override
		public void update(World world, Entity e) {
			box2dDebugRenderer.render(physicsWorld, camera.getCombinedMatrix());
		}

	}

	static class CharacterControllerScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			Vector2 movementDirection = new Vector2(0f, 0f);

			if (Gdx.input.isKeyPressed(Keys.D)) {
				movementDirection.x += 1f;
			} else if (Gdx.input.isKeyPressed(Keys.A)) {
				movementDirection.x -= 1f;
			}

			if (Gdx.input.isKeyPressed(Keys.W)) {
				movementDirection.y += 1f;
			}

			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);

			Body body = physicsComponent.getBody();

			body.applyForceToCenter(new Vector2(3f, 0f).mul(movementDirection.x));

			// apply jetpack
			body.applyForceToCenter(new Vector2(0f, 15f).mul(movementDirection.y));
		}

	}

	static class DisablePlatformCollisionWhenGoingUpScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);

			Body body = physicsComponent.getBody();
			
			Vector2 linearVelocity = body.getLinearVelocity();
			
			boolean goingUp = linearVelocity.y > 0f;

			ArrayList<Fixture> fixtureList = body.getFixtureList();

			for (int i = 0; i < fixtureList.size(); i++) {
				Fixture fixture = fixtureList.get(i);

				String fixtureId = (String) fixture.getUserData();

				if (!"CharacterBase".equals(fixtureId))
					continue;
				
				Filter filterData = fixture.getFilterData();
				
				if (goingUp)
					filterData.maskBits = CollisionBits.NONE;
				else
					filterData.maskBits = CollisionBits.ALL;
				
				fixture.setFilterData(filterData);
			}
		}

	}

	class Box2dDebugRendererTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			Libgdx2dCamera camera = parameters.get("camera");
			com.badlogic.gdx.physics.box2d.World physicsWorld = parameters.get("physicsWorld");
			entity.addComponent(new RenderScriptComponent(new Box2dRenderDebugScript(camera, physicsWorld)));
		}
	}

	class CharacterTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			float width = 0.5f;
			float height = 1.5f;

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Character) //
							.maskBits(CollisionBits.NONE).boxShape(width * 0.5f, height * 0.5f), //
							"CharacterBody") //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Character) //
							.maskBits(CollisionBits.ALL).circleShape(new Vector2(0f, -0.75f), width * 0.5f), //
							"CharacterBase") //
					.position(2f, 3f) //
					.type(BodyType.DynamicBody) //
					.fixedRotation() //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new ScriptComponent(new CharacterControllerScript(), //
					new DisablePlatformCollisionWhenGoingUpScript()
					));

		}
	}

	class StaticPlatformTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			Vector2 position = parameters.get("position");

			Float width = parameters.get("width");
			Float height = parameters.get("height");

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.boxShape(width * 0.5f, height * 0.5f)) //
					.position(position.x, position.y) //
					.type(BodyType.StaticBody) //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));

		}
	}

	static class Layers {

		static final String World = "World";

	}

	// @Inject
	ResourceManager<String> resourceManager;

	EntityTemplate box2dDebugRendererTemplate = new Box2dDebugRendererTemplate();
	EntityTemplate characterTemplate = new CharacterTemplate();
	EntityTemplate staticPlatformTemplate = new StaticPlatformTemplate();

	private BodyBuilder bodyBuilder;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void apply(WorldWrapper scene) {
		final EventManager eventManager = new EventManagerImpl();

		com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0f, -10f), false);
		bodyBuilder = new BodyBuilder(physicsWorld);

		RenderLayers renderLayers = new RenderLayers();

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl();

		worldCamera.zoom(48f);

		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-100, 100, worldCamera));

		scene.addUpdateSystem(new ScriptSystem());
		scene.addUpdateSystem(new TagSystem());
		scene.addUpdateSystem(new PhysicsSystem(physicsWorld));
		scene.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));

		// scene.addRenderSystem(new SpriteUpdateSystem());
		// scene.addRenderSystem(new RenderableSystem(renderLayers));
		scene.addRenderSystem(new RenderScriptSystem());

		scene.init();

		EntityFactory entityFactory = new EntityFactoryImpl(scene.getWorld());
		entityFactory.instantiate(box2dDebugRendererTemplate, new ParametersWrapper() //
				.put("camera", worldCamera) //
				.put("physicsWorld", physicsWorld) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(15f, 0.5f)) //
				.put("width", 60f) //
				.put("height", 1f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(8f, 3.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(4f, 4.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(12f, 4.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		entityFactory.instantiate(characterTemplate);

		Gdx.app.log(GameInformation.name, "Applying scene template...");
	}

}

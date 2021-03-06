package com.gemserk.games.madjetpack.scenes;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.artemis.World;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.OwnerComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Contacts;
import com.gemserk.commons.gdx.box2d.Contacts.Contact;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.componentsengine.utils.timers.CountDownTimer;
import com.gemserk.games.madjetpack.GameInformation;
import com.gemserk.games.madjetpack.components.BoundsComponent;
import com.gemserk.games.madjetpack.components.CameraComponent;
import com.gemserk.games.madjetpack.components.ControllerComponent;
import com.gemserk.games.madjetpack.components.GameComponents;
import com.gemserk.games.madjetpack.components.RenderScriptComponent;
import com.gemserk.games.madjetpack.components.ShipPartComponent;
import com.gemserk.games.madjetpack.components.TargetComponent;
import com.gemserk.games.madjetpack.components.WeaponComponent;
import com.gemserk.games.madjetpack.components.WorldWrapTeleportComponent;
import com.gemserk.games.madjetpack.entities.Groups;
import com.gemserk.games.madjetpack.entities.Tags;
import com.gemserk.games.madjetpack.scripts.controllers.CharacterController;
import com.gemserk.games.madjetpack.systems.RenderScriptSystem;
import com.gemserk.resources.ResourceManager;

public class NormalModeSceneTemplate {

	static class CollisionBits {

		public static final short ALL = 0xFF;
		public static final short NONE = 0x00;

		public static final short Character = 0x01;
		public static final short Platform = 0x02;
		public static final short Bullet = 0x04;
		public static final short Alien = 0x08;
		public static final short WorldBound = 0x10;
		public static final short ShipPart = 0x20;
		public static final short Ship = 0x40;

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

	class KeyboardControllerScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			ControllerComponent controllerComponent = GameComponents.controllerComponent(e);

			Vector2 movementDirection = new Vector2(0f, 0f);

			if (Gdx.input.isKeyPressed(Keys.D))
				movementDirection.x += 1f;
			else if (Gdx.input.isKeyPressed(Keys.A))
				movementDirection.x -= 1f;

			if (Gdx.input.isKeyPressed(Keys.W))
				movementDirection.y += 1f;

			controllerComponent.characterController.horizontalDirection = movementDirection.x;
			controllerComponent.characterController.jetpacPower = movementDirection.y;

		}

	}

	class KeyboardControllerTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			CharacterController characterController = parameters.get("controller");
			entity.addComponent(new ControllerComponent(characterController));
			entity.addComponent(new ScriptComponent(new KeyboardControllerScript()));
		}

	}

//	class Xbox360ControllerScript extends ScriptJavaImpl {
//
//		private static final String controllerName = "Microsoft X-Box 360 pad";
//		private Controller controller;
//
//		public Xbox360ControllerScript() {
//			ControllerEnvironment controllerEnvironment = ControllerEnvironment.getDefaultEnvironment();
//			Controller[] controllers = controllerEnvironment.getControllers();
//
//			if (controllers.length == 0)
//				throw new RuntimeException("Xbox360 Controller not found");
//
//			for (int i = 0; i < controllers.length; i++) {
//				Controller controller = controllers[0];
//				if (controllerName.equals(controller.getName()))
//					this.controller = controller;
//			}
//
//			if (this.controller == null)
//				throw new RuntimeException("Xbox360 Controller not found");
//		}
//
//		@Override
//		public void update(World world, Entity e) {
//
//			controller.poll();
//
//			Component[] components = controller.getComponents();
//			ControllerComponent controllerComponent = GameComponents.controllerComponent(e);
//			CharacterController characterController = controllerComponent.characterController;
//
//			characterController.horizontalDirection = 0f;
//			characterController.jetpacPower = 0f;
//
//			for (int i = 0; i < components.length; i++) {
//
//				Component component = components[i];
//
//				float value = component.getPollData();
//
//				if ("x".equals(component.getName()))
//					characterController.horizontalDirection = value;
//
//				if ("y".equals(component.getName()) && value < 0f)
//					characterController.jetpacPower = -value;
//
//			}
//
//		}
//
//	}

//	class Xbox360ControllerTemplate extends EntityTemplateImpl {
//
//		@Override
//		public void apply(Entity entity) {
//			CharacterController characterController = parameters.get("controller");
//			entity.addComponent(new ControllerComponent(characterController));
//			entity.addComponent(new ScriptComponent(new Xbox360ControllerScript()));
//		}
//
//	}

	class CharacterControllerScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			ControllerComponent controllerComponent = GameComponents.controllerComponent(e);
			CharacterController characterController = controllerComponent.characterController;

			// Vector2 movementDirection = new Vector2(0f, 0f);
			//
			// if (Gdx.input.isKeyPressed(Keys.D)) {
			// movementDirection.x += 1f;
			// } else if (Gdx.input.isKeyPressed(Keys.A)) {
			// movementDirection.x -= 1f;
			// }
			//
			// if (Gdx.input.isKeyPressed(Keys.W)) {
			// movementDirection.y += 1f;
			// }

			PhysicsComponent physicsComponent = Components.getPhysicsComponent(e);

			Body body = physicsComponent.getBody();

			body.applyForceToCenter(new Vector2(5f, 0f).mul(characterController.horizontalDirection));

			// apply jetpack
			body.applyForceToCenter(new Vector2(0f, 15f).mul(characterController.jetpacPower));

		}

	}

	static class DisablePlatformCollisionWhenGoingUpScript extends ScriptJavaImpl {

		short disabledMaskBits = CollisionBits.ALL & ~CollisionBits.Platform;
		short enabledMaskBits = CollisionBits.ALL;
		String fixtureId = "CharacterBase";

		public DisablePlatformCollisionWhenGoingUpScript(String fixtureId, short enabledMaskBits, short disabledMaskBits) {
			this.fixtureId = fixtureId;
			this.enabledMaskBits = enabledMaskBits;
			this.disabledMaskBits = disabledMaskBits;
		}

		public DisablePlatformCollisionWhenGoingUpScript() {

		}

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

				if (!fixtureId.equals(fixtureId))
					continue;

				Filter filterData = fixture.getFilterData();

				if (goingUp) {
					filterData.maskBits = disabledMaskBits;
				} else
					filterData.maskBits = enabledMaskBits;

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

			CharacterController characterController = parameters.get("controller");

			float width = 0.5f;
			float height = 1.5f;

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Character) //
							.maskBits(CollisionBits.WorldBound).boxShape(width * 0.5f, height * 0.5f), //
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

			entity.addComponent(new TagComponent(Tags.Character));
			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			entity.addComponent(new ControllerComponent(characterController));
			entity.addComponent(new ScriptComponent(new CharacterControllerScript(), //
					new DisablePlatformCollisionWhenGoingUpScript(), //
					new LimitLinearVelocityScript(50f) //
			));
			entity.addComponent(new ContainerComponent());
			entity.addComponent(new ShipPartComponent());
			entity.addComponent(new WorldWrapTeleportComponent());

		}
	}

	class BulletScript extends ScriptJavaImpl {

		private final Vector2 direction = new Vector2();
		private final Vector2 impulse = new Vector2();

		public BulletScript(Vector2 direction) {
			this.direction.set(direction);
		}

		@Override
		public void init(World world, Entity e) {
			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();

			impulse.set(direction).mul(20f);

			body.applyLinearImpulse(impulse, body.getPosition());
		}

	}

	class RemoveBulletScript extends ScriptJavaImpl {

		private final Rectangle bounds;
		private float bulletDuration;

		public RemoveBulletScript(Rectangle bounds, float bulletDuration) {
			this.bounds = bounds;
			this.bulletDuration = bulletDuration;
		}

		@Override
		public void update(World world, Entity e) {

			bulletDuration -= GlobalTime.getDelta();

			if (bulletDuration <= 0f) {
				e.delete();
				return;
			}

			SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
			Spatial spatial = spatialComponent.getSpatial();

			if (!bounds.contains(spatial.getX(), spatial.getY()))
				e.delete();

		}

	}

	class BulletTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			float width = 0.05f;
			float height = 0.05f;

			Vector2 position = parameters.get("position");
			Vector2 direction = parameters.get("direction");
			Float bulletDuration = parameters.get("bulletDuration");

			Rectangle worldBounds = new Rectangle(0f, 0f, 30f, 30f);

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Bullet) //
							.maskBits((short) (CollisionBits.ALL & ~CollisionBits.Character)) //
							.friction(0f) //
							.restitution(0.6f) //
							.boxShape(width * 0.5f, height * 0.5f), "BulletBody") //
					.position(position.x, position.y) //
					.type(BodyType.DynamicBody) //
					.bullet() //
					.userData(entity) //
					.build();

			entity.setGroup(Groups.Bullets);

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));

			entity.addComponent(new ScriptComponent(new BulletScript(direction), 
					new RemoveBulletScript(worldBounds, bulletDuration),
					new DisablePlatformCollisionWhenGoingUpScript("BulletBody", (short)(CollisionBits.ALL & ~CollisionBits.Character), 
							(short)(CollisionBits.ALL & ~CollisionBits.Platform & ~CollisionBits.Character))
			));
			entity.addComponent(new WorldWrapTeleportComponent());

		}

	}

	class ShootBulletScript extends ScriptJavaImpl {

		private final Vector2 direction = new Vector2();
		private final Vector2 position = new Vector2();

		// the conversion between the viewport coordinates and world coordinates should be handled in the controller script
		private final Libgdx2dCamera camera;

		public ShootBulletScript(Libgdx2dCamera camera) {
			this.camera = camera;
		}

		@Override
		public void update(World world, Entity e) {

			WeaponComponent weaponComponent = e.getComponent(WeaponComponent.class);
			float reloadTime = weaponComponent.getReloadTime();
			reloadTime -= GlobalTime.getDelta();

			if (reloadTime <= 0f)
				reloadTime = 0f;

			if (Gdx.input.isButtonPressed(Buttons.LEFT)) {

				if (reloadTime == 0) {

					int x = Gdx.input.getX();
					int y = Gdx.graphics.getHeight() - Gdx.input.getY();

					OwnerComponent ownerComponent = e.getComponent(OwnerComponent.class);
					SpatialComponent spatialComponent = ownerComponent.getOwner().getComponent(SpatialComponent.class);

					Spatial spatial = spatialComponent.getSpatial();

					position.set(spatial.getX(), spatial.getY());

					camera.project(position);

					direction.set(x - position.x, y - position.y);
					direction.nor();

					entityFactory.instantiate(bulletTemplate, new ParametersWrapper()//
							.put("position", spatial.getPosition()) //
							.put("direction", direction) //
							.put("bulletDuration", weaponComponent.getBulletDuration()) //
							);

					reloadTime += weaponComponent.getFireRate();
				}

			}

			weaponComponent.setReloadTime(reloadTime);
		}

	}

	class WeaponTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			Vector2 position = parameters.get("position");
			Entity owner = parameters.get("owner");
			Libgdx2dCamera camera = parameters.get("camera");

			entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 0f, 0f, 0f)));
			entity.addComponent(new ScriptComponent(new ShootBulletScript(camera)));
			entity.addComponent(new WeaponComponent(0.1f, 1f, bulletTemplate));
			entity.addComponent(new OwnerComponent(owner));

		}

	}

	class StaticPlatformTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			Vector2 position = parameters.get("position");

			Float width = parameters.get("width");
			Float height = parameters.get("height");

			Short collisionBits = parameters.get("collisionBits", CollisionBits.Platform);

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(collisionBits) //
							.boxShape(width * 0.5f, height * 0.5f)) //
					.position(position.x, position.y) //
					.type(BodyType.StaticBody) //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		}
	}

	class WorldBoundsTemplate extends StaticPlatformTemplate {
		@Override
		public void apply(Entity e) {
			parameters.put("collisionBits", CollisionBits.WorldBound);
			super.apply(e);
		}
	}

	static class AntiGravityScript extends ScriptJavaImpl {

		private static final Vector2 antiGravity = new Vector2(0, 10f);
		private final Vector2 tmp = new Vector2();

		@Override
		public void update(World world, Entity e) {
			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getPhysics().getBody();

			tmp.set(antiGravity).mul(body.getMass());

			body.applyForceToCenter(tmp);
		}

	}

	static class LimitLinearVelocityScript extends ScriptJavaImpl {

		private final Vector2 tmp = new Vector2();
		private final float speed;

		public LimitLinearVelocityScript(float speed) {
			this.speed = speed;
		}

		@Override
		public void update(World world, Entity e) {
			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getPhysics().getBody();

			Vector2 linearVelocity = body.getLinearVelocity();

			if (linearVelocity.len() > speed) {

				float v = speed - linearVelocity.len();

				tmp.set(linearVelocity).nor();
				tmp.mul(v * body.getMass() / GlobalTime.getDelta());

				body.applyForceToCenter(tmp);
			}

		}

	}

	class FollowEntityScript extends ScriptJavaImpl {

		// should be divided in two?

		private final Vector2 direction = new Vector2();
		private final Vector2 force = new Vector2();

		@Override
		public void update(World world, Entity e) {
			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getPhysics().getBody();

			TargetComponent targetComponent = e.getComponent(TargetComponent.class);
			String targetTag = targetComponent.getEntityTag();

			Entity target = world.getTagManager().getEntity(targetTag);

			// if target not on world
			if (target == null)
				return;

			Entity worldEntity = world.getTagManager().getEntity(Tags.World);
			BoundsComponent boundsComponent = GameComponents.boundsComponent(worldEntity);

			SpatialComponent targetSpatialComponent = target.getComponent(SpatialComponent.class);
			Spatial targetSpatial = targetSpatialComponent.getSpatial();

			SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
			Spatial spatial = spatialComponent.getSpatial();

			direction.set(targetSpatial.getX(), targetSpatial.getY());
			direction.sub(spatial.getX(), spatial.getY());

			if (direction.len() > boundsComponent.getBounds().getWidth() * 0.5f)
				direction.x = -direction.x;

			direction.nor();

			force.set(direction);
			force.mul(50f);

			body.applyForceToCenter(force);

		}

	}

	static class RemoveWhenBulletCollision extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);

			Contacts contacts = physicsComponent.getContact();
			if (!contacts.isInContact())
				return;

			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);

				Entity otherEntity = (Entity) contact.getOtherFixture().getBody().getUserData();
				if (!Groups.Bullets.equals(world.getGroupManager().getGroupOf(otherEntity)))
					continue;

				otherEntity.delete();
				e.delete();

				return;
			}
		}

	}

	class AlienTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			Spatial spatial = parameters.get("spatial");

			float width = spatial.getWidth();
			float height = spatial.getHeight();

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Alien) //
							.maskBits((short) (CollisionBits.ALL & ~CollisionBits.Platform)) //
							.circleShape(width * 0.5f), //
							"EnemyBody") //
					.position(spatial.getX(), spatial.getY()) //
					.type(BodyType.DynamicBody) //
					.fixedRotation() //
					.userData(entity) //
					.build();

			entity.setGroup(Groups.Aliens);

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			entity.addComponent(new ScriptComponent(new AntiGravityScript(), new FollowEntityScript(), new LimitLinearVelocityScript(2f), new RemoveWhenBulletCollision()));
			entity.addComponent(new TargetComponent(Tags.Character));
			entity.addComponent(new WorldWrapTeleportComponent());

			// entity.addComponent(new LinearVelocityLimitComponent(2f));

		}
	}

	class SpawnerScript extends ScriptJavaImpl {

		private final Parameters parameters = new ParametersWrapper();
		private final CountDownTimer countDownTimer;

		public SpawnerScript() {
			countDownTimer = new CountDownTimer(2000, true);
		}

		@Override
		public void update(World world, Entity e) {

			if (!countDownTimer.update(world.getDelta()))
				return;

			countDownTimer.reset();

			ImmutableBag<Entity> aliens = world.getGroupManager().getEntities(Groups.Aliens);

			if (aliens.size() > 2)
				return;

			SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
			Spatial spatial = spatialComponent.getSpatial();

			entityFactory.instantiate(enemyTemplate, parameters //
					.put("spatial", new SpatialImpl(spatial.getX(), spatial.getY(), 0.5f, 0.5f, 0f)) //
					);

		}

	}

	class AlienSpawnerTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			Float x = parameters.get("x");
			Float y = parameters.get("y");

			entity.setGroup(Groups.AlienSpawner);

			entity.addComponent(new SpatialComponent(new SpatialImpl(x, y, 0, 0, 0)));
			entity.addComponent(new ScriptComponent(new SpawnerScript()));
		}
	}

	static class UpdateCameraScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			CameraComponent cameraComponent = GameComponents.cameraComponent(e);
			Camera camera = cameraComponent.getCamera();
			Libgdx2dCamera libgdx2dCamera = cameraComponent.getLibgdx2dCamera();
			libgdx2dCamera.zoom(camera.getZoom());
			libgdx2dCamera.move(camera.getX(), camera.getY());
		}

	}

	class CameraTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			Camera camera = parameters.get("camera");
			Libgdx2dCamera libgdxCamera = parameters.get("libgdxCamera");

			entity.addComponent(new TagComponent(Tags.MainCamera));
			entity.addComponent(new CameraComponent(camera, libgdxCamera));
			entity.addComponent(new ScriptComponent(new UpdateCameraScript()));
		}
	}

	class CharacterCameraTemplate extends CameraTemplate {
		@Override
		public void apply(Entity e) {
			super.apply(e);
			ScriptComponent scriptComponent = Components.getScriptComponent(e);
			scriptComponent.getScripts().add(new ScriptJavaImpl() {
				@Override
				public void update(World world, Entity e) {
					Entity character = world.getTagManager().getEntity(Tags.Character);
					if (character == null)
						return;
					SpatialComponent spatialComponent = Components.getSpatialComponent(character);
					Spatial spatial = spatialComponent.getSpatial();

					CameraComponent cameraComponent = GameComponents.cameraComponent(e);
					Camera camera = cameraComponent.getCamera();

					camera.setPosition(spatial.getX(), spatial.getY());
				}
			});
		}
	}

	class AttachShipPartToCharacterScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			// check if the main character already contains a ship part...

			Entity character = world.getTagManager().getEntity(Tags.Character);
			if (character == null)
				return;

			ShipPartComponent shipComponent = GameComponents.shipPartComponent(character);
			if (shipComponent.getPart() != null)
				return;

			PhysicsComponent physicsComponent = Components.getPhysicsComponent(e);
			Contacts contacts = physicsComponent.getContact();

			if (!contacts.isInContact())
				return;

			boolean inContactWithCharacter = false;
			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);

				String fixtureId = (String) contact.getMyFixture().getUserData();
				if (!"ShipPartSensor".equals(fixtureId))
					continue;

				Entity otherEntity = (Entity) contact.getOtherFixture().getBody().getUserData();
				if (otherEntity == null)
					continue;
				inContactWithCharacter = character == otherEntity;
			}

			if (!inContactWithCharacter)
				return;

			SpatialComponent spatialComponent = Components.getSpatialComponent(e);
			Spatial spatial = spatialComponent.getSpatial();

			entityFactory.instantiate(attachedShipPartTemplate, new ParametersWrapper() //
					.put("owner", character) //
					.put("position", spatial.getPosition()) //
					);

			e.delete();

		}

	}

	class ShipPartTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			float width = 0.25f;
			float height = 0.25f;

			Vector2 position = parameters.get("position");

			short maskBits = CollisionBits.Platform | CollisionBits.WorldBound | CollisionBits.Alien;
			short sensorMaskBits = CollisionBits.Character;

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.ShipPart) //
							.maskBits(maskBits) //
							.density(0.1f) //
							.circleShape(new Vector2(0f, 0f), width * 0.5f), //
							"ShipPartBody") //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.sensor() //
							.categoryBits(CollisionBits.ShipPart) //
							.maskBits(sensorMaskBits) //
							.density(0f) //
							.circleShape(new Vector2(0f, 0f), width * 2f), //
							"ShipPartSensor") //
					.position(position.x, position.y) //
					.type(BodyType.DynamicBody) //
					.fixedRotation() //
					.userData(entity) //
					.build();

			// entity.setGroup(Groups.ShipParts);

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			entity.addComponent(new ScriptComponent(new AttachShipPartToCharacterScript()));
			entity.addComponent(new WorldWrapTeleportComponent());
		}

	}

	class AttachedShipPartTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			float width = 0.25f;
			float height = 0.25f;

			Vector2 position = parameters.get("position");
			Entity owner = parameters.get("owner");

			short maskBits = CollisionBits.Platform | CollisionBits.WorldBound | CollisionBits.Alien | CollisionBits.Ship;

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.ShipPart) //
							.maskBits(maskBits) //
							.density(0.1f) //
							.circleShape(new Vector2(0f, 0f), width * 0.5f), //
							"ShipPartBody") //
					.position(position.x, position.y) //
					.type(BodyType.DynamicBody) //
					.fixedRotation() //
					.userData(entity) //
					.build();

			entity.setGroup(Groups.ShipParts);

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			entity.addComponent(new ScriptComponent(new DisablePlatformCollisionWhenGoingUpScript("ShipPartBody", maskBits, (short) (CollisionBits.WorldBound | CollisionBits.Alien))));

			PhysicsComponent characterPhysicsComponent = Components.getPhysicsComponent(owner);

			Joint joint = jointBuilder.ropeJointBuilder() //
					.bodyA(body, 0f, 0f) //
					.bodyB(characterPhysicsComponent.getBody(), 0f, -0.7f) //
					.maxLength(1f) //
					.build();

			ShipPartComponent shipComponent = GameComponents.shipPartComponent(owner);

			shipComponent.setPart(entity);
			shipComponent.setJoint(joint);
		}

	}

	class RemoveShipPartScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {

			// check if the main character already contains a ship part...

			Entity character = world.getTagManager().getEntity(Tags.Character);
			if (character == null)
				return;

			ShipPartComponent shipPartComponent = GameComponents.shipPartComponent(character);
			Entity shipPart = shipPartComponent.getPart();
			if (shipPart == null)
				return;

			PhysicsComponent physicsComponent = Components.getPhysicsComponent(e);
			Contacts contacts = physicsComponent.getContact();

			if (!contacts.isInContact())
				return;

			boolean inContactWithShipPart = false;
			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);

				String fixtureId = (String) contact.getMyFixture().getUserData();
				if (!"ShipSensor".equals(fixtureId))
					continue;

				Entity otherEntity = (Entity) contact.getOtherFixture().getBody().getUserData();
				if (otherEntity == null)
					continue;

				inContactWithShipPart = shipPart == otherEntity;
			}

			if (!inContactWithShipPart)
				return;

			shipPartComponent.setPart(null);
			shipPartComponent.setJoint(null);

			shipPart.delete();

			// send event of ship part retrieved!!
		}

	}

	class ShipTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {

			float width = 0.5f;
			float height = 0.5f;

			Vector2 position = parameters.get("position");

			short maskBits = CollisionBits.Platform | CollisionBits.WorldBound;
			short sensorMaskBits = CollisionBits.ShipPart;

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.categoryBits(CollisionBits.Ship) //
							.maskBits(maskBits) //
							.density(2f) //
							.circleShape(new Vector2(0f, 0f), width * 0.5f), //
							"ShipBody") //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.sensor() //
							.categoryBits(CollisionBits.Ship) //
							.maskBits(sensorMaskBits) //
							.circleShape(new Vector2(0f, 0f), width * 3f), //
							"ShipSensor") //
					.position(position.x, position.y) //
					.type(BodyType.DynamicBody) //
					.fixedRotation() //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			entity.addComponent(new ScriptComponent(new RemoveShipPartScript()));

		}

	}

	class WorldTemplate extends EntityTemplateImpl {

		@Override
		public void apply(Entity entity) {
			Rectangle bounds = parameters.get("bounds");
			entity.addComponent(new TagComponent(Tags.World));
			entity.addComponent(new BoundsComponent(bounds));
		}

	}

	static class Layers {

		static final String World = "World";

	}

	// @Inject
	ResourceManager<String> resourceManager;

	EntityTemplate worldTemplate = new WorldTemplate();

	EntityTemplate box2dDebugRendererTemplate = new Box2dDebugRendererTemplate();
	EntityTemplate characterTemplate = new CharacterTemplate();

	EntityTemplate staticPlatformTemplate = new StaticPlatformTemplate();
	EntityTemplate worldBoundsTemplate = new WorldBoundsTemplate();

	EntityTemplate bulletTemplate = new BulletTemplate();
	EntityTemplate weaponTemplate = new WeaponTemplate();
	EntityTemplate enemyTemplate = new AlienTemplate();
	EntityTemplate alienSpawnerTemplate = new AlienSpawnerTemplate();
	EntityTemplate cameraTemplate = new CameraTemplate();
	EntityTemplate characterCameraTemplate = new CharacterCameraTemplate();
	EntityTemplate shipPartTemplate = new ShipPartTemplate();
	EntityTemplate attachedShipPartTemplate = new AttachedShipPartTemplate();

	EntityTemplate shipTemplate = new ShipTemplate();

	EntityTemplate keyboardControllerTemplate = new KeyboardControllerTemplate();
//	EntityTemplate xbox360ControllerTemplate = new Xbox360ControllerTemplate();

	private BodyBuilder bodyBuilder;
	private EntityFactory entityFactory;

	private JointBuilder jointBuilder;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void apply(WorldWrapper scene) {
		final EventManager eventManager = new EventManagerImpl();

		final Rectangle worldBounds = new Rectangle(0, 0, 20f, 20f);

		Camera gameCamera = new CameraRestrictedImpl(0, 0, 48f, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), worldBounds);

		physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0f, -10f), false);
		bodyBuilder = new BodyBuilder(physicsWorld);
		jointBuilder = new JointBuilder(physicsWorld);

		RenderLayers renderLayers = new RenderLayers();

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);

		// worldCamera.zoom(48f);

		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-100, 100, worldCamera));

		scene.addUpdateSystem(new ScriptSystem());
		scene.addUpdateSystem(new TagSystem());
		scene.addUpdateSystem(new PhysicsSystem(physicsWorld));
		scene.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));
		scene.addUpdateSystem(new OwnerSystem());

		scene.addUpdateSystem(new EntityProcessingSystem(Components.spatialComponentClass, GameComponents.worldWrapTeleportComponentClass) {
			@Override
			protected void process(Entity e) {
				SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
				Spatial spatial = spatialComponent.getSpatial();

				float limit = worldBounds.getX() + worldBounds.getWidth();

				if (spatial.getX() > limit) {
					spatial.setPosition(spatial.getX() - limit, spatial.getY());
				}

				if (spatial.getX() < 0f) {
					spatial.setPosition(spatial.getX() + limit, spatial.getY());
				}
			}
		});

		// scene.addRenderSystem(new SpriteUpdateSystem());
		// scene.addRenderSystem(new RenderableSystem(renderLayers));
		scene.addRenderSystem(new RenderScriptSystem());

		scene.init();

		CharacterController characterController = new CharacterController();

		entityFactory = new EntityFactoryImpl(scene.getWorld());

		entityFactory.instantiate(worldTemplate, new ParametersWrapper().put("bounds", worldBounds));

		entityFactory.instantiate(box2dDebugRendererTemplate, new ParametersWrapper() //
				.put("camera", worldCamera) //
				.put("physicsWorld", physicsWorld) //
				);

		entityFactory.instantiate(worldBoundsTemplate, new ParametersWrapper() //
				.put("position", new Vector2(15f, 0.5f)) //
				.put("width", 60f) //
				.put("height", 1f) //
				);

		entityFactory.instantiate(worldBoundsTemplate, new ParametersWrapper() //
				.put("position", new Vector2(15f, worldBounds.getY() + worldBounds.getHeight())) //
				.put("width", 60f) //
				.put("height", 0.1f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.5f, 3.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.25f, 4.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		entityFactory.instantiate(staticPlatformTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.75f, 4.5f)) //
				.put("width", 2f) //
				.put("height", 0.2f) //
				);

		Entity character = entityFactory.instantiate(characterTemplate, new ParametersWrapper().put("controller", characterController));

		entityFactory.instantiate(weaponTemplate, new ParametersWrapper() //
				.put("position", new Vector2(3f, 2f)) //
				.put("owner", character) //
				.put("camera", worldCamera) //
				);

		entityFactory.instantiate(alienSpawnerTemplate, new ParametersWrapper() //
				.put("x", worldBounds.getWidth() * 0.5f) //
				.put("y", 8f) //
				);

		entityFactory.instantiate(shipPartTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.25f, 5.2f)) //
				);

		entityFactory.instantiate(shipPartTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.75f, 5.2f)) //
				);

		entityFactory.instantiate(shipTemplate, new ParametersWrapper() //
				.put("position", new Vector2(worldBounds.getWidth() * 0.5f, 1f)) //
				);

		entityFactory.instantiate(characterCameraTemplate, new ParametersWrapper() //
				.put("camera", gameCamera) //
				.put("libgdxCamera", worldCamera) //
				);

		entityFactory.instantiate(keyboardControllerTemplate, new ParametersWrapper() //
				.put("controller", characterController) //
				);

		// try {
		// entityFactory.instantiate(xbox360ControllerTemplate, new ParametersWrapper() //
		// .put("controller", characterController) //
		// );
		// } catch (Exception e) {
		// System.out.println(e.getMessage());
		//
		// entityFactory.instantiate(keyboardControllerTemplate, new ParametersWrapper() //
		// .put("controller", characterController) //
		// );
		// }

		Gdx.app.log(GameInformation.name, "Applying scene template...");
	}

}

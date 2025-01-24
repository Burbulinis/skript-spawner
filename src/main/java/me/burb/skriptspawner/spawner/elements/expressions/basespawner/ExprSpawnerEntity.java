package me.burb.skriptspawner.spawner.elements.expressions.basespawner;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Base Spawner - Spawner Entity")
@Description({
	"Get the spawner entity of a base spawner.",
	"This is the entity that the spawner will spawn and displays the small entity inside the spawner.",
	"Setting this will override any previous entries that have been added to potential spawns of the spawner",
	"You can set the spawner entity to an item, though that is paper-exclusive and only for spawners, not base spawners. "
		+ "Spawners are spawner minecarts and creature spawners.",
	"",
	"This expression gets the trial spawner configuration "
		+ "with the current state (i.e. ominous, normal) of the trial spawner block, if one is provided.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set {_entry} to a spawner entry with entity snapshot of a zombie:",
		"\tset weight to 5",
	"set spawner entity of event-block to {_entry}",
	"set spawner entity of event-block to entity snapshot of a zombie",
	"set spawner entity of event-block to event-entity",
	"set spawner entity of event-block to a pig",
	"set spawner entity of event-block to 16 golden apples # paper exclusive and only for spawners",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnerEntity extends SimplePropertyExpression<Object, EntitySnapshot> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntity.class, EntitySnapshot.class,
			"spawner [spawned] entity", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable EntitySnapshot convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object)) {
			return SpawnerUtils.getAsBaseSpawner(object).getSpawnedEntity();
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			// get current trial spawner config if a trial spawner block was specified
			TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);
			return SpawnerUtils.getCurrentTrialConfig(spawner).config().getSpawnedEntity();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(
				SpawnerEntry.class, EntitySnapshot.class, Entity.class, EntityData.class, ItemStack.class
			);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object value = delta != null ? delta[0] : null;

		boolean isItem = false;

		Consumer<BaseSpawner> consumer = spawner -> {};
		if (value instanceof SpawnerEntry entry) {
			consumer = spawner -> spawner.setSpawnedEntity(entry);
		} else if (value instanceof ItemStack item) {
			if (!Skript.methodExists(Spawner.class, "setSpawnedItem", ItemStack.class)) {
				error("Setting the spawner entity of '" + Classes.toString(value) + "' to an item is paper-exclusive.");
				return;
			}
			isItem = true;
			consumer = spawner -> ((Spawner) spawner).setSpawnedItem(item);
		}

		EntitySnapshot entitySnapshot;
		if (value instanceof EntitySnapshot snapshot) {
			entitySnapshot = snapshot;
		} else if (value instanceof Entity entity) {
			entitySnapshot = entity.createSnapshot();
		} else if (value instanceof EntityData<?> data) {
			Entity entity = data.create();
			if (entity == null)
				return;
			entitySnapshot = entity.createSnapshot();
		} else {
			entitySnapshot = null;
		}

		if (entitySnapshot != null)
			consumer = spawner -> spawner.setSpawnedEntity(entitySnapshot);

		for (Object object : getExpr().getArray(event)) {
			if (SpawnerUtils.isTrialSpawner(object)) {
				// get current trial spawner config if a trial spawner block was specified
				TrialSpawner trialSpawner = SpawnerUtils.getAsTrialSpawner(object);
				object = SpawnerUtils.getCurrentTrialConfig(trialSpawner);
			}

			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			if (isItem && !(spawner instanceof Spawner)) {
				error("'" + Classes.toString(spawner) + "' is not a spawner. Setting the spawner entity to an item "
					+ "is only for spawners, not base spawners.");
				continue;
			}

			consumer.accept(spawner);

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner spawned entity";
	}

}

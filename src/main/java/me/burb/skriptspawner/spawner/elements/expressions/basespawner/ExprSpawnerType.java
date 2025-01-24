package me.burb.skriptspawner.spawner.elements.expressions.basespawner;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;

@Name("Base Spawner - Spawner Type")
@Description({
	"Retrieves, sets, or resets the spawner's entity type",
	"This expression gets the trial spawner configuration "
		+ "with the current state (i.e. ominous, normal) of the trial spawner block, if one is provided.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set the spawner type of the target block to a zombie",
	"reset the spawner entity type of the target block",
	"delete the spawner entity type of the target block"
})
@Since("2.4, 2.9.2 (trial spawner), INSERT VERSION (trial spawner config)")
@RequiredPlugins("MC 1.21+ (since INSERT VERSION)")
public class ExprSpawnerType extends SimplePropertyExpression<Object, EntityData> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerType.class, EntityData.class,
			"spawner [entity|spawned] type[s]", "entities/blocks/trialspawnerconfigs");
	}

	public @Nullable EntityData<?> convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object)) {
			return EntityUtils.toSkriptEntityData(SpawnerUtils.getAsBaseSpawner(object).getSpawnedType());
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			// get current trial spawner config if a trial spawner block was specified
			TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);
			TrialSpawnerConfiguration config = SpawnerUtils.getCurrentTrialConfig(spawner).config();
			return EntityUtils.toSkriptEntityData(config.getSpawnedType());
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(EntityData.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EntityType type = delta != null ? EntityUtils.toBukkitEntityType((EntityData<?>) delta[0]) : null;

		for (Object object : getExpr().getArray(event)) {
			if (SpawnerUtils.isTrialSpawner(object)) {
				// get current trial spawner config if a trial spawner block was specified
				TrialSpawner trialSpawner = SpawnerUtils.getAsTrialSpawner(object);
				object = SpawnerUtils.getCurrentTrialConfig(trialSpawner);
			}

			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			assert spawner != null;

			switch (mode) {
				case SET -> spawner.setSpawnedType(type);
				case DELETE, RESET -> spawner.setSpawnedType(null);
			}

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entity type";
	}

}

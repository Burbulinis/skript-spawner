package me.burb.skriptspawner.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;

@Name("Base Spawner - Spawn Range")
@Description({
	"Get the radius of the area in which the spawner can spawn entities, by default 4.",
	"This expression gets the trial spawner configuration "
		+ "with the current state (i.e. ominous, normal) of the trial spawner block, if one is provided.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set the spawner spawn radius of the target block to 5",
	"add 2 to the spawner spawn radius of the target block",
	"remove 1 from the spawner spawn radius of the target block",
	"reset the spawner spawn radius of the target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+ 1.21+")
public class ExprSpawnRange extends SimplePropertyExpression<Object, Integer> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRange.class, Integer.class,
			"spawner spawn (radius|range)", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object)) {
			return SpawnerUtils.getAsBaseSpawner(object).getSpawnRange();
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			// get current trial spawner config if a trial spawner block was specified
			TrialSpawner trialSpawner = SpawnerUtils.getAsTrialSpawner(object);
			return SpawnerUtils.getCurrentTrialConfig(trialSpawner).config().getSpawnRange();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int range = delta != null ? ((Number) delta[0]).intValue() : 0;

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
				case SET -> spawner.setSpawnRange(range);
				case ADD -> spawner.setSpawnRange(spawner.getSpawnRange() + range);
				case REMOVE -> spawner.setSpawnRange(spawner.getSpawnRange() - range);
				case RESET -> spawner.setSpawnRange(4); // default value
			}

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn range";
	}

}

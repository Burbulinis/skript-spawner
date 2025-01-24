package me.burb.skriptspawner.spawner.elements.expressions.spawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;

@Name("Spawner - Maximum Nearby Entity Amount")
@Description({
	"Get the maximum amount of similar entities within the spawn range. This is 16 by default.",
	"The spawner will no longer spawn entities if the value was surpassed.",
	"",
	"Spawners are creature spawners and spawner minecarts."
})
@Examples({
	"set {_max} to spawner nearby entity amount of target block",
	"add 5 to spawner nearby entity amount of target block",
	"remove 2 from spawner nearby entity amount of target block",
	"reset spawner nearby entity amount of target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprMaxNearbyEntities extends SimplePropertyExpression<Object, Integer> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprMaxNearbyEntities.class, Integer.class,
			"spawner [max[imum]] [similar] nearby entity (value|amount)", "entities/blocks");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isSpawner(object))
			return SpawnerUtils.getAsSpawner(object).getMaxNearbyEntities();
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
		int count = delta != null ? ((int) delta[0]) : 0;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isSpawner(object))
				continue;

			Spawner spawner = SpawnerUtils.getAsSpawner(object);

			assert spawner != null;

			switch (mode) {
				case SET -> spawner.setMaxNearbyEntities(count);
				case ADD -> spawner.setMaxNearbyEntities(spawner.getMaxNearbyEntities() + count);
				case REMOVE -> spawner.setMaxNearbyEntities(spawner.getMaxNearbyEntities() - count);
				case RESET -> spawner.setMaxNearbyEntities(16); // default value
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
		return "max amount of nearby entities";
	}

}

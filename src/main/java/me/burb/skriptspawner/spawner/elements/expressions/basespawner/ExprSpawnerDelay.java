package me.burb.skriptspawner.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;

@Name("Base Spawner - Spawn Delay")
@Description({
	"The spawn delay of a base spawner is the time until the spawner will "
		+ "attempt to spawn its' potential spawns.",
	"If the spawner is inactive during the spawn attempt, the delay will remain as 0 seconds, "
		+ "and the spawner will attempt to spawn the potential spawns every tick until it is successful.",
	"After such successful attempt, the delay will be reset to a random value between the minimum and maximum "
		+ "spawn delays of the spawner.",
	"Keep in mind that this is not the case for trial spawner configurations. Their spawn delay will remain as 2 seconds "
		+ "instead of the time until the next spawn attempt.",
	"",
	"This expression gets the trial spawner configuration "
		+ "with the current state (i.e. ominous, normal) of the trial spawner block, if one is provided.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set the spawner delay of the target block to 5 seconds",
	"add 2 seconds to the spawner delay of the target block",
	"remove 1 second from the spawner delay of the target block",
	"reset the spawner delay of the target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnerDelay extends SimplePropertyExpression<Object, Timespan> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerDelay.class, Timespan.class,
			"spawner [spawn] delay", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Timespan convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object)) {
			return new Timespan(TimePeriod.TICK, SpawnerUtils.getAsBaseSpawner(object).getDelay());
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			// get current trial spawner config if a trial spawner block was specified
			TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);
			return new Timespan(TimePeriod.TICK, SpawnerUtils.getCurrentTrialConfig(spawner).config().getDelay());
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = delta != null ? (Timespan) delta[0] : null;

		long ticks = 0;
		if (timespan != null) {
			ticks = timespan.getAs(TimePeriod.TICK);
			if (ticks > Integer.MAX_VALUE)
				ticks = Integer.MAX_VALUE;
		}
		int ticksAsInt = (int) ticks;

		for (Object object : getExpr().getArray(event)) {
			if (SpawnerUtils.isTrialSpawner(object)) {
				// get current trial spawner config if a trial spawner block was specified
				TrialSpawner trialSpawner = SpawnerUtils.getAsTrialSpawner(object);
				object = SpawnerUtils.getCurrentTrialConfig(trialSpawner);
			}

			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner base = SpawnerUtils.getAsBaseSpawner(object);

			assert base != null;

			switch (mode) {
				case SET -> base.setDelay(ticksAsInt);
				case ADD -> base.setDelay(base.getDelay() + ticksAsInt);
				case REMOVE -> base.setDelay(base.getDelay() - ticksAsInt);
				case RESET -> base.setDelay(-1);
			}

			SpawnerUtils.updateState(base);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner delay";
	}

}

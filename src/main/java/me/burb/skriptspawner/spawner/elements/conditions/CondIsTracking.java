package me.burb.skriptspawner.spawner.elements.conditions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Spawner - Is Tracking")
@Description({
	"Check whether trial spawners or trial spawner configs are tracking players or entities.",
	"A player being tracked means the player has entered the activation range, meanwhile an entity being tracked means "
		+ "the entity was spawned by the trial spawner."
})
@Examples({
	"make the event-block start tracking player",
	"if the event-block is spawner player tracking player:",
		"\tsend \"indeed! you are being tracked..\""
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class CondIsTracking extends Condition {

	static {
		var info = SyntaxInfo.builder(CondIsTracking.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(CondIsTracking::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"%block/trialspawnerconfig% (is|are) [trial] spawner entity tracking %entities%",
				"%block/trialspawnerconfig% (is|are) [trial] spawner player tracking %players%",
				"%block/trialspawnerconfig% (isn't|is not|aren't|are not) [trial] spawner entity tracking %entities%",
				"%block/trialspawnerconfig% (isn't|is not|aren't|are not) [trial] spawner player tracking %entities%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.CONDITION, info);
	}

	private Expression<?> spawner, entities;
	private boolean player;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 2) {
			spawner = exprs[0];
			entities = exprs[1];
		} else {
			spawner = exprs[1];
			entities = exprs[0];
		}
		player = matchedPattern == 1 || matchedPattern == 3;
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Object object = this.spawner.getSingle(event);
		if (object == null)
			return isNegated();

		if (!SpawnerUtils.isTrialSpawner(object))
			return isNegated();

		TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);

		assert spawner != null;

		return entities.check(event, entity -> {
			if (player) {
				return spawner.isTrackingPlayer((Player) entity);
			} else {
				return spawner.isTrackingEntity((Entity) entity);
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(spawner);
		if (isNegated()) {
			builder.append("isn't tracking");
		} else {
			builder.append("is tracking");
		}
		builder.append(entities);

		return builder.toString();
	}

}

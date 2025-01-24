package me.burb.skriptspawner.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Base Spawner - Potential Spawns")
@Description({
	"Every spawn attempt, the spawner will pick a random entry "
		+ "from the list of potential spawner entries and spawn it."
		+ "The spawner entity will be overwritten to the "
		+ "entity snapshot of the highest weighted spawner entry from the list of potential spawns.",
	"",
	"This expression gets the trial spawner configuration "
		+ "with the current state (i.e. ominous, normal) of the trial spawner block, if one is provided.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set {_entry::*} to potential spawner spawns of target block",
	"add a spawner entry with entity snapshot of a zombie to potential spawner spawns of target block",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprPotentialSpawns extends PropertyExpression<Object, SpawnerEntry> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprPotentialSpawns.class, SpawnerEntry.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(ExprPotentialSpawns::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] potential spawner spawn[s] (of|from) %entities/blocks/trialspawnerconfigs%",
				"%entities/blocks/trialspawnerconfigs%'[s] potential spawner spawn[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected SpawnerEntry[] get(Event event, Object[] source) {
		List<SpawnerEntry> entries = new ArrayList<>();
		for (Object object : source) {
			if (SpawnerUtils.isTrialSpawner(object)) {
				// get current trial spawner config if a trial spawner block was specified
				TrialSpawner trialSpawner = SpawnerUtils.getAsTrialSpawner(object);
				object = SpawnerUtils.getCurrentTrialConfig(trialSpawner);
			}

			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);
			entries.addAll(spawner.getPotentialSpawns());
		}
		return entries.toArray(SpawnerEntry[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET, DELETE -> CollectionUtils.array(SpawnerEntry[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		List<SpawnerEntry> entries = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				entries.add((SpawnerEntry) object);
			}
		}

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

			List<SpawnerEntry> potentialSpawns = spawner.getPotentialSpawns();

			switch (mode) {
				case SET -> spawner.setPotentialSpawns(entries);
				case ADD -> potentialSpawns.addAll(entries);
				case REMOVE -> potentialSpawns.removeAll(entries);
				case RESET, DELETE -> spawner.setPotentialSpawns(new ArrayList<>());
			}

			// this is done because apparently there is a bug with the add method
			if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
				spawner.setPotentialSpawns(potentialSpawns);

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends SpawnerEntry> getReturnType() {
		return SpawnerEntry.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the potential spawns of " + getExpr().toString(event, debug);
	}

}

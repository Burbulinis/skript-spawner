package me.burb.skriptspawner.spawner.elements.expressions.trialconfig;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerUtils;
import me.burb.skriptspawner.spawner.util.TrialSpawnerConfig;
import me.burb.skriptspawner.spawner.util.WeightedLootTable;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("Trial Spawner Configuration with Weighted Loot Table")
@Description({
	"Returns the weighted loot tables of a trial spawner configuration.",
	"Weighted loot tables are loot tables with a weight, which determines the chance of the loot table "
		+ "being selected during the spawner's reward ejection state.",
	"Adding just a regular loot table to this list will default the weight to 1."
})
@Examples({
	"set {_loot tables::*} to weighted loot tables of {_trial config}",
	"add loot table \"minecraft:equipment/trial_chamber\" with weight 1 to weighted loot tables of {_trial config}",
	"add loot table \"minecraft:chests/simple_dungeon\" to weighted loot tables of {_trial config}",
	"# loot table with weight 1 ^"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprTrialConfigLootTables extends PropertyExpression<TrialSpawnerConfig, WeightedLootTable> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprTrialConfigLootTables.class, WeightedLootTable.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(ExprTrialConfigLootTables::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] weighted loot table[s] of %trialspawnerconfigs%",
				"%trialspawnerconfigs%'[s] weighted loot table[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends TrialSpawnerConfig>) exprs[0]);
		return true;
	}

	@Override
	protected WeightedLootTable[] get(Event event, TrialSpawnerConfig[] source) {
		List<WeightedLootTable> rewards = new ArrayList<>();
		for (TrialSpawnerConfig config : source) {
			for (Map.Entry<LootTable, Integer> entrySet : config.config().getPossibleRewards().entrySet()) {
				rewards.add(new WeightedLootTable(entrySet.getKey(), entrySet.getValue()));
			}
		}

		return rewards.toArray(WeightedLootTable[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(WeightedLootTable[].class, LootTable[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		for (TrialSpawnerConfig trialConfig : getExpr().getArray(event)) {
			Map<LootTable, Integer> possibleRewards = null;
			if (mode == ChangeMode.SET)
				possibleRewards = new HashMap<>();

			TrialSpawnerConfiguration config = trialConfig.config();

			for (Object object : delta) {
				if (object instanceof LootTable lootTable) {
					switch (mode) {
						case SET -> possibleRewards.put(lootTable, 1);
						case ADD -> config.addPossibleReward(lootTable, 1);
						case REMOVE -> config.removePossibleReward(lootTable);
					}
				} else if (object instanceof WeightedLootTable weightedTable) {
					switch (mode) {
						case SET -> possibleRewards.put(weightedTable.getLootTable(), weightedTable.spawnerWeight());
						case ADD -> config.addPossibleReward(weightedTable.getLootTable(), weightedTable.spawnerWeight());
						case REMOVE -> config.removePossibleReward(weightedTable.getLootTable());
					}
				}
			}

			if (mode == ChangeMode.SET)
				config.setPossibleRewards(possibleRewards);

			SpawnerUtils.updateState(trialConfig.state());
		}
	}

	@Override
	public Class<? extends WeightedLootTable> getReturnType() {
		return WeightedLootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "weighted loot table of " + getExpr().toString(event, debug);
	}

}

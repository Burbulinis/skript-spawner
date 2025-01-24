package me.burb.skriptspawner.spawner.elements.expressions.spawnrule;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawn Rule - Block Light Spawn Level")
@Description({
	"Returns the minimum/maximum block light spawn levels of a spawn rule. "
		+ "The block light spawn levels determine the light level of the block "
		+ "that the spawner entry will spawn entities.",
	"Note that the block light spawn levels must be between 0 and 15 "
		+ ", the minimum block light spawn level must be less than or equal to "
		+ "the maximum block light spawn level and vice versa."
})
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a zombie:",
		"\tset the weight to 5",
		"\tset the spawn rule to a spawn rule:",
			"\t\tset the minimum block light spawn level to 10",
			"\t\tset the maximum block light spawn level to 15",
			"\t\tset the minimum sky light spawn level to 5",
			"\t\tset the maximum sky light spawn level to 15",
	"set spawner entity of event-block to {_entry}"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnRuleBlockLight extends SimplePropertyExpression<SpawnRule, Integer> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRuleBlockLight.class, Integer.class,
				"(1:max|min)[imum] block light [entity] spawn [rule] (level|value)", "spawnrules"
		);
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(SpawnRule rule) {
		if (max)
			return rule.getMaxBlockLight();
		return rule.getMinBlockLight();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		int light = ((int) delta[0]);

		if (light > 15) {
			error("The block light spawn level cannot be greater than 15, thus setting it to a value larger than 15 will do nothing.");
			return;
		} else if (light < 0) {
			error("The block light spawn level cannot be less than 0, thus setting it to a value less than 0 will do nothing.");
			return;
		}

		for (SpawnRule rule : getExpr().getArray(event)) {
			int minMax;
			if (max) {
				minMax = rule.getMaxBlockLight();
			} else {
				minMax = rule.getMinBlockLight();
			}

			int value = switch (mode) {
				case SET -> light;
				case ADD -> minMax + light;
				case REMOVE -> minMax - light;
				default -> 0; // should never happen
			};

			if (max) {
				rule.setMaxBlockLight(value);
			} else {
				rule.setMinBlockLight(value);
			}

			String error = getErrorMessage(value, max ? rule.getMinBlockLight() : rule.getMaxBlockLight());
			if (!error.isEmpty())
				error(error);
		}
	}

	private String getErrorMessage(int value, int compare) {
		if (value > 15) {
			return "The block light spawn level cannot be greater than 15, thus setting it to a value larger than 15 will do nothing.";
		} else if (value < 0) {
			return "The block light spawn level cannot be less than 0, thus setting it to a value less than 0 will do nothing.";
		}

		if (max && value < compare) {
			return "The maximum block light level cannot be less than the minimum block light level, "
				+ " thus setting it to a value less than the minimum block light level will do nothing.";
		} else if (!max && value > compare) {
			return "The minimum block light spawn level cannot be greater than the maximum block light spawn level, "
				+ "thus setting it to a value greater than the maximum block light spawn level will do nothing.";
		}

		return "";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (max ? "max" : "min") + " block light spawn level";
	}

}

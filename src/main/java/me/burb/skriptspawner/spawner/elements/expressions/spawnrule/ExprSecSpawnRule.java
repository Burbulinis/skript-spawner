package me.burb.skriptspawner.spawner.elements.expressions.spawnrule;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnRuleWrapper;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Create Spawn Rule")
@Description({
	"Create a spawn rule. Spawn rules are used to determine the conditions under "
		+ "which the spawner entry will spawn entities. That is, the min/max block light spawn level, "
		+ "min/max sky light spawn level."
})
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a zombie:",
		"\tset the weight to 5",
		"\tset the spawn rule to a spawn rule:",
			"\t\tset the minimum block light spawn level to 10",
			"\t\tset the maximum block light spawn level to 15",
			"\t\tset the minimum sky light spawn level to 5",
			"\t\tset the maximum sky light spawn level to 15",
	"add {_entry} to potential spawns of target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSecSpawnRule extends SectionExpression<SpawnRule> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSecSpawnRule.class, SpawnRule.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(ExprSecSpawnRule::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[a] spawn rule")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);

		EventValues.registerEventValue(SpawnRuleCreateEvent.class, SpawnRule.class, SpawnRuleCreateEvent::getSpawnRule);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result,
	                    @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null)
			//noinspection unchecked
			trigger = loadCode(node, "create spawn rule", null, SpawnRuleCreateEvent.class);
		return true;
	}

	@Override
	protected SpawnRule @Nullable [] get(Event event) {
		SpawnRule rule = new SpawnRuleWrapper(0, 0, 0, 0);
		if (trigger != null) {
			SpawnRuleCreateEvent createEvent = new SpawnRuleCreateEvent(rule);
			Variables.withLocalVariables(event, createEvent, () ->
				TriggerItem.walk(trigger, createEvent)
			);
		}
		return new SpawnRule[]{rule};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnRule> getReturnType() {
		return SpawnRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spawn rule";
	}

	public static class SpawnRuleCreateEvent extends Event {

		private final SpawnRule rule;

		public SpawnRuleCreateEvent(SpawnRule rule) {
			this.rule = rule;
		}

		public SpawnRule getSpawnRule() {
			return rule;
		}

		@Override
		public HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}

	}

}

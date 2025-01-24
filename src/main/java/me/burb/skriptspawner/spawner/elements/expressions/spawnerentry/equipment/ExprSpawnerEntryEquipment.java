package me.burb.skriptspawner.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerEntryEquipment;
import me.burb.skriptspawner.spawner.util.SpawnerEntryEquipment.Drops;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

@Name("Spawner Entry Equipment")
@Description({
	"Returns an equipment loot table with the given drop chances.",
	"The loot table must be an equipment loot table, otherwise the entities will spawn naked."
})
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a skeleton:",
		"\tset {_dropchances::*} to helmet slot with drop chance 50%, chestplate slot with drop chance 25%",
		"\tset spawner equipment loot table to loot table \"minecraft:equipment/trial_chamber\" with {_dropchances::*}",
	"set spawner entity of event-block to {_entry}",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnerEntryEquipment extends SimpleExpression<SpawnerEntryEquipment> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSpawnerEntryEquipment.class, SpawnerEntryEquipment.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(ExprSpawnerEntryEquipment::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("%loottable% with [drop chance[s]] %equipmentdropchances%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private Expression<LootTable> lootTable;
	private Expression<Drops> chances;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		chances = (Expression<Drops>) exprs[1];
		return true;
	}

	@Override
	protected SpawnerEntryEquipment @Nullable [] get(Event event) {
		LootTable lootTable = this.lootTable.getSingle(event);
		if (lootTable == null)
			return null;

		List<Drops> chances = Arrays.asList(this.chances.getArray(event));

		return new SpawnerEntryEquipment[]{new SpawnerEntryEquipment(lootTable, chances)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnerEntryEquipment> getReturnType() {
		return SpawnerEntryEquipment.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("spawner equipment with", lootTable, "and", chances);

		return builder.toString();
	}

}

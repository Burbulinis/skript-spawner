package me.burb.skriptspawner.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.spawner.util.SpawnerEntryEquipment;
import me.burb.skriptspawner.spawner.util.SpawnerEntryEquipment.Drops;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Spawner Entry - Equipment with Drops")
@Description("Returns the drops of a spawner entry equipment.")
@Examples("set {_chances::*} to spawner drop chances of {_equipment}")
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprEquipmentWithDropChances extends PropertyExpression<SpawnerEntryEquipment, Drops> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprEquipmentWithDropChances.class, Drops.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(ExprEquipmentWithDropChances::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] spawner drop chance[s] (from|of) %spawnerentryequipments%",
				"%spawnerentryequipments%'[s] spawner drop chance[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends SpawnerEntryEquipment>) exprs[0]);
		return true;
	}

	@Override
	protected Drops[] get(Event event, SpawnerEntryEquipment[] source) {
		List<Drops> drops = new ArrayList<>();
		for (SpawnerEntryEquipment equipment : source) {
			drops.addAll(equipment.getDropChances());
		}
		return drops.toArray(Drops[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(Drops[].class, EquipmentSlot[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		for (var entry : getExpr().getArray(event)) {
			if (mode == ChangeMode.SET)
				entry.setDropChances(new ArrayList<>());

			for (Object object : delta) {
				if (object instanceof Drops chance) {
					switch (mode) {
						case SET, ADD -> entry.addDropChance(chance);
						case REMOVE -> entry.removeDropChance(chance);
					}
				} else if (object instanceof EquipmentSlot slot) {
					switch (mode) {
						case SET, ADD -> entry.addDropChance(new Drops(slot, 1));
						case REMOVE -> entry.removeDropChance(new Drops(slot, 1));
					}
				}
			}
		}
	}

	@Override
	public Class<? extends Drops> getReturnType() {
		return Drops.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spawner entry drop chances of " + getExpr().toString(event, debug);
	}

}

package me.burb.skriptspawner.spawner.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import me.burb.skriptspawner.spawner.util.lang.AnySpawnerWeighted;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Weight")
@Description("Returns the weight of something that has a weight, e.g. a spawner entry.")
@Examples({
		"set {_weight} to weight of {_weighted}",
		"add 5 to weight of {_weighted}",
		"remove 2 from weight of {_weighted}"
})
@Since("INSERT VERSION")
public class ExprSpawnerWeight extends SimplePropertyExpression<Object, Integer> {

	static {
		registerDefault(ExprSpawnerWeight.class, Integer.class, "spawner weight", "weighteds");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (object instanceof AnySpawnerWeighted weighted)
			return weighted.spawnerWeight();
		assert false;
		return null;
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
		int weight = (int) delta[0];

		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof AnySpawnerWeighted weighted))
				continue;

			if (!weighted.supportsSpawnerWeightChange()) {
				error("The weight of '" + Classes.toString(object) + "' cannot be changed.");
				continue;
			}

			switch (mode) {
				case SET -> weighted.setSpawnerWeight(weight);
				case ADD -> weighted.setSpawnerWeight(weighted.spawnerWeight() + weight);
				case REMOVE -> weighted.setSpawnerWeight(weighted.spawnerWeight() - weight);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "weight";
	}

}

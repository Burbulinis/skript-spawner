package me.burb.skriptspawner.spawner.util.lang;

import ch.njol.skript.lang.util.common.AnyProvider;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything with a spawner weight.
 * Anything implementing this (or convertible to this) can be used by the {@link me.burb.skriptspawner.spawner.elements.expressions.ExprSpawnerWeight}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnySpawnerWeighted extends AnyProvider {

	/**
	 * @return This thing's spawner weight
	 */
	@UnknownNullability Integer spawnerWeight();

	/**
	 * This is called before {@link #setSpawnerWeight(Integer)}.
	 * If the result is false, setting the weight will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsSpawnerWeightChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's spawner weight, if possible.
	 * If not possible, then {@link #supportsSpawnerWeightChange()} should return false and this
	 * may throw an error.
	 *
	 * @param weight The weight to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setSpawnerWeight(Integer weight) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
package me.burb.skriptspawner.spawner.util;

import org.bukkit.block.TrialSpawner;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for {@link TrialSpawnerConfiguration}. This is used because trial spawner states
 * need to be updated once their configuration is changed.
 * @param config the config
 * @param state the state of the trial spawner
 * @param ominous whether the config is ominous
 */
public record TrialSpawnerConfig(@NotNull TrialSpawnerConfiguration config, @NotNull TrialSpawner state,
                                 boolean ominous) {}

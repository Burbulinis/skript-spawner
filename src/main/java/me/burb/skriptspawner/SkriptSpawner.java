package me.burb.skriptspawner;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import me.burb.skriptspawner.spawner.SpawnerModule;
import me.burb.skriptspawner.util.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SkriptSpawner extends JavaPlugin {

	static final int[] EARLIEST_VERSION = new int[]{1, 21};

	private static SkriptSpawner instance;
	private boolean properlyLoaded = true;
	private PluginManager pluginManager;
	private SkriptAddon addon;
	private Plugin skriptPlugin;

	@Override
	public void onEnable() {
		instance = this;
		this.pluginManager = getServer().getPluginManager();
		this.skriptPlugin = pluginManager.getPlugin("Skript");

		properlyLoaded = canLoadPlugin();
	}

	boolean canLoadPlugin() {
		if (skriptPlugin == null) {
			Utils.logLoading("&cDependency Skript was not found, Skript elements cannot load.");
			return false;
		}
		if (!skriptPlugin.isEnabled()) {
			Utils.logLoading("&cDependency Skript is not enabled, Skript elements cannot load.");
			Utils.logLoading("&cThis could mean skript-spawner is being forced to load before Skript.");
			return false;
		}
		Version skriptVersion = Skript.getVersion();
		if (skriptVersion.isSmallerThan(new Version(2, 9, 999))) {
			Utils.logLoading("&cSkript is outdated. Skript-spawner requires 2.10+ but found Skript " + skriptVersion);
			return false;
		}
		if (!Skript.isAcceptRegistrations()) {
			Utils.logLoading("&cSkript is no longer accepting registrations, addons can no longer be loaded!");
			if (isPlugmanReloaded()) {
				Utils.logLoading("&cIt appears you're running PlugMan.");
				Utils.logLoading("&cIf you're trying to reload/enable skript-spawner with PlugMan.... you can't.");
				Utils.logLoading("&ePlease restart your server!");
			} else {
				Utils.logLoading("&cNo clue how this could happen.");
				Utils.logLoading("&cSeems a plugin is delaying skript-spawner loading, which is after Skript stops accepting registrations.");
			}
			return false;
		}
		Version version = new Version(SkriptSpawner.EARLIEST_VERSION);
		if (!Skript.isRunningMinecraft(version))
			Utils.logLoading("&cYour server version &7'&bMC %s&7'&c is not supported, only &7'&bMC %s+&7'&c is supported!", Skript.getMinecraftVersion(), version);

		loadSkriptElements();
		loadMetrics();
		return true;
	}

	private void loadSkriptElements() {
		this.addon = Skript.registerAddon(this);
		this.addon.setLanguageFileDirectory("lang");

		Skript.instance().loadModules(new SpawnerModule());
	}

	private void loadMetrics() { // 24541
		new Metrics(this, 24541);
	}

	private boolean isPlugmanReloaded() {
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			if (stackTraceElement.toString().contains("rylinaux.plugman.command.")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		Utils.clearDebugs();
	}

	public static @NotNull SkriptSpawner getInstance() {
		return instance;
	}

}

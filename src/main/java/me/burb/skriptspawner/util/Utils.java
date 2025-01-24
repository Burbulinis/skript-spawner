package me.burb.skriptspawner.util;

import ch.njol.skript.Skript;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Version;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General utility class
 */
public class Utils {

	private static final String PREFIX = "&7[skript-spawner] ";
	private static final String PREFIX_ERROR = "&7[&cskript-spawner ERROR&7] ";
	private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f\\d]){6}>");
	private static final boolean SKRIPT_IS_THERE = Bukkit.getPluginManager().getPlugin("Skript") != null;

	// QuickLinks
	public static final String MCWIKI_TICK_COMMAND = "See [**Tick Command**](https://minecraft.wiki/w/Commands/tick) on McWiki for more details.";

	// Shortcut for finding stuff to remove later
	public static final boolean IS_RUNNING_SKRIPT_2_10 = Skript.getVersion().isLargerThan(new Version(2, 9, 999));
	public static final boolean IS_RUNNING_MC_1_21 = Skript.isRunningMinecraft(1, 21);

	@SuppressWarnings("deprecation") // Paper deprecation
	public static String getColString(String string) {
		Matcher matcher = HEX_PATTERN.matcher(string);
		if (SKRIPT_IS_THERE) {
			while (matcher.find()) {
				final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
				final String before = string.substring(0, matcher.start());
				final String after = string.substring(matcher.end());
				string = before + hexColor + after;
				matcher = HEX_PATTERN.matcher(string);
			}
		} else {
			string = HEX_PATTERN.matcher(string).replaceAll("");
		}
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static void sendColMsg(CommandSender receiver, String format, Object... objects) {
		receiver.sendMessage(getColString(String.format(format, objects)));
	}

	public static void log(String format, Object... objects) {
		String log = String.format(format, objects);
		Bukkit.getConsoleSender().sendMessage(getColString(PREFIX + log));
	}

	public static void skriptError(String format, Object... objects) {
		String error = String.format(format, objects);
		Skript.error(getColString(PREFIX_ERROR + error), ErrorQuality.SEMANTIC_ERROR);
	}

	private static final List<String> DEBUGS = new ArrayList<>();

	public static void logLoading(String format, Object... objects) {
		String form = String.format(format, objects);
		DEBUGS.add(form);
		log(form);
	}

	public static List<String> getDebugs() {
		return DEBUGS;
	}

	public static void clearDebugs() {
		DEBUGS.clear();
	}

}
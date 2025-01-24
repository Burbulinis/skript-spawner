package me.burb.skriptspawner.spawner.elements.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import me.burb.skriptspawner.spawner.SpawnerModule;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Spawner - Make Ominous")
@Description("Make a trial spawner or its block data ominous or normal.")
@Examples({
	"if event-block is ominous:",
		"\tmake the trial spawner state of event-block normal",
	"else:",
		"\tmake the trial spawner state of event-block ominous"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class EffMakeOminous extends Effect {

	static {
		var info = SyntaxInfo.builder(EffMakeOminous.class)
			.origin(SyntaxOrigin.of(SpawnerModule.ADDON))
			.supplier(EffMakeOminous::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns("make [the] trial spawner state of %blocks/blockdatas% (:ominous|normal)")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
	}

	private boolean ominous;
	private Expression<?> spawners;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ominous = parseResult.hasTag("ominous");
		spawners = exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : spawners.getArray(event)) {
			if (object instanceof BlockData data && data instanceof TrialSpawner spawner) {
				spawner.setOminous(ominous);
			} else if (object instanceof Block block && block.getState() instanceof org.bukkit.block.TrialSpawner spawner) {
				spawner.setOminous(ominous);
				spawner.update(true, false);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("make the trial spawner state of", spawners);
		if (ominous) {
			builder.append("ominous");
		} else {
			builder.append("regular");
		}

		return builder.toString();
	}

}

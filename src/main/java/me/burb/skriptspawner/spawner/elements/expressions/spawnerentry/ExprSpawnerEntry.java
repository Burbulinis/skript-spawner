package me.burb.skriptspawner.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import me.burb.skriptspawner.spawner.SpawnerModule;
import org.bukkit.block.spawner.SpawnerEntry;

@Name("Spawner Entry")
@Description("The spawner entry used in the create spawner entry section.")
@Examples("the spawner entry")
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnerEntry extends EventValueExpression<SpawnerEntry> {

    static {
        register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntry.class, SpawnerEntry.class,
	        "[the] spawner entry");
    }

    public ExprSpawnerEntry() {
        super(SpawnerEntry.class);
    }

    @Override
    public String toString() {
        return "the spawner entry";
    }

}

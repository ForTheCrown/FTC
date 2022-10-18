package net.forthecrown.useables.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.useables.AbstractCheckable;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.datafix.UsablesJsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;

@Getter
public abstract class CommandUsable extends AbstractCheckable implements CheckHolder, HoverEventSource<Component> {
    private final String name;

    public CommandUsable(String name) {
        this.name = name;
    }

    public CommandUsable(String name, JsonObject json) throws CommandSyntaxException {
        this(name);
        UsablesJsonReader.loadChecks(this, json);
    }

    public CommandUsable(String name, CompoundTag tag) throws CommandSyntaxException {
        this(name);
        loadChecks(tag);
    }

    protected abstract void save(CompoundTag tag);

    public Component name() {
        return Component.text(getName());
    }

    public Component displayName() {
        return name().hoverEvent(this);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        saveChecks(tag);
        save(tag);

        return tag;
    }

    public boolean interact(Player player) {
        if (!testInteraction(player)) {
            return false;
        }

        return onInteract(player);
    }

    public abstract boolean onInteract(Player player);
}
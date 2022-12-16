package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.event.Event;

public interface ThrowingListener<T extends Event> {
    void execute(T event) throws CommandSyntaxException;
}
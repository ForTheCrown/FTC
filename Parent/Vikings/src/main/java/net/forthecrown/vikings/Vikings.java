package net.forthecrown.vikings;

import net.kyori.adventure.key.Namespaced;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

public final class Vikings extends JavaPlugin implements Namespaced {

    public static Vikings inst;
    public static Logger logger;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();
    }

    @Override
    public void onDisable() {
    }

    public static Namespaced namespaced(){
        return inst;
    }

    @Override
    public @NonNull String namespace() {
        return getName().toLowerCase();
    }
}

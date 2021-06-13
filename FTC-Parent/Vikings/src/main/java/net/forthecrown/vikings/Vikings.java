package net.forthecrown.vikings;

import net.kyori.adventure.key.Namespaced;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.intellij.lang.annotations.Pattern;

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

    @Pattern("[a-z0-9_\\-.]+")
    @Override
    public @NonNull String namespace() {
        return getName().toLowerCase();
    }
}

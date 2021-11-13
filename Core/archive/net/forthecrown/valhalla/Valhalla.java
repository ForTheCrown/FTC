package net.forthecrown.valhalla;

import com.sk89q.worldedit.regions.Region;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.registry.Registry;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import org.bukkit.Location;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Valhalla extends Namespaced {
    String VIKING_NAMESPACE = "vikings";
    KeyArgument KEY_PARSER = KeyArgument.key(VIKING_NAMESPACE);

    static Valhalla getInstance() {
        return ValhallaEngine.instance;
    }

    static Key vikingKey(String value) {
        return Key.key(VIKING_NAMESPACE, value);
    }

    void saveAll();

    VikingRaid getRaid(Key key);

    VikingRaid addRaid(Key key, Location startLoc, Region region);

    Registry<VikingRaid> getLoadedRaids();

    Set<Key> getExistingRaids();

    RaidSerializer getSerializer();

    void setActiveRaid(ActiveRaid activeRaid);

    ActiveRaid getActiveRaid();

    default boolean activeRaidExists() {
        return getActiveRaid() != null;
    }

    @Pattern("[a-z0-9_\\-.]+")
    @Override
    @NotNull
    default String namespace() {
        return VIKING_NAMESPACE;
    }
}

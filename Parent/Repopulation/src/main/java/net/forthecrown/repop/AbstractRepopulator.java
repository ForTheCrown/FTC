package net.forthecrown.repop;

import net.querz.nbt.tag.CompoundTag;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.function.Predicate;

public abstract class AbstractRepopulator implements Predicate<CompoundTag> {

    protected final World world;

    public AbstractRepopulator(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void initiate(){
        Validate.isTrue(Bukkit.getOnlinePlayers().size() < 1, "Server must be empty to initiate repopulator");


    }

    protected abstract PopulatorScanResult scan();
    protected abstract void repopulate(PopulatorScanResult result);
}

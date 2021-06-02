package net.forthecrown.repop;

import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;

import java.util.Objects;

public class EndRepopulator extends AbstractRepopulator {
    public EndRepopulator() {
        super(Objects.requireNonNull(Bukkit.getWorld("world_the_end")));
    }

    @Override
    protected PopulatorScanResult scan() {
        return null;
    }

    @Override
    protected void repopulate(PopulatorScanResult result) {

    }

    @Override
    public boolean test(CompoundTag entries) {
    }
}

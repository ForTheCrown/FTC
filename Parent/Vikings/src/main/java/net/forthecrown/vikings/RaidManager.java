package net.forthecrown.vikings;

import net.forthecrown.emperor.registry.BaseRegistry;
import net.forthecrown.emperor.registry.Registry;
import net.forthecrown.vikings.valhalla.RaidSerializer;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;
import net.forthecrown.vikings.valhalla.triggers.TriggerRegistry;
import net.forthecrown.vikings.valhalla.triggers.checks.CheckBoundingBox;

import java.util.function.Supplier;

public class RaidManager {
    public RaidSerializer serializer;
    public TriggerRegistry registry;
    public BaseRegistry<VikingRaid> raidRegistry;

    public RaidManager() {
        serializer = new RaidSerializer();
        registry = new TriggerRegistry();
    }

    public void loadDefaults(){
        Registry<Supplier<TriggerCheck<?>>> cReg = registry.checkRegistry();

        cReg.register(CheckBoundingBox.ENTER_KEY, () -> new CheckBoundingBox(false));
        cReg.register(CheckBoundingBox.EXIT_KEY, () -> new CheckBoundingBox(true));
    }

    public RaidSerializer getSerializer() {
        return serializer;
    }

    public TriggerRegistry getRegistry() {
        return registry;
    }

    public BaseRegistry<VikingRaid> getRaidRegistry() {
        return raidRegistry;
    }
}

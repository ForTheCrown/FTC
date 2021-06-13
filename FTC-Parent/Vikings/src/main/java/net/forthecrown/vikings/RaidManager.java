package net.forthecrown.vikings;

import net.forthecrown.core.registry.BaseRegistry;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.vikings.utils.CheckProvider;
import net.forthecrown.vikings.valhalla.RaidSerializer;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.triggers.TriggerRegistry;
import net.forthecrown.vikings.valhalla.triggers.checks.CheckBoundingBox;

public class RaidManager {
    private final RaidSerializer serializer;
    private final TriggerRegistry registry;
    private final BaseRegistry<VikingRaid> raidRegistry;

    public RaidManager() {
        serializer = new RaidSerializer();
        registry = new TriggerRegistry();
        raidRegistry = new BaseRegistry<>();
    }

    public void loadDefaults(){
        Registry<CheckProvider> cReg = registry.checkRegistry();

        cReg.register(CheckBoundingBox.ENTER_KEY, () -> new CheckBoundingBox(false));
        cReg.register(CheckBoundingBox.EXIT_KEY, () -> new CheckBoundingBox(true));
    }

    public RaidSerializer getSerializer() {
        return serializer;
    }

    public TriggerRegistry getRegistry() {
        return registry;
    }

    public Registry<VikingRaid> getRaidRegistry() {
        return raidRegistry;
    }
}

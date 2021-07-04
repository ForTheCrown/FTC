package net.forthecrown.registry;

import net.forthecrown.useables.UsageCheck;
import net.kyori.adventure.key.Key;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

public class CrownCheckRegistry extends BaseRegistry<Supplier<UsageCheck>> implements CheckRegistry {

    private boolean accepting;

    @Override
    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    @Override
    public Supplier<UsageCheck> register(Key key, Supplier<UsageCheck> raw) {
        Validate.isTrue(accepting, "Registry is no longer acceping registrations");
        //Needed because if kits or some other usable doesn't have the registered item, it'll kill the entire plugin
        //So you've gotta register everything before usables are loaded

        return super.register(key, raw);
    }
}

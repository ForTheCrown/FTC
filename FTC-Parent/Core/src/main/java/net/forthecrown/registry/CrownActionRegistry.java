package net.forthecrown.registry;

import net.forthecrown.useables.UsageAction;
import net.kyori.adventure.key.Key;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

public class CrownActionRegistry extends BaseRegistry<Supplier<UsageAction>> implements ActionRegistry {

    private boolean accepting;

    @Override
    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    @Override
    public Supplier<UsageAction> register(Key key, Supplier<UsageAction> raw) {
        Validate.isTrue(accepting, "Registry is no longer acceping registrations");
        //Needed because if kits or some other usable doesn't have the registered item, it'll kill the entire plugin
        //So you've gotta register everything before usables are loaded

        return super.register(key, raw);
    }
}

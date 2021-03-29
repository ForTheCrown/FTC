package net.forthecrown.vikings.blessings;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.vikings.Vikings;

public class FastRunner extends VikingBlessing {
    public FastRunner() {
        super("FastRunner", Vikings.getInstance());
    }

    @Override
    public void onPlayerEquip(CrownUser user) {
        user.getPlayer().setWalkSpeed(0.25f);
    }

    @Override
    public void onPlayerUnequip(CrownUser user) {
        user.getPlayer().setWalkSpeed(0.2f);
    }
}

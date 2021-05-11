package net.forthecrown.vikings.valhalla.utils;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.vikings.valhalla.RaidBattle;

public interface TriggerExecutor {
    void execute(RaidBattle battle, CrownUser user);
}

package net.forthecrown.vikings.valhalla.utils;

import net.forthecrown.vikings.valhalla.RaidBattle;

public interface TriggerCaller {
    boolean shouldCall(RaidBattle battle);
}

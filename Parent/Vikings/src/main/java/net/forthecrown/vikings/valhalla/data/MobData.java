package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonElement;
import net.forthecrown.vikings.valhalla.active.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;

public class MobData implements RaidData {

    public MobData() {
    }

    public MobData(JsonElement element){

    }

    @Override
    public void generate(RaidParty party, BattleBuilder generator) {

    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}

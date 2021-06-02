package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonElement;
import net.forthecrown.vikings.valhalla.active.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;

import java.util.ArrayList;
import java.util.List;

public class LootData implements RaidData {

    public final List<ChestGrouping> chestGroups = new ArrayList<>();

    public LootData() {
    }

    public LootData(JsonElement element){

    }

    @Override
    public void generate(RaidParty party, BattleBuilder generator) {
    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}

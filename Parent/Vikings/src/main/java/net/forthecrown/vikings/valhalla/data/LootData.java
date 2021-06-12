package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonElement;
import net.forthecrown.emperor.utils.BlockPos;
import net.forthecrown.vikings.valhalla.active.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;
import net.minecraft.server.v1_16_R3.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootData implements RaidData {

    public final List<ChestGrouping> chestGroups = new ArrayList<>();
    public final Map<BlockPos, LootTable> definiteChests = new HashMap<>();

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

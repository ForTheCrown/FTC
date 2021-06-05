package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonElement;
import net.forthecrown.emperor.utils.BlockPos;
import net.forthecrown.vikings.valhalla.active.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;

import java.util.ArrayList;
import java.util.List;

public class WorldData implements RaidData {

    private final List<BlockPos> exemptBlocks = new ArrayList<>();

    public WorldData() {
    }

    public WorldData(JsonElement element){

    }

    @Override
    public void generate(RaidParty party, BattleBuilder generator) {

    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}

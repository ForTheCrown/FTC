package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonElement;
import net.forthecrown.core.utils.BlockPos;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
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
    public void create(RaidParty party, BattleBuilder generator) {

    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeCollection(exemptBlocks, BlockPos::serialize);
    }
}

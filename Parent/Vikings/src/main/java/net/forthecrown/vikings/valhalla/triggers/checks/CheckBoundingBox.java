package net.forthecrown.vikings.valhalla.triggers.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.utils.CrownBoundingBox;
import net.forthecrown.emperor.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.vikings.VikingUtils;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.active.ActiveRaid;
import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckBoundingBox implements TriggerCheck<PlayerMoveEvent> {
    public static final Key ENTER_KEY = Key.key(Vikings.inst, "enter_region");
    public static final Key EXIT_KEY = Key.key(Vikings.inst, "exit_region");

    private CrownBoundingBox box;
    private final boolean exit;

    public CheckBoundingBox(boolean exit){
        this.exit = exit;
    }

    @Override
    public void deserialize(JsonElement element) throws CommandSyntaxException {
        this.box = JsonUtils.deserializeBoundingBox(element.getAsJsonObject());
    }

    @Override
    public void parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        this.box = VikingUtils.parseRegion(reader, source);
    }

    @Override
    public boolean check(Player player, ActiveRaid raid, PlayerMoveEvent event) {
        return false;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeBoundingBox(box);
    }

    @Override
    public @NonNull Key key() {
        return exit ? EXIT_KEY : ENTER_KEY;
    }
}

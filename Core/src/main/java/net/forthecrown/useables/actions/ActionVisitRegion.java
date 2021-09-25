package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionVisitRegion implements UsageAction<ActionVisitRegion.ActionInstance> {
    public static final Key KEY = Crown.coreKey("visit_region");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(RegionPos.parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(RegionPos.fromString(element.getAsString()));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return new JsonPrimitive(value.getPos().toString());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class ActionInstance implements UsageActionInstance {
        private final RegionPos pos;

        public ActionInstance(RegionPos pos) {
            this.pos = pos;
        }

        public RegionPos getPos() {
            return pos;
        }

        @Override
        public void onInteract(Player player) {
            CrownUser user = UserManager.getUser(player);
            PopulationRegion region = Crown.getRegionManager().get(getPos());

            ActionFactory.visitRegion(user, region);
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "region=" + pos + '}';
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}

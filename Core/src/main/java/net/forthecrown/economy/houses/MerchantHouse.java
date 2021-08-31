package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.guild.GuildVoter;
import net.forthecrown.economy.market.guild.VoteState;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public class MerchantHouse implements Keyed, JsonSerializable, Nameable, GuildVoter {
    private final String name;
    private final Key key;

    public MerchantHouse(String name) {
        this.name = name;

        key = Crown.coreKey(name.toLowerCase().replaceAll(" ", "_"));
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key());
    }

    @Override
    public void vote(VoteState state) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public @NotNull Key key() {
        return null;
    }
}

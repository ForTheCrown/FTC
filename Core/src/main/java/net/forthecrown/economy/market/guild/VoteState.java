package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;

import java.util.UUID;

public class VoteState implements JsonSerializable, JsonDeserializable {
    private final ObjectList<UUID> pro = new ObjectArrayList<>();
    private final ObjectList<UUID> against = new ObjectArrayList<>();

    public void voteFor(UUID id) {
        pro.add(id);
    }

    public void voteAgainst(UUID id) {
        against.add(id);
    }

    public ObjectList<UUID> getAgainst() {
        return against;
    }

    public ObjectList<UUID> getPro() {
        return pro;
    }

    public VoteResult countVotes(TradersGuild guild) {
        int totalPossible = guild.getMembers().size();
        int pro = this.pro.size();
        int against = this.against.size();
        int didVote = pro + against;

        return didVote < (totalPossible / 2) ?
                (pro == against ?
                        VoteResult.TIE_WITH_ABSTENTIONS :
                        (pro > against ? VoteResult.WIN_WITH_ABSTENTIONS : VoteResult.LOSE_WITH_ABSTENTIONS)
                )

                : (pro == against ?
                    VoteResult.TIE :
                    (pro > against ? VoteResult.WIN : VoteResult.LOSE)
        );
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        if(!json.missingOrNull("pro")) pro.addAll(json.getList("pro", JsonUtils::readUUID));
        if(!json.missingOrNull("against")) pro.addAll(json.getList("against", JsonUtils::readUUID));
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        if(!pro.isEmpty()) json.addList("pro", pro, JsonUtils::writeUUID);
        if(!against.isEmpty()) json.addList("against", against, JsonUtils::writeUUID);

        return json.getSource();
    }
}

package net.forthecrown.economy.market.guild.topics;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.key.Key;

import java.util.Date;

public class PostVoteTask implements JsonSerializable, Struct {
    public final Date executionDate;
    public final JsonElement data;
    public final Key topicType;

    public PostVoteTask(Date executionDate, JsonElement data, Key topicType) {
        this.executionDate = executionDate;
        this.data = data;
        this.topicType = topicType;
    }

    public static PostVoteTask fromJson(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new PostVoteTask(
                json.getDate("executionDate"),
                json.get("data"),
                json.getKey("topicKey")
        );
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.addKey("topicType", topicType);
        json.add("data", data);
        json.add("executionDate", executionDate.toString());

        return json.getSource();
    }
}

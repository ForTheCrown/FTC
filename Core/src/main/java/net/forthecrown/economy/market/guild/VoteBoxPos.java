package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;

public class VoteBoxPos implements JsonSerializable {
    private final World world;
    private final Vector3i voteBox;
    private final Vector3i sign;

    public VoteBoxPos(World world, Vector3i voteBox, Vector3i sign) {
        this.world = world;
        this.voteBox = voteBox;
        this.sign = sign;
    }

    public World getWorld() {
        return world;
    }

    public Vector3i getSign() {
        return sign;
    }

    public Vector3i getVoteBox() {
        return voteBox;
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.add("world", world.getName());
        json.add("voteBox", voteBox);
        json.add("signPos", sign);

        return json.getSource();
    }

    public static VoteBoxPos fromJson(JsonElement element) {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        return new VoteBoxPos(
                Objects.requireNonNull(Bukkit.getWorld(json.getString("world"))),
                Vector3i.of(json.get("voteBox")),
                Vector3i.of(json.get("signPos"))
        );
    }
}

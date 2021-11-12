package net.forthecrown.economy.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.economy.guild.topics.VoteTopic;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Directional;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class VoteBox implements JsonSerializable {

    private final Vector3i voteBox;
    private final Vector3i sign;
    private final BlockFace signRotation;

    public VoteBox(Vector3i voteBox, Vector3i sign, BlockFace signRotation) {
        this.voteBox = voteBox;
        this.sign = sign;
        this.signRotation = signRotation;
    }

    public void remove(World world) {
        voteBox.getBlock(world).setType(Material.AIR);
        sign.getBlock(world).setType(Material.AIR);
    }

    public void createBallotBox(World world, VoteTopic topic) {
        Block block = voteBox.getBlock(world);
        block.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) block.getState();

        skull.setPlayerProfile(Guilds.VOTE_BOX_PROFILE);
        skull.getPersistentDataContainer().set(Guilds.VOTE_BOX_KEY, PersistentDataType.BYTE, (byte) 1);
        skull.update();

        block = sign.getBlock(world);
        block.setType(Material.OAK_WALL_SIGN);

        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(signRotation);
        block.setBlockData(directional);

        Sign sign = (Sign) block.getState();
        sign.line(1, ChatUtils.renderIfTranslatable(topic.signDisplay()));
        sign.line(2,
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .content("0/")

                        .append(Component.text("0").color(NamedTextColor.GREEN))
                        .append(Component.text("/"))
                        .append(Component.text("0").color(NamedTextColor.RED))

                        .build()
        );

        sign.update();
    }

    public void updateSign(VoteState state, World world) {
        Block block = sign.getBlock(world);
        Sign sign = (Sign) block.getState();

        ObjectList<UUID> absentions = state.compileNonVoters();
        int absentionCount = absentions.size();

        Component voteInfo = Component.text()
                .content(absentionCount + "/")
                .color(NamedTextColor.GRAY)

                .append(Component.text(state.proCount()).color(NamedTextColor.GREEN))
                .append(Component.text("/"))
                .append(Component.text(state.againstCount()).color(NamedTextColor.RED))

                .build();

        sign.line(1, ChatUtils.renderIfTranslatable(state.getTopic().signDisplay()));
        sign.line(2, voteInfo);
        sign.update();
    }

    public Vector3i getSign() {
        return sign;
    }

    public Vector3i getVoteBox() {
        return voteBox;
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("voteBox", voteBox);
        json.add("signPos", sign);
        json.addEnum("signRotation", signRotation);

        return json.getSource();
    }

    public static VoteBox fromJson(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new VoteBox(
                Vector3i.of(json.get("voteBox")),
                Vector3i.of(json.get("signPos")),
                json.getEnum("signRotation", BlockFace.class)
        );
    }
}

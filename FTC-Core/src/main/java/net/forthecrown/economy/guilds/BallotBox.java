package net.forthecrown.economy.guilds;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.core.npc.NpcDirectory;
import net.forthecrown.economy.guilds.topics.VoteData;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.SignLines;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import java.util.UUID;

public record BallotBox(Vector3i sign, Vector3i box, BlockFace face) implements JsonSerializable {
    public static final PlayerProfile
            BALLOT_PROFILE  = FtcUtils.profileWithTextureID("profile", UUID.randomUUID(), "d5f07ab40aa485ac2df62450b7f59b52e8c6589b8101f9bc8ca4a152a838f4e4"),
            QUESTION_MARK   = FtcUtils.profileWithTextureID("question_mark", UUID.randomUUID(), "badc048a7ce78f7dad72a07da27d85c0916881e5522eeed1e3daf217a38c1a");

    public static final NamespacedKey BOX_KEY = Keys.forthecrown("ballot_box");
    public static final BallotBoxNPC BALLOT_BOX_NPC = new BallotBoxNPC();

    public static final Material SIGN_MATERIAL = Material.OAK_WALL_SIGN;

    public BallotBox(Vector3i sign, Vector3i box, BlockFace face) {
        this.sign = sign;
        this.box = box;
        this.face = face;

        Validate.isTrue(face != BlockFace.UP, "BlockFace could not be UP");
        Validate.isTrue(face != BlockFace.DOWN, "BlockFace could not be DOWN");
    }

    public void onVoteStart(VoteState state, VoteData data, World world) {
        Component signDisplay = GuildUtil.display(data);

        Sign sign = getSign(world);
        SignLines.EMPTY.apply(sign);

        sign.line(1, signDisplay);
        sign.line(2, voteLine(0, state.totalAbstainCount(), 0));
        sign.update();

        Block box = this.box.getBlock(world);
        box.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) box.getState();
        skull.setPlayerProfile(BALLOT_PROFILE);
        skull.update();

        Rotatable rotatable = (Rotatable) box.getBlockData();
        rotatable.setRotation(face);
        box.setBlockData(rotatable);

        killSlime(world);
        spawnSlime(world);
    }

    static Component voteLine(int pro, int con, int none) {
        return Component.text()
                .append(Component.text(pro).color(NamedTextColor.GREEN))
                .append(Component.text('/'))
                .append(Component.text(none).color(NamedTextColor.GRAY))
                .append(Component.text('/'))
                .append(Component.text(con).color(NamedTextColor.RED))
                .build();
    }

    public void onVoteEnd(World world) {
        Sign sign = getSign(world);
        SignLines.EMPTY.apply(sign);

        sign.line(1, Component.text("No current"));
        sign.line(2, Component.text("vote"));
        sign.update();

        Block head = box.getBlock(world);
        head.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) head.getState();
        skull.setPlayerProfile(QUESTION_MARK);
        skull.update();

        killSlime(world);

        Rotatable rotatable = (Rotatable) head.getBlockData();
        rotatable.setRotation(face);
        head.setBlockData(rotatable);
    }

    public void onVote(VoteState state, World world) {
        Sign sign = getSign(world);

        sign.line(2, voteLine(state.totalProCount(), state.totalAbstainCount(), state.totalAgainstCount()));
        sign.update();
    }

    private Sign getSign(World world) {
        Block b = sign.getBlock(world);

        if (b.getType() != SIGN_MATERIAL) {
            b.setType(SIGN_MATERIAL);
        }

        return (Sign) b.getState();
    }

    private void spawnSlime(World world) {
        Location l = box.toLoc(world).add(0.5D, 0, 0.5D);

        Slime s = world.spawn(l, Slime.class, slime -> {
            slime.setInvulnerable(true);

            slime.customName(Component.text("Click me to vote!"));
            slime.setAI(false);
            slime.setSize(1);
            slime.setGravity(false);

            // BlockFace to rotation
            slime.setRotation(face.ordinal() * 45, 0f);
        });

        NpcDirectory.make(BOX_KEY, s);
    }

    private void killSlime(World world) {
        Location l = box.toLoc(world).add(0.5D, 0, 0.5D);

        world.getNearbyEntitiesByType(Slime.class, l, 0.5D)
                .forEach(slime -> {
                    if(!NpcDirectory.isNPC(slime)) return;
                    slime.remove();
                });
    }

    public static BallotBox of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new BallotBox(
                Vector3i.of(json.get("sign")),
                Vector3i.of(json.get("box")),
                json.getEnum("face", BlockFace.class)
        );
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("sign", sign);
        json.add("box", box);
        json.addEnum("face", face);

        return json.getSource();
    }

    public static class BallotBoxNPC implements InteractableNPC {
        public BallotBoxNPC() {
            Registries.NPCS.register(BOX_KEY, this);
        }

        @Override
        public void run(Player player, Entity entity) throws CommandSyntaxException {
            TradeGuild guild = Crown.getGuild();

            validate(player, guild);

            CrownUser user = UserManager.getUser(player);
            ClickableTextNode root = createVoteNodes(player, guild);

            user.sendMessage(
                    Component.translatable("guilds.vote.prompt",
                                    NamedTextColor.GRAY,
                                    guild.getCurrentState().display()
                                            .color(NamedTextColor.GOLD)
                            )
                            .append(Component.newline())
                            .append(root.presentPrompts(user))
            );
        }

        private void validate(Player player, TradeGuild guild) throws RoyalCommandException {
            if(!guild.isVoteOngoing()) {
                throw FtcExceptionProvider.translatable("guilds.notVoting");
            }

            if(!guild.isMember(player.getUniqueId())) {
                throw FtcExceptionProvider.translatable("guilds.notMember");
            }

            if(guild.getCurrentState().hasVoted(player.getUniqueId())) {
                throw FtcExceptionProvider.translatable("guilds.alreadyVoted");
            }
        }

        private ClickableTextNode createVoteNodes(Player player, TradeGuild guild) {
            String baseName = "guild_vote_instance_" + player.getName();

            ClickableTextNode root = new ClickableTextNode(baseName + "_root")
                    .addNode(createVoteOption(baseName + "_for", guild, true, baseName))
                    .addNode(createVoteOption(baseName + "_against", guild, false, baseName));

            return ClickableTexts.register(root);
        }

        private ClickableTextNode createVoteOption(String name, TradeGuild guild, boolean pro, String baseName) {
            String promptKey = "guilds.vote." + (pro ? "yes" : "no");
            String translationKey = "guilds.voted." + (pro ? "for" : "against");
            TextColor promptColor = pro ? NamedTextColor.GREEN : NamedTextColor.RED;

            ClickableTextNode node = new ClickableTextNode(name)
                    .setPrompt(user ->
                            Component.translatable("guilds.vote." + promptKey)
                                    .hoverEvent(Component.translatable(promptKey + ".hover"))
                                    .color(promptColor)
                    )

                    .setExecutor(user -> {
                        validate(user.getPlayer(), guild);

                        VoteState state = guild.getCurrentState();
                        if(pro) state.votePro(user.getUniqueId());
                        else state.voteAgainst(user.getUniqueId());

                        user.sendMessage(
                                Component.translatable("guilds.voted." + translationKey,
                                        NamedTextColor.GRAY,
                                        guild.getCurrentState().display()
                                                .color(NamedTextColor.YELLOW)
                                )
                        );

                        ClickableTexts.unregister(baseName);
                    });

            return node;
        }
    }
}

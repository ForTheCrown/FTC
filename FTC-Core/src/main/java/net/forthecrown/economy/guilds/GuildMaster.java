package net.forthecrown.economy.guilds;

import com.google.common.base.Predicates;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.click.PromptCreator;
import net.forthecrown.commands.click.TextExecutor;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.economy.guilds.screen.TopicSelectionScreen;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketData;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The NPC representing the GuildMaster.
 * <p></p>
 * DEAR FUCKING LORD WRITING THIS WAS SOOOOO BOOORRRRIINNNGGG.
 * Never again
 */
public class GuildMaster implements InteractableNPC {
    public static final NamespacedKey KEY = Keys.forthecrown("guild_master");

    private static final ClickableTextNode GUILD_MASTER_TEXT = ClickableTexts.register(
            new ClickableTextNode("guild_master_root")

                    // Info nodes
                    .addNode(infoNode(InfoSection.GUILD, null))
                    .addNode(infoNode(InfoSection.GUILD_JOINING, () -> new Component[] {new TimePrinter(ComVars.getGuildJoinRequirement()).print()}))
                    .addNode(infoNode(InfoSection.VOTE_START, null))
                    .addNode(infoNode(InfoSection.SHOP_GETTING, () -> {
                        Component[] result = new Component[2];
                        result[0] = FtcFormatter.rhines(ComVars.defaultShopPrice() - 15000).color(NamedTextColor.YELLOW);
                        result[1] = FtcFormatter.rhines(ComVars.defaultShopPrice() + 15000).color(NamedTextColor.YELLOW);

                        return result;
                    }))
                    .addNode(infoNode(InfoSection.SHOPS, null))

                    // Action nodes
                    // Leave node
                    .addNode(
                            inputtedNode("gm_action_leave",
                                    user -> {
                                        if(!Crown.getGuild().isMember(user.getUniqueId())) return null;

                                        return Component.translatable("guildMaster.leave.prompt")
                                                .color(NamedTextColor.GOLD);
                                    },
                                    (user, node) -> {
                                        if(!Crown.getGuild().isMember(user.getUniqueId())) return;

                                        user.sendMessage(
                                                Component.translatable("guildMaster.leave")
                                                        .append(node.presentPrompts(user))
                                        );
                                    }
                            )
                                    .addNode(
                                            node("gm_action_leave_confirm",
                                                    user -> {
                                                        return Component.translatable("buttons.confirm")
                                                                .color(NamedTextColor.AQUA);
                                                    },
                                                    user -> {
                                                        TradeGuild guild = Crown.getGuild();

                                                        guild.removeMember(user.getUniqueId());
                                                        user.sendMessage(Component.translatable("guildMaster.leave.confirm"));
                                                        user.getMarketData().setGuildJoinDate(0L);

                                                        guild.forEachMember(member -> {
                                                            member.sendAndMail(
                                                                    Component.translatable("guilds.left.broadcast",
                                                                            NamedTextColor.GRAY,
                                                                            user.nickDisplayName()
                                                                    )
                                                            );
                                                        });
                                                    }
                                            )
                                    )
                    )

                    // Vote start node
                    .addNode(
                            node("gm_action_start_vote",
                                    user -> {
                                        if(!Crown.getGuild().isMember(user.getUniqueId())) return null;

                                        Component result = Component.translatable("guildMaster.startVote");

                                        if(Crown.getGuild().canStartVote()) {
                                            return result
                                                    .color(NamedTextColor.AQUA)
                                                    .hoverEvent(Component.translatable("guildMaster.startVote.hover"));
                                        } else {
                                            return result
                                                    .color(NamedTextColor.GRAY)
                                                    .hoverEvent(Component.translatable("guilds.history.cannotStartVote"));
                                        }
                                    },
                                    user -> {
                                        if(!Crown.getGuild().isMember(user.getUniqueId())) return;

                                        if(!Crown.getGuild().canStartVote()) {
                                            throw FtcExceptionProvider.translatable("guilds.history.cannotStartVote");
                                        }

                                        TopicSelectionScreen.create().open(user);
                                    }
                            )
                    )

                    // Guild joining node
                    .addNode(
                            node("gm_action_join",
                                    user -> {
                                        if(!user.getMarketData().currentlyOwnsShop()) return null;

                                        Component result = Component.translatable("guildMaster.join.prompt", NamedTextColor.AQUA);
                                        UserMarketData data = user.getMarketData();
                                        TradeGuild guild = Crown.getGuild();

                                        if (!TimeUtil.hasCooldownEnded(ComVars.getGuildJoinRequirement(), data.getOwnershipBegan())) {
                                            return result
                                                    .color(NamedTextColor.GRAY)
                                                    .hoverEvent(Component.translatable("guilds.join.error.shopOwnDuration",
                                                            new TimePrinter(ComVars.guildKickSafeTime()).print()
                                                    ));
                                        }

                                        if(data.affectedByKickCooldown()) {
                                            return result
                                                    .color(NamedTextColor.GRAY)
                                                    .hoverEvent(Component.translatable("guilds.join.error.kicked"));
                                        }

                                        if(guild.memberCount() >= ComVars.getMaxGuildMembers()) {
                                            return result
                                                    .color(NamedTextColor.GRAY)
                                                    .hoverEvent(Component.translatable("guilds.join.error.memberLimit"));
                                        }

                                        return result;
                                    },
                                    user -> {
                                        TradeGuild guild = Crown.getGuild();
                                        UserMarketData data = user.getMarketData();

                                        if(data.affectedByKickCooldown()) {
                                            throw FtcExceptionProvider.translatable("guilds.join.error.kicked");
                                        }

                                        if(!data.currentlyOwnsShop()) {
                                            throw FtcExceptionProvider.translatable("guilds.join.error.noShopOwned");
                                        }

                                        if(!TimeUtil.hasCooldownEnded(ComVars.getGuildJoinRequirement(), data.getOwnershipBegan())) {
                                            throw FtcExceptionProvider.translatable("guilds.join.error.shopOwnDuration",
                                                    new TimePrinter(ComVars.guildKickSafeTime()).print()
                                            );
                                        }

                                        if(guild.memberCount() >= ComVars.getMaxGuildMembers()) {
                                            throw FtcExceptionProvider.translatable("guilds.join.error.memberLimit");
                                        }

                                        guild.forEachMember(member -> {
                                            member.sendAndMail(
                                                    Component.translatable("guilds.join.broadcast",
                                                            NamedTextColor.GRAY,
                                                            user.nickDisplayName()
                                                                    .color(NamedTextColor.YELLOW)
                                                    )
                                            );
                                        });

                                        guild.addMember(user.getUniqueId());
                                        data.setGuildJoinDate(System.currentTimeMillis());

                                        user.sendMessage(
                                                Component.translatable("guilds.join")
                                                        .append(Component.newline())
                                                        .append(Component.translatable("guilds.join2", NamedTextColor.GRAY))
                                        );
                                    }
                            )
                    )
    );

    private static ClickableTextNode node(String name, PromptCreator promptCreator, TextExecutor executor) {
        return new ClickableTextNode(name)
                .setExecutor(executor)
                .setPrompt(promptCreator);
    }

    private static ClickableTextNode inputtedNode(String name, PromptCreator creator, TextExecutorWithNodeInput executor) {
        ClickableTextNode result = new ClickableTextNode(name)
                .setPrompt(creator);

        return result
                .setExecutor(user -> executor.run(user, result));
    }

    private static interface TextExecutorWithNodeInput {
        void run(CrownUser user, ClickableTextNode node);
    }

    private static ClickableTextNode infoNode(InfoSection section, @Nullable Supplier<Component[]> argSupplier) {
        return node(
                "gm_info_" + section.name().toLowerCase(),
                user -> section.test(user) ? section.prompt() : null,
                user -> {
                    if(!section.test(user)) return;

                    if(argSupplier == null) user.sendMessage(section.info());
                    else user.sendMessage(section.info(argSupplier.get()));
                }
        );
    }

    // Register NPC
    static void init() {
        GuildMaster master = new GuildMaster();
        Registries.NPCS.register(KEY, master);
    }

    @Override
    public void run(Player player, Entity entity) throws CommandSyntaxException {
        CrownUser user = UserManager.getUser(player);

        // onNPC interact -> show text options
        user.sendMessage(
                Component.translatable("guildMaster.initial", user.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(GUILD_MASTER_TEXT.presentPrompts(user))
        );
    }

    // Just a thing to help writing all the info nodes
    // easier
    private enum InfoSection {
        GUILD,
        GUILD_JOINING,
        VOTE_START(user -> Crown.getGuild().isMember(user.getUniqueId())),
        SHOPS,
        SHOP_GETTING;

        private final String promptKey;
        private final String infoKey;
        private final Predicate<CrownUser> predicate;

        InfoSection() {
            this(Predicates.alwaysTrue());
        }

        InfoSection(Predicate<CrownUser> predicate) {
            this.predicate = predicate;

            this.promptKey = "guildMaster.info." + name().toLowerCase() + ".prompt";
            this.infoKey = "guildMaster.info." + name().toLowerCase();
        }

        public boolean test(CrownUser user) {
            return predicate.test(user);
        }

        public Component prompt() {
            return Component.translatable(promptKey);
        }

        public Component info(Component... args) {
            return Component.translatable(infoKey, args);
        }
    }
}

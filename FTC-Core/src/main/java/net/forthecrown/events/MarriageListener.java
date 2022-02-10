package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MarriageListener implements Listener, InteractableNPC {
    public static final NamespacedKey FATHER_TED_KEY = new NamespacedKey(Crown.inst(), "marriage");
    private static final Set<UUID> AWAITING_FINISH = new HashSet<>();

    private static final ClickableTextNode MARRY_CONFIRM = new ClickableTextNode("marry_confirm")
            .setPrompt(user -> {
                return Component.translatable("marriage.priestText.confirm.button")
                        .hoverEvent(Component.text("Click me!"))
                        .color(NamedTextColor.AQUA);
            })
            .setExecutor(user -> {
                CrownUser target = getTarget(user);

                if (AWAITING_FINISH.contains(user.getUniqueId())) {
                    throw FtcExceptionProvider.translatable("marriage.priestText.alreadyAccepted");
                }
                if (AWAITING_FINISH.contains(target.getUniqueId())) {
                    ActionFactory.marry(user, target, true);
                    AWAITING_FINISH.remove(target.getUniqueId());
                    return;
                }

                AWAITING_FINISH.add(user.getUniqueId());
                user.sendMessage(
                        Component.translatable("marriage.priestText.waiting").color(NamedTextColor.YELLOW)
                );
            });

    private static final ClickableTextNode MARRY = new ClickableTextNode("marry")
            .setPrompt(user -> {
                return Component.translatable("marriage.priestText.option")
                        .color(NamedTextColor.AQUA)
                        .hoverEvent(Component.text("Click me!"));
            })
            .setExecutor(user -> {
                CrownUser target = getTarget(user);

                Component message = Component.translatable(
                                "marriage.priestText.confirm",
                                user.nickDisplayName().color(NamedTextColor.YELLOW),
                                target.nickDisplayName().color(NamedTextColor.YELLOW)
                        )
                        .append(Component.space())
                        .append(MARRY_CONFIRM.prompt(user));

                user.sendMessage(message);
            });

    private static final ClickableTextNode MARRIAGE_NODE_PARENT = ClickableTexts.register(
            new ClickableTextNode("marriage")
                    .addNode(MARRY)
                    .addNode(MARRY_CONFIRM)
    );

    public MarriageListener() {
        Registries.NPCS.register(FATHER_TED_KEY, this);
    }

    private static CrownUser getTarget(CrownUser user) throws RoyalCommandException {
        UserInteractions inter = user.getInteractions();

        if (inter.getSpouse() != null) throw FtcExceptionProvider.senderAlreadyMarried();
        if (inter.getWaitingFinish() == null) throw FtcExceptionProvider.translatable("marriage.nooneWaiting");

        return UserManager.getUser(inter.getWaitingFinish());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        if (Cooldown.containsOrAdd(event.getPlayer(), "Core_Marriage_Smooch", 2)) return;

        //This is dumb, I love it
        //Right click spouse to smooch them

        CrownUser user = UserManager.getUser(event.getPlayer());
        UserInteractions inter = user.getInteractions();
        if (inter.getSpouse() == null) return;
        if (!user.getPlayer().isSneaking()) return;

        CrownUser target = UserManager.getUser(event.getRightClicked().getUniqueId());
        if (!inter.getSpouse().equals(target.getUniqueId())) return;

        CosmeticEmotes.SMOOCH.getCommand().execute(user, target);
    }

    @Override
    public void run(Player player, Entity entity) throws CommandSyntaxException {
        if (Cooldown.containsOrAdd(player, "Core_Marriage_Priest", 20)) return;

        //Send the initial interaction message
        //For father ted
        player.sendMessage(
                Component.text()
                        .append(Component.translatable("marriage.priestText"))
                        .color(NamedTextColor.YELLOW)
                        .append(Component.space())
                        .append(MARRY.prompt(UserManager.getUser(player)))
                        .build()
        );
    }
}

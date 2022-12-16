package net.forthecrown.events.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MarriageListener implements Listener, SimpleNpc {
    public static final String FATHER_TED_KEY = "marriage";
    private static final Set<UUID> AWAITING_FINISH = new HashSet<>();

    private static final ClickableTextNode MARRY_CONFIRM = new ClickableTextNode("marry_confirm")
            .setPrompt(user -> Messages.PRIEST_TEXT_CONFIRM)
            .setExecutor(user -> {
                User target = getTarget(user);

                if (AWAITING_FINISH.contains(user.getUniqueId())) {
                    throw Exceptions.PRIEST_ALREADY_ACCEPTED;
                }

                if (AWAITING_FINISH.contains(target.getUniqueId())) {
                    Users.marry(user, target);
                    AWAITING_FINISH.remove(target.getUniqueId());

                    return;
                }

                AWAITING_FINISH.add(user.getUniqueId());
                user.sendMessage(Messages.PRIEST_TEXT_WAITING);
            });

    private static final ClickableTextNode MARRY = new ClickableTextNode("marry")
            .setPrompt(user -> Messages.PRIEST_TEXT_MARRY)
            .setExecutor(user -> {
                User target = getTarget(user);

                user.sendMessage(
                        Messages.priestTextConfirm(user, target)
                                .append(Component.space())
                                .append(MARRY_CONFIRM.prompt(user))
                );
            });

    private static final ClickableTextNode MARRIAGE_NODE_PARENT = ClickableTexts.register(
            new ClickableTextNode("marriage")
                    .addNode(MARRY)
                    .addNode(MARRY_CONFIRM)
    );

    public MarriageListener() {
        Registries.NPCS.register(FATHER_TED_KEY, this);
    }

    private static User getTarget(User user) throws CommandSyntaxException {
        UserInteractions inter = user.getInteractions();

        if (inter.getSpouse() != null) {
            throw Exceptions.ALREADY_MARRIED;
        }

        if (inter.getWaitingFinish() == null) {
            throw Exceptions.PRIEST_NO_ONE_WAITING;
        }

        return Users.get(inter.getWaitingFinish());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        if (Cooldown.containsOrAdd(event.getPlayer(), "Core_Marriage_Smooch", 2)) {
            return;
        }

        //This is dumb, I love it
        //Right click spouse to smooch them

        User user = Users.get(event.getPlayer());
        UserInteractions inter = user.getInteractions();
        if (inter.getSpouse() == null) {
            return;
        }

        if (!user.getPlayer().isSneaking()) {
            return;
        }

        User target = Users.get(event.getRightClicked().getUniqueId());

        if (!inter.getSpouse().equals(target.getUniqueId())) {
            return;
        }

        CosmeticEmotes.SMOOCH.getCommand().execute(user, target);
    }

    @Override
    public boolean run(Player player, Entity entity) throws CommandSyntaxException {
        if (Cooldown.containsOrAdd(player, "Core_Marriage_Priest", 20)) {
            return false;
        }

        player.sendMessage(
                Messages.PRIEST_TEXT
                        .append(Component.space())
                        .append(MARRY.prompt(Users.get(player)))
        );
        return false;
    }
}
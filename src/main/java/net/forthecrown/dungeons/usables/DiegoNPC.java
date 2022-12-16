package net.forthecrown.dungeons.usables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Messages;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.events.DungeonListeners;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DiegoNPC implements SimpleNpc {
    public static final Key KEY = Keys.royals("diego");

    private static final ClickableTextNode TRIDENT_NODE = ClickableTexts.register(
            new ClickableTextNode("trident_fork")
                    .setExecutor(user -> {
                        PlayerInventory inv = user.getInventory();

                        if (cannotClaim(user)) {
                            throw Exceptions.DIEGO_ERROR;
                        }

                        ItemStack toGive = DungeonListeners.createFork();
                        inv.removeItemAnySlot(BossItems.DRAWNED.item());
                        inv.addItem(toGive);
                    })
                    .setPrompt(user -> {
                        Component text = Messages.DIEGO_BUTTON;

                        if (cannotClaim(user)) {
                            return text
                                    .hoverEvent(Messages.DIEGO_ERROR)
                                    .color(NamedTextColor.GRAY);
                        }

                        return text;
                    })
    );

    private static boolean cannotClaim(User user) {
        return !user.getInventory().containsAtLeast(BossItems.DRAWNED.item(), 1);
    }

    @Override
    public boolean run(Player player, Entity entity) throws CommandSyntaxException {
        player.sendMessage(
                Messages.DIEGO_TEXT
                        .append(Component.newline())
                        .append(TRIDENT_NODE.prompt(Users.get(player)))
        );

        return true;
    }
}
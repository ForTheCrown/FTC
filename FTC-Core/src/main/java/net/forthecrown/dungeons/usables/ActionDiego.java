package net.forthecrown.dungeons.usables;

import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.events.DungeonListeners;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ActionDiego implements UsageActionInstance {
    public static final Key KEY = Squire.createRoyalKey("diego");

    private static final ClickableTextNode TRIDENT_NODE = ClickableTexts.register(
            new ClickableTextNode("trident_fork")
                    .setExecutor(user -> {
                        PlayerInventory inv = user.getInventory();

                        if(cannotClaim(user)) {
                            throw FtcExceptionProvider.translatable("dungeons.diego.error");
                        }

                        ItemStack toGive = DungeonListeners.fork();
                        inv.removeItemAnySlot(BossItems.DRAWNED.item());
                        inv.addItem(toGive);
                    })
                    .setPrompt(user -> {
                        Component text = Component.text("[")
                                .color(NamedTextColor.AQUA)
                                .append(Component.translatable("dungeons.diego.button"))
                                .append(Component.text("]"));

                        if(cannotClaim(user)) {
                            return text
                                    .hoverEvent(Component.translatable("dungeons.diego.error"))
                                    .color(NamedTextColor.GRAY);
                        }

                        return text;
                    })
    );

    private static boolean cannotClaim(CrownUser user) {
        return !user.getInventory().containsAtLeast(BossItems.DRAWNED.item(), 1);
    }

    @Override
    public String asString() {
        return typeKey().asString() + "{}";
    }

    @Override
    public Key typeKey() {
        return KEY;
    }

    @Override
    public void onInteract(Player player) {
        player.sendMessage(
                Component.translatable("dungeons.diegoText", NamedTextColor.YELLOW)
                        .append(Component.newline())
                        .append(TRIDENT_NODE.prompt(UserManager.getUser(player)))
        );
    }
}

package net.forthecrown.events.player;

import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class DurabilityListener implements Listener {
    static final Sound BREAK_SOUND = Sound.sound(
            org.bukkit.Sound.ENTITY_ITEM_BREAK,
            Sound.Source.MASTER,
            1f, 1f
    );

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageItem(PlayerItemDamageEvent event) {
        User user = Users.get(event.getPlayer());

        if (!user.get(Properties.DURABILITY_ALERTS)) {
            return;
        }

        ItemStack item = event.getItem();
        Damageable damageable = (Damageable) item.getItemMeta();

        float damage = damageable.getDamage();
        float maxDurability = item.getType().getMaxDurability();
        float remaining = maxDurability - damage - 1;

        // If durability is above threshold or player is on cooldown, do not show alert
        if (remaining >= (maxDurability * GeneralConfig.durabilityWarnThreshold)
                || remaining <= 0
                || Cooldown.containsOrAdd(user, Properties.DURABILITY_ALERTS.getKey(), 20 * 10)
        ) {
            return;
        }

        user.playSound(BREAK_SOUND);
        user.showTitle(
                Title.title(
                        // Title
                        Text.format("Your {0, item, -!amount} is about to break!",
                                NamedTextColor.RED,
                                item
                        ),

                        // Subtitle
                        Text.format("{0, number} / {1, number} durability left",
                                NamedTextColor.GOLD,
                                remaining, maxDurability
                        )
                )
        );
    }
}
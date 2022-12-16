package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PotionEffectListener implements Listener {

    private static final String MOVE_CD = "move";

    private static final ObjectSet<UUID> effectReceivers = new ObjectOpenHashSet<>();

    private static final ObjectSet<UUID> ignored = new ObjectOpenHashSet<>();

    @EventHandler
    public void onModifyPotionEffect(EntityPotionEffectEvent event) {
        // Jules: Combine if statements
        if (!(event.getEntity() instanceof Player player)
                || ignored.contains(player.getUniqueId())
        ) {
            return;
        }

        UUID playerId = player.getUniqueId();

        // Jules: Invert if statements
        // If in guild chunk
        if (!PlayerMoveGuildChunkListener.isInOwnGuild(playerId)) {
            return;
        }

        // Jules: Use guild variable instead of calling user ID lookup
        //        and, by extension GuildId lookup twice lol
        var guild = Users.getLoadedUser(playerId)
                .getGuild();

        // If effect is being removed due to cooldown, replenish it
        if (event.getAction() == EntityPotionEffectEvent.Action.REMOVED) {
            PotionEffectType oldType = event.getOldEffect().getType();

            if (guild.getActivePotionEffectTypes().contains(oldType)) {
                ignored.add(playerId);
                giveEffect(oldType, player);
                ignored.remove(playerId);
            }
        }

        // If effect is being added or changed, make it one level stronger
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED
                && event.getAction() != EntityPotionEffectEvent.Action.CHANGED
        ) {
            return;
        }

        PotionEffect effect = event.getNewEffect();
        PotionEffect betterNewEffect = effect.withAmplifier(effect.getAmplifier() + 1);

        if (!guild.getActivePotionEffectTypes().contains(effect.getType())) {
            return;
        }

        ignored.add(playerId);
        player.addPotionEffect(betterNewEffect);
        ignored.remove(playerId);
    }

    @EventHandler
    public void onChunkEnter(PlayerMoveEvent event) {
        if (!Cooldown.containsOrAdd(event.getPlayer(), MOVE_CD, 5)) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (PlayerMoveGuildChunkListener.isInOwnGuild(playerId)) {
            tryGiveInitialEffects(player);
        } else if (effectReceivers.contains(playerId)) {
            removeEffects(player);
        }
    }

    public static void tryGiveInitialEffects(Player player) {
        // Return if already received
        UUID playerId = player.getUniqueId();
        if (effectReceivers.contains(playerId)) {
            return;
        }

        giveEffects(player, playerId);
        effectReceivers.add(playerId);
    }

    public static void giveEffects(Player player, UUID playerId) {
        ignored.add(playerId);

        Users.get(playerId).getGuild().getActiveEffects().forEach(e -> {
            giveEffect(e.getPotionEffectType(), player);
        });

        ignored.remove(playerId);
    }

    public static void giveEffect(PotionEffectType type, Player player) {
        if (type == null) {
            return;
        }

        // Give or increment potion, which causes PotionEvent
        if (!player.hasPotionEffect(type) || player.getPotionEffect(type).getDuration() < 1) {
            player.addPotionEffect(new PotionEffect(type, 210, 0, true, false, true));
        } else {
            PotionEffect effect = player.getPotionEffect(type);
            if (effect == null) {
                return;
            }

            player.addPotionEffect(effect.withAmplifier(effect.getAmplifier() + 1));
        }
    }

    public static void giveEffectSafe(PotionEffectType type, Player player) {
        ignored.add(player.getUniqueId());
        giveEffect(type, player);
        ignored.remove(player.getUniqueId());
    }

    public static void removeEffects(Player player) {
        ignored.add(player.getUniqueId());
        var guild = Users.get(player).getGuild();

        if (guild != null) {
            guild.getActiveEffects().forEach(e -> {
                removeEffect(e.getPotionEffectType(), player);
            });
        }

        ignored.remove(player.getUniqueId());
        effectReceivers.remove(player.getUniqueId());
    }

    public static void removeEffect(PotionEffectType type, Player player) {
        if (type == null) {
            return;
        }

        PotionEffect effect = player.getPotionEffect(type);
        if (effect == null) {
            return;
        }

        player.removePotionEffect(type);
        int amplifier = effect.getAmplifier();

        if (amplifier > 0) {
            player.addPotionEffect(effect.withAmplifier(effect.getAmplifier() - 1));
        }
    }

    public static void removeEffectSafe(PotionEffectType type, Player player) {
        ignored.add(player.getUniqueId());
        removeEffect(type, player);
        ignored.remove(player.getUniqueId());
    }
}
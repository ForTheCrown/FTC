package net.forthecrown.pirates.grappling;

import net.forthecrown.core.CrownCore;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.economy.Balances;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class GrapplingHookParkour implements Listener {

    private final GhParkourData data = new GhParkourData();

    private static final String COOLDOWN_CATEGORY = "Pirates_GH";
    private static final byte TP_COOLDOWN = 40;

    public GrapplingHookParkour(){
        Bukkit.getPluginManager().registerEvents(this, CrownCore.inst());
    }

    public GhParkourData getData() {
        return data;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!event.getRightClicked().getPersistentDataContainer().has(Pirates.GH_STAND_KEY, PersistentDataType.STRING)) return;
        if(Cooldown.contains(event.getPlayer(), COOLDOWN_CATEGORY)) return;

        String id = event.getRightClicked().getPersistentDataContainer().get(Pirates.GH_STAND_KEY, PersistentDataType.STRING);
        assert id != null : "Id was null";

        GhLevelData levelData = data.get(id);
        if(levelData == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        CrownUser user = UserManager.getUser(player);
        player.sendMessage(Component.translatable("gh.standOnBlock", NamedTextColor.GRAY));

        Cooldown.add(player, COOLDOWN_CATEGORY, (int) TP_COOLDOWN);
        Bukkit.getScheduler().runTaskLater(CrownCore.inst(), () -> {
            Material mat = player.getLocation().subtract(0, 1, 0).getBlock().getType();
            if(mat != Material.GLOWSTONE){
                player.sendMessage(Component.translatable("gh.cancelled", NamedTextColor.GRAY));
                return;
            }

            player.getInventory().clear();
            player.teleport(levelData.getExitDest().toLoc(player.getWorld()));

            if(levelData.getType() != GhType.END) levelData.giveHook(player);

            if(!levelData.hasCompleted(player.getUniqueId())) {
                switch (levelData.getType()) {
                    case NORMAL: break;
                    case END:
                        CrownCore.getBalances().add(user.getUniqueId(), CrownCore.getGhFinalReward(), false);
                        user.sendMessage(
                                Component.translatable("gh.reward.final", Balances.formatted(CrownCore.getGhFinalReward()).color(NamedTextColor.YELLOW))
                                        .color(NamedTextColor.GRAY)
                        );

                        player.getInventory().addItem(CrownItems.cutlass());
                        break;

                    case SPECIAL:
                        CrownCore.getBalances().add(user.getUniqueId(), CrownCore.getGhSpecialReward(), false);
                        user.sendMessage(
                                Component.translatable("gh.reward.special", Balances.formatted(CrownCore.getGhSpecialReward()).color(NamedTextColor.YELLOW))
                                        .color(NamedTextColor.GRAY)
                        );

                        break;
                }

                levelData.complete(player.getUniqueId());
            }
        }, TP_COOLDOWN);
    }
}
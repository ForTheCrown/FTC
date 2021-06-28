package net.forthecrown.events;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.economy.pirates.merchants.UsablePirateNpc;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.TreasureShulker;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PirateEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event1) {
        if(event1.getRightClicked().getPersistentDataContainer().has(Pirates.BM_MERCHANT, PersistentDataType.STRING)){
            if(!(event1.getRightClicked() instanceof Villager)) return;

            Entity entity = event1.getRightClicked();

            String id = entity.getPersistentDataContainer().get(Pirates.BM_MERCHANT, PersistentDataType.STRING);
            assert  id != null : "Id was null";

            CrownUser user = UserManager.getUser(event1.getPlayer());
            PirateEconomy bm = Pirates.getPirateEconomy();

            if(user.getBranch() != Branch.PIRATES){
                user.sendMessage(Component.translatable("pirates.wrongBranch", NamedTextColor.YELLOW, entity.customName()));
                return;
            }

            UsablePirateNpc npc = bm.getNpcById(id);
            npc.onUse(user, entity);
            return;
        }

        if(event1.getRightClicked().getPersistentDataContainer().has(Pirates.SHULKER_KEY, PersistentDataType.BYTE)){
            Events.handlePlayer(event1, event -> {
                Player player = event.getPlayer();
                CrownUser user = UserManager.getUser(player);
                if(user.getBranch() != Branch.PIRATES) throw FtcExceptionProvider.notPirate();

                TreasureShulker shulker = Pirates.getTreasure();
                shulker.createLoot(player, event.getRightClicked()).giveRewards(player);
            });
        }
    }
}

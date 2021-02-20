package net.forthecrown.vikings.blessings;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.vikings.Vikings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadChoppingBlessing extends VikingBlessing{

    private int dropChance = 3; //0.3%

    public HeadChoppingBlessing() {
        super("HeadChopping", Vikings.getInstance());
        registerEvents();
    }

    @Override
    protected void onPlayerEquip(CrownUser user) {

    }

    @Override
    protected void onPlayerUnequip(CrownUser user) {

    }

    @Override
    public void save() {
        getFile().set("DropChance", dropChance);
        super.save();
    }

    @Override
    public void reload() {
        super.reload();

        dropChance = getFile().getInt("DropChance");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().getKiller() == null) return;
        if(!getUsers().contains(event.getEntity().getKiller().getUniqueId())) return;
        if(CrownUtils.getRandomNumberInRange(0, 1000) > dropChance) return;

        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), getPlayerHead(event.getEntity()));
    }

    private ItemStack getPlayerHead(Player player){
        ItemStack playerHead = CrownUtils.makeItem(Material.PLAYER_HEAD, 1, false, null);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        playerHead.setItemMeta(skullMeta);

        return playerHead;
    }
}

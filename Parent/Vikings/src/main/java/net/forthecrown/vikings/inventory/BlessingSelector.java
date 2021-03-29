package net.forthecrown.vikings.inventory;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.blessings.VikingBlessing;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;

public class BlessingSelector implements InventoryHolder, Listener {

    private final CrownUser user;
    private final Player player;
    private Inventory inv;

    public BlessingSelector(CrownUser user){
        this.user = user;
        this.player = user.getPlayer();

        initInv();

        Vikings.getInstance().getServer().getPluginManager().registerEvents(this, Vikings.getInstance());
    }

    private void initInv(){
        inv = Bukkit.createInventory(this, 54, "Raid Selection");

        final ItemStack borderItem = CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "-");
        for (int i = 0; i < inv.getSize(); i++){
            if(i == 4) i++;
            if(i == 10 || i == 19 || i == 28 || i == 37) i += 7;
            inv.setItem(i, borderItem);
        }

        inv.setItem(4, CrownItems.makeItem(Material.POTION, 1, true, "Blessings"));

        int index = 11;
        for (VikingBlessing b: VikingBlessing.getBlessings()){
            inv.setItem(index, getBlessingItem(b));
            index++;
        }
    }

    private ItemStack getBlessingItem(VikingBlessing b){
        ItemStack toReturn = CrownItems.makeItem(Material.PAPER, 1, true, b.getName());
        if(isAvailableToUser(b)) toReturn.setType(Material.GLOBE_BANNER_PATTERN);
        if(isCurrentlyInUse(b)) toReturn.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return toReturn;
    }

    private boolean isCurrentlyInUse(VikingBlessing b){
        String result = user.getDataContainer().get(Vikings.getInstance()).getString("Active");
        if(result == null) return false;
        return result.equals(b.getName());
    }

    private boolean isAvailableToUser(VikingBlessing b){
        return user.getDataContainer().get(Vikings.getInstance()).getStringList("AvailableBlessings").contains(b.getName());
    }

    @Override
    @Nonnull
    public Inventory getInventory() {
        return inv;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getWhoClicked().equals(player)) return;
        if(event.getClickedInventory() instanceof PlayerInventory) return;
        if(event.getCurrentItem() == null) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        Player player = (Player) event.getWhoClicked();

        if(clickedItem.getEnchantments().size() > 0){
            player.sendMessage("You are already using this blessing!");
            return;
        }

        if(clickedItem.getType() == Material.PAPER){
            player.sendMessage("This is not an available blessing!");
            return;
        }

        Announcer.ac(clickedItem.getItemMeta().getDisplayName());

        VikingBlessing blessing = VikingBlessing.fromName(clickedItem.getItemMeta().getDisplayName());
        if(blessing == null) throw new CrownException(player, "Blessing is null");

        blessing.beginUsage(FtcCore.getUser(player));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getInventory().getHolder() instanceof RaidSelector)) return;
        HandlerList.unregisterAll(this);
    }
}

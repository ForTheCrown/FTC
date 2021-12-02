package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.user.data.Pet;
import net.forthecrown.economy.pirates.BlackMarketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BmParrotListener extends AbstractInvListener implements Listener {

    public BmParrotListener(Player player) {
        super(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getWhoClicked().equals(player)) return;

        if(event.isShiftClick()) event.setCancelled(true);
        if(event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if(BlackMarketUtils.isInvalidItem(item, event.getView())) return;

        NBT tag = NbtHandler.ofItemTags(item);
        Pet pet = Pet.valueOf(tag.getString("pet"));

        if(user.hasPet(pet)){
            user.sendMessage(
                    Component.translatable("pirates.parrot.alreadyOwned", NamedTextColor.GRAY, pet.getName())
            );
            return;
        }

        if(pet.requiresRank()){
            if(!user.hasRank(pet.getRequiredRank())){
                user.sendMessage(
                        Component.translatable(
                                "pirates.parrot.noRank",
                                NamedTextColor.GRAY,
                                pet.getRequiredRank().noEndSpacePrefix()
                        )
                );
                return;
            }
        } else {
            if(!Crown.getEconomy().has(user.getUniqueId(), pet.getRequiredBal())){
                user.sendMessage(
                        Component.translatable("commands.cannotAfford",
                                NamedTextColor.GRAY,
                                FtcFormatter.rhines(pet.getRequiredBal())
                        )
                );
                return;
            }
        }

        String variantName = pet.getVariant().name().toLowerCase().replaceAll("cyan", "aqua");

        user.addPet(pet);
        user.sendMessage(
                Component.translatable(
                        "pirates.parrot.bought",
                        NamedTextColor.YELLOW,
                        pet.getName().color(NamedTextColor.GOLD),
                        Component.text("/parrot " + variantName)
                                .hoverEvent(Component.translatable("pirates.parrot.bought.hover"))
                                .clickEvent(ClickEvent.runCommand("/parrot " + variantName))
                )
        );
    }
}

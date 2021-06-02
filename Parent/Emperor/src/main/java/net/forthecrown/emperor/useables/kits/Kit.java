package net.forthecrown.emperor.useables.kits;

import net.forthecrown.emperor.utils.Nameable;
import net.forthecrown.emperor.utils.SilentPredicate;
import net.forthecrown.emperor.serialization.Deleteable;
import net.forthecrown.emperor.serialization.JsonSerializable;
import net.forthecrown.emperor.useables.Preconditionable;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.function.Predicate;

//Lot of extensions huh :|
public interface Kit extends JsonSerializable, Preconditionable, Predicate<Player>, SilentPredicate<Player>, Nameable, Keyed, HoverEventSource<Component>, Deleteable {
    boolean attemptItemGiving(Player player);
    void giveItems(Player player);

    default Component displayName(){
        return Component.text(getName())
                .hoverEvent(this)
                .clickEvent(ClickEvent.runCommand("/kit " + getName()));
    }

    List<ItemStack> getItems();

    boolean hasSpace(PlayerInventory inventory);
}

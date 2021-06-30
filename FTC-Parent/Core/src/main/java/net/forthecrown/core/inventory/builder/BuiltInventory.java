package net.forthecrown.core.inventory.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.forthecrown.core.CrownCore;
import net.forthecrown.events.dynamic.InventoryBuilderListener;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BuiltInventory implements InventoryHolder {

    private final Int2ObjectMap<InventoryOption> options;
    private final Component title;
    private final int size;

    private final InventoryAction onClose;
    private final InventoryAction onOpen;

    public BuiltInventory(Int2ObjectMap<InventoryOption> options,
                          Component title,
                          int size,
                          InventoryAction onClose,
                          InventoryAction onOpen
    ) {
        this.options = options;
        this.title = title;
        this.size = size;
        this.onClose = onClose;
        this.onOpen = onOpen;
    }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("Use createInventory(CrownUser user) or open(CrownUser user)");
    }

    public boolean hasOption(int slot){
        return options.containsKey(slot);
    }

    public InventoryOption getOption(int slot){
        return options.get(slot);
    }

    public void run(Player player, InventoryClickEvent event){
        InventoryOption option = options.get(event.getSlot());
        if(option == null) return;

        ClickContext context = new ClickContext(player, event.getSlot(), event.getCursor());

        try {
            option.run(UserManager.getUser(player), context);
        } catch (RoyalCommandException e){
            player.sendMessage(e.formattedText());
        }
    }

    public void open(Player player) {
        open(UserManager.getUser(player));
    }

    public void open(CrownUser user){
        if(onOpen != null) onOpen.run(user);

        Inventory inventory = createInventory(user);
        user.getPlayer().openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(new InventoryBuilderListener(this, user.getPlayer()), CrownCore.inst());
    }

    public Inventory createInventory(CrownUser user){
        Inventory inv = Bukkit.createInventory(this, size, title);

        for (InventoryOption o: options.values()){
            o.place(inv, user);
        }

        return inv;
    }

    public InventoryAction getOnClose() {
        return onClose;
    }

    public InventoryAction getOnOpen() {
        return onOpen;
    }
}

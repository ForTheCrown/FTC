package net.forthecrown.inventory.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.inventory.builder.options.OptionPriority;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
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

    private final InventoryCloseAction onClose;
    private final InventoryAction onOpen;

    public BuiltInventory(Int2ObjectMap<InventoryOption> options,
                          Component title,
                          int size,
                          InventoryCloseAction onClose,
                          InventoryAction onOpen
    ) {
        this.options = new Int2ObjectOpenHashMap<>(options);
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
        return getOptions().containsKey(slot);
    }

    public InventoryOption getOption(int slot){
        return getOptions().get(slot);
    }

    public void run(Player player, InventoryClickEvent event){
        InventoryOption option = getOptions().get(event.getSlot());
        if(option == null) return;
        if(Cooldown.contains(player, getClass().getSimpleName())) return;

        ClickContext context = new ClickContext(player, event.getSlot(), event.getCursor(), event.getClick());

        try {
            option.onClick(UserManager.getUser(player), context);

            if(context.shouldCooldown()) Cooldown.add(player, getClass().getSimpleName(), 5);
            if(context.shouldReload()) open(player);
            event.setCancelled(context.shouldCancelEvent());
        } catch (RoyalCommandException e){
            player.sendMessage(e.formattedText());
        }
    }

    public void open(Player player) {
        open(UserManager.getUser(player));
    }

    public void open(CrownUser user){
        if(getOnOpen() != null) getOnOpen().run(user);

        Inventory inventory = createInventory(user);
        user.getPlayer().openInventory(inventory);
    }

    public Inventory createInventory(CrownUser user){
        Inventory inv = Bukkit.createInventory(this, size, title);

        ObjectList<InventoryOption> lows = new ObjectArrayList<>();
        ObjectList<InventoryOption> mids = new ObjectArrayList<>();
        ObjectList<InventoryOption> highs = new ObjectArrayList<>();

        for (InventoryOption o: options.values()){
            if(o.getPriority() == OptionPriority.LOW) lows.add(o);
            else if(o.getPriority() == OptionPriority.MID) mids.add(o);
            else highs.add(o);
        }

        lows.forEach(o -> o.place(inv, user));
        mids.forEach(o -> o.place(inv, user));
        highs.forEach(o -> o.place(inv, user));

        return inv;
    }

    public InventoryCloseAction getOnClose() {
        return onClose;
    }

    public InventoryAction getOnOpen() {
        return onOpen;
    }

    public Int2ObjectMap<InventoryOption> getOptions() {
        return Int2ObjectMaps.unmodifiable(options);
    }
}

package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record TimeSelectionOption(PunishEntry entry,
                           PunishType type,
                           String extra,
                           TimeAmount timeAmount,
                           InventoryPos pos
) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(timeAmount.displayMaterial, 1)
                .setName(Component.text(timeAmount.display).style(nonItalic()));

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        Punishments.handlePunish(entry.entryUser(), user.getCommandSource(), null, timeAmount.time, type, extra);

        context.setShouldClose(true);
    }

    public enum TimeAmount {
        HOUR ("1 Hour", TimeUtil.HOUR_IN_MILLIS, Material.LIME_WOOL),
        DAY ("1 Day", TimeUtil.DAY_IN_MILLIS, Material.YELLOW_WOOL),
        WEEK ("1 Day", TimeUtil.WEEK_IN_MILLIS, Material.ORANGE_WOOL),
        MONTH ("1 Month", TimeUtil.MONTH_IN_MILLIS, Material.RED_WOOL),

        INDEFINITE ("Forever", Punishments.INDEFINITE_EXPIRY, Material.BLACK_WOOL);

        private final long time;
        private final String display;
        private final Material displayMaterial;

        TimeAmount(String display, long expiry, Material material) {
            this.display = display;
            this.time = expiry;
            this.displayMaterial = material;
        }
    }
}
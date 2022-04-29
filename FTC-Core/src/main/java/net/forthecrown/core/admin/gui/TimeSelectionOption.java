package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.util.Mth;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record TimeSelectionOption(PunishBuilder builder,
                           TimeAmount timeAmount,
                           int multiplier,
                           InventoryPos pos
) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(timeAmount.displayMaterial, 1)
                .setName(Component.text(timeAmount.getDisplay(multiplier)).style(nonItalic()));

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        builder.setLength(timeAmount.time * multiplier);

        builder.handle(user.getCommandSource());
        context.setShouldClose(true);
    }

    @RequiredArgsConstructor
    public enum TimeAmount {
        HOUR ("{} Hour{}", TimeUtil.HOUR_IN_MILLIS, Material.LIME_WOOL),
        DAY ("{} Day{}", TimeUtil.DAY_IN_MILLIS, Material.YELLOW_WOOL),
        WEEK ("{} Week{}", TimeUtil.WEEK_IN_MILLIS, Material.ORANGE_WOOL),
        MONTH ("{} Month{}", TimeUtil.MONTH_IN_MILLIS, Material.RED_WOOL),

        INDEFINITE ("Forever", Punishments.INDEFINITE_EXPIRY, Material.BLACK_WOOL);

        private final String display;
        private final long time;
        private final Material displayMaterial;

        public String getDisplay(int multiplier) {
            multiplier = Mth.clamp(multiplier, 1, 500);

            return ChatUtils.format(display, multiplier, FtcUtils.addAnS(multiplier));
        }
    }
}
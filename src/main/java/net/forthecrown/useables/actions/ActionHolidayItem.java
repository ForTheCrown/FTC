package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.FTC;
import net.forthecrown.core.holidays.Holiday;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

@Getter
public class ActionHolidayItem extends UsageAction {
    // --- TYPE ---
    public static final UsageType<ActionHolidayItem> TYPE = UsageType.of(ActionHolidayItem.class);

    private final String holidayName;

    public ActionHolidayItem(String holidayName) {
        super(TYPE);
        this.holidayName = holidayName;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        ServerHolidays holidays = ServerHolidays.get();
        Holiday holiday = holidays.getHoliday(holidayName);

        if (holiday == null) {
            FTC.getLogger().info("Can't give holiday item for '{}', holiday does not exist", holidayName);
            return;
        }

        ItemStack item = holidays.getRewardItem(holiday, Users.get(player), ZonedDateTime.now());

        if (ItemStacks.isEmpty(item)) {
            FTC.getLogger().warn("Cannot give rewards for '{}', reward item is empty", holidayName);
            return;
        }

        Util.giveOrDropItem(
                player.getInventory(),
                player.getLocation(),
                item
        );
    }

    @Override
    public @Nullable Component displayInfo() {
        return Component.text(holidayName);
    }

    @Override
    public @Nullable Tag save() {
        return StringTag.valueOf(holidayName);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionHolidayItem parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionHolidayItem(Arguments.HOLIDAY.parse(reader).getName());
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionHolidayItem load(Tag tag) {
        return new ActionHolidayItem(tag.getAsString());
    }
}
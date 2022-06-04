package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.HolidayArgument;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.ServerHolidays;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ActionGiveHolidayItem implements UsageAction<ActionGiveHolidayItem.ActionInstance> {
    public static final Key KEY = Keys.forthecrown("give_holiday_item");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        ServerHolidays.Holiday holiday = HolidayArgument.holiday().parse(reader);

        return new ActionInstance(holiday.getName());
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(element.getAsString());
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return new JsonPrimitive(value.holidayName());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record ActionInstance(String holidayName) implements UsageActionInstance {

        @Override
        public String asString() {
            return typeKey() + "{holiday=" + holidayName + "}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }

        @Override
        public void onInteract(Player player) {
            ServerHolidays holidays = Crown.getHolidays();
            ServerHolidays.Holiday holiday = holidays.getHoliday(holidayName);

            if (holiday == null) {
                Crown.logger().info("Can't give holiday item for '{}', holiday does not exist", holidayName);
                return;
            }

            ItemStack item = holidays.getRewardItem(holiday, UserManager.getUser(player));

            if (ItemStacks.isEmpty(item)) {
                Crown.logger().warn("Cannot give rewards for '{}', reward item is empty", holidayName);
                return;
            }

            boolean hasRoom = player.getInventory().firstEmpty() != -1;

            if (hasRoom) player.getInventory().addItem(item);
            else player.getWorld().dropItem(player.getLocation(), item);
        }
    }
}
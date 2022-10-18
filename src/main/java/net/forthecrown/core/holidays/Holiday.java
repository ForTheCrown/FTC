package net.forthecrown.core.holidays;

import com.sk89q.worldedit.bukkit.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

/**
 * The class representing a single holiday instance
 */
@Getter
@Setter
public class Holiday implements InventoryHolder {
    /**
     * The holiday's name
     */
    private final String name;

    /**
     * The inventory of items the holiday might potentially give
     * to players
     */
    private final FtcInventory inventory;

    /**
     * The period of time/day the holiday might be active
     */
    private MonthDayPeriod period;

    /**
     * A list of potential mail messages that might get sent
     * to each player the holiday's rewards are given to.
     * @see #getMailMessage(ZonedDateTime, Random, User)
     */
    private final List<Component> mails = new ObjectArrayList<>();

    /**
     * True, if the holiday should give it's inventory automatically,
     * false otherwise
     */
    private boolean autoGiveItems;

    /**
     * The reward container, it contains data about the container the
     * inventory will be given in, like if it's a chest or shulker
     */
    private RewardContainer container = RewardContainer.defaultContainer();

    private RewardRange
            rhines = RewardRange.NONE,
            gems = RewardRange.NONE;

    /**
     * True by default, determines if the dayUpdate ignores this holiday
     * or not, if false, it gets ignored
     */
    private boolean enabled = true;

    /**
     * True, if the holiday is currently ongoing, false otherwise
     */
    private boolean active = false;

    public Holiday(String name) {
        this.name = name;
        this.inventory = FtcInventory.of(this, ServerHolidays.INV_SIZE, name());
    }

    public void save(CompoundTag tag) {
        tag.put("time", period.save());
        tag.putBoolean("autoItems", autoGiveItems);
        tag.put("container", container.save());

        tag.putBoolean("enabled", enabled);
        tag.putBoolean("active", active);

        if (!mails.isEmpty()) {
            ListTag mailTag = new ListTag();
            mails.forEach(component -> mailTag.add(TagUtil.writeText(component)));

            tag.put("mails", mailTag);
        }

        if (!rhines.isNone()) {
            tag.put("rhines", rhines.save());
        }
        if (!gems.isNone()) {
            tag.put("gems", gems.save());
        }

        if (!inventory.isEmpty()) {
            ListTag content = new ListTag();

            for (int i = 0; i < ServerHolidays.INV_SIZE; i++) {
                ItemStack item = inventory.getItem(i);
                if (ItemStacks.isEmpty(item)) {
                    continue;
                }

                CompoundTag iTag = ItemStacks.save(item);

                iTag.putInt("slot", i);
                content.add(iTag);
            }

            tag.put("inventory", content);
        }
    }

    public void load(CompoundTag tag) {
        setPeriod(MonthDayPeriod.load(tag.get("time")));
        setAutoGiveItems(tag.getBoolean("autoItems"));
        setContainer(RewardContainer.load(tag.get("container")));

        setRhines(RewardRange.load(tag.get("rhines")));
        setGems(RewardRange.load(tag.get("gems")));

        enabled = tag.getBoolean("enabled");
        active = tag.getBoolean("active");

        if (tag.contains("mails")) {
            ListTag list = tag.getList("mails", Tag.TAG_STRING);
            list.forEach(tag1 -> mails.add(TagUtil.readText(tag1)));
        } else {
            mails.clear();
        }

        if (tag.contains("inventory")) {
            ListTag content = tag.getList("inventory", Tag.TAG_COMPOUND);

            for (Tag t : content) {
                CompoundTag iTag = (CompoundTag) t;
                int slot = iTag.getInt("slot");
                iTag.remove("slot");

                ItemStack item = ItemStacks.load(iTag);

                inventory.setItem(slot, item);
            }
        } else {
            inventory.clear();
        }
    }

    /**
     * Gets a random mail message to display.
     * <p>
     * This will get a random mail message, or the
     * first message in the list if the size is 0,
     * and then use {@link HolidayTags} to format
     * the message to the correct from given the
     * current context.
     *
     * @param time The date time to use for formatting
     *             the result.
     *
     * @param random The random used to pick the random
     *               mail message.
     *
     * @param user The user the message is targeted towards.
     *
     * @return The found and formatted mail message, or null
     *         if the messages list is empty.
     */
    public Component getMailMessage(ZonedDateTime time, Random random, User user) {
        if (mails.isEmpty()) {
            return null;
        }

        Component c;

        // If list size 1, get entry 0
        // otherwise, let random pick an
        // entry
        if (mails.size() == 1) {
            c = mails.get(0);
        } else {
            c = mails.get(random.nextInt(mails.size()));
        }

        return HolidayTags.replaceTags(c, user, this, time);
    }

    /**
     * Checks if this holiday has no rewards to give
     *
     * @return True, if and only if, the rhine and gem rewards are empty AND autoGiveItems is false or the inventory is empty
     */
    public boolean hasNoRewards() {
        return rhines.isNone() && gems.isNone() && (!autoGiveItems || inventory.isEmpty());
    }

    /**
     * Gets the mail message attachment tag
     *
     * @param removable True, if the mail message should be removeable
     * @return The mail attachment tag
     */
    public String getAttachmentTag(boolean removable) {
        return ServerHolidays.TAG_NAMESPACE + (removable ? (ServerHolidays.TAG_SEPARATOR + "temp") : "") + ServerHolidays.TAG_SEPARATOR + getName();
    }

    public Component name() {
        return Component.text(getFilteredName());
    }

    /**
     * Gets the holiday's filtered name, this is just
     * {@link #getName()} with all the '_' replaced with spaces
     *
     * @return The filtered holiday name
     */
    public String getFilteredName() {
        return getName().replaceAll("_", " ");
    }
}
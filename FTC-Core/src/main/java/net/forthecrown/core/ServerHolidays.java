package net.forthecrown.core;

import com.sk89q.worldedit.bukkit.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.serializer.AbstractNbtSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.*;

/**
 * The class which manages the server's automated Holiday system.
 */
public class ServerHolidays extends AbstractNbtSerializer implements DayChangeListener {
    /**
     * The size of the holiday inventory given in shulkers/chests
     */
    public static final int INV_SIZE = 27;

    public static final String
            TAG_NAMESPACE = "holiday",
            TAG_SEPARATOR = ":";

    private static final Logger LOGGER = Crown.logger();

    private final Map<String, Holiday> holidays = new Object2ObjectOpenHashMap<>();

    public ServerHolidays() {
        super("holidays");

        reload();
    }

    @Override
    public void onDayChange() {
        ZonedDateTime time = ZonedDateTime.now();

        // Go through each holiday
        for (var h: holidays.values()) {
            // If the holiday has no rewards or is disabled, skip it
            if (h.hasNoRewards() || !h.isEnabled()) continue;

            HolidayPeriod period = h.getPeriod();

            // If should be active
            if (period.shouldBeActive(time)) {
                if (period.isActive()) continue; // It's already active, no need to do anything
                // Activate holiday
                runHoliday(h);

                // If not exact, set it to be active
                // Needs to be exact so we can later
                // remove items of people who didn't claim
                if (!period.isExact()) {
                    LOGGER.info("Set {} to be active", h.getName());
                    period.setActive(true);
                }
            } else {
                if (period.isExact()) continue;

                // If this is still active when it shouldn't be
                // then turn it off lol
                if (period.isActive()) {
                    LOGGER.info("Deactivating {}", h.getName());

                    period.setActive(false);
                    deactivate(h);
                }
            }
        }
    }

    /**
     * Gets all holidays
     * @return All holidays
     */
    public Collection<Holiday> getAll() {
        return holidays.values();
    }

    /**
     * Adds the given holiday
     * @param holiday The holiday to add
     */
    public void addHoliday(Holiday holiday) {
        holidays.put(holiday.getName().toLowerCase(), holiday);
    }

    /**
     * Removes the given holiday
     * @param holiday The holiday to remove
     */
    public void remove(Holiday holiday) {
        holidays.remove(holiday.getName().toLowerCase());
    }

    /**
     * Gets a holiday by the given name
     * @param name The name of the holiday
     * @return The gotten holiday, null, if none found
     */
    public Holiday getHoliday(String name) {
        return holidays.get(name.toLowerCase());
    }

    /**
     * Gets all the holiday's names
     * @return The holiday names
     */
    public Collection<String> getNames() {
        return ListUtils.convert(holidays.values(), Holiday::getName);
    }

    /**
     * Deactivates the given holiday.
     * What this means is that it will go through every users'
     * mail and remove the given holiday's rewards from it, if
     * they have not claimed the rewards.
     *
     * @param holiday The holiday to disable
     */
    public void deactivate(Holiday holiday) {
        Crown.getUserManager().getAllUsers().whenComplete((users, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Couldn't get all users", throwable);
                return;
            }

            LOGGER.info("Removing all {} unclaimed mail from users", holiday.getName());
            String tag = holiday.getAttachmentTag(true);

            users.forEach(user -> {
                UserMail mail = user.getMail();

                mail.getMail().removeIf(message -> {
                    if (!UserMail.hasAttachment(message)) {
                        return false;
                    }

                    if (message.attachmentClaimed) {
                        return false;
                    }

                    return tag.equals(message.attachment.tag);
                });
            });

            Crown.getUserManager().unloadOffline();
        });
    }

    /**
     * Runs the given holiday, gives all the
     * rhine and/or gem rewards and items to all
     * players by sending it to their mailbox
     *
     * @param holiday The holiday to run
     */
    public void runHoliday(Holiday holiday) {
        Validate.isTrue(!holiday.hasNoRewards(), "Holiday has no rewards to give");

        Crown.getUserManager().getAllUsers().whenComplete((users, throwable) -> {
            if(throwable != null) {
                LOGGER.error("Couldn't get all users", throwable);
                return;
            }

            LOGGER.info("Giving all {} rewards", holiday.getName());

            // Generate gem and rhine amounts here
            // so all players get the same amount
            final int rhines = holiday.getRhines().get(FtcUtils.RANDOM);
            final int gems = holiday.getGems().get(FtcUtils.RANDOM);

            users.forEach(user -> giveRewards(user, holiday, rhines, gems));
            Crown.getUserManager().unloadOffline();
        });
    }

    /**
     * Gives a holiday's rewards to a specific user
     * @param user The user to give
     * @param holiday The holiday to give the rewards of
     */
    public void giveRewards(CrownUser user, Holiday holiday) {
        giveRewards(user, holiday,
                holiday.getRhines().get(FtcUtils.RANDOM),
                holiday.getGems().get(FtcUtils.RANDOM)
        );
    }

    // Since runHoliday(Holiday) needs to give the same
    // amount of rhines/gems to all players we need to
    // have this method which accepts a rhine and gem
    // reward parameter, as those amounts are generated
    // in the runHoliday function
    private void giveRewards(CrownUser user, Holiday holiday, int rhines, int gems) {
        Validate.isTrue(!holiday.hasNoRewards(), "Holiday has no rewards to give");

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW);

        // If the holiday has mails, add a random mail message
        // else use the generic message lol
        if (!holiday.getMails().isEmpty()) {
            builder.append(holiday.getMailMessage(ZonedDateTime.now(), FtcUtils.RANDOM, user));
        } else {
            builder.append(
                    Component.text("Holiday!", NamedTextColor.YELLOW)
                            .append(Component.text(" What holiday? We don't know :D", NamedTextColor.GRAY))
            );
        }

        builder.append(Component.text("."));

        // If they're online, tell them they can claim stuff :D
        if (user.isOnline()) {
            user.sendMessage(
                    builder.build()
                            .append(Component.space())
                            .append(
                                    Component.translatable("mail.goClaim", NamedTextColor.AQUA)
                                            .hoverEvent(Component.text("Click me!"))
                                            .clickEvent(ClickEvent.runCommand("/mail claim 1"))
                            )
            );
        }

        ItemStack item = null;

        // If item should be given and if we have items to give
        if(holiday.isAutoGiveItems() && !holiday.getInventory().isEmpty()) {
            item = getRewardItem(holiday, user);
        }

        // Create the mail attachment
        UserMail.MailAttachment attachment = new UserMail.MailAttachment();
        attachment.item = item;
        attachment.gems = gems;
        attachment.rhines = rhines;
        attachment.tag = holiday.getAttachmentTag(!holiday.getPeriod().isExact());

        // Create the mail message
        UserMail.MailMessage message = new UserMail.MailMessage(builder.build(), null, System.currentTimeMillis());
        message.attachmentClaimed = false;
        message.attachment = attachment;

        // Just add the mail
        user.getMail().add(message);
    }

    /**
     * Gets the reward item of a given holiday for the given user
     * @param holiday The holiday to get the reward item for
     *
     * @param user The user to make the item for, it will not be given
     *             just used as a context object
     * @return The created item, null, if holiday has no items to give
     */
    public ItemStack getRewardItem(Holiday holiday, CrownUser user) {
        // Nothing to give? Return null B)
        if (holiday.getInventory().isEmpty()) {
            return null;
        }

        ZonedDateTime time = ZonedDateTime.now();

        ItemStack item = holiday.getContainer().createItem();
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        Container chest = (Container) meta.getBlockState();
        Inventory inv = chest.getInventory();

        for (int i = 0; i < INV_SIZE; i++) {
            ItemStack instItem = holiday.getInventory().getItem(i);
            if(ItemStacks.isEmpty(instItem)) continue;

            instItem = instItem.clone();
            ItemMeta meta1 = instItem.getItemMeta();

            // Format item metadata
            holiday.getContainer().formatExistingInfo(time, holiday, meta1, user);

            instItem.setItemMeta(meta1);
            inv.setItem(i, instItem);
        }

        // Format the container's metadata
        holiday.getContainer().apply(time, holiday, meta, user);

        // Set the stuff
        meta.setBlockState(chest);
        item.setItemMeta(meta);

        // Return the stuff
        return item.clone();
    }

    @Override
    protected void save(CompoundTag tag) {
        for (var e: holidays.entrySet()) {
            CompoundTag hTag = new CompoundTag();
            e.getValue().save(hTag);

            // Save each holiday with the getName() function and
            // not the e.getKey() because that gets toLowerCase()'d
            tag.put(e.getValue().getName(), hTag);
        }
    }

    @Override
    protected void reload(CompoundTag tag) {
        holidays.clear();

        for (var e: tag.tags.entrySet()) {
            // Create holiday and load it, then add it, simple as
            Holiday holiday = new Holiday(e.getKey());
            holiday.load((CompoundTag) e.getValue());

            addHoliday(holiday);
        }
    }

    /**
     * The class representing a single holiday instance
     */
    @Getter @Setter
    public static class Holiday implements InventoryHolder, Nameable {
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
        private HolidayPeriod period;

        /**
         * A list of potential mail messages that might get sent
         * to each player the holiday's rewards are given to
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

        public Holiday(String name) {
            this.name = name;
            this.inventory = FtcInventory.of(this, INV_SIZE, name());
        }

        public void save(CompoundTag tag) {
            tag.put("time", period.save());
            tag.putBoolean("autoItems", autoGiveItems);
            tag.put("container", container.save());

            if (!enabled) {
                tag.putBoolean("enabled", false);
            }

            if (!mails.isEmpty()) {
                ListTag mailTag = new ListTag();
                mails.forEach(component -> mailTag.add(StringTag.valueOf(ChatUtils.GSON.serialize(component))));

                tag.put("mails", mailTag);
            }

            if (!rhines.isNone()) tag.put("rhines", rhines.save());
            if (!gems.isNone()) tag.put("gems", gems.save());

            if (!inventory.isEmpty()) {
                ListTag content = new ListTag();

                for (int i = 0; i < INV_SIZE; i++) {
                    ItemStack item = inventory.getItem(i);
                    if (ItemStacks.isEmpty(item)) continue;

                    CompoundTag iTag = ItemStacks.save(item);

                    iTag.putInt("slot", i);
                    content.add(iTag);
                }

                tag.put("inventory", content);
            }
        }

        public void load(CompoundTag tag) {
            setPeriod(HolidayPeriod.load(tag.get("time")));
            setAutoGiveItems(tag.getBoolean("autoItems"));
            setContainer(RewardContainer.load(tag.get("container")));

            setRhines(RewardRange.load(tag.get("rhines")));
            setGems(RewardRange.load(tag.get("gems")));

            enabled = !tag.contains("enabled");

            if(tag.contains("mails")) {
                ListTag list = tag.getList("mails", Tag.TAG_STRING);
                list.forEach(tag1 -> mails.add(ChatUtils.fromJsonText(tag1.getAsString())));
            } else mails.clear();

            if (tag.contains("inventory")) {
                ListTag content = tag.getList("inventory", Tag.TAG_COMPOUND);

                for (Tag t: content) {
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
         * Gets a random mail message
         * @param random The random to use
         * @return A random mail message
         */
        public Component getMailMessage(ZonedDateTime time, Random random, CrownUser user) {
            if (mails.isEmpty()) return null;
            if (mails.size() == 1) return mails.get(0);

            Component c = mails.get(random.nextInt(mails.size()));
            return getContainer().format(time, this, c, user);
        }

        /**
         * Checks if this holiday has no rewards to give
         * @return True, if and only if, the rhine and gem rewards are empty AND autoGiveItems is false or the inventory is empty
         */
        public boolean hasNoRewards() {
            return rhines.isNone() && gems.isNone() && (!autoGiveItems || inventory.isEmpty());
        }

        /**
         * Gets the mail message attachment tag
         * @param removable True, if the mail message should be removeable
         * @return The mail attachment tag
         */
        public String getAttachmentTag(boolean removable) {
            return TAG_NAMESPACE + (removable ? (TAG_SEPARATOR + "temp") : "") + TAG_SEPARATOR + getName();
        }

        @Override
        public Component name() {
            return Component.text(getFilteredName());
        }

        /**
         * Gets the holiday's filtered name, this is just
         * {@link #getName()} with all the '_' replaced with spaces
         * @return The filtered holiday name
         */
        public String getFilteredName() {
            return getName().replaceAll("_" , " ");
        }
    }

    /**
     * A range of rewards for a currency type item
     */
    @Data
    public static class RewardRange {
        public static RewardRange NONE = new RewardRange(0, 0);

        private final int min, max;

        /**
         * Creates a reward range between the given amounts
         * @param min The minimum amount
         * @param max The maximum amount
         * @return The created range
         */
        public static RewardRange between(int min, int max) {
            return new RewardRange(
                    Math.min(min, max),
                    Math.max(min, max)
            );
        }

        /**
         * Creates a reward range of the exact value
         * @param val The value of the range
         * @return The created range
         */
        public static RewardRange exact(int val) {
            return new RewardRange(val, val);
        }

        /**
         * Loads the rewards from the given tag
         * @param tag Tag to load from, {@link RewardRange#NONE}, if the tag is null
         * @return The loaded range
         */
        public static RewardRange load(Tag tag) {
            if (tag == null) {
                return NONE;
            }

            if(tag.getId() == Tag.TAG_INT) {
                return exact(((IntTag) tag).getAsInt());
            }

            IntArrayTag arr = (IntArrayTag) tag;
            return between(arr.get(0).getAsInt(), arr.get(1).getAsInt());
        }

        /**
         * Checks if the range is not a range lol
         * @return True if the min == max
         */
        public boolean isExact() {
            return min == max;
        }

        /**
         * Gets if the range represents a NULL value range,
         * meaning a range which gives nothing, it goes from 0 to 0
         * @return True, if this reward will always reward 0
         */
        public boolean isNone() {
            return this == NONE || min > max || (min == 0 && max == 0);
        }

        /**
         * Gets A random, rounded amount this range gives
         * @param random The random to use
         * @return A random amount between min and max, rounded down
         */
        public int get(Random random) {
            if (isNone()) return 0;
            if (isExact()) return min;

            int dif = max - min;
            int initialResult = min + random.nextInt(dif);

            // If less than 10000, we should round down to
            // the next 100 not the next 1000
            int rndValue = initialResult < 10_000 ? 100 : 1000;

            // Round down either by a thousand or a hundred,
            // depending on the initially found random result
            return initialResult - (initialResult % rndValue);
        }

        /**
         * Saves the range
         * @return The saved tag, will be an int tag if this range is exact, otherwise IntArrayTag
         */
        public Tag save() {
            return isExact() ? IntTag.valueOf(min) : new IntArrayTag(new int[] {min, max});
        }

        public String toString() {
            if(isNone()) return "None";
            if(isExact()) return min + "";

            return min + ".." + max;
        }
    }

    /**
     * Represents the period of time in which a holiday is active,
     * or just the date its active
     */
    @Data
    public static class HolidayPeriod {
        private final Month startMonth, endMonth;
        private final int startDate, endDate;

        /**
         * True, if the holiday is currently ongoing, false otherwise
         */
        private boolean active;

        /**
         * Loads a period from the given tag
         * @param t The tag to load from
         * @return The loaded tag
         */
        public static HolidayPeriod load(Tag t) {
            CompoundTag tag = (CompoundTag) t;
            boolean active = tag.getBoolean("active");

            if (tag.contains("date")) {
                int[] arr = tag.getIntArray("date");

                HolidayPeriod period = exact(Month.of(arr[0]), arr[1]);
                period.setActive(active);

                return period;
            }

            int[] arr = tag.getIntArray("period");

            HolidayPeriod period = between(
                    Month.of(arr[0]), arr[1],
                    Month.of(arr[2]), arr[3]
            );

            period.setActive(active);
            return period;
        }

        /**
         * Gets a period for a single date of a year
         * @param month The month of the holiday
         * @param date The day-of-month of the holiday
         * @return The created period
         */
        public static HolidayPeriod exact(Month month, int date) {
            return new HolidayPeriod(month, month, date, date);
        }

        /**
         * Creates a period that would be active between the given dates
         * @param m1 The starting month
         * @param start The starting day-of-month
         * @param m2 The ending month
         * @param end The ending day-of-month
         * @return The created period
         */
        public static HolidayPeriod between(Month m1, int start,  Month m2, int end) {
            return new HolidayPeriod(m1, m2, start, end);
        }

        /**
         * Checks if this period should be active right now
         * @param time The timezone to use
         * @return True, if this holiday should be active right now
         */
        public boolean shouldBeActive(ZonedDateTime time) {
            if (isExact()) {
                return time.getDayOfMonth() == startDate && time.getMonth() == startMonth;
            }

            if (!MathUtil.inRange(time.getMonthValue(), startMonth.getValue(), endMonth.getValue())) {
                return false;
            }

            return MathUtil.inRange(time.getDayOfMonth(), startDate, endDate);
        }

        /**
         * Checks if this period only lasts a single day
         * @return True, if the period only lasts a single day
         */
        public boolean isExact() {
            return endMonth == startMonth && startDate == endDate;
        }

        public Tag save() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("active", active);

            if (isExact()) {
                tag.putIntArray("date", new int[] {
                        startMonth.getValue(), startDate
                });
            } else {
                tag.putIntArray("period", new int[] {
                        startMonth.getValue(), startDate,
                        endMonth.getValue(), endDate
                });
            }

            return tag;
        }

        @Override
        public String toString() {
            if (isExact()) {
                return formatTime(startMonth, startDate);
            }

            return formatTime(startMonth, startDate) + " - " + formatTime(endMonth, endDate);
        }

        private static String formatTime(Month month, int date) {
            return String.format("%02d/%s", date, month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH));
        }
    }

    /**
     * Holds data about the display name, lore and type
     * a holiday's reward items should be given in
     */
    @Getter @Setter
    public static class RewardContainer {
        public static final String
                DEF_NAME = "&6%year %name Rewards",
                DEF_LORE = "&eA special %type full of goodies!";

        public static final String[] REPLACE_TAGS = {
                "%name",
                "%month",
                "%wday",
                "%date",
                "%year",
                "%type",
                "%plr"
        };

        /**
         * True, if the items should be given in a chest, false for a shulker
         */
        private boolean chest;

        /**
         * The container's unformatted name
         */
        private String name;

        /**
         * The container's unformatted lore
         */
        private List<String> lore = new ObjectArrayList<>();

        /**
         * Loads a container from the given tag
         * @param t The tag to load from
         * @return The created container
         */
        public static RewardContainer load(Tag t) {
            CompoundTag tag = (CompoundTag) t;

            RewardContainer container = new RewardContainer();
            container.setChest(tag.getBoolean("chest"));
            container.setName(tag.getString("name"));

            if (tag.contains("lore")) {
                ListTag loreT = tag.getList("lore", Tag.TAG_STRING);
                for (Tag lTag: loreT) container.lore.add(lTag.getAsString());
            }

            return container;
        }

        /**
         * Creates a default container with the {@link RewardContainer#DEF_NAME} and
         * {@link RewardContainer#DEF_LORE}
         *
         * @return The created default container
         */
        public static RewardContainer defaultContainer() {
            RewardContainer c = new RewardContainer();
            c.setName(DEF_NAME);
            c.getLore().add(DEF_LORE);

            return c;
        }

        public Tag save() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("chest", chest);
            tag.putString("name", name);

            if (!lore.isEmpty()) {
                ListTag loreT = new ListTag();
                for (String s: lore) loreT.add(StringTag.valueOf(s));
                tag.put("lore", loreT);
            }

            return tag;
        }

        /**
         * Formats the lore and display name of an existing item meta,
         * unlike {@link #apply(ZonedDateTime, Holiday, ItemMeta, CrownUser)} this
         * will not override any existing display name or lore
         *
         * @param time The current time
         * @param holiday The holiday this container belongs to
         * @param meta The meta to format
         * @param recipient The recipient of the reward
         */
        public void formatExistingInfo(ZonedDateTime time, Holiday holiday, ItemMeta meta, CrownUser recipient) {
            if (meta.hasDisplayName()) {
                Component display = meta.displayName();
                meta.displayName(format(time, holiday, display, recipient));
            }

            List<Component> lore = meta.lore();
            if (!ListUtils.isNullOrEmpty(lore)) {
                lore = ListUtils.convert(lore, component -> format(time, holiday, component, recipient));
                meta.lore(lore);
            }
        }

        /**
         * Applies the container's formatting to the given meta. Sets the display
         * name to a formatted {@link #getName()} and formats the lore
         *
         * @param time The current time
         * @param holiday The holiday this container belongs to
         * @param meta The meta to format
         * @param recipient The recipient of the reward
         */
        public void apply(ZonedDateTime time, Holiday holiday, ItemMeta meta, CrownUser recipient) {
            meta.displayName(format(time, holiday, getName(), recipient));

            if (!lore.isEmpty()) {
                List<Component> fLore = ListUtils.convert(lore, s -> format(time, holiday, s, recipient));
                meta.lore(fLore);
            }
        }

        /**
         * Formats a given message
         * @param time The current time
         * @param holiday The holiday this container belongs to
         * @param c The message to format
         * @param recipient The recipient of the reward
         * @return The formatted message
         */
        public Component format(ZonedDateTime time, Holiday holiday, Component c, CrownUser recipient) {
            return format(time, holiday, ChatUtils.LEGACY.serialize(ChatUtils.renderToSimple(c)), recipient);
        }

        /**
         * Formats a given message
         * @param time The current time
         * @param holiday The holiday this container belongs to
         * @param s The message to format
         * @param recipient The recipient of the reward
         * @return The formatted message
         */
        public Component format(ZonedDateTime time, Holiday holiday, String s, CrownUser recipient) {
            s = s
                    .replaceAll(REPLACE_TAGS[0], holiday.getFilteredName())
                    .replaceAll(REPLACE_TAGS[1], time.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH))
                    .replaceAll(REPLACE_TAGS[2], time.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH))
                    .replaceAll(REPLACE_TAGS[3], time.getDayOfMonth() + "")
                    .replaceAll(REPLACE_TAGS[4], time.getYear() + "")
                    .replaceAll(REPLACE_TAGS[5], chest ? "Chest" : "Shulker")
                    .replaceAll(REPLACE_TAGS[6], recipient.getNickOrName());

            return ChatUtils.wrapForItems(FtcFormatter.formatString(s));
        }

        /**
         * Creates an item for this container
         * @return The created item
         */
        public ItemStack createItem() {
            return new ItemStack(chest ? Material.CHEST : Material.SHULKER_BOX, 1);
        }

        public String getName() {
            return FtcUtils.isNullOrBlank(name) ? DEF_NAME : name;
        }
    }
}
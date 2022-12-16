package net.forthecrown.core.holidays;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.script2.Script;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.MailAttachment;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.UserMail;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * The class which manages the server's automated Holiday system.
 */
public class ServerHolidays extends SerializableObject.NbtDat {
    /**
     * The size of the holiday inventory given in shulkers/chests
     */
    public static final int INV_SIZE = 27;

    public static final String
            TAG_NAMESPACE = "holiday",
            TAG_SEPARATOR = ":";

    private static final Logger LOGGER = FTC.getLogger();

    private static final ServerHolidays inst = new ServerHolidays();

    private final Map<String, Holiday> holidays = new Object2ObjectOpenHashMap<>();

    public ServerHolidays() {
        super(PathUtil.pluginPath("holidays.dat"));
    }

    public static ServerHolidays get() {
        return inst;
    }

    @OnDayChange
    void onDayChange(ZonedDateTime time) {
        // Go through each holiday
        for (var h: holidays.values()) {
            // If the holiday has no rewards or is disabled, skip it
            if (h.hasNoRewards() || !h.isEnabled()) {
                continue;
            }

            MonthDayPeriod period = h.getPeriod();

            // If should be active
            if (period.contains(time.toLocalDate())) {
                // It's already active, no need to do anything
                if (h.isActive()) {
                    continue;
                }

                // Activate holiday
                runHoliday(h);

                // If not exact, set it to be active
                // Needs to be exact so we can later
                // remove items of people who didn't claim
                if (!period.isExact()) {
                    LOGGER.info("Set {} to be active", h.getName());
                    h.setActive(true);
                }
            } else {
                // Shouldn't be active, and it isn't,
                // no need to disable
                if (period.isExact()) {
                    continue;
                }

                // If this is still active when it shouldn't be
                // then turn it off lol
                if (h.isActive()) {
                    LOGGER.info("Deactivating {}", h.getName());

                    h.setActive(false);
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
        return Collections2.transform(holidays.values(), Holiday::getName);
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
        if (!Strings.isNullOrEmpty(holiday.getPeriodEndScript())) {
            Script.run(holiday.getPeriodEndScript(), "onHolidayEnd");
        }

        UserManager.get().getAllUsers().whenComplete((users, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Couldn't get all users", throwable);
                return;
            }

            LOGGER.info("Removing all {} unclaimed mail from users", holiday.getName());
            String tag = holiday.getAttachmentTag(true);

            // Remove mails with holiday tag
            users.forEach(user -> {
                UserMail mail = user.getMail();

                mail.getMail().removeIf(message -> {
                    if (MailAttachment.isEmpty(message.getAttachment())) {
                        return false;
                    }

                    if (message.getAttachment().isClaimed()) {
                        return false;
                    }

                    return tag.equals(message.getAttachment().getTag());
                });
            });

            Users.unloadOffline();
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

        if (holiday.getPeriod().isExact()) {
            if (!Strings.isNullOrEmpty(holiday.getActivationScript())) {
                Script.run(holiday.getActivationScript(), "onHolidayRun");
            }
        } else {
            if (!Strings.isNullOrEmpty(holiday.getPeriodStartScript())) {
                Script.run(holiday.getPeriodStartScript(), "onHolidayStart");
            }
        }

        UserManager.get().getAllUsers().whenComplete((users, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Couldn't get all users", throwable);
                return;
            }

            LOGGER.info("Giving all {} rewards", holiday.getName());

            // Generate gem and rhine amounts here
            // so all players get the same amount
            final int rhines = holiday.getRhines().get(Util.RANDOM);
            final int gems = holiday.getGems().get(Util.RANDOM);

            users.forEach(user -> giveRewards(user, holiday, rhines, gems));

            holiday.closeRenderer();
            Users.unloadOffline();
        });
    }

    /**
     * Gives a holiday's rewards to a specific user
     * @param user The user to give
     * @param holiday The holiday to give the rewards of
     */
    public void giveRewards(User user, Holiday holiday) {
        giveRewards(user, holiday,
                holiday.getRhines().get(Util.RANDOM),
                holiday.getGems().get(Util.RANDOM)
        );
    }

    // Since runHoliday(Holiday) needs to give the same
    // amount of rhines/gems to all players we need to
    // have this method which accepts a rhine and gem
    // reward parameter, as those amounts are generated
    // in the runHoliday function
    private void giveRewards(User user, Holiday holiday, int rhines, int gems) {
        Validate.isTrue(!holiday.hasNoRewards(), "Holiday has no rewards to give");

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW);

        ZonedDateTime time = ZonedDateTime.now();
        Component mailMsg = holiday.getMailMessage(time, Util.RANDOM, user);

        // If the holiday has mails, add a random mail message
        // else use the generic message lol
        builder.append(Objects.requireNonNullElseGet(
                mailMsg,

                () -> Component.text("Holiday!", NamedTextColor.YELLOW)
                        .append(Component.text(
                                " What holiday? We don't know :D",
                                NamedTextColor.GRAY
                        ))
        ));

        builder.append(Component.text("."));

        // If they're online, tell them they can claim stuff :D
        if (user.isOnline()) {
            user.sendMessage(
                    builder.build()
                            .append(Component.space())
                            .append(
                                    Messages.HOLIDAYS_GO_CLAIM
                            )
            );
        }

        ItemStack item = null;

        // If item should be given and if we have items to give
        if (holiday.isAutoGiveItems()
                && !holiday.getInventory().isEmpty()
        ) {
            item = getRewardItem(holiday, user, time);
        }

        // Create the mail attachment
        MailAttachment attachment = new MailAttachment();
        attachment.setItem(item);
        attachment.setGems(gems);
        attachment.setRhines(rhines);
        attachment.setTag(holiday.getAttachmentTag(!holiday.getPeriod().isExact()));

        // Create the mail message
        MailMessage message = MailMessage.of(builder.build());
        message.setAttachment(attachment);

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
    public ItemStack getRewardItem(Holiday holiday, User user, ZonedDateTime time) {
        // Nothing to give? Return null B)
        if (holiday.getInventory().isEmpty()) {
            return null;
        }

        ItemStack item = holiday.getContainer().createBaseItem();
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        Container chest = (Container) meta.getBlockState();
        Inventory inv = chest.getInventory();

        var it = ItemStacks.nonEmptyIterator(holiday.getInventory());

        while (it.hasNext()) {
            int i = it.nextIndex();
            var instItem = it.next();

            instItem = instItem.clone();
            ItemMeta meta1 = instItem.getItemMeta();

            // Format item metadata
            holiday.getContainer()
                    .formatExistingInfo(time, holiday, meta1, user);

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

    protected void save(CompoundTag tag) {
        for (var e: holidays.entrySet()) {
            CompoundTag hTag = new CompoundTag();
            e.getValue().save(hTag);

            // Save each holiday with the getName() function and
            // not the e.getKey() because that gets toLowerCase()'d
            tag.put(e.getValue().getName(), hTag);
        }
    }

    protected void load(CompoundTag tag) {
        holidays.clear();

        for (var e: tag.tags.entrySet()) {
            // Create holiday and load it, then add it, simple as
            Holiday holiday = new Holiday(e.getKey());
            holiday.load((CompoundTag) e.getValue());

            addHoliday(holiday);
        }
    }
}
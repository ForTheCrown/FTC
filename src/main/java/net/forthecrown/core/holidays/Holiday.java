package net.forthecrown.core.holidays;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.script2.Script;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.MailAttachment;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.UserMail;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The class representing a single holiday instance
 */
@Getter
@Setter
public class Holiday implements InventoryHolder {
  private static final Logger LOGGER = FTC.getLogger();

  private static final String
      TAG_SCRIPT = "activateScript",
      TAG_END_SCRIPT = "periodEndScript",
      TAG_START_SCRIPT = "periodStartScript",
      TAG_MAIL_SCRIPT = "mailScript",
      TAG_RENDER_SCRIPT = "tagRenderScript";

  /**
   * The holiday's name
   */
  private final String name;

  /**
   * The inventory of items the holiday might potentially give to players
   */
  private final FtcInventory inventory;

  /**
   * The period of time/day the holiday might be active
   */
  private MonthDayPeriod period;

  /**
   * A list of potential mail messages that might get sent to each player the holiday's rewards are
   * given to.
   *
   * @see #getMailMessage(ZonedDateTime, Random, User)
   */
  private final List<Component> mails = new ObjectArrayList<>();

  /**
   * True, if the holiday should give it's inventory automatically, false otherwise
   */
  private boolean autoGiveItems;

  /**
   * The reward container, it contains data about the container the inventory will be given in, like
   * if it's a chest or shulker
   */
  private RewardContainer container = RewardContainer.defaultContainer();

  private RewardRange
      rhines = RewardRange.NONE,
      gems = RewardRange.NONE;

  /**
   * True by default, determines if the dayUpdate ignores this holiday or not, if false, it gets
   * ignored
   */
  private boolean enabled = true;

  /**
   * True, if the holiday is currently ongoing, false otherwise
   */
  private boolean active = false;

  /**
   * Script set all mail messages this holiday sends
   */
  private String mailScript;

  /**
   * Script called when the holiday becomes active
   * <p>
   * If the holiday is a single day, this script is never called, instead
   * {@link #getActivationScript()} will be called
   */
  private String periodStartScript;

  /**
   * Script called when the holiday becomes inactive.
   * <p>
   * If the holiday is a single day, this script is never called, instead
   * {@link #getActivationScript()} will be called
   */
  private String periodEndScript;

  /**
   * Script called when the holiday is activated.
   * <p>
   * If the holiday is a period holiday, instead of a single day holiday, this will not be called,
   * instead {@link #getPeriodStartScript()} will be called for in the beginning of the period and
   * {@link #getPeriodEndScript()} called for the period ending.
   */
  private String activationScript;

  /**
   * Script called to generate a mail message for users when the holiday is being activated or ran.
   * <p>
   * Calls a <code>createMessage(ZonedDateTime, Random, User)</code> function to get the mail
   * message.
   * <p>
   * If this script is not set, then 1 of the mail messages will be randomly selected and used
   * instead.
   */
  private String mailGenScript;

  /** Cached value of {@link #mailGenScript} */
  private Script mailGen;

  /**
   * Script called to render tags in mail messages, item lore, item display names and such.
   * <p>
   * The function called in the script will be
   * <code>renderTags(Component, User, Holiday, ZonedDateTime)</code>, which
   * must return an object.
   * <p>
   * Script will be called for each line of lore, for each user and for each instance of tags being
   * replaced, as such, this may be called thousands of times per holiday activation. For that
   * reason, this script will be cached in {@link #tagRenderer} to prevent the script from being
   * loaded, executed and closed thousands of times.
   * <p>
   * If this script is set, then {@link HolidayTags} will be called before this script to render the
   * default tags.
   */
  private String tagRenderScript;

  /**
   * Cached value of {@link #tagRenderScript}
   */
  private Script tagRenderer;

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

    if (!Strings.isNullOrEmpty(activationScript)) {
      tag.putString(TAG_SCRIPT, activationScript);
    }

    if (!Strings.isNullOrEmpty(periodStartScript)) {
      tag.putString(TAG_START_SCRIPT, periodStartScript);
    }

    if (!Strings.isNullOrEmpty(periodEndScript)) {
      tag.putString(TAG_END_SCRIPT, periodEndScript);
    }

    if (!Strings.isNullOrEmpty(mailGenScript)) {
      tag.putString(TAG_MAIL_SCRIPT, mailGenScript);
    }

    if (!Strings.isNullOrEmpty(tagRenderScript)) {
      tag.putString(TAG_RENDER_SCRIPT, tagRenderScript);
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

    setActivationScript(tag.getString(TAG_SCRIPT));
    setPeriodStartScript(tag.getString(TAG_START_SCRIPT));
    setPeriodEndScript(tag.getString(TAG_END_SCRIPT));
    setMailGenScript(tag.getString(TAG_MAIL_SCRIPT));
    setTagRenderScript(tag.getString(TAG_RENDER_SCRIPT));

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
   * This will get a random mail message, or the first message in the list if the size is 0, and
   * then use {@link HolidayTags} to format the message to the correct from given the current
   * context.
   *
   * @param time   The date time to use for formatting the result.
   * @param random The random used to pick the random mail message.
   * @param user   The user the message is targeted towards.
   * @return The found and formatted mail message, or null if the messages list is empty.
   */
  public Component getMailMessage(ZonedDateTime time, Random random, User user) {
    Component c;

    if (!Strings.isNullOrEmpty(mailGenScript)) {
      if (mailGen == null) {
        mailGen = Script.of(mailGenScript).compile();
      }

      mailGen.put("holiday", this);
      mailGen.put("time", time);
      mailGen.put("random", random);
      mailGen.put("user", user);

      var result = mailGen.eval().result();

      if (result.isEmpty()) {
        return null;
      }

      c = Text.valueOf(result.get());
    } else {
      if (mails.isEmpty()) {
        return null;
      }

      // If list size 1, get entry 0
      // otherwise, let random pick an
      // entry
      if (mails.size() == 1) {
        c = mails.get(0);
      } else {
        c = mails.get(random.nextInt(mails.size()));
      }
    }

    return renderTags(c, user, time);
  }

  /**
   * Checks if this holiday has no rewards to give
   *
   * @return True, if and only if, the rhine and gem rewards are empty AND autoGiveItems is false or
   * the inventory is empty
   */
  public boolean hasNoRewards() {
    return rhines.isNone()
        && gems.isNone()
        && (!autoGiveItems || inventory.isEmpty());
  }

  /**
   * Gets the mail message attachment tag
   *
   * @param removable True, if the mail message should be removeable
   * @return The mail attachment tag
   */
  public String getAttachmentTag(boolean removable) {
    return ServerHolidays.TAG_NAMESPACE
        + (removable ? (ServerHolidays.TAG_SEPARATOR + "temp") : "")
        + ServerHolidays.TAG_SEPARATOR + getName();
  }

  public Component name() {
    return Component.text(getFilteredName());
  }

  /**
   * Gets the holiday's filtered name, this is just {@link #getName()} with all the '_' replaced
   * with spaces
   *
   * @return The filtered holiday name
   */
  public String getFilteredName() {
    return getName().replaceAll("_", " ");
  }

  public Component renderTags(Component initial,
                              User user,
                              ZonedDateTime time
  ) {
    var rendered = HolidayTags.replaceTags(initial, user, this, time);

    if (!Strings.isNullOrEmpty(tagRenderScript)) {
      if (tagRenderer == null) {
        tagRenderer = Script.of(tagRenderScript).compile();
      }

      tagRenderer.put("text", rendered);
      tagRenderer.put("user", user);
      tagRenderer.put("holiday", this);
      tagRenderer.put("time", time);

      var result = tagRenderer.eval().result();

      if (result.isPresent()) {
        rendered = Text.valueOf(result.get());
      }
    }

    return rendered;
  }

  public void closeScripts() {
    if (tagRenderer != null) {
      tagRenderer.close();
      tagRenderer = null;
    }

    if (mailGen != null) {
      mailGen.close();
      mailGen = null;
    }
  }

  public void run() {
    Validate.isTrue(!hasNoRewards(), "No rewards to give");

    if (period.isExact()) {
      if (!Strings.isNullOrEmpty(activationScript)) {
        Script.read(activationScript).close();
      }
    } else if (!Strings.isNullOrEmpty(periodStartScript)) {
      Script.read(periodStartScript).close();
    }

    UserManager.get().getAllUsers().whenComplete((users, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Couldn't get all users", throwable);
        return;
      }

      LOGGER.info("Giving all {} rewards", getName());

      // Generate gem and rhine amounts here
      // so all players get the same amount
      final int rhines = getRhines().get(Util.RANDOM);
      final int gems = getGems().get(Util.RANDOM);

      users.forEach(user -> giveRewards(user, rhines, gems));

      closeScripts();
      Users.unloadOffline();
    });
  }

  public void giveRewards(User user) {
    giveRewards(
        user,
        getRhines().get(Util.RANDOM),
        getGems().get(Util.RANDOM)
    );
  }

  public void giveRewards(User user, int rhines, int gems) {
    Validate.isTrue(!hasNoRewards(), "No rewards to give");

    TextComponent.Builder builder = Component.text()
        .color(NamedTextColor.YELLOW);

    ZonedDateTime time = ZonedDateTime.now();
    Component mailMsg = getMailMessage(time, Util.RANDOM, user);

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
    if (autoGiveItems
        && !inventory.isEmpty()
    ) {
      item = getRewardItem(user, time);
    }

    // Create the mail attachment
    MailAttachment attachment = new MailAttachment();
    attachment.setItem(item);
    attachment.setGems(gems);
    attachment.setRhines(rhines);
    attachment.setScript(mailScript);
    attachment.setTag(getAttachmentTag(!period.isExact()));

    // Create the mail message
    MailMessage message = MailMessage.of(builder.build());
    message.setAttachment(attachment);

    // Just add the mail
    user.getMail().add(message);
  }


  /**
   * Gets the reward item of a given holiday for the given user
   *
   * @param user The user to make the item for, it will not be given just used as a context
   *             object
   * @return The created item, null, if holiday has no items to give
   */
  public ItemStack getRewardItem(User user, ZonedDateTime time) {
    // Nothing to give? Return null B)
    if (inventory.isEmpty()) {
      return null;
    }

    ItemStack item = getContainer().createBaseItem();
    BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
    Container chest = (Container) meta.getBlockState();
    Inventory inv = chest.getInventory();

    var it = ItemStacks.nonEmptyIterator(inventory);

    while (it.hasNext()) {
      int i = it.nextIndex();
      var instItem = it.next();

      instItem = instItem.clone();
      ItemMeta meta1 = instItem.getItemMeta();

      // Format item metadata
      container.formatExistingInfo(time, this, meta1, user);

      instItem.setItemMeta(meta1);
      inv.setItem(i, instItem);
    }

    // Format the container's metadata
    container.apply(time, this, meta, user);

    // Set the stuff
    meta.setBlockState(chest);
    item.setItemMeta(meta);

    // Return the stuff
    return item.clone();
  }

  public void deactivate() {
    if (!Strings.isNullOrEmpty(periodEndScript)) {
      Script.read(periodEndScript).close();
    }

    UserManager.get().getAllUsers().whenComplete((users, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Couldn't get all users", throwable);
        return;
      }

      LOGGER.info("Removing all {} unclaimed mail from users", getName());
      String tag = getAttachmentTag(true);

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
}
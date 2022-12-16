package net.forthecrown.core.holidays;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

/**
 * Holds data about the display name, lore and type
 * a holiday's reward items should be given in
 */
@Getter
@Setter
public class RewardContainer {
    public static final String
            TAG_NAME = "name",
            TAG_TYPE = "is_chest",
            TAG_LORE = "lore";

    public static final Component
            DEF_NAME = text("%year %name Rewards", NamedTextColor.GOLD),
            DEF_LORE = text("A special %type full of goodies!", NamedTextColor.YELLOW);

    /**
     * True, if the items should be given in a chest, false for a shulker
     */
    private boolean chest;

    /**
     * The container's unformatted name
     */
    private Component name;

    /**
     * The container's unformatted lore
     */
    private List<Component> lore = new ObjectArrayList<>();

    /**
     * Loads a container from the given tag
     *
     * @param t The tag to load from
     * @return The created container
     */
    public static RewardContainer load(Tag t) {
        CompoundTag tag = (CompoundTag) t;

        RewardContainer container = new RewardContainer();
        container.setChest(tag.getBoolean(TAG_TYPE));
        container.setName(TagUtil.readText(tag.get(TAG_NAME)));

        if (tag.contains(TAG_LORE)) {
            ListTag loreT = tag.getList(TAG_LORE, Tag.TAG_STRING);
            for (Tag lTag : loreT) {
                container.lore.add(TagUtil.readText(lTag));
            }
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
        tag.putBoolean(TAG_TYPE, chest);
        tag.put(TAG_NAME, TagUtil.writeText(name));

        if (!lore.isEmpty()) {
            ListTag loreT = new ListTag();
            for (var text : lore) {
                loreT.add(TagUtil.writeText(text));
            }
            tag.put(TAG_LORE, loreT);
        }

        return tag;
    }

    /**
     * Formats the lore and display name of an existing item meta,
     * unlike {@link #apply(ZonedDateTime, Holiday, ItemMeta, User)} this
     * will not override any existing display name or lore
     *
     * @param time      The current time
     * @param holiday   The holiday this container belongs to
     * @param meta      The meta to format
     * @param recipient The recipient of the reward
     */
    public void formatExistingInfo(ZonedDateTime time, Holiday holiday, ItemMeta meta, User recipient) {
        if (meta.hasDisplayName()) {
            Component display = meta.displayName();
            meta.displayName(holiday.renderTags(display, recipient, time));
        }

        List<Component> lore = meta.lore();

        if (lore != null && !lore.isEmpty()) {
            lore = lore.stream()
                    .map(component -> holiday.renderTags(component, recipient, time))
                    .collect(Collectors.toList());

            meta.lore(lore);
        }
    }

    /**
     * Applies the container's formatting to the given meta. Sets the display
     * name to a formatted {@link #getName()} and formats the lore
     *
     * @param time      The current time
     * @param holiday   The holiday this container belongs to
     * @param meta      The meta to format
     * @param recipient The recipient of the reward
     */
    public void apply(ZonedDateTime time, Holiday holiday, ItemMeta meta, User recipient) {
        meta.displayName(
                holiday.renderTags(name, recipient, time)
        );

        if (!lore.isEmpty()) {
            List<Component> fLore = lore.stream()
                    .map(s -> holiday.renderTags(s, recipient, time))
                    .collect(Collectors.toList());

            meta.lore(fLore);
        }
    }

    /**
     * Creates the base shulker or chest item for this container
     *
     * @return The created item
     */
    public ItemStack createBaseItem() {
        return new ItemStack(chest ? Material.CHEST : Material.SHULKER_BOX, 1);
    }
}
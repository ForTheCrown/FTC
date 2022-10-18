package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * A list of usage type instances
 * @param <V> The type of the instances
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UsageTypeList<V extends UsageInstance> implements Iterable<V> {
    public static final String
            TAG_TYPE = "type",
            TAG_VALUE = "value";

    private static final Logger LOGGER = Crown.logger();

    @Getter
    private final Registry<UsageType<? extends V>> registry;
    private final List<V> instances = new ObjectArrayList<>();

    public static UsageTypeList<UsageAction> newActionList() {
        return new UsageTypeList<>(Registries.USAGE_ACTIONS);
    }

    public static UsageTypeList<UsageTest> newTestList() {
        return new UsageTypeList<>(Registries.USAGE_CHECKS);
    }

    /**
     * Adds a usage type instance
     * @param add The usage type instance to add
     * @return True, if the list changed as a result of this method call, always true
     */
    public boolean add(V add) {
        return instances.add(add);
    }

    /**
     * Adds the given entry into the list
     * at the given index
     * @param index The index to insert at
     * @param add The element to add
     */
    public void add(int index, V add) {
        instances.add(index, add);
    }

    /**
     * Removes the usage type instance at the given index
     * @param index The index to remove the usage type instance at
     * @return The removed usage type instance
     */
    public V remove(int index) {
        return instances.remove(index);
    }

    /**
     * Clears this list
     */
    public void clear() {
        instances.clear();
    }

    /**
     * Gets the size of this list
     * @return The list's size
     */
    public int size() {
        return instances.size();
    }

    /**
     * Checks if this list is empty
     * @return True, if {@link #size()} <= 0
     */
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Removes all usage type instances with
     * the given type key
     * @param key The type's key
     * @return True, if any usage type instances were removed, false otherwise
     */
    public boolean removeType(UsageType<? extends V> key) {
        return instances.removeIf(v -> v.getType().equals(key));
    }

    /**
     * Gets the instance at the given index
     * @param index The index to get from
     * @return The gotten index
     */
    public V get(int index) {
        return instances.get(index);
    }

    public void write(TextWriter writer) {
        writer.write("{");

        if (!isEmpty()) {
            writer.newLine();

            var vWriter = writer.withIndent();

            for (int i = 0; i < size(); i++) {
                vWriter.write((i + 1) + ") ", NamedTextColor.GRAY);
                vWriter.write(listInfo(i));
                vWriter.newLine();
            }
        }

        writer.write("}");
    }

    /**
     * Gets the "type: list_info" display text for
     * an entry with the given index.
     * @param index The index of the entry to get the text of
     * @return The text
     */
    public Component listInfo(int index) {
        var entry = get(index);

        return Text.format("&e{0}&r: {1}",
                registry.getKey(entry.getType()).orElse("UNKNOWN"),
                entry.displayInfo()
        );
    }

    public List<V> subList(int startInclusive, int endExclusive) {
        return instances.subList(startInclusive, endExclusive);
    }

    public Tag save() {
        ListTag result = new ListTag();

        for (var v: instances) {
            Optional<String> key = registry.getKey(v.getType());

            if (key.isEmpty()) {
                LOGGER.warn("Unknown type, could not serialize: '{}'", v.getClass().getName());
                continue;
            }

            CompoundTag instTag = new CompoundTag();
            instTag.putString(TAG_TYPE, key.get());

            var saved = v.save();

            if (saved != null) {
                instTag.put(TAG_VALUE, saved);
            }

            result.add(instTag);
        }

        return result;
    }

    public void load(Tag t) throws CommandSyntaxException {
        ListTag lTag = (ListTag) t;

        for (var tL: lTag) {
            CompoundTag tag = (CompoundTag) tL;
            String key = tag.getString(TAG_TYPE);

            var typeOptional = registry.get(key);

            if (typeOptional.isEmpty()) {
                LOGGER.warn("Unknown type found, cannot deserialize: '{}'", key);
                continue;
            }

            UsageType<V> type = (UsageType<V>) typeOptional.get();
            var read = type.load(tag.get(TAG_VALUE));

            add(read);
        }
    }

    @NotNull
    @Override
    public ListIterator<V> iterator() {
        return instances.listIterator();
    }
}
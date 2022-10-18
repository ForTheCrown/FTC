package net.forthecrown.utils;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.math.Vectors;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * General Utility functions as well as some useful variables, like variables for the two main worlds, world and world_void and the server's time zone lol
 */
public final class Util {
    private Util() {}

    //The max and min Y level constants
    public static final int
            MAX_Y   = 312,
            MIN_Y   = -64,
            Y_SIZE  = MAX_Y - MIN_Y;

    public static final Random RANDOM = new Random();

    public static long worldTimeToYears(World world) {
        return ((world.getFullTime() / 1000) / 24) / 365;
    }

    //True if the string is null or contains only blank spaces
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String conditionalPlural(long amount) {
        return amount == 1 ? "" : "s";
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<T> initializer) {
        initializer.accept(object);
        return object;
    }

    public static CraftPlayerProfile profileWithTexture(@Nullable String name, @Nullable UUID id, String textureID) {
        var textureLink = "http://textures.minecraft.net/texture/" + textureID;
        CraftPlayerProfile profile = new CraftPlayerProfile(id, name);

        profile.setProperty(
                new ProfileProperty(
                        "textures",
                        Base64.getEncoder().encodeToString(
                                ("{\"textures\":{\"SKIN\":{\"url\":\"" + textureLink + "\"}}}").getBytes()
                        )
                )
        );

        return profile;
    }

    public static boolean isClearAbove(Location location) {
        return isClearAbove0(Vectors.fromI(location), location.getWorld());
    }

    private static boolean isClearAbove0(Vector3i pos, World world) {
        for (int i = pos.y(); i < MAX_Y; i++) {
            pos = pos.withY(i);
            var block = Vectors.getBlock(pos, world);

            if (block.isCollidable() || block.isSolid()) {
                return false;
            }
        }

        return true;
    }

    public static void runSafe(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            Crown.logger().error("Error while running safeRunnable", e);
        }
    }

    public static Region getSelectionSafe(com.sk89q.worldedit.entity.Player wePlayer) throws CommandSyntaxException {
        Region selection;

        try {
            selection = wePlayer.getSelection();
        } catch (IncompleteRegionException e) {
            throw Exceptions.NO_REGION_SELECTION;
        }

        return selection;
    }

    public static <F, T> List<T> fromIterable(Iterable<F> from, Function<F, T> converter){
        List<T> convert = new ArrayList<>();

        for (F f: from) {
            convert.add(converter.apply(f));
        }

        return convert;
    }

    public static boolean isNullOrEmpty(@Nullable Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    public static void giveOrDropItem(Inventory inv, Location loc, ItemStack item) {
        if(inv.firstEmpty() == -1) {
            loc.getWorld().dropItem(loc, item);
        } else {
            inv.addItem(item);
        }
    }

    public static void clearModifiers(AttributeInstance instance) {
        for (AttributeModifier m: instance.getModifiers()) {
            instance.removeModifier(m);
        }
    }

    public static <T> Set<T> pickUniqueEntries(List<T> list, Random random, int amount) {
        Validate.isTrue(amount <= list.size());

        if (amount == list.size()) {
            return new ObjectOpenHashSet<>(amount);
        }

        Set<T> result = new ObjectOpenHashSet<>();
        T entry;

        while (result.size() < amount) {
            entry = list.get(random.nextInt(list.size()));
            result.add(entry);
        }

        return result;
    }
}
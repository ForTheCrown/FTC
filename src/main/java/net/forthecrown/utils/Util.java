package net.forthecrown.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
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

    public static PlayerProfile profileWithTexture(@Nullable String name,
                                                   @Nullable UUID id,
                                                   String textureID
    ) {
        var textureLink = "http://textures.minecraft.net/texture/" + textureID;
        PlayerProfile profile = Bukkit.getServer().createProfile(id, name);

        var textures = profile.getTextures();

        try {
            textures.setSkin(new URL(textureLink));
        } catch (MalformedURLException exc) {
            FTC.getLogger().error("Couldn't set textures of profile", exc);
        }

        profile.setTextures(textures);
        return profile;
    }

    public static boolean isClearAbove(Location location) {
        Vector3i pos = Vectors.intFrom(location);

        for (int i = pos.y(); i <= MAX_Y; i++) {
            pos = pos.withY(i);
            var block = Vectors.getBlock(pos, location.getWorld());

            if (block.isCollidable()) {
                return false;
            }
        }

        return true;
    }

    public static Region getSelectionSafe(com.sk89q.worldedit.entity.Player wePlayer)
            throws CommandSyntaxException
    {
        Region selection;

        try {
            selection = wePlayer.getSelection();
        } catch (IncompleteRegionException e) {
            throw Exceptions.NO_REGION_SELECTION;
        }

        return selection;
    }

    public static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static void giveOrDropItem(Inventory inv,
                                      Location loc,
                                      ItemStack item
    ) {
        if (inv.firstEmpty() == -1) {
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

    public static <T> Set<T> pickUniqueEntries(List<T> list,
                                               Random random,
                                               int amount
    ) {
        Validate.isTrue(amount <= list.size());

        if (amount == 0) {
            return Collections.emptySet();
        }

        if (amount == list.size()) {
            return new ObjectOpenHashSet<>(list);
        }

        Set<T> result = new ObjectOpenHashSet<>();
        T entry;

        while (result.size() < amount) {
            entry = list.get(random.nextInt(list.size()));
            result.add(entry);
        }

        return result;
    }

    public static int getDataVersion() {
        return SharedConstants.getCurrentVersion()
                .getDataVersion()
                .getVersion();
    }

    public static boolean isPluginEnabled(String s) {
        return Bukkit.getPluginManager().isPluginEnabled(s);
    }

    /**
     * Performs the given input as a command executed by the
     * server's console
     * @param format The command format
     * @param args Any arguments to give to the format
     */
    @FormatMethod
    public static void consoleCommand(@FormatString String format, Object... args) {
        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                format.formatted(args)
        );
    }

    /**
     * Creates a new illegal argument exception
     * @param format The message format
     * @param args The arguments to give to the message
     * @return The created message
     */
    public static @FormatMethod IllegalArgumentException newException(@FormatString String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    public static Set<Method> getAllMethods(Class c) {
        Set<Method> methods = new ObjectOpenHashSet<>();
        Class<?> type = c;

        while (type != null) {
            methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            type = type.getSuperclass();
        }

        return methods;
    }
}
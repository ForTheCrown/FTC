package net.forthecrown.core.script2;

import jdk.dynalink.beans.StaticClass;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.FtcLogger;
import net.forthecrown.core.Messages;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@UtilityClass
class ScriptsBuiltIn {
    private final Class[] DEFAULT_CLASSES = {
            Util.class,             FTC.class,
            Vector3i.class,         Vector3d.class,
            Vector2i.class,         Vector2d.class,
            WorldBounds3i.class,    Bounds3i.class,
            WorldVec3i.class,       Cooldown.class,
            Bukkit.class,           Material.class,
            EntityType.class,       Vectors.class,
            Location.class,         Component.class,
            Text.class,             NamedTextColor.class,
            Messages.class,         Style.class,
            Users.class,            HoverEvent.class,
            TextDecoration.class,   ClickEvent.class,
            TextColor.class
    };

    void populate(String name, NashornScriptEngine engine) {
        for (var c: DEFAULT_CLASSES) {
            engine.put(c.getSimpleName(), StaticClass.forClass(c));
        }

        FtcLogger logger = new FtcLogger(
                LogManager.getContext()
                        .getLogger(name)
        );

        engine.put("logger", logger);
    }
}
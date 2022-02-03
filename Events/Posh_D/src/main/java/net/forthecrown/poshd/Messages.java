package net.forthecrown.poshd;

import net.forthecrown.crown.EventTimer;
import net.forthecrown.crown.TimerMessageFormatter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;

public class Messages {
    public static final String FILE_NAME = "messages.properties";
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Key KEY = Key.key("posh_d", "messages");
    private static final TranslationRegistry TRANSLATOR = TranslationRegistry.create(KEY);

    public static void load(Plugin plugin) {
        try {
            File f = new File(plugin.getDataFolder(), FILE_NAME);
            Reader reader;

            if(f.exists()) {
                reader = new FileReader(f, CHARSET);
            } else {
                plugin.saveResource(FILE_NAME, false);

                reader = new InputStreamReader(
                        Objects.requireNonNull(plugin.getResource(FILE_NAME)),
                        CHARSET
                );
            }

            PropertyResourceBundle bundle = new PropertyResourceBundle(reader);

            TRANSLATOR.registerAll(Locale.ENGLISH, bundle, true);
            plugin.getLogger().info("Loaded messages");
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlobalTranslator.get().addSource(TRANSLATOR);
    }

    public static TranslatableComponent timerRecord(EventTimer timer) {
        return Component.translatable("timer.end.win", timer.getFormattedMessage());
    }

    public static TranslatableComponent timerNotNew(EventTimer timer) {
        return Component.translatable("timer.end.lose", timer.getFormattedMessage());
    }

    public static TranslatableComponent timerStart() {
        return Component.translatable("timer.start");
    }

    public static TranslatableComponent invNotEmpty() {
        return Component.translatable("error.itemsInInv");
    }

    public static TimerMessageFormatter timerFormatter() {
        return (timer, millis) -> Component.translatable("timer.format", Component.text(timer));
    }

    public static TranslatableComponent leftEvent() {
        return Component.translatable("leftEvent");
    }
}

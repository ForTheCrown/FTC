package net.forthecrown.core.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Checks and manages banned words in user
 * messages and inputs.
 */
public final class BannedWords {
    private BannedWords() {}

    private static final ObjectList<String> BANNED_WORDS = new ObjectArrayList<>();
    private static final String COOLDOWN_CATEGORY = "banned_words";
    private static final int COOLDOWN_TIME = 3 * 60 * 20;

    public static void load() {
        InputStream stream = getFileOrResource("banned_words.json");
        JsonElement element = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        JsonArray array = element.getAsJsonArray();

        BANNED_WORDS.clear();

        for (JsonElement e: array) {
            BANNED_WORDS.add(e.getAsString().toLowerCase());
        }
    }

    public static boolean contains(String unfiltered) {
        return contains(Text.renderString(unfiltered));
    }

    public static boolean contains(Component component) {
        return containsBannedWords(Text.plain(component));
    }

    private static boolean containsBannedWords(String input) {
        String filteredInput = input.toLowerCase();
        filteredInput = StringUtils.replaceChars(filteredInput, "АВЕЅZІКМНОРСТХШѴУ", "ABESZIKMHOPCTXWVY");

        for (String s: BANNED_WORDS) {
            if (filteredInput.contains(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkAndWarn(CommandSender sender, Component component) {
        return checkAndWarn(sender, Text.plain(component));
    }

    public static boolean checkAndWarn(CommandSender sender, String input) {
        if (sender == null || sender.hasPermission(Permissions.IGNORE_SWEARS)) {
            return false;
        }

        boolean result = contains(input);

        if (result) {
            if (!Cooldown.containsOrAdd(sender, COOLDOWN_CATEGORY, COOLDOWN_TIME)) {
                sender.sendMessage(Messages.BAD_LANGUAGE);
            }
        }

        return result;
    }

    private static InputStream getFileOrResource(String path) {
        var file = PathUtil.pluginPath(path);

        if (Files.exists(file)) {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return Crown.plugin().getResource(path);
    }
}
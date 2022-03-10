package net.forthecrown.core.chat;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.Permissions;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BannedWords {
    private BannedWords() {}

    private static final ObjectList<String> BANNED_WORDS = new ObjectArrayList<>();
    private static final String COOLDOWN_CATEGORY = "banned_words";
    private static final int COOLDOWN_TIME = 3 * 60 * 20;

    public static void loadFromResource() {
        InputStream stream = FtcUtils.getFileOrResource("banned_words.json");
        JsonElement element = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        JsonArray array = element.getAsJsonArray();

        BANNED_WORDS.clear();
        for (JsonElement e: array) {
            BANNED_WORDS.add(e.getAsString().toLowerCase());
        }
    }

    public static void loadFromGP() {
        File file = new File("plugins/GriefPreventionData/bannedWords.txt");
        Validate.isTrue(file.exists(), "bannedWords.txt does not exist");

        try {
            List<String> words = Files.readLines(file, Charsets.UTF_8);
            BANNED_WORDS.clear();

            for (String s: words) {
                if(FtcUtils.isNullOrBlank(s)) continue;

                BANNED_WORDS.add(s.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean contains(String unfiltered) {
        return containsBannedWords(ChatColor.stripColor(FtcFormatter.formatColorCodes(unfiltered)));
    }

    public static boolean contains(Component component) {
        return containsBannedWords(PlainTextComponentSerializer.plainText().serialize(component));
    }

    private static boolean containsBannedWords(String input) {
        String inputLowerCase = input.toLowerCase();

        for (String s: BANNED_WORDS) {
            if(inputLowerCase.contains(s)) return true;
        }

        return false;
    }

    public static boolean checkAndWarn(CommandSender sender, Component component) {
        return checkAndWarn(sender, PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static boolean checkAndWarn(CommandSender sender, String input) {
        if(sender == null || sender.hasPermission(Permissions.IGNORE_SWEARS)) return false;

        boolean result = contains(input);

        if(result) {
            if(!Cooldown.containsOrAdd(sender, COOLDOWN_CATEGORY, COOLDOWN_TIME)) {
                sender.sendMessage(
                        Component.translatable("server.inappropriateLanguage", NamedTextColor.RED)
                );
            }
        }

        return result;
    }
}

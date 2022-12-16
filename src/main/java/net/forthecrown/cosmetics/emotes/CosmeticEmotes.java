package net.forthecrown.cosmetics.emotes;

import net.forthecrown.commands.emotes.*;
import net.forthecrown.core.Permissions;
import net.kyori.adventure.text.Component;

public final class CosmeticEmotes {
    private CosmeticEmotes() {}

    public static final CosmeticEmote
            BONK = new CosmeticEmote(12, new EmoteBonk(), "bonk",
                    "Bonk."
            ),

            SMOOCH = new CosmeticEmote(13, new EmoteSmooch(), "smooch",
                    "Shower your friends with love."
            ),

            POKE = new CosmeticEmote(14, new EmotePoke(), "poke",
                    "Poke someone and make 'em jump back a bit."
            ),

            SCARE = new CosmeticEmote(21, new EmoteScare(), "scare",
                    Permissions.EMOTE_SCARE,
                    Component.text("Can be earned around Halloween."),
                    Component.text("Scares someone")
            ),

            JINGLE = new CosmeticEmote(22, new EmoteJingle(), "jingle",
                    Permissions.EMOTE_JINGLE,
                    Component.text("Can be earned around Christmas."),
                    Component.text("Plays a christmas tune")
            ),

            HUG = new CosmeticEmote(23, new EmoteHug(), "hug",
                    Permissions.EMOTE_HUG,
                    Component.text("Can be earned around Valentine's Day."),
                    Component.text("Hugs someone :D")
            );
}
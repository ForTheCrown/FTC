package net.forthecrown.cosmetics.emotes;

import net.forthecrown.commands.emotes.*;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public final class CosmeticEmotes {
    private CosmeticEmotes() {}

    private static BuiltInventory INVENTORY;

    public static final CosmeticEmote
            BONK        = register(new CosmeticEmote(12, new EmoteBonk(), "bonk", "Bonk.")),
            SMOOCH      = register(new CosmeticEmote(13, new EmoteSmooch(), "smooch", "Shower your friends with love.")),
            POKE        = register(new CosmeticEmote(14, new EmotePoke(), "poke", "Poke someone and make 'em jump back a bit.")),

            SCARE       = register(new CosmeticEmote(21, new EmoteScare(), "scare", Permissions.EMOTE_SCARE,
                    Component.text("Can be earned around Halloween."),
                    Component.text("Scares someone")
            )),

            JINGLE      = register(new CosmeticEmote(22, new EmoteJingle(), "jingle", Permissions.EMOTE_JINGLE,
                    Component.text("Can be earned around Christmas."),
                    Component.text("Plays a christmas tune")
            )),

            HUG         = register(new CosmeticEmote(23, new EmoteHug(), "hug", Permissions.EMOTE_HUG,
                    Component.text("Can be earned around Valentine's Day."),
                    Component.text("Hugs someone :D")
            ));

    public static void init(){
        INVENTORY = CosmeticConstants.baseInventory(36, Component.text("Emotes"), true)
                .addAll(Registries.EMOTES)
                .add(CosmeticConstants.EMOTE_TOGGLE)
                .build();

        Registries.EMOTES.close();
        Crown.logger().info("Emotes registered");
    }

    private static CosmeticEmote register(CosmeticEmote emote){
        return Registries.EMOTES.register(emote.key(), emote);
    }

    public static BuiltInventory getInventory(){
        return INVENTORY;
    }
}

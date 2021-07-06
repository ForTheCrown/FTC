package net.forthecrown.cosmetics.emotes;

import net.forthecrown.commands.emotes.*;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public class Emotes {
    private static BuiltInventory INVENTORY;

    public static Emote BONK;
    public static Emote SMOOCH;
    public static Emote POKE;

    public static Emote SCARE;
    public static Emote JINGLE;
    public static Emote HUG;

    public static void init(){
        BONK = register(new Emote(12, new EmoteBonk(), "bonk", "Bonk."));
        SMOOCH = register(new Emote(13, new EmoteSmooch(), "smooch", "Shower your friends with love."));
        POKE = register(new Emote(14, new EmotePoke(), "poke", "Poke someone and make 'em jump back a bit."));

        SCARE = register(new Emote(21, new EmoteScare(), "scare", null,
                Component.text("Can be earned around Halloween."),
                Component.text("Scares someone")
        ));

        JINGLE = register(new Emote(22, new EmoteJingle(), "jingle", null,
                Component.text("Can be earned around Christmas."),
                Component.text("Plays a christmas tune")
        ));

        HUG = register(new Emote(23, new EmoteHug(), "hug", Permissions.EMOTE_HUG,
                Component.text("Can bea earned around Valentine's Day."),
                Component.text("Hugs someone :D")
        ));

        INVENTORY = CosmeticConstants.baseInventory(36, Component.text("Emotes"), true)
                .addOptions(Registries.EMOTES)
                .addOption(CosmeticConstants.EMOTE_TOGGLE)
                .build();

        Registries.EMOTES.close();
        CrownCore.logger().info("Emotes registered");
    }

    private static Emote register(Emote emote){
        return Registries.EMOTES.register(emote.key(), emote);
    }

    public static BuiltInventory getInventory(){
        return INVENTORY;
    }
}

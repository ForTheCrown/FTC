package net.forthecrown.cosmetics.emotes;

import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.MenuNodeFactory;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.Properties;
import net.forthecrown.user.UserProperty;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class Emotes {
  private Emotes() {}

  public static UserProperty<Boolean> EMOTES_ENABLED = Properties.booleanProperty()
      .key("emotes")
      .defaultValue(true)
      .build();

  public static CosmeticType<Emote> TYPE = CosmeticType.<Emote>builder()
      .displayName(Component.text("Emotes"))
      .predicate((user, cosmetic) -> cosmetic.getValue().test(user.getCommandSource()))
      .factory(createFactory())
      .build();

  public static final Cosmetic<Emote>
      BONK = create(12, new EmoteBonk(), "bonk", "Bonk.");

  public static final Cosmetic<Emote>
      SMOOCH = create(13, new EmoteSmooch(), "smooch", "Shower your friends with love.");

  public static final Cosmetic<Emote>
      POKE = create(14, new EmotePoke(), "poke", "Poke someone and make 'em jump back a bit.");

  public static final Cosmetic<Emote>
      SCARE = create(21, new EmoteScare(), "scare", "Can be earned around Halloween.", "Scares someone");

  public static final Cosmetic<Emote>
      JINGLE = create(22, new EmoteJingle(), "jingle", "Can be earned around Christmas.", "Plays a christmas tune");

  public static final Cosmetic<Emote>
      HUG = create(23, new EmoteHug(), "hug", "Can be earned around Valentine's Day.", "Hugs someone :D");

  private static Cosmetic<Emote> create(int slot, Emote emote, String name, String... desc) {
    return Cosmetic.create(emote, slot, name, desc);
  }

  private static MenuNodeFactory<Emote> createFactory() {
    return cosmetic -> {
      return MenuNode.builder()
          .setItem(user -> {
            boolean owned = cosmetic.test(user);
            Material material = Cosmetics.getCosmeticMaterial(owned);

            return ItemStacks.builder(material)
                .setName(cosmetic.getDisplayName())
                .addLore(cosmetic.getDescription())
                .build();
          })

          .build();
    };
  }

  public static void registerAll(Registry<Cosmetic<Emote>> r) {
    r.register("bonk", BONK);
    r.register("smooch", SMOOCH);
    r.register("poke", POKE);
    r.register("scare", SCARE);
    r.register("jingle", JINGLE);
    r.register("hug", HUG);
  }
}
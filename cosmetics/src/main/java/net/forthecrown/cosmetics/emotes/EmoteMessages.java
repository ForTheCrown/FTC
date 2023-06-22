package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public interface EmoteMessages {

  /**
   * Red ❤
   */
  TextComponent HEART = text("❤", NamedTextColor.DARK_RED);

  // ---------------------------
  // --- SECTION: CMD EMOTES ---
  // ---------------------------

  // --- BONK ---

  Component EMOTE_BONK_COOLDOWN = text("Calm down there buckaroo, don't go 'round bonkin so much");

  Component EMOTE_BONK_SELF = format("Don't hurt yourself {0}", HEART);

  static Component bonkSender(User target) {
    return format("You bonked &e{0, user}&r!", target);
  }

  static Component bonkTarget(User sender) {
    return format("{0, user} bonked you!", sender)
        .hoverEvent(text("Bonk them back!"))
        .clickEvent(runCommand("/bonk " + sender.getName()));
  }

  // --- HUG ---

  Component EMOTE_HUG_COOLDOWN = format("{0} You're too nice of a person! {0}", HEART);

  Component EMOTE_HUG_SELF = format(
      "It's alright to love yourself {0}" +
          "\nWe've all got to love ourselves {0}",
      HEART
  );

  static Component hugReceived(User target) {
    return format("&e{0, user}&e has already received some love lol", target);
  }

  static Component hugSender(User target) {
    return format("{0} You hugged &e{1, user}&r {0}",
        HEART, target
    );
  }

  static Component hugTarget(User sender, boolean sendBack) {
    var initial = format("{0} &e{1, user}&r hugged you! {0}", HEART, sender);

    if (sendBack) {
      initial = initial
          .hoverEvent(text("Click to hug them back!"))
          .clickEvent(runCommand("/hug " + sender.getName()));
    }

    return initial;
  }

  // --- JINGLE ---

  Component EMOTE_JINGLE_COOLDOWN = text("You jingle people too often lol");

  static Component jingleSender(User target) {
    return format("You sent &e{0, user}&r a sick Christmas beat!", target);
  }

  static Component jingleTarget(User sender, boolean sendBack) {
    var initial = format("&e{0, user}&r sent you a sick Christmas beat!", sender);

    if (sendBack) {
      initial = initial
          .hoverEvent(text("Click to send some jingles back!"))
          .clickEvent(runCommand("/jingle " + sender.getName()));
    }

    return initial;
  }

  // --- POKE ---

  Component[] EMOTE_POKE_PARTS = {
      text("Stomach"),
      text("Back"),
      text("Arm"),
      text("Butt"),
      text("Cheek"),
      text("Neck"),
      text("Belly")
  };

  Component EMOTE_POKE_SELF = text("You poked yourself... weirdo");

  Component EMOTE_POKE_COOLDOWN = text("You poke people too often lol");

  static Component pokeSender(User target, Component bodyPart) {
    return format("You poked &e{0, user}&r's {1}.",
        target, bodyPart
    );
  }

  static Component pokeTarget(User sender, Component bodyPart) {
    return format("&e{0, user}&r poke your {1}.",
        sender, bodyPart
    );
  }

  // --- SCARE ---

  Component EMOTE_SCARE_COOLDOWN = text("D: Too scary! Take a lil break!");

  static Component scareSender(User target) {
    return format("You scared &e{0, user}&r!", target);
  }

  static Component scareTarget(User sender, boolean scareBack) {
    var initial = format("&e{0, user}&r scared you!", sender);

    if (scareBack) {
      initial = initial
          .hoverEvent(text("Click to scare them back! >:)"))
          .clickEvent(runCommand("/scare " + sender.getName()));
    }

    return initial;
  }

  // --- SMOOCH ---

  Component EMOTE_SMOOCH_SELF = format("&eLove yourself!&r ( ^ 3^) ❤")
      .hoverEvent(text("You're amazing! ʕっ•ᴥ•ʔっ"));

  Component EMOTE_SMOOCH_COOLDOWN = text("You kiss too much lol");

  static Component smoochSender(User target) {
    return format("{0} You smooched &e{1, user}&r! {0}",
        HEART, target
    );
  }

  static Component smoochTarget(User sender) {
    return format("{0} &e{1, user}&r smooched you! {0}",
        HEART, sender
    )
        .hoverEvent(text("Click to smooch back!"))
        .clickEvent(runCommand("/smooch " + sender.getName()));
  }
}
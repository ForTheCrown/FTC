package net.forthecrown.dungeons.boss.evoker;

import com.google.common.collect.ImmutableMap;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.text;

@FunctionalInterface
public interface BossMessage {
    ImmutableMap<String, Component> MESSAGES = ImmutableMap.<String, Component>builder()
            // Summoning
            .put("summon_awaken",               text("Who dares summon me!"))
            .put("summon_insult_single",        text("A lone traveller? This will be easy!"))
            .put("summon_insult_party",         text("What fools you are, this room will be your grave!"))
            .put("summon_shield",               text("The ones before were weak, I know your tactics."))
            .put("summon_shield_secondline",    text("I prepared a shield that no mortal weapon can pierce!"))

            // Zhombie summon phase
            .put("phase_zombie_start_0",        text("Army of the undead, I summon thee!"))
            .put("phase_zombie_start_1",        text("Consumers of flesh, I command thee!"))
            .put("phase_zombie_end_0",          text("Wretched fools, what are you good foor!?"))
            .put("phase_zombie_end_1",          text("You simpletons, can't you do anything!"))

            // Illager summon phase
            .put("phase_illager_start_single",  text("Go! Pillage this fool!"))
            .put("phase_illager_start_party",   text("Go! Pillage these fools!"))
            .put("phase_illager_end_single",    text("My brothers! How dare you, insolent traveller!"))
            .put("phase_illager_end_party",     text("No, my brothers! You insolent wretches!"))

            // Shulker phase
            .put("phase_shulker_start_0",       text("Go! Weird purple boxes"))
            .put("phase_shulker_start_1",       text("Kill them, living boxes!"))
            .put("phase_shulker_betrayal",      text("You traitors! Stop sabotaging my shield!"))

            // Normal attack phase
            .put("phase_normal_start_0",        text("Ugh, do I have to do everything myself!"))
            .put("phase_normal_start_1",        text("Must I destroy you with my own spells!"))
            .put("phase_normal_end_single",     text("My magic has been worn out by a single fool?"))
            .put("phase_normal_end_multi",      text("My magic has worn out :("))

            // Potion phase
            .put("phase_potion_start",          text("My mom made these potions, hope you like them :>"))

            // Ghast phase
            .put("phase_ghast_start",           text("Go, flying beasts >:D"))

            // Vulnerable phase
            .put("phase_shield_lost",           text("My shield! No, it's gone"))
            .put("phase_shield_regained",       text("Aha! My shield is back!"))

            // Death
            .put("death_start",                 text("How!?!"))
            .put("death_middle_single",         text("Beaten by a single fool, I'm a grand failure"))
            .put("death_middle_party",          text("This wasn't supposed to happen, I was meant to be different!"))
            .put("death_end",                   text("We will meet again"))

            .build();

    Component createMessage(BossContext context);

    static BossMessage random(String template, int length) {
        return context -> {
            String chosen = template + "_" + Util.RANDOM.nextInt(length);
            return MESSAGES.get(chosen);
        };
    }

    static BossMessage partySize(String base) {
        return context -> {
            String chosen = base + "_" + (context.players().size() < 2 ? "single" : "party");
            return MESSAGES.get(chosen);
        };
    }

    static BossMessage simple(String s) {
        return simple(MESSAGES.get(s));
    }

    static BossMessage simple(Component msg) {
        return context -> msg;
    }
}
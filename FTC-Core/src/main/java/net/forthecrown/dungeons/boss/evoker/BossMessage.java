package net.forthecrown.dungeons.boss.evoker;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface BossMessage {
    String PREFIX = "evoker.";

    Component createMessage(BossContext context);

    static BossMessage random(String template, int length) {
        return context -> {
            String chosen = PREFIX + template + "_" + FtcUtils.RANDOM.nextInt(length);
            return Component.translatable(chosen);
        };
    }

    static BossMessage partySize(String base) {
        return context -> {
            String chosen = PREFIX + base + "_" + (context.players().size() < 2 ? "single" : "party");
            return Component.translatable(chosen);
        };
    }

    static BossMessage simple(String s) {
        return simple(Component.translatable(PREFIX + s));
    }

    static BossMessage simple(Component msg) {
        return context -> msg;
    }
}

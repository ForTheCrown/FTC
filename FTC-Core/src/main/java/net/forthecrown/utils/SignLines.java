package net.forthecrown.utils;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;

public record SignLines(Component line0, Component line1, Component line2, Component line3) {
    public static final SignLines EMPTY = new SignLines(Component.empty(), Component.empty(), Component.empty(), Component.empty());

    public SignLines(String s1, String s2, String s3, String s4) {
        this(convert(s1), convert(s2), convert(s3), convert(s4));
    }

    public SignLines(Sign sign) {
        this(
                sign.line(0),
                sign.line(1),
                sign.line(2),
                sign.line(3)
        );
    }

    public void apply(Sign sign) {
        sign.line(0, emptyIfNull(line0));
        sign.line(1, emptyIfNull(line1));
        sign.line(2, emptyIfNull(line2));
        sign.line(3, emptyIfNull(line3));
    }

    private static Component convert(String s) {
        return FtcUtils.isNullOrBlank(s) ? null : ChatUtils.convertString(s, true);
    }

    private static Component emptyIfNull(Component component) {
        return component == null ? Component.empty() : ChatUtils.renderToSimple(component);
    }
}

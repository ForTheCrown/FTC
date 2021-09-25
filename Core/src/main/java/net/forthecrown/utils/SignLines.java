package net.forthecrown.utils;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;

public class SignLines implements Struct {
    public final Component line0;
    public final Component line1;
    public final Component line2;
    public final Component line3;

    public SignLines(String s1, String s2, String s3, String s4) {
        this(convert(s1), convert(s2), convert(s3), convert(s4));
    }

    public SignLines(Component line0, Component line1, Component line2, Component line3) {
        this.line0 = line0;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
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
        return component == null ? Component.empty() : component;
    }
}

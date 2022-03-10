package net.forthecrown.book.builder;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.Crown;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public final class TextInfo {
    public static final Font MC_FONT = Util.make(() -> {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Crown.resource("minecraft_font.ttf"));
        } catch (Exception e) {
            Crown.logger().error("Couldn't create MC font", e);
            return null;
        }
    });

    public static final FontRenderContext RENDER_CONTEXT = new FontRenderContext(MC_FONT.getTransform(), false, true);
    public static final Vec2 SIZE_MOD = Util.make(() -> {
        String example = "i";
        BlockVector2 exampleSize = BlockVector2.at(1, 7);

        TextLayout layout = new TextLayout(example, MC_FONT, RENDER_CONTEXT);
        Rectangle2D rec = layout.getBounds();

        return new Vec2((float) (exampleSize.getX() / rec.getWidth()), (float) (exampleSize.getZ() / rec.getHeight()));
    });

    public static int getPxLength(char c) {
        return switch (c) {
            case 'i', ':', '.' -> 1;
            case 'l' -> 2;
            case '*', 't', '[', ']' -> 3;
            case 'f', 'k', ' ' -> 4;
            default -> 5;
        };
    }

    public static int getPxLengthNew(char c) {
        Rectangle2D gottenSize = MC_FONT.getStringBounds("" + c, RENDER_CONTEXT);
        return (int) Math.ceil(gottenSize.getWidth() * SIZE_MOD.x);
    }

    public static int getPixLengthLegacy(String string) {
        return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
    }

    public static int getPxLength(String string) {
        if(string.length() == 0) return 0;

        TextLayout layout = new TextLayout(string.replaceAll(" ", "t"), MC_FONT, RENDER_CONTEXT);
        Rectangle2D rec = layout.getBounds();

        return (int) Math.ceil(rec.getWidth() * SIZE_MOD.x);
    }
}

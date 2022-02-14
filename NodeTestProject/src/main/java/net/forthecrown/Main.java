package net.forthecrown;

import net.forthecrown.math.Rot;
import net.forthecrown.math.Transform;
import net.forthecrown.math.Vec2i;
import net.forthecrown.structure.*;
import net.forthecrown.test.TestNodeTypes;
import net.forthecrown.test.TestStruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final int SIZE = 1024;
    private static final int HALF_SIZE = SIZE / 2;

    public static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);

        DrawThing drawThing = new DrawThing() {
            @Override
            public void draw(Vec2i point, Color color) {
                point = point.add(HALF_SIZE, HALF_SIZE);

                LOGGER.info("Drawing at {}, color {}", point, color);

                image.setRGB(point.getX(), point.getX(), color.getRGB());
            }
        };

        /*TestNodeTypes.init();
        NodeTree<TemplateNode> tree = new TreeGenerator<>(TestStruct.INSTANCE).create();

        PlaceContext.drawThing = drawThing;
        tree.gen(new PlaceContext());*/

        Template template = Templates.L_LEFT;
        template.draw(drawThing, Vec2i.ZERO, Vec2i.ZERO, Rot.D_0, Transform.DEFAULT);

        File test = new File("test.png");
        ImageIO.write(image, "png", test);
    }
}

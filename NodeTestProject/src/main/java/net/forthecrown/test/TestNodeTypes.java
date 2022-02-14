package net.forthecrown.test;

import net.forthecrown.math.Bounds2i;
import net.forthecrown.math.Rot;
import net.forthecrown.math.Vec2i;
import net.forthecrown.structure.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestNodeTypes {
    public static final TestNodeType
        END = new TestNodeType(Templates.ONE_BLOCK),
        HALLWAY = new TestNodeType(Templates.CORRIDOR, new Connector(new Vec2i(0, 3), Rot.D_0)),

        L_BLOCK = new TestNodeType(Templates.L_LEFT, new Connector(new Vec2i(0, 1), Rot.D_0)) {
            @Override
            public Vec2i entrancePos() {
                return new Vec2i(1, 0);
            }
        },

        FOUR_WAY = new TestNodeType(Templates.FOUR_WAY,
                new Connector(new Vec2i(0, 1), Rot.D_270),
                new Connector(new Vec2i(1, 2), Rot.D_0),
                new Connector(new Vec2i(2, 1), Rot.D_90)
        ) {
            @Override
            public Vec2i entrancePos() {
                return new Vec2i(1, 0);
            }
        },

        THREE_WAY = new TestNodeType(Templates.THREE_WAY,
                new Connector(new Vec2i(0, 1), Rot.D_270),
                new Connector(new Vec2i(2, 1), Rot.D_90)
        ) {
            @Override
            public Vec2i entrancePos() {
                return new Vec2i(1, 0);
            }
        };

    public static void init() {
    }

    public static class TestNodeType implements StructureNodeType<TemplateNode> {
        private final Template template;
        private final List<Connector> connectors = new ArrayList<>();

        public TestNodeType(Template template, Connector... arr) {
            this.template = template;

            if(arr == null || arr.length < 1) return;
            connectors.addAll(Arrays.asList(arr));

            TestStruct.INSTANCE.types.add(this);
        }

        @Override
        public TemplateNode create() {
            return new TemplateNode(this, template);
        }

        @Override
        public Vec2i entrancePos() {
            return Vec2i.ZERO;
        }

        @Override
        public Bounds2i createBounds(Vec2i pos, Rot rotation) {
            return new Bounds2i(pos, pos.add(template.getSize()));
        }

        @Override
        public Structure structure() {
            return TestStruct.INSTANCE;
        }

        @Override
        public List<Connector> connectors() {
            return connectors;
        }
    }
}

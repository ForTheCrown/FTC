package net.forthecrown.structure;

import net.forthecrown.math.Transform;

public class TemplateNode extends StructureNode {
    private final Template template;

    public TemplateNode(StructureNodeType type, Template template) {
        super(type);
        this.template = template;
    }

    @Override
    public boolean onPlace(PlaceContext context) {
        template.draw(context.getDrawThing(), getPivot(), context.getEffectivePlace(), getRotation(), context.getTransform());
        return true;
    }
}

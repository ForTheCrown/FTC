package net.forthecrown.structure.tree;

import net.forthecrown.registry.Registries;
import net.forthecrown.structure.*;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.nbt.CompoundTag;

public abstract class TemplateStructureNode extends StructureNode {
    private final BlockStructure structure;

    public TemplateStructureNode(StructureNodeType<? extends StructureNode> type, BlockStructure structure) {
        super(type);
        this.structure = structure;
    }

    public TemplateStructureNode(StructureNodeType<? extends StructureNode> type, CompoundTag tag) {
        super(type, tag);
        this.structure = Registries.STRUCTURES.read(tag.get("struct"));
    }

    @Override
    public boolean onPlace(NodePlaceContext context) {
        Vector3i add = Vector3i.ZERO;
        if(rotation == PlaceRotation.D_180) {
            add = new Vector3i(-1, 0, getStructure().getSize().getZ() - 1);
        }

        context.addOffset(add);

        StructurePlaceContext structContext = new StructurePlaceContext(
                structure,
                context.getEffectivePlacePos(),
                BlockPlacer.world(context.getWorld())
        )
                //.setPivot(getType().createPivot())
                .placeEntities(true)
                .setRotation(rotation)
                .addEmptyEntityProcessor()
                .addEmptyProcessor()
                .addProccessor(new BlockTransformProcessor());

        configureContext(structContext);

        structure.place(structContext);

        afterStructPlace(context);
        return true;
    }

    protected void configureContext(StructurePlaceContext context) {}
    protected void afterStructPlace(NodePlaceContext context) {}

    @Override
    public void saveAdditionalData(CompoundTag tag) {
        tag.putString("struct", structure.key().asString());
    }

    public BlockStructure getStructure() {
        return structure;
    }
}

package net.forthecrown.economy.guilds.screen;

import net.forthecrown.inventory.builder.InventoryPos;

public interface InvPosProvider {
    InventoryPos getPos(int index);

    static InvPosProvider create(int maxIndex, InventoryPos start) {
        return new InvPosProvider() {
            @Override
            public InventoryPos getPos(int index) {
                if(index > maxIndex) throw new IndexOutOfBoundsException(index);

                InventoryPos pos = InventoryPos.fromSlot(index);
                return start.add(pos.getColumn(), pos.getRow());
            }
        };
    }
}

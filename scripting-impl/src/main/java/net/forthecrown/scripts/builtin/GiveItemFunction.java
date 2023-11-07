package net.forthecrown.scripts.builtin;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.scripts.ScriptUtils;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

public class GiveItemFunction implements Callable {

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    if (args.length < 2) {
      throw ScriptRuntime.typeError("2 Arguments required, player and item(s)");
    }

    InventoryHolder holder = toInventoryHolder(args, 0);

    if (holder == null) {
      throw ScriptRuntime.typeError("First argument must be inventory-holder / inventory");
    }

    Inventory inventory = holder.getInventory();

    for (int i = 1; i < args.length; i++) {
      ItemStack item = ScriptUtils.toItemStack(args, i);
      ItemStacks.giveOrDrop(inventory, item);
    }

    if (inventory instanceof BlockState state) {
      state.update(false, false);
    }

    return null;
  }

  private InventoryHolder toInventoryHolder(Object[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    CommandSource source = ScriptUtils.toSource(args, index);
    if (source != null && source.asBukkit() instanceof InventoryHolder holder) {
      return holder;
    }

    Object jType = Context.jsToJava(args[index], Object.class);

    if (jType instanceof Block block) {
      BlockState state = block.getState();

      if (state instanceof InventoryHolder holder) {
        return holder;
      }

      return null;
    }

    if (jType instanceof InventoryHolder holder) {
      return holder;
    }

    if (jType instanceof Inventory inventory) {
      if (inventory.getHolder() == null) {
        return () -> inventory;
      }
      return inventory.getHolder();
    }

    return null;
  }
}

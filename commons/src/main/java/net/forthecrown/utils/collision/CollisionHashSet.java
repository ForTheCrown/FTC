package net.forthecrown.utils.collision;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class CollisionHashSet<T>
    extends ObjectOpenHashSet<Collision<T>>
    implements CollisionSet<T>
{

  public CollisionHashSet() {
    clear();
  }

  public CollisionHashSet(int expected) {
    super(expected);
  }
}

package net.forthecrown.utils.collision;

public interface CollisionListener<S, T> {

  void onEnter(S source, T t);

  void onExit(S source, T t);

  void onMoveInside(S source, T t);
}

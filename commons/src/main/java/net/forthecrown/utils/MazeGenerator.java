package net.forthecrown.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.Stack;
import lombok.Getter;

public class MazeGenerator {

  @Getter
  private int width;

  @Getter
  private int height;

  private BitSet maze;
  private Stack<Integer> stack = new Stack();
  private int finish;

  //initializes the maze
  public MazeGenerator(int w, int h){
    width = w * 2 + 1;
    height = h * 2 + 1;
    maze = new BitSet(width * height);

    finish = (int)(Math.random() * (width / 2)) * 2 + 1;
    maze.set(width*height - 1 - finish);
    finish = width*height - 1 - finish - width;

    int start = (int)(Math.random() * (width / 2)) * 2 + 1;
    maze.set(start);
    maze.set(start + width);

    stack.push(start + width);
  }

  public boolean get(int x, int y) {
    int bitIndex = y * width + x;
    return maze.get(bitIndex);
  }

  public void generate() {
    while (!genStep()) {
      continue;
    }
  }

  //takes one step towards converting maze
  //returns true if maze is finished
  //else returns false
  public boolean genStep(){
    if (stack.empty()) {
      return true;
    }

    int m;
    int loc = stack.peek();
    IntList move = new IntArrayList(4);

    if (loc - 2 * width >= 0 && !maze.get(loc - 2*width)) {
      move.add(-1 * width);
    }
    if (loc + 2 * width < width * height && !maze.get(loc + 2*width)) {
      move.add(width);
    }
    if (loc + 2 < width * height && (loc + 2)/width == loc/width && !maze.get(loc + 2)) {
      move.add(1);
    }
    if (loc - 2 >= 0 && (loc - 2)/width == loc/width && !maze.get(loc - 2)) {
      move.add(-1);
    }

    if (!move.isEmpty()){
      m = move.getInt((int)(Math.random()*move.size()));
      maze.set(stack.push(loc + 2*m));
      maze.set(loc + m);
      return false;
    }

    stack.pop();
    return stack.empty();
  }
}

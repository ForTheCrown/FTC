package net.forthecrown.mazegen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;

public class MazeGenerator {
    private int width;
    private int height;
    private BitSet maze;
    private Stack<Integer> stack = new Stack();
    private char[] hashKey;
    private int finish;
    private int count = 0;


    public MazeGenerator(BitSet maze, int width, int height){
        this.maze = maze;
        this.width = width;
        this.height = height;
    }

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

    public BitSet getBitSet(){
        return maze;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    //takes one step towards converting maze
    //returns true if maze is finished
    //else returns false
    public boolean generate(){
        if (stack.empty()) return true;
        int m;
        int loc = stack.peek();

        ArrayList<Integer> move = new ArrayList();
        if (loc - 2 * width >= 0 && !maze.get(loc - 2*width)) move.add(-1 * width);
        if (loc + 2 * width < width * height && !maze.get(loc + 2*width)) move.add(1 * width);
        if (loc + 2 < width * height && (loc + 2)/width == loc/width && !maze.get(loc + 2)) move.add(1);
        if (loc - 2 >= 0 && (loc - 2)/width == loc/width && !maze.get(loc - 2)) move.add(-1);

        if (move.size()>0){
            m = move.get((int)(Math.random()*move.size()));
            maze.set(stack.push(loc + 2*m));
            maze.set(loc + m);
            return false;
        }
        stack.pop();
        return stack.empty();
    }

    //takes one step towards converting maze
    //returns true if maze is finished
    //else returns false
    //reverses the stack after every move
    public boolean _generate(){
        if (stack.empty()) return true;
        int m;
        int loc = stack.peek();

        ArrayList<Integer> move = new ArrayList();
        if (loc - 2 * width >= 0 && !maze.get(loc - 2*width)) move.add(-1 * width);
        if (loc + 2 * width < width * height && !maze.get(loc + 2*width)) move.add(1 * width);
        if (loc + 2 < width * height && (loc + 2)/width == loc/width && !maze.get(loc + 2)) move.add(1);
        if (loc - 2 >= 0 && (loc - 2)/width == loc/width && !maze.get(loc - 2)) move.add(-1);

        if (move.size()>0){
            count++;
            m = move.get((int)(Math.random()*move.size()));
            maze.set(stack.push(loc + 2*m));
            maze.set(loc + m);
            if (loc + 2*m == finish){
                Stack<Integer> temp = new Stack();
                while(!stack.empty())
                    temp.push(stack.pop());
                stack = temp;
            }
            if (count == 1){
                Stack<Integer> temp = new Stack();
                while(!stack.empty())
                    temp.push(stack.pop());
                stack = temp;
                count = 0;
            }
            return false;
        }

        stack.pop();
        return stack.empty();
    }


    //returns a string representation of the maze
    //1 represents path and 0 represents wall
    @Override
    public String toString(){
        String str = "";
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                if (maze.get(i * width + j)){
                    str += 1;
                }
                else str += 0;
            }
            str += "\n";
        }
        return str;
    }

    //returns a string representation of the maze
    //0 represents path and 1 represents wall
    public java.util.List<String> toStringList(){
        List<String> asd = new ArrayList<>();
        String str = "";
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                str = "";
                if (maze.get(i * width + j)){
                    str += 0;
                }
                else str += 1;
            }
            asd.add(str);
        }
        return asd;
    }

    //draws the maze to a png file
    public void draw(String name){
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        g.setColor(Color.black);

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                if (!maze.get(i * width + j)){
                    g.drawLine(j, i, j, i);
                }
            }
        }

        try {
            File outputfile = new File(name + ".png");
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {}
    }
}

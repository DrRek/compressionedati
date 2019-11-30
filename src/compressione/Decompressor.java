package compressione;

import java.util.*;
import java.io.*;

public class Decompressor {
    private Dictionary dict;
    private File ref, tar, out;
    private int c;

    public Decompressor(int c, String ref, String tar, String out) throws FileNotFoundException {
        this.dict = new Dictionary(c, ref, tar);
        this.tar = new File(tar);
        this.out = new File(out);
        this.c = c;
    }

    public boolean run(){
        return true;
    }
}

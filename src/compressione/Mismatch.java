package compressione;

import javax.sound.sampled.AudioFormat;
import java.util.*;
import java.io.*;

class Mismatch{
    private List<Byte> ref;
    private List<Byte> tar;
    private short[] delta;

    //this will represent the number of bytes -c to skip from match start to the actual missmatch location
    private int offset;

    Mismatch(List<Byte> r, List<Byte> t, int offset){
        this.ref=r;
        this.tar=t;
        this.offset = offset;
        this.delta = new short[r.size()];
        for(int i = 0; i<r.size(); i++){
            delta[i] = (short)(r.get(i) - t.get(i));
        }
    }

    Mismatch(byte[] bytes, List<Short> d, int offset){
        this.ref = new ArrayList<>();
        for(byte b : bytes){
            ref.add(b);
        }

        this.tar=new ArrayList<>();
        this.delta = new short[d.size()];
        for(int i = 0; i<d.size(); i++){
            delta[i] = d.get(i);
            tar.add((byte)(ref.get(i) - delta[i]));
        }

        this.offset = offset;
    }

    List<Byte> getRef(){
        return this.ref;
    }

    List<Byte> getTar(){
        return this.tar;
    }

    short[] getDelta(){
        return this.delta;
    }

    boolean equals(Mismatch mm){
        return ref.equals(mm.ref) && tar.equals(mm.tar);
    }

    boolean hasSameDelta(Mismatch mm){
        return Arrays.equals(delta, mm.delta);
    }

    boolean hasSameRef(List<Byte> r){
            return ref.equals(r);
    }

    int getOffset(){
        return offset;
    }

    void setOffset(int off){
        this.offset = off;
    }

    static String patternToString(List<Byte> pattern){
        StringBuilder out= new StringBuilder();
        for(Byte b:pattern){
            out.append((char) b.byteValue());
        }
        return out.toString();
    }

    String getRefAsString(){
        StringBuilder res = new StringBuilder();
        for(Byte b  : ref){
            res.append(b);
        }
        return res.toString();
    }

    public int getLen() {
        return ref.size();
    }
}

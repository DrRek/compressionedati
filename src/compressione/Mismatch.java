package compressione;

import java.util.*;
import java.io.*;

class Mismatch{
    private List<Byte> ref;
    private List<Byte> tar;
    private short[] delta;

    //this will represent the number of bytes -c to skip from match start to the actual missmatch location
    private long offset;

    Mismatch(List<Byte> r, List<Byte> t, long offset){
        this.ref=r;
        this.tar=t;
        this.offset = offset;
        this.delta = new short[r.size()];
        for(int i = 0; i<r.size(); i++){
            delta[i] = (short)(r.get(i) - t.get(i));
        }
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

    long getOffset(){
        return offset;
    }

    static String patternToString(List<Byte> pattern){
        StringBuilder out= new StringBuilder();
        for(Byte b:pattern){
            out.append((char) b.byteValue());
        }
        return out.toString();
    }
}

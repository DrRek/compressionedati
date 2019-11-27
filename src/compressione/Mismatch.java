package compressione;

import java.util.*;
import java.io.*;

public class Mismatch{
    private List<Byte> ref;
    private List<Byte> tar;
    private short[] delta;
    private long position;

    public Mismatch(List<Byte> r, List<Byte> t){
        this.ref=r;
        this.tar=t;
        this.delta = new short[r.size()];
        for(int i = 0; i<r.size(); i++){
            System.out.println("Ricordati di riscrivere la class Mismatch che attualmente non va bene!");
            //this.delta[i] = t[i] - r[i];
        }
    }

    public Mismatch(long pos, List<Byte> r, List<Byte> t){
        this.ref=r;
        this.tar=t;
        this.position=pos;
        this.delta = new short[r.size()];
        for(int i = 0; i<r.size(); i++){
            System.out.println("Ricordati di riscrivere la class Mismatch che attualmente non va bene!");
            //this.delta[i] = t[i] - r[i];
        }
    }

    public List<Byte> getRef(){
        return this.ref;
    }

    public List<Byte> getTar(){
        return this.tar;
    }

    public short[] getDelta(){
        return this.delta;
    }

    public Boolean equals(Mismatch mm){
        if(this.ref.equals(mm.ref) && this.tar.equals(mm.tar)) 
            return true;

        else return false;
    }

    public Boolean hasSameDelta(Mismatch mm){
        if(this.delta.equals(mm.delta))
            return true;

        else return false;
    }

    //potrebbe non servire
    public Boolean hasSameRef(List<Byte> r){
        if(this.ref.equals(r))
            return true;

        else return false;
    }

    public static String patternToString(List<Byte> pattern){
        String out="";
        for(Byte b:pattern){
            out+=(char) b.byteValue();
        }
        return out;
    }
}

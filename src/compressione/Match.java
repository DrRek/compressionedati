package compressione;

import java.util.ArrayList;
import java.util.List;

public class Match{
    private int dictIndex;
    private long matchLength;
    private ArrayList<Mismatch> mismatches;

    public Match(){
        this.mismatches=new ArrayList<Mismatch>();
    }

    public Match(int dictIndex, long matchLength){
        this.dictIndex=dictIndex;
        this.matchLength=matchLength;
        this.mismatches=new ArrayList<Mismatch>();
    }

    public long getMatchLength(){
        return this.matchLength;
    }

    public void increaseLen(int len){
        this.matchLength+=len;
    }

    public double getCost(){
        //System.out.println("getCost in Match needs to be implemented");

        //probabilmente non necessario, Java ritorna +infinito alle divisioni per 0
        if(this.matchLength==0)
            throw new Exception("Errore in Match.getCost(): provata divisione per 0");
            
        return (this.mismatches.length/this.matchLength);
    }

    public void addMissmatch(int position, List<Byte> ref, List<Byte> tar){
        //System.out.println("addMissmatch in Match needs to be implemented");
        this.increaseLen(ref.length);
        this.mismatches.put(new Mismatch(position, ref, tar));
    }

}

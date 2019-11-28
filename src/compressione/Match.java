package compressione;

import java.util.ArrayList;
import java.util.List;

public class Match{
    private int dictIndex;
    private long matchLength;
    private List<Mismatch> mismatches;

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

    public void increaseLen(){
        this.matchLength++;
    }

    public double getCost(){
        System.out.println("RICORDATI CHE DEVI ANCORA IMPLEMENTARE LA FUNZIONE DI COSTO CAZZ");
        if(this.matchLength == 0)
            return Long.MAX_VALUE;
        return this.mismatches.size() / this.matchLength;
    }

    public List<Mismatch> getMismatches(){
        return this.mismatches;
    }

    public void addMissmatch(List<Byte> ref, List<Byte> tar, long offset){
        this.mismatches.add(new Mismatch(ref, tar, offset));
        this.matchLength += ref.size();
    }

    public long getDictIndex(){
        return dictIndex;
    }
}

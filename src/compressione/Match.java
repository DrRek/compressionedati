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
        if(this.matchLength == 0)
            return Long.MAX_VALUE;
        return this.mismatches.size() / this.matchLength;
    }

    public List<Mismatch> getMismatches(){
        return this.mismatches;
    }

    public void addMissmatch(int position, List<Byte> ref, List<Byte> tar){
        //System.out.println("addMissmatch in Match needs to be implemented");
        this.mismatches.add(new Mismatch(position, ref, tar));
        this.matchLength += ref.size();
    }

    public long getDictIndex(){
        return dictIndex;
    }
}

package compressione;

import java.util.ArrayList;
import java.util.List;

public class Match{
    private static double MISSMATCH_COST_MULTIPLIER = 1.1;
    private static int MISSMATCH_LENGTH_COST_MULTIPLIER = 1;
    private static int MATCH_LENGTH_CONST_MULTIPLIER = 1;
    private int dictMapIndex;
    private int dictListIndex;
    private int matchLength;
    private List<Mismatch> mismatches;
    private double computedCost;

    Match(int dictMapIndex, int dictListIndex){
        this.dictMapIndex = dictMapIndex;
        this.dictListIndex = dictListIndex;
        this.mismatches=new ArrayList<>();
        this.computedCost = Integer.MAX_VALUE;
    }

    int getMatchLength(){
        return this.matchLength;
    }

    void increaseLen(){
        this.matchLength++;
    }

    double getCost(){
        if(computedCost == Integer.MAX_VALUE){
            computedCost = (matchLength + 1 + mismatches.size() * MISSMATCH_COST_MULTIPLIER) / matchLength + 1;
        }
        return computedCost;
    }

    List<Mismatch> getMismatches(){
        return this.mismatches;
    }

    void addMissmatch(List<Byte> ref, List<Byte> tar, int offset){
        this.mismatches.add(new Mismatch(ref, tar, offset));
        this.matchLength += ref.size();
    }

    int getDictMapIndex(){
        return dictMapIndex;
    }

    int getDictListIndex(){
        return dictListIndex;
    }
}

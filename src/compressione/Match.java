package compressione;

public class Match{
    private int dictIndex;
    private long matchLength;

    public Match(int dictIndex, long matchLength){
        this.dictIndex=dictIndex;
        this.matchLength=matchLength;
    }

    public long getMatchLength(){
        return this.matchLength;
    }

}

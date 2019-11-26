package compressione;

public class Match{
    private int dictIndex;
    private long matchLength;

    public Match(){}

    public Match(int dictIndex, long matchLength){
        this.dictIndex=dictIndex;
        this.matchLength=matchLength;
    }

    public long getMatchLength(){
        return this.matchLength;
    }

    public int getCost(){
        System.out.println("getCost in Match needs to be implemented");
        return 1;
    }

    public void addMissmatch(int position, String ref, String tar){
        System.out.println("addMissmatch in Match needs to be implemented");
    }

}

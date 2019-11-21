package compressione;

public class BlockPointer{
    private long offset;
    private Boolean file;

    public BlockPointer(long off, Boolean f){
        this.offset=off;
        this.file=f;
    }

    public long getOffset(){
        return this.offset;
    }

    public Boolean isTarget(){
        return this.file;
    }

    public Boolean isReference(){
        return !this.file;
    }

}

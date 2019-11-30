package compressione;

class BlockPointer{
    private long offset;
    private Boolean file;
    private int dictMapIndex;
    private int dictListIndex;

    BlockPointer(long off, Boolean f){
        this.offset=off;
        this.file=f;
        this.dictMapIndex = -1;
        this.dictListIndex = -1;
    }

    long getOffset(){
        return this.offset;
    }

    Boolean isTarget(){
        return this.file;
    }

    Boolean isReference(){
        return !this.file;
    }

    int getDictMapIndex() { return dictMapIndex; }

    int getDictListIndex() { return dictListIndex; }

    void setDictMapIndex(int x) { this.dictMapIndex = x; }

    void setDictListIndex(int x) { this.dictListIndex = x; }

}

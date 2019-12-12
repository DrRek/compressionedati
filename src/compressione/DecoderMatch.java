package compressione;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class DecoderMatch{
    private BlockPointer ptr;
    private List<Mismatch> mismatches;
    private int matchLength;

    DecoderMatch(BlockPointer ptr, int matchLength){
        this.ptr = ptr;
        this.mismatches=new ArrayList<>();
        this.matchLength = matchLength;
    }

    void addMismatch(Mismatch mm){
        mismatches.add(mm);
    }

    public BlockPointer getPtr() {
        return ptr;
    }

    public List<Mismatch> getMismatches() {
        return mismatches;
    }

    public int getMatchLength() {
        return matchLength;
    }
}

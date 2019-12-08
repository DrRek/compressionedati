package compressione;

import java.util.*;

class MMTable{

    static int PERFECT_HIT = -1;
    static int NO_HIT = -2;

    private List<Mismatch> table;
    private final static int MAX_SIZE=20;

    MMTable(){
        this.table=new ArrayList<Mismatch>();
    }

    void push(Mismatch mm){
        this.table.add(0, mm);
        if(table.size()>MAX_SIZE)
            table.remove(MAX_SIZE);
    }

    int searchAndUpdate(Mismatch mm){
        boolean isPerfectMatchPossible = true;
        int bestIndex = -1, bestFullMatchIndex = -1;
        for(int currentIndex = 0; currentIndex < table.size(); currentIndex++){
            Mismatch curr = table.get(currentIndex);
            if(curr.getRef().equals(mm.getRef())) {
                if(curr.getTar().equals(mm.getTar())) {
                    bestFullMatchIndex = currentIndex;
                } else {
                    isPerfectMatchPossible = false;
                }
            }

            if(Arrays.equals(curr.getDelta(), mm.getDelta())){
                bestIndex = currentIndex;
            }
        }

        if(isPerfectMatchPossible && bestFullMatchIndex >= 0){
            updateEntry(bestFullMatchIndex);
            return MMTable.PERFECT_HIT;
        }
        if(bestIndex >= 0){
            updateEntry(bestIndex);
            return bestIndex;
        }

        addEntry(mm);
        return MMTable.NO_HIT;
    }

    private void updateEntry(int index){
        Mismatch ele = table.remove(index);
        table.add(0, ele);
    }

    void addEntry(Mismatch mm){
        table.add(0, mm);
        removeOldEntries();
    }

    private void removeOldEntries(){
        if(table.size() > MMTable.MAX_SIZE)
            table = table.subList(0, MMTable.MAX_SIZE);
    }

    Mismatch getMismatchFromRefAndUpdate(byte[] sref) {
        StringBuilder res = new StringBuilder();
        for(byte b  : sref){
            res.append(b);
        }
        for(int i = 0; i < table.size(); i++)
            if(res.toString().equals(table.get(i).getRefAsString())){
                updateEntry(i);
                System.out.println("Sometimes it is succesfully");
                return table.get(0);
            }
        System.out.println("Error: unable to find a missmatch from a sref "+ sref);
        return null;
    }

    Mismatch getMismatchFromRefAndUpdate(byte[] sref, int index) {
        updateEntry(index);
        return table.get(0);
    }
}

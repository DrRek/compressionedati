package compressione;

import java.util.*;

public class MMTable{

    public static int PERFECT_HIT = -1;
    public static int NO_HIT = -2;

    private ArrayList<Mismatch> table;
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
        boolean hasFoundFullMatch = false;
        int bestIndex = -1;
        for(int currentIndex = 0; currentIndex < table.size(); currentIndex++){
            Mismatch curr = table.get(currentIndex);
            if(curr.getRef().equals(mm.getRef())) {
                if(curr.getTar().equals(mm.getTar())) {
                    hasFoundFullMatch = true;
                } else {
                    isPerfectMatchPossible = false;
                }
            }

            if(curr.getDelta().equals(mm.getDelta())){
                bestIndex = currentIndex;
            }
        }

        System.out.println("RICORDATI CHE IN SEARCHANDUPDATE DI MMTABLE BISOGNA AGGIORNARE LA TABELLA NEI VARI CASI");
        if(hasFoundFullMatch && isPerfectMatchPossible) return MMTable.PERFECT_HIT;
        if(bestIndex >= 0) return bestIndex;
        return MMTable.NO_HIT;
    }
}

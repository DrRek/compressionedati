package compressione;

import java.util.ArrayList.*;

public class MMTable{
    private ArrayList<Mismatch> table;
    private final static int MAX_SIZE=20;

    public MMTable(){
        this.table=new ArrayList<Mismatch>();
    }

    public void push(Mismatch mm){
        this.table.add(0, mm);
        if(table.size()>MAX_SIZE)
            table.remove(MAX_SIZE);
    }

    public int find(Mismatch mm){        
        Mismatch temp;
        for(int i=0; i<table.size(); i++){
            
            if((temp=table.get(i)).equals(mm)){                
                table.remove(i);
                table.add(0, temp);
                return i;
            }
        }
        return -1;
    }

    public int findSameDelta(byte[] d){
        Mismatch temp;
        for(int i=0; i<table.size(); i++){            
            if((temp=table.get(i)).getDelta().equals(d)){                
                table.remove(i);
                table.add(0, temp);
                return i;
            }
        }

        return -1;
    }

    public int findSameDelta(Mismatch mm){
        Mismatch temp;
        for(int i=0; i<table.size(); i++){            
            if((temp=table.get(i)).hasSameDelta(mm)){                
                table.remove(i);
                table.add(0, temp);
                return i;
            }
        }
        
        return -1;
    }



}
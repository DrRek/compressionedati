package compressione;

import java.util.*;

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

    public int findSameDelta(short[] d){
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

    public byte[] findSameRef(Mismatch mm){
        Mismatch temp;
        for(int i=0; i<table.size(); i++){            
            if((temp=table.get(i)).hasSameRef(mm.getRef())){                
                table.remove(i);
                table.add(0, temp);
                return temp.getRef();
            }
        }

        return -1;
    }

    public Boolean isUnique(byte[] ref){
        int t=0;
        String r=new String(ref);
        for(int i=0; i<table.size(); i++){   
            if(new String(table.get(i).getRef()).equals(r))
                t++;
            if(t>1)
                return false;
        }  
        return true;
    }


}

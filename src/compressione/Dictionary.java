package compressione;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Dictionary{

    private Map<String, List<BlockPointer>> dict;
    private int c;

    public Dictionary(int c, String file){
        this.c = c;
        this.dict = new HashMap<String, List<BlockPointer>>();
        this.addFile(file);
    }

    private void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            InputStream is = new FileInputStream(filename+".reference");
            
            int k=0;
            int readBytes = 0;
            while((readBytes  = is.read(b)) != -1){
                String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));
                BlockPointer p=new BlockPointer(k*c, false);
                List<BlockPointer> currentList = this.dict.getOrDefault(currentBlock, null);
                if(currentList==null){
                    currentList=new  ArrayList<BlockPointer>();
                    currentList.add(p);
                    this.dict.put(currentBlock, currentList);
                }
                else
                    currentList.add(p);
                    //TODO: verificare che non ci siano collisioni
                k++;
            }
            is.close();

        }catch(IOException ioe){
            System.out.println("Error "+ioe.getMessage());
        }
    }

    public List<BlockPointer> getPointers(String block){
        return dict.getOrDefault(block, null);
    }

    public void put(String block, BlockPointer p){
        List<BlockPointer> list = this.dict.getOrDefault(block, null);
        if(list==null){
            list=new  ArrayList<BlockPointer>();
            list.add(p);
            this.dict.put(block, list);
        }
        else
            list.add(p);
    }

    //da aggiustare
    @Override
    public String toString(){
        String out = "";
        for (String key: this.dict.keySet()) {
            out+=key+"->\n[";
            List<BlockPointer> currentList = this.dict.get(key);
            for(BlockPointer item: currentList){
                out+=" \""+item+"\" ";
            }
            out+="]\n";
        }
        return out;
    }
}

package compressione;

import java.util.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Dictionary{

    private Map<String, List<Long>> dict;
    private List<String> blocks;
    private int c;

    public Dictionary(int c){
        this.c = c;
        this.dict = new HashMap<String, List<Long>>();
        this.blocks = new ArrayList<String>();
    }

    public void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            RandomAccessFile is = new RandomAccessFile(new File(filename+".reference"), "r");
            
            int readBytes = 0;
            while((readBytes  = is.read(b)) != -1){
                String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));

                List<Long> currentList = this.dict.get(currentBlock);
                if(currentList == null){
                    //in questo caso è un blocco nuovo e lo devo aggiungere alla array di blocks
                    this.blocks.add(currentBlock);
                    currentList = new ArrayList<Long>();
                }

                currentList.add(is.getFilePointer() - readBytes);
                this.dict.put(currentBlock, currentList);
            }
            is.close();

        }catch(IOException ioe){
            System.out.println("Error "+ioe.getMessage());
        }
    }

    public String getBlockFromIf(int id){
        return this.blocks.get(id);
    }

    public int getIdFromBlock(String block){
        for(int i=0; i<this.blocks.size(); i++)
            if(this.blocks.get(i).equals(block))
                return i;
        return -1;
    }

    @Override
    public String toString(){
        String out = "";
        for (String key: this.dict.keySet()) {
            out+=key+"->\n[";
            List<Long> currentList = this.dict.get(key);
            for(long item: currentList){
                out+=" \""+item+"\" ";
            }
            out+="]\n\n";
        }

        out+="Blocks: [";
        for(String block: this.blocks){
            out+= " "+block+" ";
        }
        out+="]\n\n";
        return out;
    }
}

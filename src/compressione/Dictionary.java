package compressione;

import java.util.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Dictionary{

    private Map<String, List<Long>> dict;
    private int c;

    public Dictionary(int c){
        this.c = c;
        this.dict = new HashMap<String, List<Long>>();
    }

    public void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            RandomAccessFile is = new RandomAccessFile(new File(filename+".reference"), "r");
            
            int readBytes = 0;
            while((readBytes  = is.read(b)) != -1){
                String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));
                List<Long> currentList = this.dict.getOrDefault(currentBlock, new ArrayList<Long>());

                //TODO: all posto di "asd" ci deve andare qualcosa che mi permetta di avere un puntatore a quel punto del file
                currentList.add(is.getFilePointer() - readBytes);
                this.dict.put(currentBlock, currentList);
            }
            is.close();

        }catch(IOException ioe){
            System.out.println("Error "+ioe.getMessage());
        }
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
        return out;
    }
}

package compressione;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Dictionary{

    private Map<String, List<String>> dict;
    private int c;

    public Dictionary(int c){
        this.c = c;
        this.dict = new HashMap<String, List<String>>();
    }

    public void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            InputStream is = new FileInputStream(filename+".reference");
            
            int readBytes = 0;
            while((readBytes  = is.read(b)) != -1){
                String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));
                List<String> currentList = this.dict.getOrDefault(currentBlock, new ArrayList<String>());

                //TODO: all posto di "asd" ci deve andare qualcosa che mi permetta di avere un puntatore a quel punto del file
                currentList.add("asd");
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
            List<String> currentList = this.dict.get(key);
            for(String item: currentList){
                out+=" \""+item+"\" ";
            }
            out+="]\n";
        }
        return out;
    }
}

package compressione;

import java.io.*;
import java.util.*;

class Dictionary{

    private List<String> blocks;
    private Map<String, List<BlockPointer>> dict;
    private int c;
    private String buffer;
    private long bufferPosition;
    private RandomAccessFile target;

    Dictionary(int c, String reference, String target) throws FileNotFoundException {
        this.c = c;
        this.dict = new HashMap<>();
        this.blocks = new ArrayList<>();
        this.addFile(reference);
        this.buffer = "";
        this.bufferPosition = 0;
        this.target = new RandomAccessFile(new File(target), "r");
    }

    private void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            RandomAccessFile is = new RandomAccessFile(new File(filename), "r");
            
            int numberOfBlocksFound=0;
            int readBytes = 0;
            while((readBytes  = is.read(b)) != -1){
                String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));
                BlockPointer p = new BlockPointer(numberOfBlocksFound*c, false);
                addBlock(currentBlock, p);

                numberOfBlocksFound++;
            }
            is.close();

        }catch(IOException ioe){
            System.out.println("Error "+ioe.getMessage());
        }
    }

    void addMatch(Match m) throws IOException {
        target.seek(bufferPosition);
        byte[] bytes = new byte[(int)m.getMatchLength()+c];
        target.read(bytes);
        String toAdd = new String(bytes);
        buffer+= toAdd;
        updateFromBuffer();
    }

    void addString(String m){
        buffer += m;
        updateFromBuffer();
    }

    private void updateFromBuffer(){
        while (buffer.getBytes().length >= this.c){
            String current = buffer.substring(0, c);
            buffer = buffer.substring(c);
            BlockPointer ptr = new BlockPointer(bufferPosition, true);
            addBlock(current, ptr);
            bufferPosition += c;
        }
    }

    private void addBlock(String block, BlockPointer ptr){
        List<BlockPointer> currentList = this.dict.getOrDefault(block, null);
        if(currentList==null){
            blocks.add(block);
            ptr.setDictMapIndex(blocks.size()-1);
            ptr.setDictListIndex(0);
            currentList=new  ArrayList<>();
            currentList.add(ptr);
            this.dict.put(block, currentList);
        } else {
            currentList.add(ptr);
            int dictMapIndex = getIdFromBlock(block);
            ptr.setDictMapIndex(dictMapIndex);
            ptr.setDictListIndex(currentList.size() - 1);
        }
    }

    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < blocks.size(); i++){
            String currBlock = blocks.get(i);

            ret.append(i).append(" - ").append(currBlock.replaceAll("\\n","%")).append(" -->\n  ");
            for(BlockPointer ptr : dict.get(currBlock)){
                ret.append("<").append(ptr.getOffset()).append(",").append(ptr.isTarget()).append("> ");
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    private int getIdFromBlock(String block){
        for(int i = 0; i < blocks.size(); i++){
            if(block.equals(blocks.get(i)))
                return i;
        }
        return -1;
    }

    List<BlockPointer> getPointersForBlock(String block){
        return this.dict.getOrDefault(block, null);
    }

    BlockPointer getPtrFromParamaters(int dictMapIndex, int dictListIndex) {
        String block = blocks.get(dictMapIndex);
        List<BlockPointer> list = dict.get(block);
        return list.get(dictListIndex);
    }
}

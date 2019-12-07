package compressione;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.*;

class Dictionary{

    private List<String> blocks;
    private Map<String, List<BlockPointer>> dict;
    private int c;
    private List<Byte> buffer;
    private int bufferPosition;
    private RandomAccessFile target;

    Dictionary(int c, String reference, String target) throws FileNotFoundException {
        this.c = c;
        this.dict = new HashMap<>();
        this.blocks = new ArrayList<>();
        this.addFile(reference);
        this.buffer = new ArrayList<>();
        this.bufferPosition = 0;
        this.target = new RandomAccessFile(new File(target), "r");
    }

    private void addFile(String filename){
        try{
            byte[] b = new byte[this.c];
            RandomAccessFile is = new RandomAccessFile(new File(filename), "r");
            
            int numberOfBlocksFound=0;
            while(is.read(b) != -1){
                BlockPointer p = new BlockPointer(numberOfBlocksFound*c, false);

                Byte[] newB = new Byte[b.length];
                for (int i=0;i<b.length;i++) {
                    newB[i] = b[i];
                }
                addBlock(newB, p);

                numberOfBlocksFound++;
            }
            is.close();

        }catch(IOException ioe){
            System.out.println("Error "+ioe.getMessage());
        }
    }

    void addMatch(Match m) throws IOException {
        target.seek(bufferPosition);
        byte[] abytes = new byte[(int)m.getMatchLength()+c];
        Byte[] bytes = new Byte[(int)m.getMatchLength()+c];
        target.read(abytes);
        for (int i=0;i<abytes.length;i++) {
            bytes[i] = abytes[i];
        }
        buffer.addAll(Arrays.asList(bytes));
        updateFromBuffer();
    }

    void addString(List<Byte> m){
        buffer.addAll(m);
        updateFromBuffer();
    }

    void addString(byte[] m){
        for(byte b : m)
            buffer.add(b);
        updateFromBuffer();
    }

    private void updateFromBuffer(){
        while (buffer.size() >= this.c){
            List<Byte> current = buffer.subList(0, c);

            buffer = buffer.subList(c, buffer.size());
            BlockPointer ptr = new BlockPointer(bufferPosition, true);

            Byte[] array = new Byte[current.size()];
            array = current.toArray(array);
            addBlock(array, ptr);
            bufferPosition += c;
        }
    }

    private void addBlock(Byte[] block, BlockPointer ptr){
        List<BlockPointer> currentList = this.dict.getOrDefault(new String(ArrayUtils.toPrimitive(block)), null);
        if(currentList==null){
            blocks.add(new String(ArrayUtils.toPrimitive(block)));
            ptr.setDictMapIndex(blocks.size()-1);
            ptr.setDictListIndex(0);
            currentList=new  ArrayList<>();
            currentList.add(ptr);
            this.dict.put(new String(ArrayUtils.toPrimitive(block)), currentList);
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

            ret.append(i).append(" - ").append(currBlock).append(" -->\n  ");
            for(BlockPointer ptr : dict.get(currBlock)){
                ret.append("<").append(ptr.getOffset()).append(",").append(ptr.isTarget()).append("> ");
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    private int getIdFromBlock(Byte[] block){
        String blockToString = new String(ArrayUtils.toPrimitive(block));
        for(int i = 0; i < blocks.size(); i++){
            if(blockToString.equals(blocks.get(i)))
                return i;
        }
        return -1;
    }

    List<BlockPointer> getPointersForBlock(Byte[] block){
        return this.dict.getOrDefault(new String(ArrayUtils.toPrimitive(block)), null);
    }

    BlockPointer getPtrFromParamaters(int dictMapIndex, int dictListIndex) {
        String block = blocks.get(dictMapIndex);
        List<BlockPointer> list = dict.get(block);
        return list.get(dictListIndex);
    }
}

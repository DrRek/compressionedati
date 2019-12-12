package compressione;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.*;

class Dictionary{

    private final boolean magic;
    private List<WrappedByteArray> blocks;
    private Map<WrappedByteArray, List<BlockPointer>> dict;
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
        magic = false;
    }

    Dictionary(int c, String reference, String target, boolean magic) throws FileNotFoundException {
        this.c = c;
        this.dict = new HashMap<>();
        this.blocks = new ArrayList<>();
        this.addFile(reference);
        this.buffer = new ArrayList<>();
        this.bufferPosition = 0;
        this.target = new RandomAccessFile(new File(target), "r");
        this.magic = magic;
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
        for(byte b : m){
            buffer.add(b);
        }
        updateFromBuffer();
    }

    void addString(byte b){
        buffer.add(b);
        updateFromBuffer();
    }

    private void updateFromBuffer(){
        int margin;
        if(magic)
            margin = this.c*1;
        else
            margin = this.c;
        while (buffer.size() >= margin){
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
        WrappedByteArray blockPrim = new WrappedByteArray(block);
        List<BlockPointer> currentList = this.dict.getOrDefault(blockPrim, null);
        if(currentList==null){
            blocks.add(blockPrim);
            ptr.setDictMapIndex(blocks.size()-1);
            ptr.setDictListIndex(0);
            currentList=new  ArrayList<>();
            currentList.add(ptr);
            this.dict.put(blockPrim, currentList);
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
            String currBlock = new String(blocks.get(i).getSource());

            ret.append(i).append(" - ").append(currBlock).append(" -->\n  ");
            for(BlockPointer ptr : dict.get(currBlock)){
                ret.append("<").append(ptr.getOffset()).append(",").append(ptr.isTarget()).append("> ");
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    private int getIdFromBlock(Byte[] block){
        for(int i = 0; i < blocks.size(); i++){
            if(blocks.get(i).equals(new WrappedByteArray(block))){
                return i;
            }
        }
        return -1;
    }

    List<BlockPointer> getPointersForBlock(Byte[] block){
        return this.dict.getOrDefault(new WrappedByteArray(block), null);
    }

    BlockPointer getPtrFromParamaters(int dictMapIndex, int dictListIndex) {
        WrappedByteArray block = blocks.get(dictMapIndex);
        List<BlockPointer> list = dict.get(block);
        return list.get(dictListIndex);
    }

    public byte[] getBlockById(int id){
        return blocks.get(id).getSource();
    }

    private class WrappedByteArray {
        private byte[] source;

        WrappedByteArray(byte[] source){
            this.source = source;
        }

        WrappedByteArray(Byte[] source){
            this.source = ArrayUtils.toPrimitive(source);
        }

        @Override
        public boolean equals(Object obj){
            if(!(obj instanceof WrappedByteArray))
                return false;
            byte[] toCompare = ((WrappedByteArray)obj).getSource();
            if(toCompare.length != source.length)
                return false;
            for(int i=0; i<source.length; i++)
                if(source[i] != toCompare[i])
                    return false;
            return true;
        }

        @Override
        public int hashCode(){
            int result = 0;
            for (byte b : source) result += b;
            return result;
        }

        public byte[] getSource() {
            return source;
        }
    }
}

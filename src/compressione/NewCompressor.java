package compressione;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewCompressor {

    private final int c;
    private final RandomAccessFile targetFile;
    private final Dictionary dict;
    private final RandomAccessFile referenceFile;
    private final RandomAccessFile targetFileForGetMatch;
    private final int mmlen;
    private final FileOutputStream compressedFileWriter;
    private final MMTable[] mismatchTables;
    private List<Byte> setBuffer;

    NewCompressor(int c, int mmlen, String referencePath, String targetPath, String compressedFile) throws IOException{
        this.dict = new Dictionary(c, referencePath, targetPath);

        this.referenceFile = new RandomAccessFile(new File(referencePath), "r");
        this.targetFile = new RandomAccessFile(new File(targetPath), "r");
        this.targetFileForGetMatch = new RandomAccessFile(new File(targetPath), "r");
        this.compressedFileWriter=new FileOutputStream(new File(compressedFile));

        this.c = c;
        this.mmlen = mmlen;

        this.mismatchTables=new MMTable[this.mmlen];
        for(int i=0; i<this.mmlen; i++){
            this.mismatchTables[i]=new MMTable();
        }

        this.setBuffer = new ArrayList<>();
    }

    boolean run() throws IOException {
        int currentPos = 0;
        byte[] currentBlock;
        while((currentBlock = readFromPos(currentPos)) != null &&
                currentBlock.length >= this.c
        ){

            List<BlockPointer> possibleMatches = this.dict.getPointersForBlock(ArrayUtils.toObject(currentBlock));
            Match bestMatch = getBestMatch(currentPos, possibleMatches);
            if(bestMatch == null) {
                setBuffer.add(currentBlock[0]);
                ++currentPos;
            } else {
                encodeSetBufferAndAddToDict();
                encodeMatchAndAddToDict(bestMatch, currentPos);
                currentPos += this.c + bestMatch.getMatchLength();
            }
        }

        if(currentBlock != null && currentBlock.length < this.c){
            List<Byte> lastBytesList = Arrays.asList(ArrayUtils.toObject(currentBlock));
            setBuffer.addAll(lastBytesList);
            encodeSetBufferAndAddToDict();
        }

        return true;
    }

    /*
        Matches will be encoded as follow <c><dict_map_index><,><dic_list_index><,><match_length>
        example c1,10   from dict[1] pointer copy c+10 bytes
        unless the dictionary entry in the list is 0, in this case the encoded would be <c><dict_index><,><match_length>
    */
    private void encodeMatchAndAddToDict(Match m, int pos) throws IOException {
        String enc = "c"+m.getDictMapIndex();
        int dictListIndex = m.getDictListIndex();
        if(dictListIndex != 0)
            enc += ","+dictListIndex;
        enc += ","+m.getMatchLength();
        compressedFileWriter.write(enc.getBytes());

        for(Mismatch mm : m.getMismatches())
            encodeMismatch(mm);

        byte[] toAdd = new byte[this.c + m.getMatchLength()];
        targetFileForGetMatch.seek(pos);
        int read = targetFileForGetMatch.read(toAdd);
        if(read < toAdd.length)
            System.err.println("While encoding match unable to add to dict the ful encoded. Match:"+m+", pos:"+pos);
        dict.addString(toAdd);
    }

    /*
        There are 3 possible types of missmatch
        A missmatch will always be encoded with it's position and i't number of bytes
        if refs and tgts are equals for a row and no other row has same ref then sample message will be <m><offset><,><bytes_n>
        if entry with same delta is found then <m><offset><,><bytes_n><,><row_index>
        else <m><offset><+|-><d1><+|-><d2><+|-><d3>...
    */
    private void encodeMismatch(Mismatch mm) throws IOException {
        MMTable relevantTable = mismatchTables[mm.getRef().size()-1];
        int cacheLockupResult = relevantTable.searchAndUpdate(mm);
        String encMissmatchStr = "m"+mm.getOffset();
        if(cacheLockupResult == MMTable.PERFECT_HIT){
            encMissmatchStr += ","+mm.getRef().size();
        } else if(cacheLockupResult == MMTable.NO_HIT){
            for(short i : mm.getDelta())
                if( i >= 0)
                    encMissmatchStr += "+"+i;
                else
                    encMissmatchStr += i;
        } else {
            encMissmatchStr += ","+mm.getRef().size()+","+cacheLockupResult;
        }
        compressedFileWriter.write(encMissmatchStr.getBytes());
    }

    /*
        A set message will be composed as this: <s><message_len><,><message>
    */
    private void encodeSetBufferAndAddToDict() throws IOException {
        if(setBuffer.isEmpty()) return;
        compressedFileWriter.write(("s"+setBuffer.size()+",").getBytes());
        Byte[] setArray = new Byte[setBuffer.size()];
        setArray = setBuffer.toArray(setArray);
        compressedFileWriter.write(ArrayUtils.toPrimitive(setArray));
        
        this.dict.addString(setBuffer);
        this.setBuffer.clear();
    }

    private Match getBestMatch(int currentPos, List<BlockPointer> list) {
        if(list == null || list.isEmpty())
            return null;

        Match optimalMatch = null, tempMatch;
        for(BlockPointer ptr : list){
            tempMatch = getMatch(currentPos, ptr);

            if(ptr.isTarget() && ptr.getOffset() + this.c + tempMatch.getMatchLength() >= currentPos)
                continue;

            if(optimalMatch == null || optimalMatch.getCost() > tempMatch.getCost())
                optimalMatch = tempMatch;
        }
        return optimalMatch;
    }

    private Match getMatch(int pos, BlockPointer ptr) {
        RandomAccessFile source, destination = targetFile;
        if(ptr.isReference()){
            source = referenceFile;
        } else {
            source = targetFileForGetMatch;
        }

        Match resultMatch =  new Match(ptr.getDictMapIndex(), ptr.getDictListIndex());
        List<Byte> currentSourceMismatch = new ArrayList<>();
        List<Byte> currentDestinationMismatch = new ArrayList<>();
        boolean cont = true;
        try {
            source.seek(ptr.getOffset() + this.c);
            destination.seek(pos + this.c);

            int mismatchStartOffset = 0, currentPosition = 0;

            do {
                byte sb = source.readByte();
                byte db = destination.readByte();

                if(sb == db){
                    if(!currentSourceMismatch.isEmpty()){
                        resultMatch.addMissmatch(currentSourceMismatch, currentDestinationMismatch, mismatchStartOffset);

                        currentSourceMismatch = new ArrayList<>();
                        currentDestinationMismatch = new ArrayList<>();
                    }
                    resultMatch.increaseLen();
                } else {
                    if(currentSourceMismatch.isEmpty())
                        mismatchStartOffset = currentPosition;

                    currentSourceMismatch.add(sb);
                    currentDestinationMismatch.add(db);

                    if(currentSourceMismatch.size() > this.mmlen)
                        cont = false;
                }
                ++currentPosition;
            } while (cont);
        } catch (EOFException eof){
            return resultMatch;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during get match at offset:"+pos+" and BlockPointer:"+ptr);
        }
        return resultMatch;
    }

    private byte[] readFromPos(int currentPos){
        try {
            targetFile.seek(currentPos);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error at readFromPos offset: "+currentPos);
            return null;
        }

        byte[] res = new byte[this.c];
        try {
            int read = targetFile.read(res);
            if(read == -1) return null;
            if(read < this.c){
                byte[] trueRes = new byte[read];
                trueRes = Arrays.copyOfRange(res, 0, read);
                return trueRes;
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dictionary getDictionary() {
        return dict;
    }
}
